package com.app.ble.utils;

import static android.app.PendingIntent.getActivity;

import static java.lang.Long.valueOf;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import com.app.ble.activities.MainActivity;
import com.app.ble.database.DBHelper;
import com.app.ble.model.Data;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BLEController {

    String deviceAddress;

    String deviceName;
    DBHelper dbHelper;

    RemoteControl remoteControl;
    int bluetoothRequestCode = 2, permissionRequestCode = 1;


    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
    DecimalFormat decimalFormat = new DecimalFormat("0.0", decimalFormatSymbols);
    //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());


    //SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String TAG = "theS";

    double timeInterval = 1.0; //Time in second representing interval to calculate DUVD

    private static final String CHANNEL_ID = "push_notifications";

    double dailyUVdose = 8000.0; //UVIs -> 2.22UVIh -> 200J/m^2

    int factor = 0;

    double VUV;
    double sumVUV;
    String previusDate = "0000-00-00";

    boolean percDnotify = false;
    boolean perc50notify = false;
    boolean perc100notify = false;

    Calendar sunTimer = Calendar.getInstance();
    private static BLEController instance;

    private BluetoothLeScanner scanner;
    private BluetoothDevice device;
    private BluetoothManager bluetoothManager;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic btGattChar = null;

    private ArrayList<BLEControllerListener> listeners = new ArrayList<>();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();



    public BLEController(Context ctx) {
        this.bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        dbHelper = new DBHelper(ctx);
    }

    public static BLEController getInstance(Context ctx) {
        if (null == instance)
            instance = new BLEController((ctx));

        return instance;
    }

    public void addBLEControllerListener(BLEControllerListener l) {
        if (!this.listeners.contains(l))
            this.listeners.add(l);
    }

    public void removeBLEControllerListener(BLEControllerListener l) {
        this.listeners.remove(l);
    }

    @SuppressLint("MissingPermission")
    public void init() {
        this.devices.clear();
        this.scanner = this.bluetoothManager.getAdapter().getBluetoothLeScanner();
        this.scanner.startScan(bleCallback);
    }

    private void fireDisconnected() {
        for (BLEControllerListener l : this.listeners)
            l.BLEControllerDisconnected();

        this.device = null;
    }

    private void fireConnected() {
        for (BLEControllerListener l : this.listeners)
            l.BLEControllerConnected();
    }


    @SuppressLint("MissingPermission")
    private void fireDeviceFound(BluetoothDevice device) {
        for (BLEControllerListener l : this.listeners)
            l.BLEDeviceFound(device.getName().trim(), device.getAddress());
    }

    private ScanCallback bleCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                deviceFound(device);
            }
        }

        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                BluetoothDevice device = sr.getDevice();
                if (!devices.containsKey(device.getAddress()) && isThisTheDevice(device)) {
                    deviceFound(device);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("[BLE]", "scan failed with errorcode: " + errorCode);
        }
    };

    @SuppressLint("MissingPermission")
    private boolean isThisTheDevice(BluetoothDevice device) {
        return null != device.getName() && device.getName().startsWith("BT.OZON");
    }

    private void deviceFound(BluetoothDevice device) {
        this.devices.put(device.getAddress(), device);
        fireDeviceFound(device);
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(String address) {
        this.device = this.devices.get(address);
        this.scanner.stopScan(this.bleCallback);
        Log.i("[BLE]", "connect to device " + device.getAddress());
        this.bluetoothGatt = device.connectGatt(null, false, this.bleConnectCallback);
    }


    final BluetoothGattCallback bleConnectCallback = new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                btGattChar = null;
                fireDisconnected();
            }
        }


        /*@SuppressLint("MissingPermission")
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic bgc) {

            super.onCharacteristicChanged(gatt, bgc);
            ;
            byte[] newValue = bgc.getValue();

            if (newValue != null && newValue.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(newValue.length);
                for (byte byteChar : newValue)
                    stringBuilder.append(String.format("%02X", byteChar));

                Log.i("[BLE]", (stringBuilder.toString()));

                StringBuilder output = new StringBuilder("");


                for (int i = 0; i < stringBuilder.toString().length(); i += 2) {
                    String str = stringBuilder.substring(i, i + 2);
                    output.append((char) Integer.parseInt(str, 16));
                }
                Log.i("[BLE]", (output.toString()));


                for (BLEControllerListener l : listeners)
                    l.BLEInfo(output.toString());


            }

        }*/

        @SuppressLint("MissingPermission")
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic bgc) {

            super.onCharacteristicChanged(gatt, bgc);
            ;

            byte[] newValue = bgc.getValue();
            System.out.println("data received");

            if (newValue != null && newValue.length > 0) {
                //Log.i("byte", String.valueOf((newValue)));
                final StringBuilder stringBuilder = new StringBuilder(newValue.length);
                for (byte byteChar : newValue)
                    stringBuilder.append(String.format("%02X", byteChar));

                Log.i("[BLE]", (stringBuilder.toString()));
                System.out.println(stringBuilder.toString());

                int length = stringBuilder.length();
                Log.i("[BLE]", "value=" + (stringBuilder.toString()));

                if (stringBuilder.length() == 24) {
                    Log.i("[BLE]", "lenght=" + (length));
                    StringBuilder output = new StringBuilder("");


                /*for (int i = 0; i < stringBuilder.toString().length(); i += 2) {
                    String str = stringBuilder.substring(i, i + 2);
                    output.append((char) Integer.parseInt(str, 16));
                }*/

                for (int i = 0; i < 8; i += 2) {
                    String str1 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str1); // Insert at the beginning to maintain the order

                }

                int value1 = Integer.parseInt(output.toString(), 16);

                output = new StringBuilder("");

                for (int i = 8; i < 12; i += 2) {
                    String str2 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str2); // Insert at the beginning to maintain the order

                }

                int value2 = Integer.parseInt(output.toString(), 16);


                output = new StringBuilder("");

                for (int i = 12; i < 16; i += 2) {
                    String str3 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str3); // Insert at the beginning to maintain the order

                }

                int value3 = Integer.parseInt(output.toString(), 16);

                output = new StringBuilder("");

                for (int i = 16; i < 20; i += 2) {
                    String str4 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str4); // Insert at the beginning to maintain the order

                }

                int value4 = Integer.parseInt(output.toString(), 16);

                output = new StringBuilder("");

                for (int i = 20; i < 24; i += 2) {
                    String str5 = stringBuilder.substring(i, i + 2);
                    output.insert(0, str5); // Insert at the beginning to maintain the order

                }

                int value5 = Integer.parseInt(output.toString(), 16);

                int[] valuearray;

                valuearray = new int[5];

                valuearray[0] = value1;
                valuearray[1] = value2;
                valuearray[2] = value3;
                valuearray[3] = value4;
                valuearray[4] = value5;

                    long unixtime = valueOf(value1) * 1000;
                    Log.d(TAG, "run: unixtime: " + unixtime);

                    //DecimalFormat dfrmt = new DecimalFormat("#.#");
                    if (Float.valueOf(value2).floatValue() < 20) {
                        VUV = 0.0;
                    } else {
                        VUV = 0.05 * Float.valueOf(value2).floatValue() - 0.3;
                        //Log.d(TAG, "run: VUV: " + VUV);
                    }
                    //double batteryLevel = Float.valueOf(arrOfStr[3]).floatValue() * 100 / 716;
                    double batteryLevel = Float.valueOf(value4).floatValue();
                    System.out.println(batteryLevel);
                    //Log.d(TAG, "run: batteryLevel: " + arrOfStr[3]);

                    //double R0 = 10000; //ohm
                    //double B = 3590; //K
                    //double T0 = 298.15 //K -> 25°C/2


                    //double Tabsolute = 273.15 //K -> 0°C
                    double Vout = 3.0 * ((Float.valueOf(value3).floatValue()) / 1023.0);
                    double Rout = (10000 * Vout / (3.0 - Vout));
                    double TempC;
                    if (Rout <= 0){
                        TempC = 0;
                    }
                    else {
                        //TempC = (3590 / Math.log(Rout / 0.057) - 272.15);
                        //TempC = (3590 / Math.log(Rout / (10000*Math.exp(-3590/298.15))))-273.15;
                        TempC = (3594.45 / Math.log(Rout / (149.49*Math.exp(-3594.45/455.36))))-273.15;
                        Log.d(TAG, "run: TempC: " + TempC);
                    }

                    int transferState = value5;


                    //System.out.println(dailyUVdose*(1+Double.parseDouble(decimalFormat.format((double) getFactor()/100))));
                    //System.out.println(getAdjusteddailyUVdose());

                Log.i("[BLE]", String.valueOf(value1));
                Log.i("[BLE]", "Decimal value: " + value5);

                //for (BLEControllerListener l : listeners)
                  //  l.BLEInfo(valuearray);

                    for (@SuppressLint("SuspiciousIndentation") int value : valuearray) {
                        System.out.println(value);
                    }
                    System.out.println("VUV: "+decimalFormat.format(VUV));
                    Data d = new Data(dateFormat.format(new Date()),
                            Double.parseDouble(decimalFormat.format(VUV)),
                            Double.parseDouble(decimalFormat.format(batteryLevel)),
                            Double.parseDouble(decimalFormat.format(TempC)),
                            transferState);
                    System.out.println("data: "+d);
                    dbHelper.createData(d);
                    EventBus.getDefault().postSticky(d);

            }

            else {
                // stringBuilder more than 20
                System.out.println("stringBuilder more than 24");
            }


        }

    }



        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (null == btGattChar) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if (service.getUuid().toString().toUpperCase().startsWith("0000FFE0")) {
                        List<BluetoothGattCharacteristic> gattCharacteristics = service.getCharacteristics();
                        for (BluetoothGattCharacteristic bgc : gattCharacteristics) {
                            Log.i("[BLE]", bgc.getUuid().toString());
                            if (bgc.getUuid().toString().toUpperCase().startsWith("0000FFE1")) {
                                int chprop = bgc.getProperties();
                                if ((chprop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    btGattChar = bgc;
                                    gatt.setCharacteristicNotification(bgc, true);
                                    BluetoothGattDescriptor descriptor = bgc.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(descriptor);

                                    Log.i("[BLE]", "CONNECTED and ready to read");
                                    fireConnected();
                                }
                            }
                        }
                    }
                }
            }

        }


    };


    /*@SuppressLint("MissingPermission")
    public void sendData(byte [] data) {
        this.btGattChar.setValue(data);
        bluetoothGatt.writeCharacteristic(this.btGattChar);
    }*/

    @SuppressLint("MissingPermission")
    public void sendData(byte[] data) {
        if (this.btGattChar != null) {
            this.btGattChar.setValue(data);
            bluetoothGatt.writeCharacteristic(this.btGattChar);
            Log.i("info", "Time sent");
            // Proceed with further operations on the characteristic and send data.
        } else {
            // Handle the case where the characteristic is null.
            // You might want to log an error or take appropriate action.
            this.bluetoothGatt.disconnect();
            Log.i("error", "this.btGattChar is Null");
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (this.bluetoothGatt !=null){
            this.bluetoothGatt.disconnect();
        }
    }

}

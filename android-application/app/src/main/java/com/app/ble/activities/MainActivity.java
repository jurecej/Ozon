package com.app.ble.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.lang.Long.valueOf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.app.ble.database.DBHelper;
import com.app.ble.R;
import com.app.ble.fragments.DashboardFragment;
import com.app.ble.fragments.HomeFragment;
import com.app.ble.fragments.ProfileFragment;
import com.app.ble.fragments.SettingFragment;
import com.app.ble.model.Data;
import com.app.ble.utils.BLEController;
import com.app.ble.utils.BLEControllerListener;
import com.app.ble.utils.RemoteControl;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BLEControllerListener {
    BottomNavigationView bottomNavigationView;
    BLEController bleController;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkBLESupport();

        startBluetooth();

        bottomNavigationView = findViewById(R.id.bottom_view);

        loadFragment(new HomeFragment());

        this.bleController = BLEController.getInstance(this);
        this.remoteControl = new RemoteControl(this.bleController);

        // Load dailyUVdose from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        float savedDailyUVdose = preferences.getFloat("dailyUVdose", 8000.0f);
        dailyUVdose = (float) savedDailyUVdose;

        sunTimer.setTimeInMillis(System.currentTimeMillis());
        sunTimer.set(Calendar.HOUR_OF_DAY, 11);
        sunTimer.set(Calendar.MINUTE, 0);
        sunTimer.set(Calendar.SECOND, 0);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new HomeFragment();
                int id = item.getItemId();
                if (id == R.id.item_home) {
                    fragment = new HomeFragment();
                } else if (id == R.id.item_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.item_profile) {
                    fragment = new ProfileFragment();
                } else if (id == R.id.item_setting) {
                    fragment = new SettingFragment();
                }
                loadFragment(fragment);
                return true;
            }
        });

        dbHelper = new DBHelper(MainActivity.this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
////                Log.d(TAG, "postDelayed: " + dbHelper.createData(new Data(dateFormat.format(new Date()), 1.3, 13.4, 35.6)));
//                handler.postDelayed(this, 5000);
//                EventBus.getDefault().postSticky(new Data(dateFormat.format(new Date()), 1.3, 13.4, 35.6));
//                Log.d(TAG, "run: ");
//            }
//        }, 5000);

//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data(dateFormat.format(new Date()), 1.3, 13.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data(dateFormat.format(new Date()), 1.3, 13.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("25-03-2023", 1.3, 13.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("25-03-2023", 2.3, 14.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("26-03-2023", 3.3, 15.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("26-03-2023", 4.3, 16.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("26-03-2023", 5.3, 17.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("27-03-2023", 6.3, 18.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("27-03-2023", 7.3, 19.4, 35.6)));
//        Log.d(TAG, "onCreate: "+dbHelper.createData(new Data("28-03-2023", 8.3, 10.4, 35.6)));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Data data) {
        // Retrieve data from the database
        //List<Data> dataList = dbHelper.readData();
        //int dataSize = dataList.size();
        //if (dataSize > 0) {
            //Data data = dataList.get(dataSize - 1); // Get the last data element
        //System.out.println(data.getVuv());
            // Check condition and send push notification
           // if (data.getVuv() > 1) {
             //   System.out.println("notify with alert");
             //   sendPushNotification();
            //}
        //}
        alertNotification();
    }

    @SuppressLint("MissingPermission")
    public void alertNotification() {
        List<Data> dataList = dbHelper.readData();
        int dataSize = dataList.size();
        if(dataSize > 0) {
            Data data = dataList.get(dataSize - 1);
            //System.out.println(data.getVuv());
            System.out.println("previus date "+ previusDate);
            Calendar currentTime = Calendar.getInstance();
            //System.out.println("current time: "+ currentTime);
            //System.out.println("sun time vitamin D: "+ sunTimer);
            if (!previusDate.equals(data.getDate())) {
                System.out.println("current date "+ data.getDate());
                System.out.println(0.125*getAdjusteddailyUVdose());
                if (currentTime.after(sunTimer)==true && percDnotify == false && Double.parseDouble(decimalFormat.format(data.getVuv() * timeInterval)) < 0.125*getAdjusteddailyUVdose()) {
                    percDnotify = true;
                    System.out.println("Notification sun time vitamin D sent");
                    sendPushNotificationvitaminD();
                }
                if (perc50notify==false && Double.parseDouble(decimalFormat.format(data.getVuv() * timeInterval)) > 0.5*getAdjusteddailyUVdose()) {
                    //System.out.println("notify with alert");
                    perc50notify = true;
                    //previusDate = data.getDate();
                    //System.out.println("next date " + previusDate);
                    sendPushNotification50perc();
                }
                else if (perc50notify==true && perc100notify==false && Double.parseDouble(decimalFormat.format(data.getVuv() * timeInterval)) > getAdjusteddailyUVdose()) {
                    //System.out.println("notify with alert");
                    //previusDate = data.getDate();
                    //System.out.println("next date " + previusDate);
                    perc100notify = true;
                    sendPushNotification();
                }
                else if(perc50notify == true && perc100notify == true){
                    perc50notify = false;
                    perc100notify = false;
                    percDnotify = false;
                    previusDate = data.getDate();
                }
            }
        }
        else {
            System.out.println("No data yet!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.bleController.addBLEControllerListener(this);
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //log("[BLE]\tSearching for Bluetooth device...");
            this.bleController.init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: removeBLEListener");
        removeBLEListener();
    }

    @Override
    public void BLEControllerConnected() {
        Log.d(TAG, "BLEControllerConnected: ");
    }

    @Override
    public void BLEControllerDisconnected() {
        Log.d(TAG, "BLEControllerDisconnected: ");
    }

    @Override
    public void BLEDeviceFound(String name, String address) {
        deviceAddress = address;
        this.deviceName = name;
        Log.d(TAG, "BLEDeviceFound: name: " + name + " address: " + address);
    }

    // Add a getter method for the name variable
    public String getName() {
            return deviceName;
    }

    /*@Override
    public void BLEInfo(String data) {


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: " + data);

                String BLEvalue = data;
                String[] arrOfStr = BLEvalue.split("\t");

                DecimalFormat dfrmt = new DecimalFormat("#.#");
                double VUV = 0.05 * Float.valueOf(arrOfStr[0]).floatValue() - 0.3;
                Log.d(TAG, "run: VUV: " + VUV);

                double batteryLevel = (100 / 3.8) * 3.8 * Float.valueOf(arrOfStr[1]).floatValue() / 1023;
                Log.d(TAG, "run: batteryLevel: " + batteryLevel);

                double Vout = 3.3 * ((Float.valueOf(arrOfStr[2]).floatValue()) / 1023.0);
                double Rout = (10000 * Vout / (3.3 - Vout));
                double TempC = (3600 / Math.log(Rout / 0.057) - 272.15);
                Log.d(TAG, "run: TempC: " + TempC);
                Data d = new Data(dateFormat.format(new Date()), Double.parseDouble(decimalFormat.format(VUV)), Double.parseDouble(decimalFormat.format(batteryLevel)), Double.parseDouble(decimalFormat.format(TempC)));
                dbHelper.createData(d);
                EventBus.getDefault().postSticky(d);
            }
        });
    }*/

    /*public void BLEInfo(final int[] arrOfStr) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                //value BLEvalue is the value which is converted from the string "text"
                //"text" represents the data receivet from the device via BLE (Bluetooth Low Energy)
                //String BLEvalue = text;
                //String[] arrOfStr = BLEvalue.split("\t");

                Log.d(TAG, String.valueOf(arrOfStr));
                System.out.println(Float.valueOf(arrOfStr[0]).floatValue());
                if (arrOfStr.length <= 5) {
                    long unixtime = valueOf(arrOfStr[0]) * 1000;
                    Log.d(TAG, "run: unixtime: " + unixtime);

                    //DecimalFormat dfrmt = new DecimalFormat("#.#");
                    if (Float.valueOf(arrOfStr[1]).floatValue() < 20) {
                        VUV = 0.0;
                    } else {
                        VUV = 0.05 * Float.valueOf(arrOfStr[1]).floatValue() - 0.3;
                        //Log.d(TAG, "run: VUV: " + VUV);
                    }
                    //double batteryLevel = Float.valueOf(arrOfStr[3]).floatValue() * 100 / 716;
                    double batteryLevel = Float.valueOf(arrOfStr[3]).floatValue();
                    System.out.println(batteryLevel);
                    //Log.d(TAG, "run: batteryLevel: " + arrOfStr[3]);

                    //double R0 = 10000; //ohm
                    //double B = 3590; //K
                    //double T0 = 298.15 //K -> 25°C/2


                    //double Tabsolute = 273.15 //K -> 0°C
                    double Vout = 3.0 * ((Float.valueOf(arrOfStr[2]).floatValue()) / 1023.0);
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

                    int transferState = arrOfStr[4];

                    System.out.println("VUV: "+decimalFormat.format(VUV));
                    Data d = new Data(dateFormat.format(new Date()),
                            Double.parseDouble(decimalFormat.format(VUV)),
                            Double.parseDouble(decimalFormat.format(batteryLevel)),
                            Double.parseDouble(decimalFormat.format(TempC)),
                            transferState);
                    System.out.println("data: "+d);
                    dbHelper.createData(d);
                    EventBus.getDefault().postSticky(d);

                    //System.out.println(dailyUVdose*(1+Double.parseDouble(decimalFormat.format((double) getFactor()/100))));
                    System.out.println(getAdjusteddailyUVdose());

                } else {
                    // Handle the case when the split doesn't occur as expected
                    //System.out.println("Unexpected format: " + BLEvalue);
                    System.out.println("Unexpected format: ");
                }

            }
        });
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + resultCode);

        if (requestCode == permissionRequestCode) {
            startBluetooth();
        } else if (resultCode == Activity.RESULT_OK && requestCode == bluetoothRequestCode) {
            init();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_root, fragment);
        transaction.commit();
    }

    private void checkBLESupport() {
        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void startBluetooth() {
        Log.d(TAG, "startBluetooth: " + BluetoothAdapter.getDefaultAdapter().isEnabled() + " " + checkPermission());
        if (checkPermission()) {
            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                enableBluetooth();
            } else {
                init();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void enableBluetooth() {
        Log.d(TAG, "enableBluetooth: ");
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBTIntent, bluetoothRequestCode);
    }

    public String[] getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
        } else {
            return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};
        }
    }

    public boolean checkPermission() {
        boolean result = false;
        for (String permission : getPermissions()) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            } else {
                Log.d(TAG, "checkPermission: " + permission);
                result = false;
                requestPermission();
                break;
            }
        }
        return result;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, getPermissions(), bluetoothRequestCode);
    }

    public void init() {
        Log.d(TAG, "init: ");
        bleController = BLEController.getInstance(MainActivity.this);
        addBLEListener();
    }

    public void addBLEListener() {
        if (bleController != null && checkPermission()) {
            bleController.addBLEControllerListener(MainActivity.this);
            bleController.init();
            Log.d(TAG, "addBLEListener: ");
        }
    }

    public void removeBLEListener() {
        if (bleController != null && checkPermission()) {
            bleController.removeBLEControllerListener(MainActivity.this);
            Log.d(TAG, "removeBLEListener: ");
        }
    }


    public void connect() {
        if (bleController != null) {
            Log.d(TAG, "connect: " + deviceAddress);
            if (TextUtils.isEmpty(deviceAddress)) {
                //Toast.makeText(this, "no device found.", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, R.string.no_device_found, Toast.LENGTH_SHORT).show();
            } else {
                bleController.connectToDevice(deviceAddress);
                //Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, R.string.connecting, Toast.LENGTH_SHORT).show();
                // Add delay before sending Unix time data
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("[BLE]", "Unix time sent 2x");
                        System.out.println("Unix time sent to BLE device");
                        long unixTime = System.currentTimeMillis() / 1000L;
                        remoteControl.sendUnixTime(unixTime);
                    }
                }, 4000);
                //Log.i("[BLE]", "Unix time sent 2x");

            }
        } else {
            startBluetooth();
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnect() {
        if (bleController != null) {
            Log.d(TAG, "disconnect: ");
            bleController.disconnect();
            //Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NotificationPermission")
    public void sendPushNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Push Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("This channel is used for push notifications");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }

        //Data data = new Data(0, "2023-08-10", 23.0, 1.0, 87.5);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        //builder.setContentTitle("UV index Alert");
        builder.setContentTitle(getString(R.string.UV_index_alert));
        //builder.setContentText("The UV index level is above 100% daily dose. Please take precautions. Hide in shade.");
        builder.setContentText(getString(R.string.dose_100));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_SOUND);

        notificationManager.notify(0, builder.build());
    }
    @SuppressLint("NotificationPermission")
    public void sendPushNotification50perc() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Push Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("This channel is used for push notifications");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }

        //Data data = new Data(0, "2023-08-10", 23.0, 1.0, 87.5);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        //builder.setContentTitle("UV index Alert");
        builder.setContentTitle(getString(R.string.UV_index_alert));
        //builder.setContentText("You reached 50% daily UV dose. Please take precautions. Renew sunscreen protection.");
        builder.setContentText(getString(R.string.dose_50));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_SOUND);

        notificationManager.notify(0, builder.build());
    }
    @SuppressLint("NotificationPermission")
    public void sendPushNotificationvitaminD() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Push Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("This channel is used for push notifications");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            notificationManager.createNotificationChannel(channel);
        }

        //Data data = new Data(0, "2023-08-10", 23.0, 1.0, 87.5);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        //builder.setContentTitle("UV index Alert");
        //builder.setContentText("Go outside to catch some sun.");
        builder.setContentTitle(getString(R.string.UV_index_alert));
        builder.setContentText(getString(R.string.go_outside_to_catch_some_sun));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_SOUND);

        notificationManager.notify(0, builder.build());
    }

    public void skinType_I(){
        dailyUVdose = 8000.0; //needs to be changed
        //Toast.makeText(this, "Skin type I", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.skin_type_I, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("dailyUVdose", (float) dailyUVdose);
        editor.apply();
    }

    public void skinType_II(){
        dailyUVdose = 10000.0; //needs to be changed
        //Toast.makeText(this, "Skin type II", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.skin_type_II, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("dailyUVdose", (float) dailyUVdose);
        editor.apply();
    }

    public void skinType_III(){
        dailyUVdose = 12000.0; //needs to be changed
        //Toast.makeText(this, "Skin type III", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.skin_type_III, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("dailyUVdose", (float) dailyUVdose);
        editor.apply();
    }

    public void skinType_IV(){
        dailyUVdose = 18000.0; //needs to be changed
        //Toast.makeText(this, "Skin type IV", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.skin_type_IV, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("dailyUVdose", (float) dailyUVdose);
        editor.apply();
    }

    public void skinType_V(){
        dailyUVdose = 24000.0; //needs to be changed
        //Toast.makeText(this, "Skin type V", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.skin_type_V, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("dailyUVdose", (float) dailyUVdose);
        editor.apply();
    }

    public void skinType_VI(){
        dailyUVdose = 40000.0; //needs to be changed
        //Toast.makeText(this, "Skin type VI", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, R.string.skin_type_VI, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("dailyUVdose", (float) dailyUVdose);
        editor.apply();
    }

    /*public void sunScreen_Factor(int factor){
        Toast.makeText(this, "Sun screen factor "+factor, Toast.LENGTH_SHORT).show();
    }

    public void notConnected(){
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }*/

    public int incrementNumber() {
        factor += 10; // You can adjust the increment as needed
        return factor;
    }

    public int decrementNumber() {
        if (factor > 0) { // Ensure the factor doesn't go below 0.1
            factor -= 10; // You can adjust the decrement as needed
        }
        return factor;
    }

    public int getFactor() {
        Log.i("factor", String.valueOf(factor));
        return factor;
    }

    public double getAdjusteddailyUVdose(){
        if(getFactor() == 0) {
            double AdjusteddailyUVdose = dailyUVdose * 1; //SPF*DUVD = DUVD_adjusted
            return AdjusteddailyUVdose;
        }
        else{
            double AdjusteddailyUVdose = dailyUVdose * Double.parseDouble(decimalFormat.format((double) getFactor())); //SPF*DUVD = DUVD_adjusted
            return AdjusteddailyUVdose;
        }
    }

    //Czerwinska, Agnieszka & Krzyścin, Janusz. (2020). Analysis of Measurements and Modelling of the Biologically Active UV Solar Radiation for Selected Sites in Poland – Assessment of Photo-medical Effects. 10.25171/InstGeoph_PAS_Publs-2020-002.

}
package com.app.ble.utils;


public interface BLEControllerListener {
    void BLEControllerConnected();
    void BLEControllerDisconnected();
    void BLEDeviceFound(String name, String address);
    //void BLEInfo(String data);

    //void showdata(int[] data);

    //void BLEInfo(int[] data);
}
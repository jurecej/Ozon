package com.app.ble.model;

public class Data {
    private long id;
    private String date;
    private double vuv, battery, temperature;

    int transferState;
    public Data() {
    }

    public Data(String date, double vuv, double battery, double temperature, int transferState) {
        this.date = date;
        this.vuv = vuv;
        this.battery = battery;
        this.temperature = temperature;
        this.transferState = transferState;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getVuv() {
        return vuv;
    }

    public void setVuv(double vuv) {
        this.vuv = vuv;
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    public double getTemperature() {
        return temperature;
    }

    public double gettransferState() {
        return transferState;
    }

    public void settransferState(int transferState) {
        this.transferState = transferState;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "Data{" +
                "id=" + id +
                ", date=" + date +
                ", vuv=" + vuv +
                ", battery=" + battery +
                ", temperature=" + temperature +
                ", transferState=" + transferState +
                '}';
    }
}

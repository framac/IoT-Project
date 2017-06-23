package com.example.user.iot.model;


public class Beacon {
    private String macaddress, posizione;
    private double temperatureLevel, accelerometerX, accelerometerY, accelerometerZ, lightLevel;
    private int batteryLevel;

    public Beacon(String macaddress){
        this.macaddress = macaddress;
        this.posizione = null;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public String getPosizione() {
        return posizione;
    }

    public double getTemperatureLevel(){ return temperatureLevel;}

    public double getAccelerometerX(){ return accelerometerX;}

    public double getAccelerometerY(){ return accelerometerY;}

    public double getAccelerometerZ(){ return accelerometerZ;}

    public double getLightLevel(){ return lightLevel;}

    public int getBatteryLevel(){ return batteryLevel;}

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public void setPosizione(String posizione) {this.posizione = posizione;}

    public void setTemperatureLevel(double temperatureLevel){this.temperatureLevel=temperatureLevel;}

    public void setAccelerometerX(double accelerometerX){this.accelerometerX=accelerometerX;}

    public void setAccelerometerY(double accelerometerY){this.accelerometerY=accelerometerY;}

    public void setAccelerometerZ(double accelerometerZ){this.accelerometerZ=accelerometerZ;}

    public void setLightLevel(double lightLevel){this.lightLevel=lightLevel;}

    public void setBatteryLevel(int batteryLevel){this.batteryLevel=batteryLevel;}
}

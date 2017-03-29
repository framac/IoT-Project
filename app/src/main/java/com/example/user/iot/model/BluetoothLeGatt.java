package com.example.user.iot.model;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.fromString;

public class BluetoothLeGatt extends IntentService {

    private final UUID batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID
            UUID_IRT_SERV = fromString("f000aa00-0451-4000-b000-000000000000"),
            UUID_IRT_DATA = fromString("f000aa01-0451-4000-b000-000000000000"),
            UUID_IRT_CONF = fromString("f000aa02-0451-4000-b000-000000000000"), // 0: disable, 1: enable
            UUID_OPT_SERV = fromString("f000aa70-0451-4000-b000-000000000000"),
            UUID_OPT_DATA = fromString("f000aa71-0451-4000-b000-000000000000"),
            UUID_OPT_CONF = fromString("f000aa72-0451-4000-b000-000000000000"),
            UUID_MOV_SERV = fromString("f000aa80-0451-4000-b000-000000000000"),
            UUID_MOV_DATA = fromString("f000aa81-0451-4000-b000-000000000000"),
            UUID_MOV_CONF = fromString("f000aa82-0451-4000-b000-000000000000");


    public static BluetoothGattService temperature;
    public static BluetoothGattService opt;
    public static BluetoothGattService accellerometer;
    private BluetoothGatt mGatt;
    List<BluetoothGattService> services;
    int countLux = 0;
    int countTemp = 0;
    int countAcc = 0;
    boolean data = true;

    public BluetoothLeGatt() {
        super("ReadingService");
    }

    public void getServices(BluetoothDevice device){
        mGatt = device.connectGatt(this, true, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    gatt.discoverServices();
                    Log.i("gattCallback", "STATE_CONNECTED");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            services = gatt.getServices();
            readBattery();
            Log.d("onServicesDiscovered", services.toString());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(characteristic.getUuid().equals(UUID_IRT_DATA)){

                byte[] value = characteristic.getValue();
                Point3D v = Sensor.IR_TEMPERATURE.convert(value);
                    double temperatureLevel = v.x;
                    if(v.x == 0 && countTemp < 20){
                        countTemp++;
                        gatt.readCharacteristic(characteristic);
                    } else{
                        broadcastUpdate(temperatureLevel, "temperatureService");
                    }
            } else if (characteristic.getUuid().equals(UUID_OPT_DATA)){
                byte[] value = characteristic.getValue();
                Point3D v = Sensor.LUXOMETER.convert(value);
                    if(v.x == 0 && countLux < 20){
                        countLux++;
                        gatt.readCharacteristic(characteristic);
                    } else{
                        broadcastUpdate(v.x, "lightService");
                    }

            } else if (characteristic.getUuid().equals(UUID_MOV_DATA)){
                byte[] value = characteristic.getValue();
                Point3D v = Sensor.MOVEMENT_ACC.convert(value);

                if(v.x == 0 && v.y == 0 && v.z == 0 && countAcc < 20){
                    countAcc++;
                    gatt.readCharacteristic(characteristic);
                } else{
                    broadcastUpdate(v.x, v.y, v.z);
                }

            } else {
                Log.i("onCharacteristicRead", characteristic.toString());
                int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                broadcastUpdate(batteryLevel);

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic c){
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt,
                                    BluetoothGattCharacteristic characteristic,
                                    int status){
            if(data){
                if(characteristic.getUuid().equals(UUID_IRT_CONF)) {
                    gatt.readCharacteristic(temperature.getCharacteristic(UUID_IRT_DATA));
                } else if(characteristic.getUuid().equals(UUID_OPT_CONF)){
                    gatt.readCharacteristic(opt.getCharacteristic(UUID_OPT_DATA));
                } else if(characteristic.getUuid().equals(UUID_MOV_CONF)){
                    gatt.readCharacteristic(accellerometer.getCharacteristic(UUID_MOV_DATA));
                }
            }

        }
    };

    @Override
    protected void onHandleIntent(Intent intent) {
        BluetoothDevice device = (BluetoothDevice) intent.getExtras().get("device");
        getServices(device);

    }

    private void broadcastUpdate(int batteryLevel) {
        Intent resIntent = new Intent("BatteryService");
        resIntent.putExtra("batteryLevel", batteryLevel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
        readTemperature();
    }

    private void broadcastUpdate(double value, String stringa) {
        if(stringa.equals("temperatureService")){
            Intent resIntent = new Intent("TemperatureService");
            resIntent.putExtra("temperatureLevel", value);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
            readAccellerometer();
        } else if (stringa.equals("lightService")){
            Intent resIntent = new Intent("LightService");
            resIntent.putExtra("lightLevel", value);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
            data = false;
            BluetoothGattCharacteristic characteristic = temperature.getCharacteristic(UUID_IRT_CONF);
            characteristic.setValue(new byte[]{0x00});
            mGatt.writeCharacteristic(characteristic);
            characteristic = opt.getCharacteristic(UUID_OPT_CONF);
            characteristic.setValue(new byte[]{0x00});
            mGatt.writeCharacteristic(characteristic);
            characteristic = accellerometer.getCharacteristic(UUID_MOV_CONF);
            characteristic.setValue(new byte[]{0x00});
            mGatt.writeCharacteristic(characteristic);
            mGatt.disconnect();

        }
    }

    private void broadcastUpdate(double x, double y, double z) {
            Intent resIntent = new Intent("AccelerometerService");
            resIntent.putExtra("accelerometerX", x);
            resIntent.putExtra("accelerometerY", y);
            resIntent.putExtra("accelerometerZ", z);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
            readLux();
    }

    public void readBattery(){
        for (BluetoothGattService service : services){
            if(service.getUuid().equals(batteryServiceUuid)){
                List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                    mGatt.readCharacteristic(characteristic);
                }
                break;
            }
        }
    }

    public void readTemperature(){
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(UUID_IRT_SERV)) {
                temperature = service;
                BluetoothGattCharacteristic characteristic = temperature.getCharacteristic(UUID_IRT_CONF);
                characteristic.setValue(new byte[]{0x01});
                 mGatt.writeCharacteristic(characteristic);
                 break;
            }
        }
    }

    public void readLux(){
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(UUID_OPT_SERV)) {
                opt = service;
                BluetoothGattCharacteristic characteristic = opt.getCharacteristic(UUID_OPT_CONF);
                characteristic.setValue(new byte[]{0x01});
                mGatt.writeCharacteristic(characteristic);
                break;
            }
        }
    }

    public void readAccellerometer(){
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(UUID_MOV_SERV)) {
                accellerometer = service;
                BluetoothGattCharacteristic characteristic = accellerometer.getCharacteristic(UUID_MOV_CONF);
                characteristic.setValue(new byte[]{(byte)0xFF, 0x00});
                mGatt.writeCharacteristic(characteristic);
                break;
            }
        }
    }
}

package com.example.user.iot.controller;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import com.example.user.iot.model.BluetoothLeGatt;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GestioneConnessioneBA extends Service {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    BluetoothDevice device;

    HashMap<BluetoothDevice, Integer> bluetoothDevices = new HashMap<BluetoothDevice, Integer>();

    boolean scan = false;

    DecimalFormat df = new DecimalFormat("###.##");

    Handler handler = new Handler();
    // Define the code block to be executed
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            if (!scan) {
                startScanning();
            } else {
                stopScanning();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        startScanning();

        return START_NOT_STICKY;
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getDevice().getName() != null && (result.getDevice().getName().equals("SensorTag2") || result.getDevice().getName().equals("CC2650 SensorTag")) && !bluetoothDevices.containsKey(result.getDevice())) {
                bluetoothDevices.put(result.getDevice(), result.getRssi());

            }
        }
    };

    public void startScanning() {
        System.out.println("start scanning");
        scan = true;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
        handler.postDelayed(runnableCode, 5000);
    }

    public void stopScanning() {
        Map<BluetoothDevice, Integer> sortedMap = sortByValue(bluetoothDevices);
        bluetoothDevices.clear();
        scan = false;
        System.out.println("stopping scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });

        Set<BluetoothDevice> listaDevices = sortedMap.keySet();
        for (BluetoothDevice device : listaDevices) {
            this.device = device;
            connectToDevice(device);
            double distance = getDistance(sortedMap.get(device));
            broadcastUpdate(device.getAddress(), distance);
            break;

        }
        // TODO PORTARE RUNNABLECODE A 10000
        handler.postDelayed(runnableCode, 300000);
    }

    private static Map<BluetoothDevice, Integer> sortByValue(Map<BluetoothDevice, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<BluetoothDevice, Integer>> list =
                new LinkedList<Map.Entry<BluetoothDevice, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order

        Collections.sort(list, new Comparator<Map.Entry<BluetoothDevice, Integer>>() {
            public int compare(Map.Entry<BluetoothDevice, Integer> o1,
                               Map.Entry<BluetoothDevice, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<BluetoothDevice, Integer> sortedMap = new LinkedHashMap<BluetoothDevice, Integer>();
        for (Map.Entry<BluetoothDevice, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public void connectToDevice(BluetoothDevice device) {

        startService(new Intent(this, BluetoothLeGatt.class).putExtra("device", device));

    }

    public double getDistance(int rssi) {

//         RSSI = txpower - 10 * n * lg (d)
//         N = 2 (nello spazio libero)
//         D = 10 ^ ((txpower - RSSI) / (10 * n))

        return (Math.pow(10d, ((double) (-30) - rssi) / (10 * 2))) / 100;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void broadcastUpdate(String macAddress, double distance) {
        Intent resIntent = new Intent("DeviceParameters");
        resIntent.putExtra("macAddress", macAddress);
        resIntent.putExtra("distance", distance);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
    }
}


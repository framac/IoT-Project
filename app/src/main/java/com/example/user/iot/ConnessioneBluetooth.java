package com.example.user.iot;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.iot.model.SensorTagBattery;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@RequiresApi(api = Build.VERSION_CODES.M)
public class ConnessioneBluetooth extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    Button startScanningButton;
    Button stopScanningButton;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    ListView lv;
    ArrayAdapter<String> arrayAdapter;
    HashMap<BluetoothDevice, Integer> bluetoothDevices = new HashMap<BluetoothDevice, Integer>();
    boolean scan = false;
    TextView txtValoreBatteria;
    int batteryLevel = 0;
    BluetoothGattCallback mGattCallback;
    BluetoothGatt gattClient;
    SensorTagBattery batteryInfo;
    private final UUID batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    private final UUID batteryLevelUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    BluetoothGattCharacteristic characteristic;


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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    // Run the above code block on the main thread after 2 seconds


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connessione_bluetooth);

        txtValoreBatteria = (TextView) findViewById(R.id.txtValoreBatteria);
        txtValoreBatteria.setText("Sconosciuto");
        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();


        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                if(status == BluetoothGatt.GATT_SUCCESS){
                    Log.d("MYAPP", "VEDIAMO SE SCRIVE" );
                    batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    writeTxt();
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                batteryInfo.getGattClient().disconnect();
                writeTxt();
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
            }

            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }

        };

        lv = (ListView) findViewById(R.id.listaDispositivi);
        arrayAdapter = new ArrayAdapter<String>(ConnessioneBluetooth.this, android.R.layout.simple_list_item_1);
        lv.setAdapter(arrayAdapter);

        startScanning();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getDevice().getName() != null && (result.getDevice().getName().equals("SensorTag2") || result.getDevice().getName().equals("CC2650 SensorTag")) && !bluetoothDevices.containsKey(result.getDevice())) {
                bluetoothDevices.put(result.getDevice(), result.getRssi());
                arrayAdapter.add("Device Name: " + result.getDevice().getName() + " rssi: " + result.getRssi() + "\n");
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void startScanning() {
        System.out.println("start scanning");
        arrayAdapter.clear();
        arrayAdapter.notifyDataSetChanged();
        scan = true;
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
        // Repeat this the same runnable code block again another 2 seconds
        handler.postDelayed(runnableCode, 10000);
    }

    public void stopScanning() {
        Map<BluetoothDevice, Integer> sortedMap = sortByValue(bluetoothDevices);
        bluetoothDevices.clear();
        scan = false;
        System.out.println("stopping scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
        // Repeat this the same runnable code block again another 2 seconds
       Set<BluetoothDevice> listaDevices = sortedMap.keySet();

        for (BluetoothDevice device : listaDevices){
            //batteryInfo = new SensorTagBattery();
            //batteryInfo.getBatteryLevel(device,this, mGattCallback);

            gattClient = device.connectGatt(this, true, mGattCallback);
            BluetoothGattService batteryService = gattClient.getService(batteryServiceUuid);
            if(batteryService != null) {
                characteristic = batteryService.getCharacteristic(batteryLevelUuid);
                if (characteristic != null) {
                    gattClient.readCharacteristic(characteristic);
                }
            }
        }
        //handler.postDelayed(runnableCode, 300000);
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
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<BluetoothDevice, Integer> sortedMap = new LinkedHashMap<BluetoothDevice, Integer>();
        for (Map.Entry<BluetoothDevice, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public void writeTxt(){
        txtValoreBatteria.setText(String.valueOf(batteryLevel));
    }
}

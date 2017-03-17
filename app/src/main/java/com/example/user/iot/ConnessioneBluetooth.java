package com.example.user.iot;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.user.iot.model.BatteryLevel;
import com.example.user.iot.model.TemperatureLevel;
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


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConnessioneBluetooth extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    BluetoothGatt gattClient;
    BluetoothDevice device;

    Button startScanningButton;
    Button stopScanningButton;
    TextView txtValoreBatteria;
    TextView txtValoreTemperatura;
    ListView lv;

    ArrayAdapter<String> arrayAdapter;
    HashMap<BluetoothDevice, Integer> bluetoothDevices = new HashMap<BluetoothDevice, Integer>();

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    boolean scan = false;

    String livelloBatteria;
    String livelloTemperatura;

    Runnable battery;
    Runnable temperatureThread;

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

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connessione_bluetooth);

        txtValoreBatteria = (TextView) findViewById(R.id.txtValoreBatteria);
        txtValoreBatteria.setText("Sconosciuto");
        txtValoreTemperatura = (TextView) findViewById(R.id.txtValoreTemperatura);
        txtValoreTemperatura.setText("Sconosciuto");
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


        battery = new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        txtValoreBatteria.setText(livelloBatteria);
                    }
                });

            }
        };

        temperatureThread = new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        txtValoreTemperatura.setText(livelloTemperatura);
                    }
                });

            }
        };


        lv = (ListView) findViewById(R.id.listaDispositivi);
        arrayAdapter = new ArrayAdapter<String>(ConnessioneBluetooth.this, android.R.layout.simple_list_item_1);
        lv.setAdapter(arrayAdapter);

        startScanning();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //registerReceiver(dataReceiver, makeIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(dataReceiver, makeIntentFilter());

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
            this.device = device;
            connectToDevice(device);
            break;

        }
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

    public void connectToDevice(BluetoothDevice device) {
        if (gattClient == null) {
            BatteryLevel batteryLevel = new BatteryLevel();
            int batteryLevel2 = batteryLevel.getBatteryLevel(gattClient,this, device);
            livelloBatteria = String.valueOf(batteryLevel2);
            new Thread(battery).start();
            TemperatureLevel temperatureLevel = new TemperatureLevel();
            double temperatureLevel1 = temperatureLevel.getTemperatureLevel(gattClient,this, device);
            livelloTemperatura = String.valueOf(temperatureLevel1);
            new Thread(temperatureThread).start();

            gattClient = null;
        }
    }

//    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            if (intent.getAction().equals("LivelloBatteria")) {
//                int batteryLevel = intent.getExtras().getInt("LivelloBatteria");
//                livelloBatteria = String.valueOf(batteryLevel);
//                new Thread(battery).start();
//
//            }
//        }
//    };
//
//    private static IntentFilter makeIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("LivelloBatteria");
//        return intentFilter;
//    }

}


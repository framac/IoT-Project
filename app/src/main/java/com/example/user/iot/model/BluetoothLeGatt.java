package com.example.user.iot.model;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;
import com.example.user.iot.controller.GestioneConnessioneBA;
import com.example.user.iot.utility.Json;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private static BluetoothGattService temperature;
    private static BluetoothGattService opt;
    private static BluetoothGattService accellerometer;
    private BluetoothGatt mGatt;

    private List<BluetoothGattService> services;

    private int countLux = 0;
    private int countTemp = 0;
    private int countAcc = 0;

    private int batteryLevel;
    private double temperatureLevel;
    private Point3D vAcc;
    private Point3D vOpt;


    private int temperatureThreshold;
    private int luxThreshold;
    private int batteryThreshold;
    private double xAccThreshold;
    private double yAccThreshold;
    private double zAccThreshold;

    private boolean data = true;
    private boolean conn = false;
    private boolean dati = false;

    private String macAddress;

    private SharedPreferences prefs;

    private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

    private JSONObject letturaDaSalvare;

    Handler handlerConnessione = new Handler();
    // se non riesco a connettermi al device entro 5 secondi dalla chiamata disconnetto il GattClient
    private Runnable runnableCodeConnessione = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            if (!conn) {
               mGatt.disconnect();
            }
        }
    };


    Handler handlerDati = new Handler();
    // se non riesco a leggere tutti i dati entro 20 secondi mi sconnetto il gattClient perchè probabilmente il Beacon è diventato fuori portata
    private Runnable runnableCodeDati = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            if (!dati) {
                mGatt.disconnect();
            }
        }
    };

    public BluetoothLeGatt() {
        super("ReadingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        BluetoothDevice device = (BluetoothDevice) intent.getExtras().get("device");
        try {
            JSONObject json = new JSONObject(Json.loadJSONFromAsset());
            temperatureThreshold = json.getInt("temperatureThreshold");
            luxThreshold = json.getInt("lightThreshold");
            xAccThreshold = json.getDouble("xAccThreshold");
            yAccThreshold = json.getDouble("yAccThreshold");
            zAccThreshold = json.getDouble("zAccThreshold");
            batteryThreshold = json.getInt("batteryThreshold");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getServices(device);

    }

    public void getServices(BluetoothDevice device){
        mGatt = device.connectGatt(this, true, gattCallback);
        //controlla l'avvenuta connessione al dispositivo entro 5 secondi
        handlerConnessione.postDelayed(runnableCodeConnessione, 5000);
        macAddress = device.getAddress();
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    conn = true;
                    gatt.discoverServices();
                    handlerDati.postDelayed(runnableCodeDati, 20000);
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
                    temperatureLevel = v.x;
                    if(v.x == 0 && countTemp < 20){
                        countTemp++;
                        gatt.readCharacteristic(characteristic);
                    } else{
                        broadcastUpdate(temperatureLevel, "temperatureService");
                    }
            } else if (characteristic.getUuid().equals(UUID_OPT_DATA)){
                byte[] value = characteristic.getValue();
                vOpt = Sensor.LUXOMETER.convert(value);
                    if(vOpt.x == 0 && countLux < 20){
                        countLux++;
                        gatt.readCharacteristic(characteristic);
                    } else{
                        broadcastUpdate(vOpt.x, "lightService");
                    }

            } else if (characteristic.getUuid().equals(UUID_MOV_DATA)){
                byte[] value = characteristic.getValue();
                 vAcc = Sensor.MOVEMENT_ACC.convert(value);

                if(vAcc.x == 0 && vAcc.y == 0 && vAcc.z == 0 && countAcc < 20){
                    countAcc++;
                    gatt.readCharacteristic(characteristic);
                } else{
                    broadcastUpdate(vAcc.x, vAcc.y, vAcc.z);
                }

            } else {
                Log.i("onCharacteristicRead", characteristic.toString());
                batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
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

    private void broadcastUpdate(int batteryLevel) {
        if(batteryLevel <= batteryThreshold){
            Intent resIntent = new Intent("BatteryService");
            resIntent.putExtra("batteryLevel", batteryLevel);
            resIntent.putExtra("dove", macAddress);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
        }
        readTemperature();
    }

    private void broadcastUpdate(double value, String stringa) {
        if(stringa.equals("temperatureService")){
            if(value >= temperatureThreshold) {
                Intent resIntent = new Intent("Incendio");
                resIntent.putExtra("dove", macAddress);
                LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
            }
            readAccellerometer();

        } else if (stringa.equals("LightService")){
            if(value <= luxThreshold) {
                Intent resIntent = new Intent("Illuminazione");
                resIntent.putExtra("dove", macAddress);
                LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
            }
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
            dati=true;
            mGatt.disconnect();

            RequestQueue mRequestQueue= Volley.newRequestQueue(this);
            JSONObject letturaDati=new JSONObject();

            try {
                letturaDati.put("macAdd", macAddress);
                letturaDati.put("batteria", batteryLevel);
                letturaDati.put("temperatura", temperatureLevel);
                letturaDati.put("lux", vOpt.x);
                letturaDati.put("xAcc", vAcc.x);
                letturaDati.put("yAcc", vAcc.y);
                letturaDati.put("zAcc", vAcc.z);
                long timeStamp = System.currentTimeMillis();
                Date date = (new Date(timeStamp));
                letturaDati.put("timestamp", sdf.format(date));
                letturaDati.put("username", prefs.getString("username", null));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.saveDatiAmb), letturaDati, postListener, errorListener);
            mRequestQueue.add(request);
            letturaDaSalvare = letturaDati;
        }
    }

    private void broadcastUpdate(double x, double y, double z) {
        if((x >= xAccThreshold  || x <= xAccThreshold)|| (y >= yAccThreshold || y <= yAccThreshold)
            || (z >= zAccThreshold || z <= zAccThreshold)){
            Intent resIntent = new Intent("Terremoto");
            resIntent.putExtra("dove", macAddress);
            LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
        }
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

    private Response.ErrorListener errorListener=new Response.ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError err)
        {

            Log.d(getString(R.string.datiAmbientali), "Erorre di rete: " +err.getMessage());
            GestioneConnessioneBA.lettureNonSalvate.add(letturaDaSalvare);
        }
    };

    private Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>()
    {
        @Override
        public void onResponse(JSONObject response) {

            try {
                String ris=response.getString("response");
                if(ris.equals("true")) {
                    Log.d(getString(R.string.datiAmbientali), "Dati ambientali salvati");
                    if(!GestioneConnessioneBA.lettureNonSalvate.isEmpty()){
                        saveDataLost(GestioneConnessioneBA.lettureNonSalvate.get(0));
                    }
                } else{
                    Log.d(getString(R.string.datiAmbientali), "Dati ambientali non salvati");
                    GestioneConnessioneBA.lettureNonSalvate.add(letturaDaSalvare);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

        public void saveDataLost(final JSONObject object){

            RequestQueue mRequestQueue= Volley.newRequestQueue(this);
            JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.saveDatiAmb), object,
                    new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String ris=response.getString("response");
                        if(ris.equals("true")) {
                            Log.d(getString(R.string.datiAmbientali), "Dati ambientali salvati");
                            GestioneConnessioneBA.lettureNonSalvate.remove(object);
                            if(!GestioneConnessioneBA.lettureNonSalvate.isEmpty()){
                                saveDataLost(GestioneConnessioneBA.lettureNonSalvate.get(0));
                            }
                        } else{
                            Log.d(getString(R.string.datiAmbientali), "Dati ambientali non salvati");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(getString(R.string.datiAmbientali), "Erorre di rete: " + error.getMessage());                        }
                    }
            );
            mRequestQueue.add(request);
    }
}

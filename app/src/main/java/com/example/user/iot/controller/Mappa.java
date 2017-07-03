package com.example.user.iot.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;
import com.example.user.iot.model.BeaconDataSource;
import com.example.user.iot.model.CustomMapView;
import com.example.user.iot.model.Node;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Mappa extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Node> list;
    private Node node;
    private TextView text;
    private MapViewController mapViewController;
    private GestureDetector gestureDetector;
    private BeaconDataSource datasource;
    private String type;
    boolean service = false;

    Button cerca;
    EditText aula;
    LinearLayout ricercaAula;
    private PointF p;

    private BluetoothManager btManager;
    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner btScanner;
    private BluetoothDevice device;
    private HashMap<BluetoothDevice, Integer> bluetoothDevices = new HashMap<BluetoothDevice, Integer>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mappa);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        datasource = new BeaconDataSource(this);
        datasource.open();

        text = (TextView) findViewById(R.id.textView);
        CustomMapView customMapView = (CustomMapView) findViewById(R.id.map);
        mapViewController = new MapViewController(customMapView);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (mapViewController.isMapReady()) {
                    PointF sCoord = mapViewController.sourceImgCoord(e.getX(), e.getY());
                    list = mapViewController.getCurrentList();
                    if(list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            node = list.get(i);
                            String mac = node.getMacAddress();
                            if(node.isSelected(sCoord) && mac!=null){
                                getLastData(mac);
                            }
                        }
                    }
                }
            }
        });

        mapViewController.setListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        //vengono caricati i beacon e le uscite
        mapViewController.addNodes(datasource.getAllBeacon());
        mapViewController.addNodes(datasource.getExit());

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            if(btAdapter != null && btAdapter.isEnabled()){
                startService(new Intent(getBaseContext(), GestioneConnessioneBA.class));
                LocalBroadcastManager.getInstance(this).registerReceiver(receiver, makeIntentFilter());
                service = true;
                btScanner = btAdapter.getBluetoothLeScanner();
            } else{
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Ricorda!");
                builder.setMessage("Non avendo attivato il bluetooth l'app funzionera solamente come mappa." + '\n' + "Se si desidera usufruire al massimo dell app riavviarla dopo aver attivato il bluetooth" );
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }
        } else{
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Ricorda!");
            builder.setMessage("Non avendo concesso i permessi per la localizzazione l'app funzionera solamente come mappa." + '\n' + "Se si desidera usufruire al massimo dell app riavviarla dopo aver concesso il permesso dalle impostazioni del telefono");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }

        cerca = (Button) findViewById(R.id.Cerca);
        aula =  (EditText) findViewById(R.id.txtAula);
        ricercaAula = (LinearLayout) findViewById(R.id.ricercaAula);


        cerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                node = datasource.getNode(aula.getText().toString());
                if(node==null){
                    aula.setError("L'aula cercata non esiste");
                }
                else {
                    mapViewController.deleteAula();
                    node.setDrawable("Aula");
                    p=node.getPoint();
                    p.x++;
                    node.setPoint(p);
                    mapViewController.addNode(node);
                    mapViewController.changeFloor(node.getFloor());
                }
            }
        });

    }

    @Override
    protected void onNewIntent(Intent newintent){
        super.onNewIntent(newintent);
        setIntent(newintent);
    }

    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
        if (this.getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                if(key.equals("type")){
                    type = getIntent().getExtras().getString(key);
                    if(type.equals("Beacon Cambiati")){
                        RequestQueue mRequestQueue= Volley.newRequestQueue(this);
                        JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET,
                                getResources().getString(R.string.getBeacon), null, postListenerJsonArray, errorListener2);
                        mRequestQueue.add(request);
                    }
                }else if(key.equals("where")){
                    String where = getIntent().getExtras().getString(key);
                    if(type.equals("Incendio")) {
                        node = datasource.getBeacon(where);
                        node.setDrawable("Emergenza");
                        mapViewController.addNode(node);
                        mapViewController.changeFloor(node.getFloor());
                        setTitle("Alert Mode");
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    }
                    if(type.equals("Incendio1")){
                        node = datasource.getNode(where);
                        node.setDrawable("Emergenza");
                        mapViewController.addNode(node);
                        mapViewController.changeFloor(node.getFloor());
                        setTitle("Alert Mode");
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    }
                    if(type.equals("Illuminazione")) {
                        node = datasource.getBeacon(where);
                        node.setDrawable("Illuminazione");
                        mapViewController.addNode(node);
                        mapViewController.changeFloor(node.getFloor());
                        setTitle("Alert Mode");
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    }
                    if(type.equals("Illuminazione1")){
                        node = datasource.getNode(where);
                        node.setDrawable("Illuminazione");
                        mapViewController.addNode(node);
                        mapViewController.changeFloor(node.getFloor());
                        setTitle("Alert Mode");
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    }
                    if(type.equals("Terremoto")){
                        text.setText("Allarme Terremoto, segui le indicazioni a schermo");
                        text.setVisibility(View.VISIBLE);
                        setTitle("Alert Mode");
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    }
                    if(type.equals("Terremoto1")){
                        text.setText("Allarme Terremoto, segui le indicazioni a schermo");
                        text.setVisibility(View.VISIBLE);
                        setTitle("Alert Mode");
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
                    }
                }
            }
        } else {
           ricercaPosizione();
        }
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(service){
            stopService(new Intent(getBaseContext(), GestioneConnessioneBA.class));
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.piano145){
            ricercaAula.setVisibility(View.INVISIBLE);
            mapViewController.changeFloor(145);
        } else if (id == R.id.piano150){
            ricercaAula.setVisibility(View.INVISIBLE);
            mapViewController.changeFloor(150);
        } else if (id == R.id.piano155){
            ricercaAula.setVisibility(View.INVISIBLE);
            mapViewController.changeFloor(155);
        } else if (id == R.id.ricerca){
            ricercaAula.setVisibility(View.INVISIBLE);
            ricercaPosizione();
        } else if (id == R.id.ricercaAula){
            ricercaAula.setVisibility(View.VISIBLE);
            ricercaAula.bringToFront();
        }else if(id == R.id.reset){
            recreate();
        }else if (id == R.id.test1) { //test beacon db
            ricercaAula.setVisibility(View.INVISIBLE);
            node = datasource.getBeacon("24:71:89:E7:13:87");
            mapViewController.addNode(node);
            mapViewController.changeFloor(node.getFloor());
        } else if (id == R.id.test2) { //test nodo db
            ricercaAula.setVisibility(View.INVISIBLE);
            node = datasource.getNode("150WC1");
            mapViewController.addNode(node);
            mapViewController.changeFloor(node.getFloor());
        } else if(id == R.id.test3){ //test emergenza
            ricercaAula.setVisibility(View.INVISIBLE);
            node = new Node(129,465,"Beacon",150,"150Boh");
            node.setDrawable("Emergenza");
            mapViewController.addNode(node);
            mapViewController.changeFloor(150);
            getLastData("24:71:89:E7:13:87");
            text.setText("Emergenza in corso, segui le indicazioni a schermo");
            text.setVisibility(View.VISIBLE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("DeviceParameters")){
                String macAddress = intent.getExtras().getString("macAddress");
                double distance = intent.getExtras().getDouble("distance");
                Node beacon = datasource.getBeacon(macAddress);
                Node utente = new Node((beacon.getPoint().x)-(float) distance,(beacon.getPoint().y)-(float) distance,
                                        R.drawable.user,beacon.getFloor());
                mapViewController.addNode(utente);
                mapViewController.changeFloor(beacon.getFloor());
            } else if (intent.getAction().equals("Incendio")) {
                String macAddress = intent.getExtras().getString("dove");
                node = datasource.getBeacon(macAddress);
                node.setDrawable("Emergenza");
                mapViewController.addNode(node);
                mapViewController.changeFloor(node.getFloor());
                setTitle("Alert Mode");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
            } else if (intent.getAction().equals("Incendio1")) {
                String nodo = intent.getExtras().getString("dove");
                node = datasource.getNode(nodo);
                node.setDrawable("Emergenza");
                mapViewController.addNode(node);
                mapViewController.changeFloor(node.getFloor());
                setTitle("Alert Mode");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
            } else if (intent.getAction().equals("Illuminazione")) {
                String macAddress = intent.getExtras().getString("dove");
                node = datasource.getBeacon(macAddress);
                node.setDrawable("Illuminazione");
                mapViewController.addNode(node);
                mapViewController.changeFloor(node.getFloor());
                setTitle("Alert Mode");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
            } else if (intent.getAction().equals("Illuminazione1")) {
                String nodo = intent.getExtras().getString("dove");
                node = datasource.getNode(nodo);
                node.setDrawable("Illuminazione");
                mapViewController.addNode(node);
                mapViewController.changeFloor(node.getFloor());
                setTitle("Alert Mode");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
            } else if (intent.getAction().equals("Terremoto")) {
                //String macAddress = intent.getExtras().getString("dove");
                text.setText("Allarme Terremoto, segui le indicazioni a schermo");
                text.setVisibility(View.VISIBLE);
                setTitle("Alert Mode");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));

            } else if (intent.getAction().equals("Terremoto1")) {
                text.setText("Allarme Terremoto, segui le indicazioni a schermo");
                text.setVisibility(View.VISIBLE);
                setTitle("Alert Mode");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.RED));
            } else if (intent.getAction().equals("recreate")) {
                recreate();
        }


        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction("DeviceParameters");
        fi.addAction("BatteryService");
        fi.addAction("Incendio");
        fi.addAction("Incendio1");
        fi.addAction("Illuminazione");
        fi.addAction("Illuminazione1");
        fi.addAction("Terremoto");
        fi.addAction("Terremoto1");
        fi.addAction("recreate");
        return fi;
    }

    private Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>()
    {
        @Override
        public void onResponse(JSONObject response) {

            try {
                if(response != null) {
                    Log.d(getString(R.string.datiAmbientali), "Dati ricevuti");
                    if(response.getString("macAdd").equals("null")) {
                        AlertDialog.Builder builder=new AlertDialog.Builder(Mappa.this);
                        builder.setTitle("Dati non disponibili");
                        builder.setMessage("Non ci sono dati da visualizzare per il beacon selezionato");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                    }else{
                        node = datasource.getBeacon(response.getString("macAdd"));
                        AlertDialog.Builder builder=new AlertDialog.Builder(Mappa.this);
                        builder.setTitle("Beacon "+node.getId());
                        builder.setMessage("Mac: " + response.getString("macAdd") + "\n" +
                                "Batteria: " + response.getString("batteria") + "\n" +
                                "Temperatura: " + response.getString("temperatura") + "\n" +
                                "AccelX: " + response.getString("xAcc") + "\n" +
                                "AccelY: " + response.getString("yAcc") + "\n" +
                                "AccelZ: " + response.getString("zAcc") + "\n" +
                                "Luminosità: " + response.getString("lux"));
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.show();
                    }
                } else{
                    Log.d(getString(R.string.datiAmbientali), "Dati non ricevuti");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.Listener<JSONArray> postListenerJsonArray= new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {

            BeaconDataSource datasource =  new BeaconDataSource(MainActivity.context);
            datasource.open();
            datasource.updateBeacon(response);
        }
    };

    private Response.ErrorListener errorListener=new Response.ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError err)
        {
            Log.d(getString(R.string.datiAmbientali), "Erorre di rete: " +err.getMessage());
            AlertDialog.Builder builder=new AlertDialog.Builder(Mappa.this);
            builder.setTitle("Dati non disponibili");
            builder.setMessage("Non è stato possibile collegarsi al server" +
                    " per recuperare i dati del beacon selezionato");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    };

    private Response.ErrorListener errorListener2=new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError err)
        {
            Log.d(MainActivity.context.getResources().getString(R.string.serverError), "Errore di rete. Non è stato possibile accedere al server");
        }
    };

    private void getLastData(String macAddress){
        RequestQueue mRequestQueue= Volley.newRequestQueue(this);
        JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.getDatiAmb)+macAddress, null, postListener, errorListener);
        mRequestQueue.add(request);
    }

    public void ricercaPosizione(){
        if(btAdapter != null && btAdapter.isEnabled()){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    btScanner.startScan(leScanCallback);
                }
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 4 seconds
                    btScanner.stopScan(leScanCallback);

                    Map<BluetoothDevice, Integer> sortedMap = sortByValue(bluetoothDevices);
                    bluetoothDevices.clear();

                    Set<BluetoothDevice> listaDevices = sortedMap.keySet();
                    for (BluetoothDevice device : listaDevices) {
                        double distance = getDistance(sortedMap.get(device));
                        broadcastUpdate(device.getAddress(), distance);
                        break;
                    }
                }
            }, 4000);
        }
    }
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if (result.getDevice().getName() != null && (result.getDevice().getName().equals("SensorTag2") || result.getDevice().getName().equals("CC2650 SensorTag")) && !bluetoothDevices.containsKey(result.getDevice())) {
                bluetoothDevices.put(result.getDevice(), result.getRssi());

            }
        }
    };

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

    public double getDistance(int rssi) {

//         RSSI = txpower - 10 * n * lg (d)
//         N = 2 (nello spazio libero)
//         D = 10 ^ ((txpower - RSSI) / (10 * n))

        return (Math.pow(10d, ((double) (-30) - rssi) / (10 * 2))) / 100;

    }

    private void broadcastUpdate(String macAddress, double distance) {
        Intent resIntent = new Intent("DeviceParameters");
        resIntent.putExtra("macAddress", macAddress);
        resIntent.putExtra("distance", distance);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
    }
}

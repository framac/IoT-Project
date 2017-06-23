package com.example.user.iot.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;
import com.example.user.iot.model.BeaconDataSource;
import com.example.user.iot.model.CustomMapView;
import com.example.user.iot.model.Node;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Mappa extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Node> list;
    private Node node;
    private TextView text;
    private CustomMapView customMapView;
    private MapViewController mapViewController;
    private GestureDetector gestureDetector;
    private BeaconDataSource datasource;
    private int resume = 145;
    boolean service = false,active = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) { //TODO:caricare tutti i beacon e le uscite
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
        customMapView = (CustomMapView) findViewById(R.id.map);
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
                            List<String> dati = node.getDatiBeacon();
                            if(node.isNear(sCoord) && dati!= null){
                                AlertDialog.Builder builder=new AlertDialog.Builder(Mappa.this);
                                builder.setTitle("Beacon "+node.getId());
                                builder.setMessage("Mac: " + dati.get(0) + "\n" +
                                                   "Batteria: " + dati.get(1) + "\n" +
                                                   "Temperatura: " + dati.get(2) + "\n" +
                                                   "AccelX: " + dati.get(3) + "\n" +
                                                   "AccelY: " + dati.get(4) + "\n" +
                                                   "AccelZ: " + dati.get(5) + "\n" +
                                                   "Luminosità: " + dati.get(6));
                                builder.setPositiveButton(android.R.string.ok, null);
                                builder.show();
                            }
                        }
                    }
                }
            }
        });

        mapViewController.changeFloor(145);
        mapViewController.setListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            if(btAdapter != null && btAdapter.isEnabled()){
                startService(new Intent(getBaseContext(), GestioneConnessioneBA.class));
                LocalBroadcastManager.getInstance(this).registerReceiver(receiver, makeIntentFilter());
                service = true;
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

    }

    @Override
    protected void onResume() {
        active = true;
        mapViewController.changeFloor(resume);
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        active = false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) { //il menu settings che esce dai tre puntini
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.test1) { //test db
            node = datasource.getBeacon("24:71:89:E7:13:87");
            mapViewController.addNode(node);
            mapViewController.changeFloor(node.getFloor());
        } else if (id == R.id.test2) {

        } else if(id == R.id.test3){ //test server e messaggio
            node = new Node(129,465,"Beacon",150);
            mapViewController.addNode(node);
            mapViewController.changeFloor(150);
            getLastData("24:71:89:E7:13:87");
            text.setText("Emergenza in corso, segui le indicazioni a schermo");
            text.setVisibility(View.VISIBLE);
        } else if(id == R.id.reset){
            mapViewController.clearFloor(145);
            mapViewController.clearFloor(150);
            mapViewController.clearFloor(155);
            mapViewController.changeFloor(145);
            text.setVisibility(View.INVISIBLE);
        } else if (id == R.id.piano145){
            mapViewController.changeFloor(145);
        } else if (id == R.id.piano150){
            mapViewController.changeFloor(150);
        } else if (id == R.id.piano155){
            mapViewController.changeFloor(155);
        } else if (id == R.id.ricerca){
            Intent resIntent = new Intent("ricercaPosizione");
            LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
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
                //Qua ricevi il macAddres del beacon più vicino, quindi devi usare questo valore
                //per mostare la tua posizione sulla mappa. Ti arriverà questo valore ogni volta
                // che l'app si connette con un beacon

                //Sempre qua riceverai la posizione quando si cliccherà sul tasto ricerca posizione
                // (che è da aggiungere alla tua view, dove preferisci, Come richiesto all'ultima revisione)
                //nell'actionListener sul tasto devi aggiungere queste linee di codice per ottenere la posizione:

                //Intent resIntent = new Intent("ricercaPosizione");
                //LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);

                String macAddress = intent.getExtras().getString("macadress");
                double distance = intent.getExtras().getDouble("distance");
                Node beacon = datasource.getBeacon(macAddress);
                Node utente = new Node((beacon.getPoint().x)-(float) distance,(beacon.getPoint().y)-(float) distance,
                                        R.drawable.user,beacon.getFloor());
                mapViewController.addNode(utente);
                if(active){mapViewController.changeFloor(beacon.getFloor());}
                else if(!active){resume = beacon.getFloor();}
            }
            else if (intent.getAction().equals("BatteryService")) {
                //Per ora lasciar così!;
                int batteryLevel = intent.getExtras().getInt("batteryLevel");
                String macAddress = intent.getExtras().getString("dove");
            } else if (intent.getAction().equals("Incendio")) {
                //Se sei qui è perchè è stato rilevato un incendio da un beacon quindi devi usare
                //la var. macAdress qui sotto per recuperare la posizione del beacon e segnalare in
                //tale posizione un incendio
                String macAddress = intent.getExtras().getString("dove");
                node = datasource.getBeacon(macAddress);
                node.setDrawable("Emergenza");
                mapViewController.addNode(node);
                mapViewController.changeFloor(node.getFloor());
                resume = 200;
                    NotificationCompat.Builder notBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.flame)
                                    .setContentTitle("Allarme Incendio")
                                    .setContentText("Segui le istruzioni dell'app")
                                    .setAutoCancel(true)
                                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                    Intent notificationIntent = new Intent(context, Mappa.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    notBuilder.setContentIntent(pendingIntent);
                    int mNotificationId = 001;
                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, notBuilder.build());

                getLastData(macAddress);
            } else if (intent.getAction().equals("Incendio1")) {
                //Se sei qui è perchè qualcuno manualmente ha invianto un alert per un incendio
                // devi quindi utilizzare la variabile nodo per capire dove è stato segnalato l'incendio
                //e visualizzarlo sulla mappa
                String nodo = intent.getExtras().getString("dove");
                NotificationCompat.Builder notBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.flame)
                                .setContentTitle("Allarme Incendio")
                                .setContentText("Segui le istruzioni dell'app")
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                Intent notificationIntent = new Intent(context, Mappa.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                notBuilder.setContentIntent(pendingIntent);
                int mNotificationId = 001;
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, notBuilder.build());


            } else if (intent.getAction().equals("Illuminazione")) {
                //Se sei qui è perchè è stato rilevato un problema di illuminazione da un beacon quindi devi usare
                //la var. macAdress qui sotto per recuperare la posizione del beacon e segnalare in
                //tale posizione un problema di illuminazione
                String macAddress = intent.getExtras().getString("dove");
                node = datasource.getBeacon(macAddress);
                node.setDrawable("Illuminazione");
                mapViewController.addNode(node);
                mapViewController.changeFloor(node.getFloor());
                NotificationCompat.Builder notBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.flame)
                                .setContentTitle("Problema illuminazione")
                                .setContentText("Il problema sarà presto risolto")
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                Intent notificationIntent = new Intent(context, Mappa.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                notBuilder.setContentIntent(pendingIntent);
                int mNotificationId = 001;
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, notBuilder.build());
                getLastData(macAddress);
            } else if (intent.getAction().equals("Illuminazione1")) {
                //Se sei qui è perchè qualcuno manualmente ha invianto un alert per un problema di illuminazione
                // devi quindi utilizzare la variabile nodo per capire dove è stato segnalato il problema
                //e visualizzarlo sulla mappa
                String nodo = intent.getExtras().getString("dove");
                NotificationCompat.Builder notBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.flame)
                                .setContentTitle("Problema illuminazione")
                                .setContentText("Il problema sarà presto risolto")
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                Intent notificationIntent = new Intent(context, Mappa.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                notBuilder.setContentIntent(pendingIntent);
                int mNotificationId = 001;
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, notBuilder.build());

            } else if (intent.getAction().equals("Terremoto")) {
                //Se sei qui è perchè è stato rilevato un terremoto da un beacon. Io ti passo il macAdress
                // ma visto che il terremoto riguarda tutto l'edificio gestisci come credi meglio l'ermergenza, nel
                //senso che non ce bisogno che fai vedere il pericolo in un punto particolare. Fai come credi meglio
                //String macAddress = intent.getExtras().getString("dove");
                text.setText("Allarme Terremoto, segui le indicazioni a schermo");
                text.setVisibility(View.VISIBLE);
                NotificationCompat.Builder notBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.terremoto)
                                .setContentTitle("Allarme Terremoto")
                                .setContentText("Segui le istruzioni dell'app")
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                Intent notificationIntent = new Intent(context, Mappa.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                notBuilder.setContentIntent(pendingIntent);
                int mNotificationId = 001;
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, notBuilder.build());

            } else if (intent.getAction().equals("Terremoto1")) {
                //Se sei qui è perchè è stato Inviato manualmente un alert per un terremoto. Qua non ho
                //nessun parametro da darti quindi gestisci come sopra e come credi meglio
                text.setText("Allarme Terremoto, segui le indicazioni a schermo");
                text.setVisibility(View.VISIBLE);
                NotificationCompat.Builder notBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.terremoto)
                                .setContentTitle("Allarme Terremoto")
                                .setContentText("Segui le istruzioni dell'app")
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
                Intent notificationIntent = new Intent(context, Mappa.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                notBuilder.setContentIntent(pendingIntent);
                int mNotificationId = 001;
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(mNotificationId, notBuilder.build());
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
        return fi;
    }

    private Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>()
    {
        @Override
        public void onResponse(JSONObject response) {

            try {
                if(response != null) {
                    Log.d(getString(R.string.datiAmbientali), "Dati ricevuti");
                    List<String> datiAmbientali = new ArrayList<>();
                    datiAmbientali.add(response.getString("macAdd"));
                    datiAmbientali.add(response.getString("batteria"));
                    datiAmbientali.add(response.getString("temperatura"));
                    datiAmbientali.add(response.getString("xAcc"));
                    datiAmbientali.add(response.getString("yAcc"));
                    datiAmbientali.add(response.getString("zAcc"));
                    datiAmbientali.add(response.getString("lux"));
                    node = datasource.getBeacon(response.getString("macAdd"));
                    node.setBeacon(datiAmbientali);
                    mapViewController.updateBeacon(node);
                    if(active){mapViewController.changeFloor(node.getFloor());}
                    else if(!active){resume = node.getFloor();}
                } else{
                    Log.d(getString(R.string.datiAmbientali), "Dati non ricevuti");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener errorListener=new Response.ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError err)
        {
            Log.d(getString(R.string.datiAmbientali), "Erorre di rete: " +err.getMessage());
        }
    };

    private void getLastData(String macAddress){
        RequestQueue mRequestQueue= Volley.newRequestQueue(this);
        JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.getDatiAmb)+macAddress, null, postListener, errorListener);
        mRequestQueue.add(request);
    }
}

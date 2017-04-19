package com.example.user.iot.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.example.user.iot.R;
import com.example.user.iot.model.CustomMapView;
import com.example.user.iot.model.Node;

import java.util.ArrayList;

public class Mappa extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Node> list;
    private PointF point;
    private Node node;
    private int i;
    boolean service = false;

    private PointF coordConverter(PointF point, int piano){
        switch(piano) {
            case 145: point.x = Math.round((point.x - 52) * 6.5);
                point.y = Math.round((1600 - ((point.y - 261.5) * 6.5)));
                break;

            case 150: point.x = Math.round((point.x - 51)*6.2);
                point.y = Math.round( (1572-((point.y-255.6)*6.2)));
                break;

            case 155: point.x = Math.round((point.x - 53)*6.4);
                point.y = Math.round( (1600-((point.y-259)*6.4)));
                break;
            default: point.x = 0;
                     point.y = 0;
                     break;
        }
        return point;
    }

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
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //creazione e controllo della view
        CustomMapView customMapView = (CustomMapView) findViewById(R.id.map);
        //customMapView.setZoomEnabled(false);
        customMapView.setImage(ImageSource.resource(R.drawable.floor150));

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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        CustomMapView customMapView = (CustomMapView) findViewById(R.id.map);

        if (id == R.id.test1) {
            list = new ArrayList<>();
            for(i=0;i<2;i++){
                if(i==0) {
                    node = new Node(150,500,R.drawable.purple,150); //EMRL
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                if(i==1) {
                    node = new Node(135,470,R.drawable.purple,150); //R2
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                list.add(i,node);
            }
            customMapView.setImage(ImageSource.resource(R.drawable.floor150));
        } else if (id == R.id.test2) {
            list = new ArrayList<>();
            for(i=0;i<2;i++){
                if(i==0) {
                    node = new Node(110,465,R.drawable.purple,150); //G1G2
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                if(i==1) {
                    node = new Node(92,484,R.drawable.purple,150); //A5
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                list.add(i,node);
            }
            customMapView.setImage(ImageSource.resource(R.drawable.floor150));
        } else if (id == R.id.test3) {
            list = new ArrayList<>();
            for(i=0;i<2;i++){
                if(i==0) {
                    node = new Node(90,480,R.drawable.purple,145); //RG1
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                if(i==1) {
                    node = new Node(133,480,R.drawable.purple,145); //RG2
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                list.add(i,node);
            }
            customMapView.setImage(ImageSource.resource(R.drawable.floor145));
        } else if (id == R.id.test4) {
            list = new ArrayList<>();
            for(i=0;i<2;i++){
                if(i==0) {
                    node = new Node(133,467,R.drawable.purple,155); //WC1
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                if(i==1) {
                    node = new Node(149,472,R.drawable.purple,155); //WC2
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                list.add(i,node);
            }
            customMapView.setImage(ImageSource.resource(R.drawable.floor155));
        } else if (id == R.id.test5){
            list = new ArrayList<>();
            for(i=0;i<2;i++){
                if(i==0) {
                    node = new Node(159,456,R.drawable.purple,155); //ACQ
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                if(i==1) {
                    node = new Node(160,445,R.drawable.purple,155); //UP
                    point = coordConverter(node.getPoint(),node.getPiano());
                    node.setPoint(point);
                }
                list.add(i,node);
            }
            customMapView.setImage(ImageSource.resource(R.drawable.floor155));
        } else if (id == R.id.piano145){
            list = new ArrayList<>();
            customMapView.setImage(ImageSource.resource(R.drawable.floor145));
        } else if (id == R.id.piano150){
            list = new ArrayList<>();
            customMapView.setImage(ImageSource.resource(R.drawable.floor150));
        } else if (id == R.id.piano155){
            list = new ArrayList<>();
            customMapView.setImage(ImageSource.resource(R.drawable.floor155));
        }


        customMapView.setRoute(list);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("DeviceParameters")){
                String macAddress = intent.getExtras().getString("macAddress");
                double distance = intent.getExtras().getDouble("distance");
                System.out.print(distance);
            }
            else if (intent.getAction().equals("BatteryService")) {
                int batteryLevel = intent.getExtras().getInt("batteryLevel");
                System.out.print(batteryLevel);
            } else if (intent.getAction().equals("TemperatureService")) {
                double temperatureLevel = intent.getExtras().getDouble("temperatureLevel");
                System.out.print(temperatureLevel);
            } else if (intent.getAction().equals("AccelerometerService")) {
                double accelerometerX = intent.getExtras().getDouble("accelerometerX");
                double accelerometerY = intent.getExtras().getDouble("accelerometerY");
                double accelerometerZ = intent.getExtras().getDouble("accelerometerZ");
            } else if (intent.getAction().equals("LightService")) {
                double light = intent.getExtras().getDouble("lightLevel");
                System.out.print(light);
            }
        }
    };

    private static IntentFilter makeIntentFilter() {
        final IntentFilter fi = new IntentFilter();
        fi.addAction("DeviceParameters");
        fi.addAction("BatteryService");
        fi.addAction("TemperatureService");
        fi.addAction("AccelerometerService");
        fi.addAction("LightService");
        return fi;
    }
}

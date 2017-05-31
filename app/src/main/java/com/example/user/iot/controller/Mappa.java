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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.user.iot.R;
import com.example.user.iot.model.CustomMapView;
import com.example.user.iot.model.Node;

import java.util.ArrayList;

public class Mappa extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ArrayList<Node> list;
    private Node node;
    private TextView text;
    private CustomMapView customMapView;
    private MapViewController mapViewController;
    private GestureDetector gestureDetector;
    boolean service = false;

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
        text = (TextView) findViewById(R.id.textView);
        customMapView = (CustomMapView) findViewById(R.id.map);
        mapViewController = new MapViewController(customMapView);
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (mapViewController.isMapReady()) {
                    PointF sCoord = mapViewController.sourceImgCoord(e.getX(), e.getY());
                    text.setText(sCoord.toString());
                    list = mapViewController.getCurrentList();
                    if(list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            node = list.get(i);
                            if(node.isNear(sCoord)){
                                AlertDialog.Builder builder=new AlertDialog.Builder(Mappa.this);
                                builder.setTitle("Node selezionato");
                                builder.setMessage(node.getPoint().toString() );
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

        if (id == R.id.test1) {
            list = new ArrayList<>();
            for(int i=0;i<10;i++){
                if(i==0) {
                    node = new Node(150,500,R.drawable.beacon,150); //EMRL
                }if(i==1) {
                    node = new Node(135,470,R.drawable.flame,150); //R2
                }if(i==2) {
                    node = new Node(156,452,R.drawable.beacon,155); //ACQ
                }if(i==3) {
                    node = new Node(154,454,R.drawable.exit,155); //UP
                }if(i==4) {
                    node = new Node(110,465,R.drawable.user,150); //G1G2
                }if(i==5) {
                    node = new Node(92,484,R.drawable.target,150); //A5
                }if(i==6) {
                    node = new Node(90,480,R.drawable.user,145); //RG1
                }if(i==7) {
                    node = new Node(133,480,R.drawable.flame,145); //RG2
                }if(i==8) {
                    node = new Node(100,454,R.drawable.target,155); //WC1
                }if(i==9) {
                    node = new Node(149,472,R.drawable.beacon,150); //WC2
                }
                list.add(i,node);
            }
            mapViewController.clearFloor(145);
            mapViewController.clearFloor(150);
            mapViewController.clearFloor(155);
            mapViewController.addNodes(list);
            mapViewController.changeFloor(150);
        } else if (id == R.id.test2) {
            list = new ArrayList<>();
            for(int i=0;i<10;i++){
                if(i==0) {
                    node = new Node(150,500,R.drawable.beacon,155); //EMRL
                }if(i==1) {
                    node = new Node(135,470,R.drawable.flame,155); //R2
                }if(i==2) {
                    node = new Node(159,456,R.drawable.beacon,155); //ACQ
                }if(i==3) {
                    node = new Node(160,445,R.drawable.beacon,155); //UP
                }if(i==4) {
                    node = new Node(110,465,R.drawable.user,155); //G1G2
                }if(i==5) {
                    node = new Node(92,484,R.drawable.target,155); //A5
                }if(i==6) {
                    node = new Node(90,480,R.drawable.user,155); //RG1
                }if(i==7) {
                    node = new Node(133,480,R.drawable.flame,155); //RG2
                }if(i==8) {
                    node = new Node(133,467,R.drawable.target,155); //WC1
                }if(i==9) {
                    node = new Node(149,472,R.drawable.beacon,155); //WC2
                }
                list.add(i,node);
            }
            mapViewController.clearFloor(155);
            mapViewController.addNodes(list);
            mapViewController.changeFloor(155);
        } else if (id == R.id.piano145){
            mapViewController.changeFloor(145);
        } else if (id == R.id.piano150){
            mapViewController.changeFloor(150);
        } else if (id == R.id.piano155){
            mapViewController.changeFloor(155);
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
                System.out.print("ciao");
                System.out.print(macAddress);

            } else if (intent.getAction().equals("Incendio1")) {
                //Se sei qui è perchè qualcuno manualmente ha invianto un alert per un incendio
                // devi quindi utilizzare la variabile nodo per capire dove è stato segnalato l'incendio
                //e visualizzarlo sulla mappa
                String nodo = intent.getExtras().getString("dove");

            } else if (intent.getAction().equals("Illuminazione")) {
                //Se sei qui è perchè è stato rilevato un problema di illuminazione da un beacon quindi devi usare
                //la var. macAdress qui sotto per recuperare la posizione del beacon e segnalare in
                //tale posizione un problema di illuminazione
                String macAddress = intent.getExtras().getString("dove");
                System.out.print("ciao");
                System.out.print(macAddress);

            } else if (intent.getAction().equals("Illuminazione1")) {
                //Se sei qui è perchè qualcuno manualmente ha invianto un alert per un problema di illuminazione
                // devi quindi utilizzare la variabile nodo per capire dove è stato segnalato il problema
                //e visualizzarlo sulla mappa
                String nodo = intent.getExtras().getString("dove");

            } else if (intent.getAction().equals("Terremoto")) {
                //Se sei qui è perchè è stato rilevato un terremoto da un beacon. Io ti passo il macAdress
                // ma visto che il terremoto riguarda tutto l'edificio gestisci come credi meglio l'ermergenza, nel
                //senso che non ce bisogno che fai vedere il pericolo in un punto particolare. Fai come credi meglio
                String macAddress = intent.getExtras().getString("dove");

            } else if (intent.getAction().equals("Terremoto1")) {
                //Se sei qui è perchè è stato Inviato manualmente un alert per un terremoto. Qua non ho
                //nessun parametro da darti quindi gestisci come sopra e come credi meglio
                System.out.print("ciao");
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



}

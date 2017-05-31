package com.example.user.iot.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    Button accedi,registrati;
    EditText editUser,editPass;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public static Context context;
    //Shared preferences
    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getBaseContext();
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        //richiesta
        if(!prefs.getBoolean("firstTime", false)) {
            RequestQueue mRequestQueue= Volley.newRequestQueue(this);
            JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.saveNewUser), null, postListener, errorListener);
            mRequestQueue.add(request);
        }
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        //TODO gesitre eccezioni quando si nega il bluetooth o l'altra permission
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            });
            builder.show();
        }

        accedi = (Button) findViewById(R.id.Accedi);
        registrati = (Button) findViewById(R.id.Registrati);
        editUser =  (EditText) findViewById(R.id.txtUser);
        editPass = (EditText) findViewById(R.id.txtPass);

        registrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent registrazione = new Intent(getApplicationContext(), Registrazione.class);
                    startActivity(registrazione);

            }
        });

        accedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mappa = new Intent(getApplicationContext(),Mappa.class);
                startActivity(mappa);

            }
        });
    }

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
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Attenzione!");
            builder.setMessage("Non attivando il bluetooth l'app funzionerà in maniera limitata");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }
    private Response.ErrorListener errorListener=new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError err)
           {
                    Toast.makeText(MainActivity.context, "Errore di rete. Non è stato possibile accedere al server", Toast.LENGTH_SHORT).show();
           }
    };

    private Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String ris=response.getString("response");
                if(!ris.equals("null")) {
                    editor = prefs.edit();
                    editor.putBoolean("firstTime", true);
                    editor.putString("username",ris);
                    editor.commit();
                    MyFirebaseInstanceIDService.sendRegistrationToServer(ris);
                    Toast.makeText(MainActivity.context, "Username casuale memorizzato", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.context, "Non è stato generato lo username", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                    e.printStackTrace();
            }
        }
    };
}


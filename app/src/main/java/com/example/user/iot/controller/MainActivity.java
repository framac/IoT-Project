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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.user.iot.utility.Md5Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    Button accedi,registrati;
    EditText editUser,editPass;
    TextView utenteOspite;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public static Context context;
    //Shared preferences
    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;

    public BeaconDataSource datasource;
    public SharedPreferences prefBeacon;
    public SharedPreferences.Editor editorBeacon;
    public Md5Utility PasswordCrypt;
    public String pass;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getBaseContext();
        prefs= PreferenceManager.getDefaultSharedPreferences(this);

        datasource = new BeaconDataSource(this);
        datasource.open();
        prefBeacon=PreferenceManager.getDefaultSharedPreferences(this);

        //richiesta
        if(!prefs.getBoolean("firstTimeUsername", false)) {
            RequestQueue mRequestQueue= Volley.newRequestQueue(this);
            JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.saveNewUser), null, postListener, errorListener);
            mRequestQueue.add(request);
        }


        //richiesta posizioni dei beacon
        if(!prefBeacon.getBoolean("firstTimeDb", false)) {
            RequestQueue mRequestQueue= Volley.newRequestQueue(this);
            JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET,getResources().getString(R.string.getBeacon), null, postListenerJsonArray, errorListener);
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
        utenteOspite = (TextView) findViewById(R.id.UtenteOspite);

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
                if(!TextUtils.isEmpty(editUser.getText().toString()) && !TextUtils.isEmpty(editPass.getText().toString())){
                    pass=PasswordCrypt.encrypt(editPass.getText().toString());
                    RequestQueue mRequestQueue= Volley.newRequestQueue(context);
                    JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.controlUser)+"username/"+editUser.getText().toString()+"/password/"+pass, null, postListener, errorListener);
                    mRequestQueue.add(request);
                }
                else if (TextUtils.isEmpty(editUser.getText().toString())){
                    editUser.setError("Il campo non può essere vuoto!");
                }
                else if (TextUtils.isEmpty(editPass.getText().toString())){
                    editPass.setError("Il campo non può essere vuoto!");
                }

            }
        });

        utenteOspite.setOnClickListener(new View.OnClickListener() {
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
            Log.d(MainActivity.context.getResources().getString(R.string.serverError), "Errore di rete. Non è stato possibile accedere al server");
        }
    };

    private Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String ris=response.getString("response");
                if (ris.equals("true")){
                    Intent mappa = new Intent(getApplicationContext(),Mappa.class);
                    startActivity(mappa);
                    Log.d(MainActivity.context.getResources().getString(R.string.autenticazione), "Autenticazione avvenuta con successo");
                }
                else if (ris.equals("false")){
                    Log.d(MainActivity.context.getResources().getString(R.string.autenticazione), "Autenticazione fallita");
                }
                else if(!ris.equals("null")){
                    editor = prefs.edit();
                    editor.putBoolean("firstTimeUsername", true);
                    editor.putString("username",ris);
                    editor.commit();
                    MyFirebaseInstanceIDService.sendRegistrationToServer(ris);
                    Log.d(MainActivity.context.getResources().getString(R.string.username), "Username casuale memorizzato");
                }
                else{
                    Log.d(MainActivity.context.getResources().getString(R.string.username), "Non è stato generato lo username");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onResume() {
        datasource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        datasource.close();
        super.onPause();
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getApplicationContext().getAssets().open("nodi.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void readNodi() {
        try{
            JSONArray nodes = new JSONArray(loadJSONFromAsset());
            datasource.createNode(nodes);

        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Response.Listener<JSONArray> postListenerJsonArray= new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {
            if (Build.VERSION.SDK_INT >= 19) {
                // run your one time code
                editorBeacon = prefBeacon.edit();
                editorBeacon.putBoolean("firstTimeDb", true);
                editorBeacon.commit();
                datasource.createBeacon(response);
                readNodi();
            }

        }
    };

}
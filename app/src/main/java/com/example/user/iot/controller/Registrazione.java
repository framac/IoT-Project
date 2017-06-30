package com.example.user.iot.controller;

/**
 * Created by user on 15/03/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;
import com.example.user.iot.utility.Md5Utility;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;


public class Registrazione extends AppCompatActivity {

    Button conferma;
    EditText editNome,editCognome,editEmail,editUsername,editPassword,editConfermaPass;
    public Md5Utility PasswordCrypt;
    public String pass;
    public JSONObject datiUtente;
    public static Context context;
    String token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrazione);

        context = this.getBaseContext();

        conferma = (Button) findViewById(R.id.Conferma);
        editNome = (EditText) findViewById(R.id.txtNome);
        editCognome =  (EditText) findViewById(R.id.txtCognome);
        editEmail = (EditText) findViewById(R.id.txtEmail);
        editUsername = (EditText) findViewById(R.id.txtUsername);
        editPassword =  (EditText) findViewById(R.id.txtPassword);
        editConfermaPass = (EditText) findViewById(R.id.txtRipetiPassword);

        token = FirebaseInstanceId.getInstance().getToken();

        final RequestQueue mRequestQueue= Volley.newRequestQueue(this);

        conferma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(editNome.getText().toString())&&!TextUtils.isEmpty(editCognome.getText().toString())&&!TextUtils.isEmpty(editEmail.getText().toString())&&!TextUtils.isEmpty(editUsername.getText().toString())&&!TextUtils.isEmpty(editPassword.getText().toString())&&!TextUtils.isEmpty(editConfermaPass.getText().toString())){
                    if(Patterns.EMAIL_ADDRESS.matcher(editEmail.getText().toString()).matches()){
                        if(editPassword.getText().toString().equals(editConfermaPass.getText().toString())){
                            pass=PasswordCrypt.encrypt(editPassword.getText().toString());
                            JSONObject nuovoUtente=new JSONObject();

                            try {
                                nuovoUtente.put("nome", editNome.getText().toString());
                                nuovoUtente.put("cognome", editCognome.getText().toString());
                                nuovoUtente.put("email", editEmail.getText().toString());
                                nuovoUtente.put("username", editUsername.getText().toString());
                                nuovoUtente.put("password", pass);
                                nuovoUtente.put("token",token);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JsonObjectRequest request=new JsonObjectRequest(getResources().getString(R.string.newUser), nuovoUtente, postListener, errorListener);
                            mRequestQueue.add(request);
                            datiUtente=nuovoUtente;

                        }
                        else{
                            editConfermaPass.setError("Ripetere la stessa password!");

                        }

                    }
                    else{
                        editEmail.setError("Email non corretta!");
                    }
                }
                else if(TextUtils.isEmpty(editNome.getText().toString())){
                    editNome.setError("Il campo non può essere vuoto!");
                }
                else if(TextUtils.isEmpty(editCognome.getText().toString())){
                    editCognome.setError("Il campo non può essere vuoto!");
                }
                else if(TextUtils.isEmpty(editEmail.getText().toString())){
                    editEmail.setError("Il campo non può essere vuoto!");
                }
                else if(TextUtils.isEmpty(editUsername.getText().toString())){
                    editUsername.setError("Il campo non può essere vuoto!");
                }
                else if(TextUtils.isEmpty(editPassword.getText().toString())){
                    editPassword.setError("Il campo non può essere vuoto!");
                }
                else if(TextUtils.isEmpty(editConfermaPass.getText().toString())){
                    editConfermaPass.setError("Il campo non può essere vuoto!");
                }

            }
        });


    }

    private Response.ErrorListener errorListener=new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError err)
        {
            Log.d(Registrazione.context.getResources().getString(R.string.serverError), "Errore di rete. Non è stato possibile accedere al server");
        }
    };

    private Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                String ris=response.getString("response");
                if (ris.equals("true")){
                    Log.d(Registrazione.context.getResources().getString(R.string.registrazione), "La registrazione è avvennuta con successo");
                    Intent mappa = new Intent(getApplicationContext(),Mappa.class);
                    startActivity(mappa);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


}

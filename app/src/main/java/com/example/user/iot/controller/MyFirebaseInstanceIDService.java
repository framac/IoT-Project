package com.example.user.iot.controller;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        Log.d(MainActivity.context.getResources().getString(R.string.token), "Refreshed token: " + FirebaseInstanceId.getInstance().getToken());

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sendRegistrationToServer(prefs.getString("username", null));
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     */
    public static void sendRegistrationToServer(String username) {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();

        RequestQueue mRequestQueue = Volley.newRequestQueue(MainActivity.context);
        JSONObject letturaDati=new JSONObject();
        try {
            letturaDati.put("username", username);
            letturaDati.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(MainActivity.context.getResources().getString(R.string.saveToken), letturaDati,
                new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String ris=response.getString("response");
                    if(ris.equals("true")) {
                        Log.d(MainActivity.context.getResources().getString(R.string.token), "Refreshed token: Token salvato");
                    } else{
                        Log.d(MainActivity.context.getResources().getString(R.string.token), "Refreshed token: Token non salvato");
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
                        Log.d(MainActivity.context.getResources().getString(R.string.serverError), "Errore di rete: " +error.getMessage());
                    }
                }
        );
        mRequestQueue.add(request);
    }
}

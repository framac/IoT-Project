package com.example.user.iot.controller;

import android.util.Log;
import android.widget.Toast;

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

    private static final String TAG = "MyFirebaseIIDService";
    public static String username;
    public static boolean primoAccesso=false;
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(MainActivity.context);
        JSONObject letturaDati=new JSONObject();
        try {
            //TODO sostituire username con valore dopo login
            // while(username==null){

            //}
            letturaDati.put("username", "mike");
            letturaDati.put("token", token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request=new JsonObjectRequest(MainActivity.context.getResources().getString(R.string.saveToken), letturaDati, postListener, errorListener);
        mRequestQueue.add(request);
    }

    public static Response.ErrorListener errorListener=new Response.ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError err)
        {
          Toast.makeText(MainActivity.context, "Errore di rete. Non è stato possibile inviare la lettura al server", Toast.LENGTH_SHORT).show();
        }
    };

    public static Response.Listener<JSONObject> postListener= new Response.Listener<JSONObject>()
    {
        @Override
        public void onResponse(JSONObject response) {

            try {
                String ris=response.getString("response");
                if(ris.equals("true")) {
                    Toast.makeText(MainActivity.context, "Dati salvati", Toast.LENGTH_SHORT).show();
                } else{
                  Toast.makeText(MainActivity.context, "Dati non salvati", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
}

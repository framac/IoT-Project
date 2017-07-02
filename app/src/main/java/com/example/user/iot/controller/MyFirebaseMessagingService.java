package com.example.user.iot.controller;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.iot.R;
import com.example.user.iot.model.BeaconDataSource;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "FROM:" + remoteMessage.getFrom());

        Log.d("ciao", "Message data: " + "ciao");

        //Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            Log.d("ciao", "Message data: " + remoteMessage.getData().get("type"));
        }

        //Check if the message contains notification

        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getData().get("where"), remoteMessage.getData().get("type"));
       }
    }


    private void sendNotification(String body, String title) {

        if (title != null){
            if(title.equals("Beacon Cambiati")){
                RequestQueue mRequestQueue= Volley.newRequestQueue(this);
                JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET,getResources().getString(R.string.getBeacon), null, postListenerJsonArray, errorListener);
                mRequestQueue.add(request);
            } else{
                Intent resIntent = new Intent(title);
                if(!body.equals("null")){
                    resIntent.putExtra("dove", body);
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);

                Intent resIntent2 = new Intent("alert");
                LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent2);
            }
        }
    }

    private Response.Listener<JSONArray> postListenerJsonArray= new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {

            BeaconDataSource datasource =  new BeaconDataSource(MainActivity.context);
            datasource.open();
            datasource.updateBeacon(response);
            Broadcaster("recreate");

        }
    };

    private Response.ErrorListener errorListener=new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError err)
        {
            Log.d(MainActivity.context.getResources().getString(R.string.serverError), "Errore di rete. Non è stato possibile accedere al server");
        }
    };

    private void Broadcaster(String title){

        Intent resIntent = new Intent(title);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resIntent);
    }


}
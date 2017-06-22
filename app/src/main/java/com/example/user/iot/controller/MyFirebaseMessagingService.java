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

        //Check if the message contains data
        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        //Check if the message contains notification

        if(remoteMessage.getNotification() != null) {
            Log.d(TAG, "Mesage body:" + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getNotification().getTitle());
        }
    }

    /**
     * Dispay the notification
     * @param body
     */
    private void sendNotification(String body, String title) {

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/*Request code*/, intent, PendingIntent.FLAG_ONE_SHOT);
//            //Set sound of notification
//            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//
//            NotificationCompat.Builder notifiBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle(title)
//                    .setContentText(body)
//                    .setAutoCancel(true)
//                    .setSound(notificationSound)
//                    .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(0 /*ID of notification*/, notifiBuilder.build());

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

    private Response.Listener<JSONArray> postListenerJsonArray= new Response.Listener<JSONArray>(){
        @Override
        public void onResponse(JSONArray response) {

            BeaconDataSource datasource =  new BeaconDataSource(MainActivity.context);
            datasource.open();
            datasource.updateBeacon(response);
        }
    };

    private Response.ErrorListener errorListener=new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError err)
        {
            Log.d(MainActivity.context.getResources().getString(R.string.serverError), "Errore di rete. Non è stato possibile accedere al server");
        }
    };

}
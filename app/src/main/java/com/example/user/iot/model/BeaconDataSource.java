package com.example.user.iot.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BeaconDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_MACADDRESS, MySQLiteHelper.COLUMN_POSIZIONE, MySQLiteHelper.COLUMN_PIANO,
            MySQLiteHelper.COLUMN_X,MySQLiteHelper.COLUMN_Y};

    public JSONObject beaconObject;

    public BeaconDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }



    public void createBeacon(JSONArray beacon) {
        ContentValues values = new ContentValues();
        long insertId;
        try{
            for (int i = 0; i < beacon.length(); i++) {
                beaconObject = beacon.getJSONObject(i);
                values.put(MySQLiteHelper.COLUMN_MACADDRESS, beaconObject.getString("macAdd"));
                values.put(MySQLiteHelper.COLUMN_POSIZIONE, beaconObject.getString("posizione"));
                values.put(MySQLiteHelper.COLUMN_PIANO, beaconObject.getString("piano"));
                values.put(MySQLiteHelper.COLUMN_X, beaconObject.getString("x"));
                values.put(MySQLiteHelper.COLUMN_Y, beaconObject.getString("y"));
                insertId = database.insert(MySQLiteHelper.TABLE_BEACON, null,
                        values);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public Node getBeacon(String mac){
        Node node = null;
        List<String> dati = new ArrayList<>();
        dati.add(mac);
        for(int i=0; i<6; i++){
            dati.add("Non disponibile");
        }
        Cursor cursor = database.query(MySQLiteHelper.TABLE_BEACON,
                allColumns, "macaddress = ?", new String[]{mac}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            node = new Node(cursor.getFloat(4),cursor.getFloat(5),"Beacon",cursor.getInt(3),dati);
            node.setId(cursor.getString(2));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return node;
    }

    public ArrayList<Node> getAllBeacon() {
        ArrayList<Node> listBeacon= new ArrayList<>();
        Node node = null;
        String id;
        List<String> dati = new ArrayList<>();
        for(int i=0; i<7; i++){
            dati.add("Non disponibile");
        }

        Cursor cursor = database.query(MySQLiteHelper.TABLE_BEACON,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dati.set(0,cursor.getString(1));
            node = new Node(cursor.getFloat(4),cursor.getFloat(5),"Beacon",cursor.getInt(3),dati);
            node.setId(cursor.getString(2));
            listBeacon.add(node);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listBeacon;

    }

    public void updateBeacon(JSONArray beacon) {
        database.delete(MySQLiteHelper.TABLE_BEACON, MySQLiteHelper.COLUMN_ID, null);
        createBeacon(beacon);
    }
}

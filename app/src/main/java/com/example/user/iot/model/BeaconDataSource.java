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

    //Colonne tabella beacon
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_MACADDRESS, MySQLiteHelper.COLUMN_POSIZIONE, MySQLiteHelper.COLUMN_PIANO,
            MySQLiteHelper.COLUMN_X,MySQLiteHelper.COLUMN_Y};

    //Colonne tabella nodi
    private String[] Columns = {MySQLiteHelper.COLUMN_KEY,
            MySQLiteHelper.COLUMN_COORDX, MySQLiteHelper.COLUMN_COORDY,
            MySQLiteHelper.COLUMN_QUOTA, MySQLiteHelper.COLUMN_CODICE};


    public JSONObject beaconObject;
    public JSONObject nodeObject;

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

        Cursor cursor = database.query(MySQLiteHelper.TABLE_BEACON,
                allColumns, "macaddress = ?", new String[]{mac}, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            node = new Node(cursor.getFloat(4),cursor.getFloat(5),"Beacon",
                    cursor.getInt(3),cursor.getString(2),mac);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return node;
    }

    public ArrayList<Node> getAllBeacon() {
        ArrayList<Node> listBeacon= new ArrayList<>();
        Node node = null;

        Cursor cursor = database.query(MySQLiteHelper.TABLE_BEACON,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            node = new Node(cursor.getFloat(4),cursor.getFloat(5),"Beacon",
                    cursor.getInt(3),cursor.getString(2),cursor.getString(1));
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


    public void createNode(JSONArray nodes) {
        //Per svuotare la tabella per i vari test dell'aplicazione
        //database.delete(MySQLiteHelper.TABLE_NODI, MySQLiteHelper.COLUMN_KEY, null);

        ContentValues values = new ContentValues();
        long nodeId = 0;
        int i=0;
        try{
            while (i < nodes.length()) {
                if(i==0){
                    i++;
                }
                else {
                    nodeObject = nodes.getJSONObject(i);
                    values.put(MySQLiteHelper.COLUMN_COORDX, nodeObject.getString("FIELD1"));
                    values.put(MySQLiteHelper.COLUMN_COORDY, nodeObject.getString("FIELD2"));
                    values.put(MySQLiteHelper.COLUMN_QUOTA, nodeObject.getString("FIELD3"));
                    values.put(MySQLiteHelper.COLUMN_CODICE, nodeObject.getString("FIELD4"));
                    nodeId = database.insert(MySQLiteHelper.TABLE_NODI, null,
                            values);
                    i++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Node getNode(String code){
        Node node = null;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NODI,
                Columns, "codice = ?", new String[]{code}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            node = new Node(cursor.getFloat(1),cursor.getFloat(2),"Beacon",cursor.getInt(3), cursor.getString(4));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return node;
    }

    public ArrayList<Node> getExit() {
        ArrayList<Node> listExit= new ArrayList<>();
        Node node = null;

        String query = "SELECT * FROM "+MySQLiteHelper.TABLE_NODI+" WHERE "+MySQLiteHelper.COLUMN_CODICE+" LIKE '%EM%'";
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            node = new Node(cursor.getFloat(1),cursor.getFloat(2),"Uscita",cursor.getInt(3),cursor.getString(4));
            listExit.add(node);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listExit;

    }

    public List<String> getAllNodes() {
        List<String> listNodes= new ArrayList<String>();
        String id;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NODI,
                Columns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            id=String.valueOf(cursor.getLong(0));
            listNodes.add("Coordinata x: "+cursor.getLong(1)+ " coordinata y : "+cursor.getLong(2)+", quota: "+cursor.getString(3)+ ", codice: "+cursor.getString(4));
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return listNodes;


    }
}

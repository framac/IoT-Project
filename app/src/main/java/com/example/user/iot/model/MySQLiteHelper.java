package com.example.user.iot.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_BEACON = "beacon";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MACADDRESS = "macaddress";
    public static final String COLUMN_POSIZIONE = "posizione";
    public static final String COLUMN_PIANO = "piano";
    public static final String COLUMN_X = "x";
    public static final String COLUMN_Y = "y";


    public static final String TABLE_NODI = "nodi";
    public static final String COLUMN_KEY = "_key";
    public static final String COLUMN_COORDX = "coordx";
    public static final String COLUMN_COORDY = "coordy";
    public static final String COLUMN_QUOTA = "quota";
    public static final String COLUMN_CODICE = "codice";


    //Database
    private static final String DATABASE_NAME = "beacon.db";
    private static final int DATABASE_VERSION = 1;


    // Database creation sql statement

    //Tabella beacon
    private static final String DATABASE_CREATE = "create table "
            + TABLE_BEACON + "( " + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_MACADDRESS + " text not null, " + COLUMN_POSIZIONE
            + " text not null, " + COLUMN_PIANO + " text not null, " + COLUMN_X
            + " integer not null, " + COLUMN_Y + " integer not null);";

    //Tabella nodi
    private static final String DATABASE_NODI =  "create table "
            + TABLE_NODI + "( " + COLUMN_KEY
            + " integer primary key autoincrement, " + COLUMN_COORDX + " integer not null, "
            + COLUMN_COORDY + " integer not null, " + COLUMN_QUOTA + " text not null, " + COLUMN_CODICE + " text not null);";


    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        database.execSQL(DATABASE_NODI);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEACON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NODI);
        onCreate(db);
    }

}
package com.app.ble.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.ble.model.Data;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Ble.db";

    private static final String TABLE_DATA = "Sensor_Data";

    private static final String KEY_ID = "Id";
    private static final String KEY_DATE = "Date";
    private static final String KEY_VUV = "vuv";
    private static final String KEY_BATTERY = "Battery";
    private static final String KEY_TEMPERATURE = "Temperature";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //String CREATE_TABLE = "CREATE TABLE " + TABLE_DATA + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT," + KEY_VUV + " REAL," + KEY_BATTERY + " REAL," + KEY_TEMPERATURE + " REAL" + ")";
        String CREATE_TABLE = "CREATE TABLE " + TABLE_DATA + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DATE + " TEXT," + KEY_VUV + " REAL," + KEY_TEMPERATURE + " REAL" + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        // Create tables again
        onCreate(sqLiteDatabase);
    }

    public boolean createData(Data data) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DATE, data.getDate());
        values.put(KEY_VUV, data.getVuv());
        //values.put(KEY_BATTERY, data.getBattery());
        values.put(KEY_TEMPERATURE, data.getTemperature());
        long result = db.insert(TABLE_DATA, null, values);
        return result != -1;
    }


    @SuppressLint("Range")
    public List<Data> readData() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Data> dataList = new ArrayList<Data>();
        //String selectQuery = "SELECT  * FROM " + TABLE_DATA;
        //String selectQuery = "SELECT * FROM " + TABLE_DATA + " LIMIT 100;";
        //limit the selected dataset

        //String selectQuery = "SELECT * FROM " + TABLE_DATA + " ORDER BY id DESC LIMIT 1000";
        /*String selectQuery = "SELECT " + KEY_DATE + ", SUM(" + KEY_VUV + ") AS total_vuv " + "FROM " + TABLE_DATA +  " GROUP BY " + KEY_DATE;


        Cursor cursor = db.rawQuery(selectQuery, null);



        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Data data = new Data();
                data.setId(cursor.getLong(0));
                data.setDate(cursor.getString(1));
                data.setVuv(cursor.getDouble(2));
                data.setBattery(cursor.getDouble(3));
                data.setTemperature(cursor.getDouble(4));
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        System.out.println(dataList);
        return dataList;


         */

        /*String selectQuery = "SELECT " + KEY_DATE + ", SUM(" + KEY_VUV + ") AS total_vuv, "
                + "SUM(" + KEY_BATTERY + ") AS total_battery, "
                + "SUM(" + KEY_TEMPERATURE + ") AS total_temperature "
                + "FROM " + TABLE_DATA + " GROUP BY " + KEY_DATE;*/

        String selectQuery = "SELECT " + KEY_DATE + ", " + "ROUND(SUM(" + KEY_VUV + "), 1) AS total_vuv, "
                //+ "" + KEY_BATTERY + " AS total_battery, "
                + "MAX(" + KEY_TEMPERATURE + ") AS total_temperature "
                + "FROM " + TABLE_DATA + " GROUP BY " + KEY_DATE;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Data data = new Data();
                data.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                data.setVuv(cursor.getDouble(cursor.getColumnIndex("total_vuv")));
                //data.setBattery(cursor.getDouble(cursor.getColumnIndex("total_battery")));
                data.setTemperature(cursor.getDouble(cursor.getColumnIndex("total_temperature")));
                dataList.add(data);
            } while (cursor.moveToNext());
        }

        cursor.close();
        //System.out.println(dataList);
        return dataList;

    }


}


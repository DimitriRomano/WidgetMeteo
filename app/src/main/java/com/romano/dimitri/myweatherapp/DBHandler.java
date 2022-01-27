package com.romano.dimitri.myweatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DBHandler extends SQLiteOpenHelper {
    //name of database
    public static final String DB_NAME = "MyWeatherApp";

    //tables names
    public static final String TABLE_CITY = "CITY";

    //columns names
    public static final String COL_NAME = "NAME";
    public static final String COL_TEMP = "TEMPERATURE";
    public static final String COL_CONDITION = "CONDITION";
    public static final String COL_IMAGE = "IMAGE";

    //create table
    private static final String CREATE_BD = "CREATE TABLE " + TABLE_CITY + "(" +
            COL_NAME + " TEXT PRIMARY KEY, " + COL_TEMP + " FLOAT, " + COL_CONDITION + " TEXT, " + COL_IMAGE + " TEXT DEFAULT NULL )" ;

    //version
    public static final int DB_VERSION = 1;

    //singleton pattern
    private static DBHandler sInstance;

    //prevent to direct instantiation
    private DBHandler(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBHandler.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITY + ";");
        onCreate(db);
    }

    /*
        getInstance is a singleton which permits us to only have one instanciation of our DBHandler class
        usable in every other class.
        @param context  Context of our activity
        @return DBHandler   Return the instanciation of our DBHandler class.
    */
    public static synchronized DBHandler getInstance(Context context){
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if(sInstance == null){
            sInstance = new DBHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addCity(String city, String temp, String condition, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHandler.COL_NAME, city);
        cv.put(DBHandler.COL_TEMP, temp);
        cv.put(DBHandler.COL_CONDITION, condition);
        cv.put(DBHandler.COL_IMAGE,icon);
        db.insert(TABLE_CITY, null, cv);
        db.close();
    }

    public void updateCity(String city, String temp, String condition, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(DBHandler.COL_NAME, city);
        cv.put(DBHandler.COL_TEMP, temp);
        cv.put(DBHandler.COL_CONDITION, condition);
        cv.put(DBHandler.COL_IMAGE,icon);
        db.update(TABLE_CITY,cv, COL_NAME + "='" + city + "'", null);
        db.close();
    }

    public boolean existCity(String city){
        Boolean retVal = false;
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CITY + " WHERE " + COL_NAME + " = '" + city +"' ";
        Cursor cursor = db.rawQuery(selectQuery,null);
        if(cursor.getCount()>0){
            retVal = true;
        }
        cursor.close();
        db.close();
        return retVal;
    }

    public CityWeather getCity(String city){
        SQLiteDatabase db=this.getReadableDatabase();
        CityWeather myCity = new CityWeather();
        Cursor cursor = db.query(TABLE_CITY,new String[]{COL_NAME,COL_TEMP,COL_CONDITION, COL_IMAGE},COL_NAME + " =  ? ",new String[]{city},null,null,null);
        cursor.moveToFirst();
        myCity.setName(cursor.getString(0));
        myCity.setTemp(cursor.getString(1));
        myCity.setCondition(cursor.getString(2));
        myCity.setIcon(cursor.getString(3));
        cursor.close();
        db.close();
        return myCity;
    }



}

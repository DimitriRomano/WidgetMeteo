package com.romano.dimitri.myweatherapp;

import android.app.Application;

import com.android.volley.RequestQueue;

public class WeatherApi extends Application {
    private RequestQueue mRequestQueue ;
    private WeatherApi mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

    }
}

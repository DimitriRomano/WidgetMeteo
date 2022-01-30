package com.romano.dimitri.myweatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;

import com.romano.dimitri.myweatherapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private String mCityLocation;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private DBHandler db;

    private SharedPreferences mPreferencesLog;
    public static final String PREF = "PREFS_LOG";
    public static final String PREF_CITY = "PREFS_CITY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        mContext = getApplicationContext();

        //binding access to elements view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Force update address on app launch
        ApiLocalisation.getInstance().forceUpdate(this);

        binding.button.setOnClickListener(v -> {
            if(this.CheckGpsStatus()){
                ApiLocalisation.getInstance().forceUpdate(this);
                ApiLocalisation.getInstance().onReceive("MainActivity", address -> {
                    String newCity = address.getLocality();
                    this.getWeatherInfo(newCity);
                    Log.v(TAG, "[MA] City: " + address.getLocality());
                    return true;
                });
            }else{
                this.locationEnabled();
            }
        });

        ApiLocalisation.getInstance().onReceive("MainActivity", address -> {
            String newCity = address.getLocality();
            this.getWeatherInfo(newCity);
            Log.v(TAG, "[MA] City: " + address.getLocality());
            return true;
        });

        //init recycle view elements
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        binding.RVWeather.setAdapter(weatherRVAdapter);


        //init sqlite
        db = DBHandler.getInstance(this);
        //init sharePreferences
        mPreferencesLog = this.mContext.getSharedPreferences(PREF, MODE_PRIVATE);
        mCityLocation = mPreferencesLog.getString(PREF_CITY, "Toulouse");

        //check internet connection
        if (!this.isInternetConnected(this.mContext)) {
            this.loadData();
        }

        //look for cities feature
        binding.IVSearch.setOnClickListener(v -> {
            String city = binding.EdtCity.getText().toString();
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a  city", Toast.LENGTH_SHORT).show();
            } else {
                getWeatherInfo(city);
                binding.EdtCity.setText("");
            }
        });


    }

    //method to check if provider are enable on check localisation button
    private void locationEnabled(){
        boolean isEnable =this.CheckGpsStatus();

        if (!isEnable) {
            new AlertDialog.Builder(this )
                    .setMessage( "GPS not Enable please turn on" )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS )) ;
                                }
                            })
                    .setNegativeButton( "Cancel" , null )
                    .show() ;
        }
    }

    //checkc is button is activate
    public boolean CheckGpsStatus() {

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return GpsStatus;
    }

    //api fetch meteo
    public void getWeatherInfo(String cityName) {
        String url = "https://www.prevision-meteo.ch/services/json/" + cityName;

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            weatherRVModalArrayList.clear();

            try {
                    String temperature = response.getJSONObject("current_condition").getString("tmp");
                    String condition = response.getJSONObject("current_condition").getString("condition");
                    String icon = response.getJSONObject("current_condition").getString("icon_big");
                    String daytime = response.getJSONObject("city_info").getString("sunrise");
                    String nightime = response.getJSONObject("city_info").getString("sunset");
                    this.loadBackImage(daytime,nightime);
                    binding.TVCityName.setText(cityName);
                    binding.TVTemperature.setText(temperature + "°c");
                    binding.TVCondition.setText(condition);
                    Picasso.get().load(icon).into(binding.IVIcon);
                    //save icon for no connection mod
                        Picasso.get().load(icon).into(picassoImageTarget(getApplicationContext(), "imageDir", cityName+".jpeg"));

                //persistence data
                if (db.existCity(cityName)) {
                    db.updateCity(cityName, temperature, condition, icon);
                } else {
                    db.addCity(cityName, temperature, condition, icon);
                }
                mPreferencesLog.edit().putString(PREF_CITY, cityName).commit();
                mCityLocation = cityName;


                //TODO change background if day or night with picasso

                JSONObject forecastOBj = response.getJSONObject("fcst_day_0");
                JSONObject hoursCast = forecastOBj.getJSONObject("hourly_data");

                for (int i = 0; i < 23; i++) {
                    JSONObject hour = hoursCast.getJSONObject(i + "H00");
                    String hTime = i + "H00";
                    String hTemp = hour.getString("TMP2m");
                    String hWindSpeed = hour.getString("WNDSPD10m");
                    String hIcon = hour.getString("ICON");
                    weatherRVModalArrayList.add(new WeatherRVModal(hTime, hTemp, hIcon, hWindSpeed));
                }
                weatherRVAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    JSONArray errors = response.getJSONArray("errors");
                    String text_error = errors.getJSONObject(0).getString("text");
                    Toast.makeText(mContext, text_error, Toast.LENGTH_LONG).show();
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }
            }

        }, error -> {
            Log.e(TAG, "error loading api");
            Toast.makeText(mContext, "Check is internet is enable, mise à jours impossible !", Toast.LENGTH_LONG).show();
        });

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.CheckGpsStatus()){
            ApiLocalisation.getInstance().forceUpdate(this);
        }else{
            getWeatherInfo(mCityLocation);
        }
        if (this.isInternetConnected(mContext) == false) {
            this.loadData();
        }
    }

    // connection is enable or not
    public static boolean isInternetConnected(Context getApplicationContext) {
        boolean status = false;

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cm.getActiveNetwork() != null && cm.getNetworkCapabilities(cm.getActiveNetwork()) != null) {
                    // connected to the internet
                    status = true;
                }

            } else {
                if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                    // connected to the internet
                    status = true;
                }
            }
        }
        return status;
    }


    //this method creates a target object that you can use with Picasso
    private Target picassoImageTarget(Context context, final String imageDir, final String imageName) {
        Log.d(TAG, " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE); // path to /data/data/yourapp/app_imageDir
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(() -> {
                    final File myImageFile = new File(directory, imageName); // Create image file
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(myImageFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i(TAG, "image saved to >>>" + myImageFile.getAbsolutePath());

                }).start();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {
                }
            }
        };
    }

   //charger image jour/nuit selon condition
   private void loadBackImage(String daytime, String nighttime){
       String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
       int H_current = Integer.parseInt(currentTime.split(":")[0]);
       int H_daytime = Integer.parseInt(daytime.split(":")[0]);
       int H_nighttime = Integer.parseInt(nighttime.split(":")[0]);
       int M_current = Integer.parseInt(currentTime.split(":")[1]);
       int M_daytime = Integer.parseInt(daytime.split(":")[1]);
       int M_nighttime = Integer.parseInt(nighttime.split(":")[1]);
       if((H_current>H_daytime && H_current< H_nighttime ) || (H_current==H_daytime && M_current>=M_daytime) || (H_current==H_nighttime && M_current<M_nighttime)){
           Picasso.get().load("https://images.unsplash.com/photo-1559628376-f3fe5f782a2e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=431&q=80").into(binding.IVBack);
           Picasso.get().load("https://images.unsplash.com/photo-1559628376-f3fe5f782a2e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=431&q=80").into(picassoImageTarget(getApplicationContext(), "imageDir", "lastBackground.jpeg"));
       }else{
           Picasso.get().load("https://images.unsplash.com/photo-1435224654926-ecc9f7fa028c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1887&q=80").into(binding.IVBack);
           Picasso.get().load("https://images.unsplash.com/photo-1435224654926-ecc9f7fa028c?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1887&q=80").into(picassoImageTarget(getApplicationContext(), "imageDir", "lastBackground.jpeg"));
       }
   }

   //when internet is off
   private void loadData(){
       if(mCityLocation !=null && db.existCity(mCityLocation)) {
           CityWeather cityWeather = db.getCity(mCityLocation);
           binding.TVCityName.setText(cityWeather.getName());
           binding.TVTemperature.setText(cityWeather.getTemp() + "°c");
           binding.TVCondition.setText(cityWeather.getCondition());
           Log.d(TAG, cityWeather.getIcon());
               //load image from last save city
               ContextWrapper cw = new ContextWrapper(getApplicationContext());
               File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
               File myImageFile = new File(directory, cityWeather.getName()+".jpeg");
               Picasso.get().load(myImageFile).into(binding.IVIcon);
           File myBackgroundImage = new File(directory, "lastBackground.jpeg");
           Picasso.get().load(myBackgroundImage).into(binding.IVBack);

        }
    }
}

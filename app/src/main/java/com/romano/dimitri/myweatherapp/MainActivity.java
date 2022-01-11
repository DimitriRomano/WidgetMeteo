package com.romano.dimitri.myweatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.romano.dimitri.myweatherapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private static final int MULTIPLE_LOCATION_REQUEST = 42;
    private String mCityLocation;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //weatherRVModalArrayList = new ArrayList<>();
        //weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        //binding.RVWeather.setAdapter(weatherRVAdapter);



        mCityLocation="Toulouse";
        //loadWeatherData(mCityLocation);
        getWeatherInfo(mCityLocation);

        binding.IVSearch.setOnClickListener(v -> {
            String city = binding.EdtCity.getText().toString();
            if(city.isEmpty()){
                Toast.makeText(MainActivity.this,"Please enter a valid city",Toast.LENGTH_SHORT).show();
            }else{
                binding.TVCityName.setText(city);
                getWeatherInfo(city);
            }
        });


    }

    private void getWeatherInfo(String cityName) {
        String url = "https://www.prevision-meteo.ch/services/json/" + cityName;
        binding.TVCityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            //weatherRVModalArrayList.clear();

            try {

                String temperature = response.getJSONObject("current_condition").getString("tmp");
                String condition = response.getJSONObject("current_condition").getString("condition");
                String icon = response.getJSONObject("current_condition").getString("icon_big");

                binding.TVTemperature.setText(temperature+"°c");
                binding.TVCondition.setText(condition);
                Picasso.get().load(icon).into(binding.IVIcon);

                //TODO change background if day or night with picasso

                JSONObject forecastOBj = response.getJSONObject("fcst_day_0");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            Toast.makeText(MainActivity.this,"Please enter valid city name...",Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void loadWeatherData(String city) {
        String url = "https://www.prevision-meteo.ch/services/json/" + city;
        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {

                    try {
                        JSONObject jObj = new JSONObject(response);
                        JSONObject jObjCurrent = jObj.getJSONObject("current_condition");
                        String tmp = jObjCurrent.getString("tmp");
                        String condition = jObjCurrent.getString("condition");
                        String icon = jObjCurrent.getString("icon_big");

                        binding.TVCondition.setText(condition);
                        binding.TVTemperature.setText(tmp);
                        Picasso.get().load(icon).into(binding.IVIcon);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e(TAG,"That didn't work!"));

        queue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWeatherData("Toulouse");
    }
}

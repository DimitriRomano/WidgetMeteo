package com.romano.dimitri.myweatherapp;

import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;

import com.romano.dimitri.myweatherapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private String mCityLocation;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        //binding access to elements view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init recycle view elements
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        binding.RVWeather.setAdapter(weatherRVAdapter);



        mCityLocation="Toulouse";

        //look for cities feature
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
            weatherRVModalArrayList.clear();

            try {

                String temperature = response.getJSONObject("current_condition").getString("tmp");
                String condition = response.getJSONObject("current_condition").getString("condition");
                String icon = response.getJSONObject("current_condition").getString("icon_big");

                binding.TVTemperature.setText(temperature+"°c");
                binding.TVCondition.setText(condition);
                Picasso.get().load(icon).into(binding.IVIcon);

                //TODO change background if day or night with picasso

                JSONObject forecastOBj = response.getJSONObject("fcst_day_0");
                JSONObject hoursCast = forecastOBj.getJSONObject("hourly_data");

                for(int i=0; i<23; i++){
                    JSONObject hour = hoursCast.getJSONObject(i+"H00");
                    String hTime = i +"H00";
                    String hTemp = hour.getString("TMP2m");
                    String hWindSpeed = hour.getString("WNDSPD10m");
                    String hIcon = hour.getString("ICON");
                    weatherRVModalArrayList.add(new WeatherRVModal(hTime,hTemp,hIcon,hWindSpeed));
                }
                weatherRVAdapter.notifyDataSetChanged();


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            Toast.makeText(MainActivity.this,"Please enter valid city name...",Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWeatherInfo(mCityLocation);
    }
}

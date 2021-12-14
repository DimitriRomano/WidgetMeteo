package com.romano.dimitri.myweatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.romano.dimitri.myweatherapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Context mContext;
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadWeatherData("Toulouse");



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

                        binding.weatherState.setText(condition);
                        binding.degree.setText(tmp);Picasso.get().load(icon).into(binding.imageView);
                        Picasso.get().load(icon).into(binding.imageView);

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

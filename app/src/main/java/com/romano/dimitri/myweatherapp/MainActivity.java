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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.romano.dimitri.myweatherapp.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Context mContext;
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MULTIPLE_LOCATION_REQUEST = 42;
    private String mCityLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mCityLocation="Toulouse";
        loadWeatherData(mCityLocation);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        this.updateLocation();

    }

    public void updateLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().
                    addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Double longitude = location.getLongitude();
                            Double latitude = location.getLatitude();
                            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                            List<Address> addresses=null;
                            try {
                                addresses = geo.getFromLocation(latitude, longitude, 1);
                                Log.e((TAG),addresses.get(0).toString());
                            }catch (Exception e){
                                Log.e(TAG,e.getMessage());
                            }
                            /*if(addresses!=null && addresses.size()>0){
                                addresses.get(0).
                            }*/
                            Log.d(TAG,addresses.get(0).toString());
                        }
                    });
        }else{
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.INTERNET},
                    MULTIPLE_LOCATION_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MULTIPLE_LOCATION_REQUEST:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    updateLocation();
                }else{
                    Toast.makeText(getApplicationContext(),"Permission denied to access device's location", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

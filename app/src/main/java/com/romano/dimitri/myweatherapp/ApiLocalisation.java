package com.romano.dimitri.myweatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.function.Function;


public class ApiLocalisation {
    private static final String TAG = "DEBUG_";

    FusedLocationProviderClient fusedLocationProviderClient;
    Map<String, Function<Address, Boolean>> listeners;
    LocationRequest mLocationRequest;
    private String lastCity;
    private static ApiLocalisation instance = null;

    public static ApiLocalisation getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = new ApiLocalisation();
        return instance;
    }


    private ApiLocalisation() {
        lastCity = "";
        listeners = new HashMap<>();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void forceUpdate(Activity a) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(a);
        if (ActivityCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(a, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    listeners.forEach((_key, cbFn) -> cbFn.apply(null));
                    return;
                }
                Geocoder geocoder = new Geocoder(a, Locale.getDefault());
                try {
                    Location location = locationResult.getLastLocation();
                    List<Address> addressList = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1
                    );
                    Address address = addressList.get(0);
                    lastCity = address.getLocality();
                    listeners.forEach((_key, cbFn) -> cbFn.apply(address));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fusedLocationProviderClient.removeLocationUpdates(this);
            }
        }, null);
    }


    public void forceUpdate(Context c) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(c);
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "No permissions");
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    listeners.forEach((_key, cbFn) -> cbFn.apply(null));
                    return;
                }
                Geocoder geocoder = new Geocoder(c, Locale.getDefault());
                try {
                    Location location = locationResult.getLastLocation();
                    List<Address> addressList = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1
                    );
                    Address address = addressList.get(0);
                    lastCity = address.getLocality();
                    listeners.forEach((_key, cbFn) -> cbFn.apply(address));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fusedLocationProviderClient.removeLocationUpdates(this);
            }
        }, null);
    }


    public void onReceive(String uniqueId, Function<Address, Boolean> fn) {
        Log.v(TAG, "Listener added: (" + uniqueId + ");");
        listeners.put(uniqueId, fn);
    }

    public String getLastCity() {
        return lastCity;
    }

}

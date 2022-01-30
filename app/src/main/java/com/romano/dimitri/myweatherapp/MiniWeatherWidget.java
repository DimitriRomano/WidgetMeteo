package com.romano.dimitri.myweatherapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of App Widget functionality.
 */
public class MiniWeatherWidget extends AppWidgetProvider {

    private static String sLocation = "Toulouse";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        DBHandler db = DBHandler.getInstance(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mini_weather_widget);

        Log.v("XDXD", "UPDATING: " + String.valueOf(appWidgetId));

        CityWeather city = db.getCity(sLocation);
        views.setTextViewText(R.id.WIDGET_CITY_NAME, city.getName());
        views.setTextViewText(R.id.WIDGET_TEMPERATURE, city.getTemp() + "°c");
        views.setTextViewText(R.id.WIDGET_CONDITION, "[" + city.getCondition() + "]");

        Intent intent = new Intent(context, MiniWeatherWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.WIDGET_REFRESH_BUTTON, pendingIntent);



        Picasso.get().load(city.getIcon()).into(views, R.id.WIDGET_WEATHER_ICON, new int[]{appWidgetId});
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        ApiLocalisation.getInstance().onReceive(addresses -> {
            Address address = addresses.get(0);
            sLocation = address.getLocality();
            return true;
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.v("XDXD", "RECIEVED");
        ApiLocalisation.getInstance().updateLocalisation(context);
    }

    public static void getWeatherInfo(Context ctx, String cityName) {
        String url = "https://www.prevision-meteo.ch/services/json/" + cityName;

        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {

            try {
                String temperature = response.getJSONObject("current_condition").getString("tmp");
                String condition = response.getJSONObject("current_condition").getString("condition");
                String icon = response.getJSONObject("current_condition").getString("icon_big");

                Picasso.get().load(icon).into(binding.IVIcon);
                //save icon for no connection mod
                Picasso.get().load(icon).into(picassoImageTarget(getApplicationContext(), "imageDir", cityName + ".jpeg"));

                //persistence data
                if (db.existCity(cityName)) {
                    db.updateCity(cityName, temperature, condition, icon);
                } else {
                    db.addCity(cityName, temperature, condition, icon);
                }
                mPreferencesLog.edit().putString(PREF_CITY, cityName).commit();


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

}
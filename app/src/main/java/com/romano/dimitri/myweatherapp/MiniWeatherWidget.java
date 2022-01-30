package com.romano.dimitri.myweatherapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Parcelable;
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

    private static final String TAG = "XDXD";
    public static final String WIDGET_IDS_KEY = "mywidgetproviderwidgetids";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.mini_weather_widget);
        Intent updateIntent = new Intent();
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(MiniWeatherWidget.WIDGET_IDS_KEY, appWidgetIds);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.WIDGET_REFRESH_BUTTON, pendingIntent);

        update(context, appWidgetManager, appWidgetIds, null);
    }

    @Override
    public void onEnabled(Context context) {
        ApiLocalisation.getInstance().forceUpdate(context);
    }

    public void update(Context context, AppWidgetManager manager, int[] ids, Object data) {
        ApiLocalisation.getInstance().forceUpdate(context);
        for (int widgetId : ids) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.mini_weather_widget);
            if (DBHandler.getInstance(context).existCity(ApiLocalisation.getInstance().getLastCity())) {
                CityWeather cw = DBHandler.getInstance(context).getCity(ApiLocalisation.getInstance().getLastCity());
                remoteViews.setTextViewText(R.id.WIDGET_CITY_NAME, cw.getName());
                remoteViews.setTextViewText(R.id.WIDGET_CONDITION, cw.getCondition());
                remoteViews.setTextViewText(R.id.WIDGET_TEMPERATURE, cw.getTemp() + "°c");
                Picasso.get().load(cw.getIcon()).into(remoteViews, R.id.WIDGET_WEATHER_ICON, new int[]{widgetId});
            }
            Intent updateIntent = new Intent();
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(MiniWeatherWidget.WIDGET_IDS_KEY, ids);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.WIDGET_REFRESH_BUTTON, pendingIntent);

            manager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(WIDGET_IDS_KEY)) {
            int[] ids = intent.getExtras().getIntArray(WIDGET_IDS_KEY);
            this.update(context, AppWidgetManager.getInstance(context), ids, null);
        } else super.onReceive(context, intent);
    }

//
//    public void getWeatherInfo(Context ctx, String cityName) {
//        Log.v("XDXD", "GET WEATHER INFO: " + cityName);
//        String url = "https://www.prevision-meteo.ch/services/json/" + cityName;
//
//        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
//
//            try {
//                String temperature = response.getJSONObject("current_condition").getString("tmp");
//                String condition = response.getJSONObject("current_condition").getString("condition");
//                String icon = response.getJSONObject("current_condition").getString("icon_big");
//                Log.v("XDXD", "DATA: " + cityName + ", " + temperature + ", " + condition + ", " + icon);
//                DBHandler db = DBHandler.getInstance(ctx);
//                if (db.existCity(cityName)) {
//                    db.updateCity(cityName, temperature, condition, icon);
//                } else {
//                    db.addCity(cityName, temperature, condition, icon);
//                }
//                RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.mini_weather_widget);
//                CityWeather city = db.getCity(sLocation);
//                views.setTextViewText(R.id.WIDGET_CITY_NAME, cityName);
//                views.setTextViewText(R.id.WIDGET_TEMPERATURE, city.getTemp() + "°c");
//                views.setTextViewText(R.id.WIDGET_CONDITION, "[" + city.getCondition() + "]");
//            } catch (JSONException e) {
//                e.printStackTrace();
//                try {
//                    JSONArray errors = response.getJSONArray("errors");
//                    String text_error = errors.getJSONObject(0).getString("text");
//                    Toast.makeText(ctx, text_error, Toast.LENGTH_LONG).show();
//                } catch (JSONException jsonException) {
//                    jsonException.printStackTrace();
//                }
//            }
//        }, error -> {
//            Log.e("XDXD", "error loading api");
//            error.printStackTrace();
//            Toast.makeText(ctx, "Error !", Toast.LENGTH_LONG).show();
//        });
//
//        requestQueue.add(jsonObjectRequest);
//    }

}
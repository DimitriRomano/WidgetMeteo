package com.romano.dimitri.myweatherapp;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class CustomJsonRequest extends JsonObjectRequest {

    public CustomJsonRequest(int method, String url, JSONObject jsonRequest,
                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    private Request.Priority mPriority;

    public void setPriority(Request.Priority priority) {
        mPriority = priority;
    }

    @Override
    public Request.Priority getPriority() {
        return mPriority == null ? Priority.NORMAL : mPriority;
    }

}

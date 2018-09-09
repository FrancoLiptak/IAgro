package com.fg.franco.i_agro;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyManager {
    private static VolleyManager instance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    private VolleyManager(Context context){
        mCtx = context;
        requestQueue = getRequestQueue();
    }


    public RequestQueue getRequestQueue() {
        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized VolleyManager getInstance(Context context){
        if(instance == null){
            instance = new VolleyManager(context);
        }
        return instance;
    }

    public<T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }

}

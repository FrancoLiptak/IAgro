package com.fg.franco.i_agro;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.SimpleMultiPartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.math.BigDecimal;

public class HttpClient {

    private ResultDialog resultDialog = new ResultDialog();
    private Map<String, Float> responseDictionary;
    private MainActivity context;

    public HttpClient(MainActivity context){
        this.context = context;
        resultDialog.setAnalyzer(new MaxAnalyzer());
        VolleyManager.getInstance(context);
    }

    public void doRequest(File file) {
        String urlName = "http://192.168.0.32:5000/";
        responseDictionary = new HashMap<>();


        SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, urlName,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        System.out.println("got response");
                        try{
                            System.out.println("make dict");
                            JSONObject jsonObject = new JSONObject(response);
                            Iterator<?> keys = jsonObject.keys();
                            while( keys.hasNext() ) {
                                String key = (String)keys.next();
                                responseDictionary.put(key, BigDecimal.valueOf(jsonObject.getDouble(key)).floatValue());
                                resultDialog.setResponse(responseDictionary);
                            }
                            System.out.println(responseDictionary.toString());
                        } catch (JSONException e) {
                            responseDictionary.put("error", 0f);
                            resultDialog.setResponse(responseDictionary);
                            e.printStackTrace();
                        }
                        resultDialog.show(context.getFragmentManager(), "result");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseDictionary.put("error", 0f);
                resultDialog.setResponse(responseDictionary);
                System.out.println(error.getMessage());
                resultDialog.show(context.getFragmentManager(), "result");
            }
        });
        smr.addFile("image", file.getPath());

        VolleyManager.getInstance(null).addToRequestQueue(smr);

    }
}

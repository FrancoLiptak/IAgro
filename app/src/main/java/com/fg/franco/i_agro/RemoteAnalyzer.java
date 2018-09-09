package com.fg.franco.i_agro;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;

public class RemoteAnalyzer implements Analyzer {
    public String analize(Map<String, Float> response){
        Map.Entry<String, Float> maxEntry = null;

        for (Map.Entry<String, Float> entry : response.entrySet())
        {
            if (entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }
    /*
    public String analize(File file){
        HttpClient client = new HttpClient(file);
        client.doRequest();
        return "hola";
    }
    */
}

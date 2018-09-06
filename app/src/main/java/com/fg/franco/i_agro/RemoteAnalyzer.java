package com.fg.franco.i_agro;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.ContentValues.TAG;

public class RemoteAnalyzer {
    private String url = "http://localhost:5000/";

    public RemoteAnalyzer(String url) {
        this.url = url;
    }

    public String doRequest(File file){

        // the URL where the file will be posted
        Log.v(TAG, "postURL: " + this.url);
        OutputStream out = null;

        try{
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(file);
            writer.flush();
            writer.close();
            out.close();

            urlConnection.connect();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

package com.fg.franco.i_agro;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class NNClient {

    public static String doRequest(File image){

        String urlName = "http://192.168.0.48:5000/";

        // the file to be posted
        String textFile = Environment.getExternalStorageDirectory() + "/sample.txt";
        Log.v(TAG, "textFile: " + textFile);

        // the URL where the file will be posted
        String postReceiverUrl = "http://192.168.0.48:5000/";
        Log.v(TAG, "postURL: " + postReceiverUrl);

        // new HttpClient
        HttpClient httpClient = new DefaultHttpClient();

        // post header
        HttpPost httpPost = new HttpPost(postReceiverUrl);

        FileBody fileBody = new FileBody(image);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("image", fileBody);
        httpPost.setEntity(reqEntity);
        // execute HTTP post request
        try{

            HttpResponse response = httpClient.execute(httpPost);
            System.out.println("lleugeeeeeeeeeeeeeeeeeeeeeeeeee");
            HttpEntity resEntity = response.getEntity();
            String responseStr = null;

            if (resEntity != null) {

                responseStr = EntityUtils.toString(resEntity).trim();
                Log.v(TAG, "Response: " +  responseStr);

                // you can add an if statement here and do other actions based on the response
            }
            return responseStr;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return e.getMessage();
        }
    }
}

package com.fg.franco.i_agro;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StorageHandler {
    private MainActivity context;

    public StorageHandler(MainActivity context) {
        this.context = context;
    }

    public void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // Show only images
        intent.setAction(Intent.ACTION_GET_CONTENT); // Always show the chooser (if there are multiple options available)
        this.context.showSelectAppForSelectImage(intent);
    }

    public File getOutputMediaFile(){
        String state = Environment.getExternalStorageState();
        if(!state.equals((Environment.MEDIA_MOUNTED))){
            return null;
        }else{
            File folder_gui = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"I-Agro");
            if(!folder_gui.exists()){
                if (!folder_gui.mkdirs()) {
                    Log.d("I-Agro", "failed to create directory");
                    return null;
                }
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";
            File outputFile = new File(folder_gui.getPath() + File.separator + imageFileName);
            MediaScannerConnection.scanFile(this.context, new String[] { outputFile.getPath() }, new String[] { "image/jpeg" }, null);
            return outputFile;
        }
    }
}

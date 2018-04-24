package com.fg.franco.i_agro;

import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;
    RandomAnalyzer analyzer = new RandomAnalyzer();
    ResultDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        camera = Camera.open();
        showCamera = new ShowCamera(this, this.camera);
        frameLayout.addView(this.showCamera);

    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            File picture_file = getOutputMediaFile();
            if (picture_file == null){
                return;
            }
            try{
                FileOutputStream fos = new FileOutputStream(picture_file);
                fos.write(data);
                fos.close();

                resultDialog = new ResultDialog();
                resultDialog.setAnalyzer(analyzer);
                resultDialog.show(getFragmentManager(), "result");

                camera.startPreview();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    };

    private File getOutputMediaFile(){
        String state = Environment.getExternalStorageState();


        if(!state.equals((Environment.MEDIA_MOUNTED))){
            return null;
        }else{
            File folder_gui = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "I-Agro");
            if(!folder_gui.exists()){
                if (!folder_gui.mkdirs()) {
                    Log.d("I-Agro", "failed to create directory");
                    return null;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "IMG_" + timeStamp + ".jpg";
            File outputFile = new File(folder_gui.getPath() + File.separator + imageFileName);
            MediaScannerConnection.scanFile(this, new String[] { outputFile.getPath() }, new String[] { "image/jpeg" }, null);
            return outputFile;
        }

    }

    public void captureImage(View v){
        if(this.camera != null){
            this.camera.takePicture(null, null, mPictureCallback);
        }
    }
}

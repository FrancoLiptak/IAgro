package com.fg.franco.i_agro;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    Camera camera;
    FrameLayout frameLayout;
    ResultDialog resultDialog;
    ImageView image;
    Button buttonGallery;
    RandomAnalyzer analyzer = new RandomAnalyzer();
    ShowCamera showCamera;
    static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    static final int PICK_IMAGE_REQUEST= 2;
    static final int REQUEST_PERMISSION_SETTING = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        if (this.checkCameraPermissions() && this.checkStoragePermissions()){
            camera = this.getCameraInstance();
            showCamera = new ShowCamera(this, this.camera);
            frameLayout.addView(this.showCamera);
        }
        image = (ImageView)findViewById(R.id.imageView);
        resultDialog = new ResultDialog();
        resultDialog.setAnalyzer(analyzer);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.d("I-Agro", "Camera is not available");
        }
        return c; // returns null if camera is unavailable
    }


    private boolean checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            return false;
        }else{
            return true;
        }
    }
    private boolean checkStoragePermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
                    if (! showRationale) { // user also checked "never ask again"
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS); // let's go to the configuration of the application
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (! showRationale) { // user also CHECKED "never ask again"
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS); // let's go to the configuration of the application
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                }
                return;
            }
        }
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
            resultDialog.show(getFragmentManager(), "result");
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
            MediaScannerConnection.scanFile(this, new String[] { outputFile.getPath() }, new String[] { "image/jpeg" }, null);
            return outputFile;
        }
    }

    public void captureImage(View v){
        if(this.camera != null){
            this.camera.takePicture(null, null, mPictureCallback);
        }
    }

    public void getImageFromGallery(View view) {
        if (this.checkStoragePermissions()){
            uploadImage();
        }
    }

    private void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*"); // Show only images
        intent.setAction(Intent.ACTION_GET_CONTENT); // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            resultDialog.show(getFragmentManager(), "result");
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        image.setVisibility(View.GONE);
        camera.startPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.checkCameraPermissions() && this.checkStoragePermissions()){
            if (camera == null) {
                camera = getCameraInstance();
                showCamera = new ShowCamera(this, this.camera);
                frameLayout.addView(this.showCamera);
            }
            camera.startPreview();
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release(); // release the camera for other applications
            frameLayout.removeView(this.showCamera);
            camera = null; showCamera = null; // importante
        }
    }

}

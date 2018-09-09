package com.fg.franco.i_agro;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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

    FrameLayout frameLayout;
    ImageView image;
    Button buttonGallery;
    ShowCamera showCamera;
    HttpClient client;
    PermissionHandler permissionHandler = new PermissionHandler(this);
    StorageHandler storageHandler = new StorageHandler(this);
    CameraHandler cameraHandler = new CameraHandler(this, storageHandler);

    static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    static final int PICK_IMAGE_REQUEST= 2;
    static final int REQUEST_PERMISSION_SETTING = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.frameLayout = findViewById(R.id.frameLayout);
        this.image = findViewById(R.id.imageView);
        client = new HttpClient(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        System.out.println("request code: "+requestCode);
        System.out.println("results length: "+ grantResults.length);
        if (grantResults.length > 0){
            System.out.println("grant results 0: "+ grantResults[0]);
        }
        System.out.println("PERMISSION_GRANTED: "+ PackageManager.PERMISSION_GRANTED);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
                    System.out.println("show rationale: "+ showRationale);
                    if (!showRationale) { // user also checked "never ask again"
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
                    if (!showRationale) { // user also CHECKED "never ask again"
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            System.out.println("path: " + uri);
            System.out.println("path: " + uri.getPath());
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.doRequest(new File(getRealPostaPath(uri)));
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        image.setVisibility(View.GONE);
        this.cameraHandler.getCameraInstance().startPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.cameraHandler.releaseCamera();
    }

    public void removeShowCamera(){
        this.frameLayout.removeView(this.showCamera);
        this.showCamera = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.permissionHandler.checkCameraPermissions() && this.permissionHandler.checkStoragePermissions()){
            if(this.cameraHandler.cameraIsNull()){
                this.showCamera = new ShowCamera(this, this.cameraHandler.getCameraInstance());
                this.frameLayout.addView(this.showCamera);
            }
            this.cameraHandler.getCameraInstance().startPreview();
        }
    }

    public void showDialogFromPictureCallback(File file){
        client.doRequest(file);
    }

    public void captureImage(View v){
        this.cameraHandler.captureImage(v);
    }

    public void getImageFromGallery(View view) {
        if (this.permissionHandler.checkStoragePermissions()){
            this.storageHandler.uploadImage();
        }
    }

    public void showSelectAppForSelectImage(Intent intent){
        startActivityForResult(intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getRealPostaPath(Uri uriImage)
    {


        // Will return "image:x*"
        String wholeID = DocumentsContract.getDocumentId(uriImage);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        return filePath;
    }


}

package com.fg.franco.i_agro;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;

import io.fotoapparat.view.CameraView;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private CameraView cameraView;
    private ImageView image;
    private ImageButton buttonGallery;
    private ImageButton configButton;
    private Button buttonCapture;
    private ShowCamera showCamera;
    private HttpClient client;
    private PermissionHandler permissionHandler = new PermissionHandler(this);
    private StorageHandler storageHandler = new StorageHandler(this);
    private CameraHandler cameraHandler = new CameraHandler(this, storageHandler);

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST= 2;
    private static final int REQUEST_PERMISSION_SETTING = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.camera_view);
        cameraHandler.setCameraView(cameraView);
        configButton = findViewById(R.id.configButton);
        buttonGallery = findViewById(R.id.buttonGallery);
        buttonCapture = findViewById(R.id.buttonCapture);
        image = findViewById(R.id.imageView);
        client = new HttpClient(this);

        cameraHandler.createAndConfigureFotoapparat();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
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
            showImage(getRealPath(uri));
            client.doRequest(new File(getRealPath(uri)));
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        image.setVisibility(View.GONE);
        buttonGallery.setVisibility(View.VISIBLE);
        buttonCapture.setVisibility(View.VISIBLE);
        configButton.setVisibility(View.VISIBLE);
        this.cameraHandler.startCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.cameraHandler.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.permissionHandler.checkCameraPermissions() && this.permissionHandler.checkStoragePermissions()){
            this.cameraHandler.startCamera();
        }
    }

    public void sendFile(File file){
        client.doRequest(file);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void captureImage(View v){
        buttonGallery.setVisibility(View.GONE);
        buttonCapture.setVisibility(View.GONE);
        configButton.setVisibility(View.GONE);
        this.cameraHandler.takePicture();
    }

    public void getImageFromGallery(View view) {
        if (this.permissionHandler.checkStoragePermissions()){
            this.storageHandler.uploadImage();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void showImage(String uri){
        image.setVisibility(View.VISIBLE);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        image.setImageBitmap(decodeSampledBitmapFromResource(uri, size.x, size.y));
        buttonGallery.setVisibility(View.GONE);
        configButton.setVisibility(View.GONE);
        buttonCapture.setVisibility(View.GONE);
    }

    public void showSelectAppForSelectImage(Intent intent){
        startActivityForResult(intent.createChooser(intent, "Seleccione una imagen"), PICK_IMAGE_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getRealPath(Uri uriImage) {


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

    public void setConfig(View view){
        (new ConfigManager(this)).setConfig();
    }

}

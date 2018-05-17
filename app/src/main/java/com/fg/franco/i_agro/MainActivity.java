package com.fg.franco.i_agro;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
    ShowCamera showCamera;
    RandomAnalyzer analyzer = new RandomAnalyzer();
    ResultDialog resultDialog;
    ImageView image;

    Button buttonGallery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(this.checkPermissions()){

            frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
            camera = this.getCameraInstance();
            showCamera = new ShowCamera(this, this.camera);
            frameLayout.addView(this.showCamera);
            image = (ImageView)findViewById(R.id.imageView);
            resultDialog = new ResultDialog();
            resultDialog.setAnalyzer(analyzer);

        }

    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @TargetApi(Build.VERSION_CODES.N)
    private boolean checkPermissions(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        if((checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            return true;
        }

        if((shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
            showDialogRecommendation();

        }else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==100){
            if(!(grantResults.length==3 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void showDialogRecommendation(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Permisos desactivados");
        dialog.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la APP");
        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA, READ_EXTERNAL_STORAGE}, 100);
            }
        });
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

    public void getImageFromGallery(View view) {
        uploadImage();

    }

    private void uploadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/");
        startActivityForResult(intent.createChooser(intent, "Seleccione la aplicación"), 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Uri path = data.getData();
            this.image.setImageURI(path);
            image.setVisibility(View.VISIBLE);

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
        if (camera == null) {
            camera = getCameraInstance();
            showCamera = new ShowCamera(this, this.camera);
            frameLayout.addView(this.showCamera);
        }
        camera.startPreview();
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
            frameLayout.removeView(this.showCamera);
            showCamera = null;
        }
    }

}

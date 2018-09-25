package com.fg.franco.i_agro;

import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraHandler {
    private Camera camera;
    private MainActivity context;
    private StorageHandler storageHandler;

    public CameraHandler(MainActivity context, StorageHandler storageHandler) {
        this.context = context;
        this.storageHandler = storageHandler;
    }

    public Camera getCameraInstance(){
        if(this.camera == null){
            try {
                this.camera = Camera.open(); // attempt to get a Camera instance
            }
            catch (Exception e){
                Log.d("I-Agro", "Camera is not available");
            }
        }
        return this.camera;
    }

    public void captureImage(){
        if(this.camera != null){
            this.camera.takePicture(null, null, mPictureCallback);
        }
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

            if (success)
            {
                captureImage();
            }

        }
    };//end


    public void releaseCamera(){
        if (this.camera != null){
            this.camera.release(); // release the camera for other applications
            this.context.removeShowCamera();
        }
        this.camera = null;
    }

    public boolean cameraIsNull(){
        return this.camera == null;
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            File picture_file = storageHandler.getOutputMediaFile();
            if (picture_file == null){
                return;
            }
            try{
                FileOutputStream fos = new FileOutputStream(picture_file);
                fos.write(data);
                fos.close();
                context.showDialogFromPictureCallback(picture_file);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    };


    public void takePicture() {
        try
        {

            // determine current focus mode
            Camera.Parameters params = camera.getParameters();
            if (params.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            {
                camera.cancelAutoFocus();      // cancels continuous focus

                List<String> lModes = params.getSupportedFocusModes();
                if (lModes != null)
                {
                    if (lModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
                    {
                        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // auto-focus mode if supported
                        camera.setParameters(params);        // set parameters on device
                    }
                }

                // start an auto-focus after a slight (100ms) delay
                new Handler().postDelayed(new Runnable() {

                    public void run()
                    {
                        camera.autoFocus(autoFocusCallback);    // auto-focus now
                    }

                }, 100);

                return;
            }

            camera.autoFocus(autoFocusCallback);       // do the focus, callback is mAutoFocusCallback

        }
        catch (Exception e)
        {
            Log.e("myApp", e.getMessage());
        }
    }
}

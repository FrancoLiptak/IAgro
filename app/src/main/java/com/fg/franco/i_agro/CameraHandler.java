package com.fg.franco.i_agro;

import android.content.BroadcastReceiver;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.LensPositionSelectorsKt;
import io.fotoapparat.selector.SelectorsKt;
import io.fotoapparat.view.CameraView;

public class CameraHandler {
    private MainActivity context;
    private StorageHandler storageHandler;
    private CameraView cameraView;
    private Fotoapparat fotoapparat;

    public CameraHandler(MainActivity context, StorageHandler storageHandler) {
        this.context = context;
        this.storageHandler = storageHandler;
    }

    public CameraView getCameraView() {
        return cameraView;
    }

    public void setCameraView(CameraView cameraView) {
        this.cameraView = cameraView;
    }

    public void createAndConfigureFotoapparat(){
        fotoapparat = Fotoapparat
                .with(context)
                .into(cameraView)           // view which will draw the camera preview
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(LensPositionSelectorsKt.back())       // we want back camera
                .focusMode(SelectorsKt.firstAvailable(  // (optional) use the first focus mode which is supported by device
                        FocusModeSelectorsKt.continuousFocusPicture(),
                        FocusModeSelectorsKt.autoFocus(),        // in case if continuous focus is not available on device, auto focus will be used
                        FocusModeSelectorsKt.fixed()             // if even auto focus is not available - fixed focus mode will be used
                ))
                .flash(SelectorsKt.firstAvailable(      // (optional) similar to how it is done for focus mode, this time for flash
                        FlashSelectorsKt.autoRedEye(),
                        FlashSelectorsKt.autoFlash(),
                        FlashSelectorsKt.torch()
                ))
                .build();
    }

    public void stopCamera(){
        fotoapparat.stop();
    }

    public void startCamera(){
        fotoapparat.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void takePicture(){
        PhotoResult photoResult = fotoapparat.takePicture();

        photoResult.toBitmap().whenDone(
                new WhenDoneListener<BitmapPhoto>() {
                    @Override
                    public void whenDone(BitmapPhoto bitmapPhoto) {
                        Bitmap bmp = bitmapPhoto.bitmap;
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        bmp.recycle();

                        File file = storageHandler.getOutputMediaFile();

                        try {

                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(byteArray);
                            fos.close();
                            context.showImage(Uri.fromFile(file).toString());
                            context.sendFile(file);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

}

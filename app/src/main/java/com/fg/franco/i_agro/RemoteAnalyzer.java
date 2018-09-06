package com.fg.franco.i_agro;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public class RemoteAnalyzer implements Analyzer {

    public String analize(File bitmap){
        return NNClient.doRequest(bitmap);
    }
}

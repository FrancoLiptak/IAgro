package com.fg.franco.i_agro;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.util.Map;
import java.util.Random;

public interface Analyzer {

    String analize(Map<String, Float> response);
}

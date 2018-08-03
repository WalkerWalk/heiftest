package com.example.walkerxu.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

//import androidx.heifwriter.HeifWritter;

public class heifonandroidp {

    private static final String TAG = "heifonandroidp";

    public static void testDisplay(String url, ImageView view) {
        Log.i(TAG, "load heif image use ImageDecoder");
        try {
            Drawable drawble = getHeifImageFromSdcardUseImageDecoder(url);
            view.setImageDrawable(drawble);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    public static Uri testHeif2Jpeg(String heif, String jpeg) {
        Log.i(TAG, "testHeif2Jpeg in");
        try {
            Bitmap bmp = getHeifImageFromSdcardUseBitmapFactory(heif);
            File file = new File(jpeg);
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Uri uri = Uri.fromFile(file);
            Log.i(TAG, "testHeif2Jpeg out");
            return uri;
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
        return null;
    }

    private static Bitmap getHeifImageFromSdcardUseBitmapFactory(String path) {
        return BitmapFactory.decodeFile(path);
    }

    public static void testJpeg2Heif(String jpeg, String heif) {
        Log.i(TAG, "testJpeg2Heif");
        try {
            Bitmap bmp = getHeifImageFromSdcardUseBitmapFactory(jpeg);
//            HeifWritter hw = new HeifWritter();
//            hw.start();
//            hw.addBitmap(bmp);
//            hw.stop();
//            hw.close();

//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.toString());
        }
    }

    private static Drawable getHeifImageFromSdcardUseImageDecoder(String path) throws IOException {
        File file = new File(path);
        ImageDecoder.Source source = ImageDecoder.createSource(file);
        return ImageDecoder.decodeDrawable(source);
    }

    public static boolean isSupportHeif() {
        Log.e(TAG, "Build.MANUFACTURER:" + Build.MANUFACTURER + ", Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT);
        if(Build.VERSION.SDK_INT>27) {
            return true;
        }
        return false;
    }
}

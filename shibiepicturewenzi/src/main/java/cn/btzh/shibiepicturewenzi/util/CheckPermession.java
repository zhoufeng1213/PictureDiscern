package cn.btzh.shibiepicturewenzi.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;


/**
 * Created by fly on 2017/3/14.
 */

public class CheckPermession {

    // Storage Permissions
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_EXTERNAL_LOCATION = 2;
    public static final int REQUEST_EXTERNAL_CAMEAR = 3;
    public static final int REQUEST_EXTERNAL_RECORD = 4;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private static String[] PERMISSIONS_RECORD = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private static String[] PERMISSIONS_LOACATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * * Checks if the app has permission to write to device storage
     * *
     * * If the app does not has permission then the user will be prompted to
     * * grant permissions
     * *
     * * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }


    public static void verifyLocationPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_LOACATION,
                    REQUEST_EXTERNAL_LOCATION);
        }
    }


    public static void verifyCameraPermissions(Activity activity) {
//        if (PackageManager.PERMISSION_GRANTED !=   ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS_CAMERA, REQUEST_EXTERNAL_CAMEAR);
//        }
    }

    public static void verifyRecordPermissions(Activity activity) {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_RECORD, REQUEST_EXTERNAL_RECORD);
        }
    }


}

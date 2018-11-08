package com.amway.wifianalyze.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by big on 2018/10/19.
 */

public class PermissionUtil {


    public void init(Activity activity) {
        checkPermissions(activity, PER_STORAGE, PERMISSIONS_STORAGE, RESULT_STORAGE);
        checkPermissions(activity, PER_WIFI, PERMISSIONS_WIFI, RESULT_WIFI);
        checkPermissions(activity, PER_PHONE, PERMISSIONS_PHONE, RESULT_PHONE);
    }

    //读写权限
    private static final int RESULT_STORAGE = 1;
    private static final String PER_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //wifi权限
    private static final int RESULT_WIFI = 2;
    private static final String PER_WIFI = Manifest.permission.ACCESS_FINE_LOCATION;
    private static String[] PERMISSIONS_WIFI = {Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION};

    //电话号码
    private static final int RESULT_PHONE = 3;
    private static final String PER_PHONE = "android.permission.READ_PHONE_NUMBERS";
    private static String[] PERMISSIONS_PHONE = {"android.permission.READ_PHONE_NUMBERS"};


    private void checkPermissions(Activity context, String permission, String[] permissions, int resultCode) {

        try {
            //检测是否有权限
            int result = ActivityCompat.checkSelfPermission(context, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                // 没有权限，去申请权限，会弹出对话框
                ActivityCompat.requestPermissions(context, permissions, resultCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

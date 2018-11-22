package com.amway.wifianalyze.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * Created by big on 2018/10/19.
 */

public class PermissionUtil {


    public void init(Activity activity) {
//        checkPermissions(activity, PER_WIFI, PERMISSIONS_WIFI, RESULT_WIFI);
//        checkPermissions(activity, PER_STORAGE, PERMISSIONS_STORAGE, RESULT_STORAGE);
//        checkPermissions(activity, PER_PHONE, PERMISSIONS_PHONE, RESULT_PHONE);
//        checkPermissions(activity, PER_AUDIO, PERMISSIONS_AUDIO, RESULT_AUDIO);
    }

    //读写权限
    public static final int RESULT_STORAGE = 1;
    public static final String PER_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //wifi权限
    public static final int RESULT_WIFI = 2;
    public static final String PER_WIFI = Manifest.permission.ACCESS_FINE_LOCATION;
    public static String[] PERMISSIONS_WIFI = {Manifest.permission.ACCESS_FINE_LOCATION
            , Manifest.permission.ACCESS_COARSE_LOCATION};

    //电话号码
    public static final int RESULT_PHONE = 3;
    public static final String PER_PHONE = "android.permission.READ_PHONE_NUMBERS";
    public static String[] PERMISSIONS_PHONE = {"android.permission.READ_PHONE_NUMBERS"};

    //录音
    public static final int RESULT_AUDIO = 4;
    public static final String PER_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static String[] PERMISSIONS_AUDIO = {Manifest.permission.RECORD_AUDIO};


    public static boolean checkPermissions(Activity context, String permission, String[] permissions, int resultCode) {
        /*Log.e("permisson", "checkPermissions permission:" + permission);
        boolean hasPermission = false;
        try {
            //检测是否有权限
            int result = ActivityCompat.checkSelfPermission(context, permission);
            hasPermission = result == PackageManager.PERMISSION_GRANTED;
            Log.e("permisson", "checkPermissions :" + permission + ",result:" + result);
            if (!hasPermission) {
                // 没有权限，去申请权限，会弹出对话框
                ActivityCompat.requestPermissions(context, permissions, resultCode);
                Log.e("permisson", "have no permission:" + permission);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasPermission;*/
        boolean grant = XXPermissions.isHasPermission(context, Permission.Group.LOCATION);
        XXPermissions.with(context).constantRequest().permission(Permission.Group.LOCATION).request(new OnPermission() {
            @Override
            public void hasPermission(List<String> list, boolean b) {

            }

            @Override
            public void noPermission(List<String> list, boolean b) {
                Log.e("permisson", "no permission :" + list);
            }
        });
        return grant;
    }
}

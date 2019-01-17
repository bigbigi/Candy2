package com.permission.manager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

/**
 * Created by big on 2019/1/15.
 */

public class PermissionManager {
    public static void check(Context context, String[] permissions, OnPermission onPermission) {
        boolean hasPermission = true;
        for (String permission : permissions) {
            int grant = PermissionChecker.checkSelfPermission(context, permission);
            Log.d("big", "PermissionManager" + grant);
            hasPermission = hasPermission && (grant == PackageManager.PERMISSION_GRANTED);
        }
        if (onPermission != null) {
            if (hasPermission) {
                onPermission.hasPermission();
            } else {
                onPermission.noPermission(permissions[0]);
            }
        }
    }

    public static void check(Context context, String permission, OnPermission onPermission) {
        check(context, new String[]{permission}, onPermission);
    }
}

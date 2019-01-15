package com.permission.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by big on 2019/1/15.
 */

public class PermissionCallback implements OnPermission {
    private Context mContext;
    private String mError;

    public PermissionCallback(Context context, String error) {
        mContext = context;
        mError = error;
    }

    @Override
    public void hasPermission() {
        Log.d("permisson", "get permisson:");
    }

    @Override
    public void noPermission(String permission) {
        SharedPreferences preferences = mContext.getSharedPreferences("permission", 0);
        String key = mContext.getClass().getSimpleName() + "_" + permission;
        boolean value = preferences.getBoolean(mContext.getClass().getSimpleName() + permission, false);
        Log.e("permisson", "has no permisson:" + permission + "\n" + key + ",valuel:" + value);
        if (!value) {
            preferences.edit().putBoolean(key, true).commit();
        } else {
            PermissionSettingPage.start(mContext, false);
            Toast.makeText(mContext, mError, Toast.LENGTH_LONG).show();
        }
    }


}

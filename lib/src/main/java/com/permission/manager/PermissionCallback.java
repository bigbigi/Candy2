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
        Log.e("permisson", "has no permisson:" + permission);
        PermissionSettingPage.start(mContext, false);
        Toast.makeText(mContext, mError, Toast.LENGTH_LONG).show();
    }
}

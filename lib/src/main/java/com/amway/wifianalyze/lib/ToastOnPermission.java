package com.amway.wifianalyze.lib;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * Created by big on 2018/11/22.
 */

public class ToastOnPermission implements OnPermission {
    private Context mContext;
    private String mError;

    public ToastOnPermission(Context context, String error) {
        mContext = context;
        mError = error;
    }

    @Override
    public void hasPermission(List<String> list, boolean b) {
        Log.d("permisson", "get permisson:" + list);
    }

    @Override
    public void noPermission(List<String> list, boolean b) {
        Log.e("permisson", "has no permisson:" + list);
        XXPermissions.gotoPermissionSettings(mContext);
        Toast.makeText(mContext, mError, Toast.LENGTH_LONG).show();
    }
}

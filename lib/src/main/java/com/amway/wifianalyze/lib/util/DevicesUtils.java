package com.amway.wifianalyze.lib.util;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

/**
 * Created by big on 2018/11/12.
 */

public class DevicesUtils {
    private static final String TAG = "DevicesUtils";

    public static final String DEVICE_ID = "AmwayDeviceId";

    public static String getDeviceId(Context context) {
        String deviceId = PreferenceUtil.getString(context, DEVICE_ID);
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = getIdFromSetting(context);
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = UUID.randomUUID().toString();
                putId2Setting(context, deviceId);
            }
            PreferenceUtil.putString(context, DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    private static String getIdFromSetting(Context context) {
        try {
            return Settings.System.getString(context.getContentResolver(),
                    DEVICE_ID);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getIdFromSetting fail");
        }
        return null;
    }

    private static void putId2Setting(Context context, String uuid) {
        try {
            Settings.System.putString(context.getContentResolver(),
                    DEVICE_ID, uuid);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "putId2Setting fail");
        }
    }
}

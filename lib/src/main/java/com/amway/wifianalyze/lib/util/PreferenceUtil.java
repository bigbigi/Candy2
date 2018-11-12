package com.amway.wifianalyze.lib.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by big on 2018/11/7.
 */

public class PreferenceUtil {

    private static final String NAME = "prefrence";

    public static final String PHONE_NUM = "phoneNum";

    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_MULTI_PROCESS);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = getSp(context);
        sp.edit().putBoolean(key, value).apply();
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = getSp(context);
        sp.edit().putString(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key) {
        return getSp(context).getBoolean(key, false);
    }

    public static String getString(Context context, String key) {
        return getSp(context).getString(key, "");
    }
}

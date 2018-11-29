package com.amway.wifianalyze.bean;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * Created by big on 2018/11/23.
 */

public class DeviceInfo {
    public String ip;
    public String mac;
    public String dns;
    public String phoneType;
    public String system;
    public String browser;
    public int wifiChannel;//5G=2,2.4G=1;
    public String shop;
    public String ssid;

    public String ap;

    public static void putJson(JSONObject obj, String key, Object value) {
        try {
            obj.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        Field[] fields = getClass().getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                try {
                    putJson(json, field.getName(), field.get(this));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return json;
    }
}

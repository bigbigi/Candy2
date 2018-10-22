package com.amway.wifianalyze.utils;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by big on 2018/10/22.
 */

public class HttpHelper {

    private final OkHttpClient mClient;
    private static volatile HttpHelper mInstance;

    public HttpHelper(Context context) {
        mClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
    }

    public synchronized static HttpHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (HttpHelper.class) {
                if (mInstance == null) {
                    mInstance = new HttpHelper(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    public String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Response getResponse(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

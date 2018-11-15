package com.amway.wifianalyze.utils;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by big on 2018/10/22.
 */

public class HttpHelper {
    private static final String TAG = "HttpHelper";

    private final OkHttpClient mClient;
    private static volatile HttpHelper mInstance;

    public HttpHelper() {
        mClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
    }

    public synchronized static HttpHelper getInstance() {
        if (mInstance == null) {
            synchronized (HttpHelper.class) {
                if (mInstance == null) {
                    mInstance = new HttpHelper();
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

    public Response post(String url, String content) {
        try {
            return post(url, new TextRequestBody(compress(content)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Response post(String url, RequestBody requestBody) {
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            response = mClient.newCall(request).execute();
            Log.i(TAG, "response:" + response.isSuccessful() + "," + response.toString());
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public ByteArrayOutputStream compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out;
    }

    class TextRequestBody extends RequestBody {
        private final MediaType MEDIA_FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
        private ByteArrayOutputStream mOutputStream;

        public TextRequestBody(ByteArrayOutputStream bos) {
            mOutputStream = bos;
        }

        @Override
        public MediaType contentType() {
            return MEDIA_FORM;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            sink.write(mOutputStream.toByteArray());
        }
    }
}

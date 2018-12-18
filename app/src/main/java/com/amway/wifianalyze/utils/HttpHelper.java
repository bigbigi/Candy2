package com.amway.wifianalyze.utils;

import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by big on 2018/10/22.
 */

public class HttpHelper {
    private static final String TAG = "HttpHelper";

    private final OkHttpClient mClient;
    private static volatile HttpHelper mInstance;

    public HttpHelper() {
        mClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts())
                .hostnameVerifier(new TrustAllHostnameVerifier()).build();
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    //信任所有的服务器,返回true
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
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
            String result = response.body().string();
            Log.i(TAG, "response:" + response.isSuccessful() + "," + response.toString() + "\nresult:" + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "url:" + url);
            e.printStackTrace();
        }
        return "";
    }

    public String getChome(String url) {
        Request request = new Request.Builder()
                .url(url).removeHeader("User-Agent")
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11")
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            String result = response.body().string();
            Log.i(TAG, "response:" + response.isSuccessful() + "," + response.toString() + "\nresult:" + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "url:" + url);
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
        } catch (Exception e) {
            Log.e(TAG, "url:" + url);
            e.printStackTrace();
        }
        return null;
    }

    public boolean post(String url, String content) {
        try {
            return post(url, new TextRequestBody(compress(content)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean post(String url, RequestBody requestBody) {
        boolean success = false;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = mClient.newCall(request).execute();
            success = response.isSuccessful();
            Log.i(TAG, "response:" + success + "," + response.toString() + ",\nbody:" + response.body().string());
        } catch (Exception e) {
            Log.e(TAG, "url:" + url);
            e.printStackTrace();
        }
        return success;
    }

    public boolean post(String url, RequestBody requestBody, HashMap<String, String> headers) {
        boolean success = false;
        try {
            Request.Builder builder = new Request.Builder()
                    .url(url).post(requestBody);
            if (headers != null && !headers.isEmpty()) {
                Iterator<String> iterator = headers.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    headers.put(key, headers.get(key));
                    Log.d(TAG, "KEY:" + key);
                }
            }
            Request request = builder.build();
            Response response = mClient.newCall(request).execute();
            success = response.isSuccessful();
            Log.i(TAG, "response:" + success + "," + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
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

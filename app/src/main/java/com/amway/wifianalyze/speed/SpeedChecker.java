package com.amway.wifianalyze.speed;

import android.util.Log;

import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.FileUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Response;

/**
 * Created by big on 2018/11/15.
 */

public class SpeedChecker {
    private static final String TAG = "SpeedChecker";
    private static final String DOWNLOAD_URL = "http://pubstatic.b0.upaiyun.com/check2.jpg";
    //        private static final String DOWNLOAD_URL = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h169_1.34.1.23_2fc1ef.apk";
    private static final String UPLOAD_URL = "http://health-test.b0.upaiyun.com/check2.jpg?t=%s";
    private static final int MAX_COUNT = 25;
    private static final int DURATION = 5000;

    private AtomicInteger mCountDownload = new AtomicInteger(0);
    private AtomicLong mLengthDownload = new AtomicLong(0);
    private AtomicBoolean mStopTagDownload = new AtomicBoolean();
    private float mSpeedDownload = 0;

    private void init() {
        mSpeedDownload = 0;
        mCountDownload.set(0);
        mLengthDownload.set(0);
        mStopTagDownload.set(false);

        mSpeedUpload = 0;
        mCountUpload.set(0);
        mLengthUpload.set(0);
        mStopTagUpload.set(false);
    }

    public float checkDownload(final Callback<Float> callback) {
        init();
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            ThreadManager.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] readBuffer = new byte[1024 * 5];
                    long lastTime = startTime;
                    while (!mStopTagDownload.get() && mCountDownload.get() < MAX_COUNT) {
                        Response response = HttpHelper.getInstance().getResponse(DOWNLOAD_URL);
                        if (response != null && response.isSuccessful()) {
                            InputStream inputStream = response.body().byteStream();
                            int readLen;
                            try {
                                while (!mStopTagDownload.get() && mCountDownload.get() < MAX_COUNT
                                        && (readLen = inputStream.read(readBuffer)) > 0) {
                                    mLengthDownload.set(mLengthDownload.get() + readLen);
                                    if (System.currentTimeMillis() - lastTime > 200) {
                                        lastTime = System.currentTimeMillis();
                                        mSpeedDownload = mLengthDownload.get() / (System.currentTimeMillis() - startTime);
                                        if (callback != null) {
                                            callback.onCallBack(true, mSpeedDownload);
                                        }
                                        mCountDownload.set(mCountDownload.get() + 1);
                                        Log.e("big", "download length：" + mLengthDownload + ",speed:" + mSpeedDownload);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        response.close();
                    }
                    if (!mStopTagDownload.get()) {
                        try {
                            synchronized (mStopTagDownload) {
                                mStopTagDownload.set(true);
                                mStopTagDownload.notifyAll();
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Exception:" + e.getMessage());
                        }
                    }
                }
            });
        }
        if (!mStopTagDownload.get()) {
            try {
                synchronized (mStopTagDownload) {
                    mStopTagDownload.wait();
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "InterruptedException:" + e);
            }
        }
        return mSpeedDownload;
    }


    private AtomicInteger mCountUpload = new AtomicInteger(0);
    private AtomicLong mLengthUpload = new AtomicLong(0);
    private float mSpeedUpload = 0;
    private static final int THREAD_NUM = 3;
    private AtomicBoolean mStopTagUpload = new AtomicBoolean();


    public float checkUpload(final Callback<Float> callback) {
        final int buffSize = 1024 * 1024 * 1;
        final String test = new String(new byte[buffSize]);
        final long startTime = System.currentTimeMillis();
        if (callback != null) {
            callback.onCallBack(true, 0f);
        }
        for (int i = 0; i < THREAD_NUM; i++) {
            ThreadManager.execute(new Runnable() {
                @Override
                public void run() {
                    while (!mStopTagUpload.get() && mCountUpload.get() < MAX_COUNT * (100 / THREAD_NUM)) {
                        int code = httpPost(String.format(UPLOAD_URL, System.currentTimeMillis()), null, test);
                        if (code == 404) {
                            mLengthUpload.set(mLengthUpload.get() + buffSize);
                        }
                        mCountUpload.set(mCountUpload.get() + 1);
                        long time = (System.currentTimeMillis() - startTime);
                        if (time > DURATION) {
                            mCountUpload.set(MAX_COUNT * (100 / THREAD_NUM));
                        }
                        mSpeedUpload = mLengthUpload.get() / time;
                        if (callback != null) {
                            callback.onCallBack(true, mSpeedUpload);
                        }
                        Log.d(TAG, "upload time:" + time + ",length:" + mLengthUpload.get() + ",mSpeedUpload:" + mSpeedUpload);
                    }
                    if (!mStopTagUpload.get()) {
                        try {
                            synchronized (mStopTagUpload) {
                                mStopTagUpload.set(true);
                                mStopTagUpload.notifyAll();
                            }
                        } catch (Exception e) {
                            Log.i(TAG, "Exception:" + e.getMessage());
                        }
                    }

                }
            });
        }
        if (!mStopTagUpload.get()) {
            try {
                synchronized (mStopTagUpload) {
                    mStopTagUpload.wait();
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "InterruptedException:" + e);
            }
        }

        return mSpeedUpload;
    }

    public static int httpPost(String requestUrl, Map<String, String> header, String postBody) {
        int code = 0;
        BufferedReader br = null;
        DataOutputStream out = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 10秒超时
            conn.setReadTimeout(30000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            if (header != null) {
                for (String key : header.keySet()) {
                    conn.setRequestProperty(key, header.get(key));
                }
            }
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            out = new DataOutputStream(conn.getOutputStream());
            out.write(postBody.getBytes("utf-8"));
            out.flush();
            code = conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.closeIO(out);
            FileUtils.closeIO(br);
        }
        return code;
    }

    public void release() {
        mStopTagDownload.set(true);
        mStopTagUpload.set(true);
        Log.e(TAG, "release");
    }


}

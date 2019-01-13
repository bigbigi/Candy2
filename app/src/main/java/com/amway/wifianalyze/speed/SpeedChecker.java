package com.amway.wifianalyze.speed;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.util.Log;

import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.FileUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.Server;
import com.chinanetcenter.wcs.android.ClientConfig;
import com.chinanetcenter.wcs.android.api.FileUploader;
import com.chinanetcenter.wcs.android.api.ParamsConf;
import com.chinanetcenter.wcs.android.entity.OperationMessage;
import com.chinanetcenter.wcs.android.internal.UploadFileRequest;
import com.chinanetcenter.wcs.android.listener.FileUploaderListener;

import android.os.Handler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    private static final int MAX_COUNT = 25;
    private static final int DURATION = 5000;

    private AtomicInteger mCountDownload = new AtomicInteger(0);
    private AtomicLong mLengthDownload = new AtomicLong(0);
    private AtomicBoolean mStopTagDownload = new AtomicBoolean();
    private float mSpeedDownload = 0;
    private Context mContext;

    public SpeedChecker(Context context) {
        mContext = context;
        ClientConfig config = new ClientConfig();
        config.setMaxConcurrentRequest(10);
        FileUploader.setClientConfig(config);
        ParamsConf conf = new ParamsConf();
        // 原始文件名称
        conf.fileName = "androiTest.txt";
        // 通过表单参数设置文件保存到云存储的名称
        conf.keyName = "";
        // 通过表单参数设置文件的mimeType
        conf.mimeType = "application/octet-stream";
        FileUploader.setParams(conf);
        FileUploader.setBlockConfigs(8, 512); //设置块大小为8M，片大小为512KB

    }

    private final static int TIME_OUT = 10000;
    private final static int MSG_DOWNLOAD_TIMEOUT = 1;
    private final static int MSG_UPLOAD_TIMEOUT = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOWNLOAD_TIMEOUT:
                    Log.d(TAG, "DOWNLOAD TIMEOUT");
                    notifyDownload();
                    break;
                case MSG_UPLOAD_TIMEOUT:
                    Log.d(TAG, "UPLAOD TIMEOUT");
                    notifyUpload();
                    break;
            }
        }
    };

    private void notifyDownload() {
        try {
            synchronized (mStopTagDownload) {
                mStopTagDownload.set(true);
                mStopTagDownload.notifyAll();
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception:" + e.getMessage());
        }
    }

    private void notifyUpload() {
        try {
            synchronized (mStopTagUpload) {
                mStopTagUpload.set(true);
                mStopTagUpload.notifyAll();
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception:" + e.getMessage());
        }
    }

    private void init() {
        Log.d(TAG, "init");
        mSpeedDownload = 0;
        mCountDownload.set(0);
        mLengthDownload.set(0);
        mStopTagDownload.set(false);

        mSpeedUpload = 0;
        mCountUpload.set(0);
        mLengthUpload.set(0);
        mStopTagUpload.set(false);
        mHandler.removeCallbacksAndMessages(null);
    }

    public float checkDownload(final Callback<Float> callback) {
        if (callback != null) {
            callback.onCallBack(true, 0f);
        }
        init();
        mHandler.sendEmptyMessageDelayed(MSG_DOWNLOAD_TIMEOUT, TIME_OUT);
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            ThreadManager.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] readBuffer = new byte[1024 * 100];
                    long lastTime = startTime;
                    while (!mStopTagDownload.get() && mCountDownload.get() < MAX_COUNT) {
                        Response response = HttpHelper.getInstance().getResponse(Server.DOWNLOAD_SERVER);
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
                                        Log.e(TAG, "download length：" + mLengthDownload + ",speed:" + mSpeedDownload);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            FileUtils.closeIO(inputStream);
                        }
                        if (response != null) {
                            response.close();
                        } else {
                            break;
                        }
                    }
                    notifyDownload();
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
        mHandler.removeMessages(MSG_DOWNLOAD_TIMEOUT);
        return mSpeedDownload;
    }


    private AtomicInteger mCountUpload = new AtomicInteger(0);
    private AtomicLong mLengthUpload = new AtomicLong(0);
    private float mSpeedUpload = 0;
    private static final int THREAD_NUM = 3;
    private AtomicBoolean mStopTagUpload = new AtomicBoolean();


    public float checkUpload(final Callback<Float> callback) {
        final int buffSize = 1024 * 1024 * 5;
        if (callback != null) {
            callback.onCallBack(true, 0f);
        }
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/amway/";
        String path = dir + "upload.txt";
        FileUtils.mkdirs(dir);
        File testFile = new File(path);
        if (!testFile.exists() || testFile.length() == 0) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                fos.write(new byte[buffSize]);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtils.closeIO(fos);
            }
        }
        mHandler.sendEmptyMessageDelayed(MSG_UPLOAD_TIMEOUT, TIME_OUT);
        Log.d(TAG, "FILE:" + testFile.length() + ",url:" + Server.UPLOAD_SERVER);
        final long startTime = System.currentTimeMillis();
        FileUploader.setUploadUrl(Server.UPLOAD_SERVER);
        FileUploader.upload(mContext, Server.TOKEN, testFile, null, new FileUploaderListener() {

            @Override
            public void onFailure(OperationMessage operationMessage) {
                Log.d(TAG, "onFailure:" + operationMessage.getMessage());
                notifyUpload();
            }

            @Override
            public void onSuccess(int i, JSONObject jsonObject) {
                Log.d(TAG, "onSuccess:");
            }

            @Override
            public void onProgress(UploadFileRequest request, long currentSize, long totalSize) {
                Log.d(TAG, "uploatd:" + currentSize + ",total:" + totalSize);
                if (!mStopTagUpload.get()){
                    long time = System.currentTimeMillis() - startTime;
                    mSpeedUpload = currentSize * 1000 / (System.currentTimeMillis() - startTime) / 1024;
                    if (callback != null) {
                        callback.onCallBack(true, mSpeedUpload);
                    }
                    if (currentSize == totalSize || time > DURATION) {
                        notifyUpload();
                    }
                    super.onProgress(request, currentSize, totalSize);
                }
            }
        });

        if (!mStopTagUpload.get()) {
            try {
                synchronized (mStopTagUpload) {
                    mStopTagUpload.wait();
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "InterruptedException:" + e);
            }
        }
        mHandler.removeMessages(MSG_UPLOAD_TIMEOUT);
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
            conn.setConnectTimeout(5000);
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
        mHandler.removeCallbacksAndMessages(null);
        mStopTagDownload.set(true);
        mStopTagUpload.set(true);
        Log.e(TAG, "release");
    }


}

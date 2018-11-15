package com.amway.wifianalyze.speed;

import android.os.Handler;
import android.util.Log;

import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * Created by big on 2018/11/15.
 */

public class SpeedChecker {
    private static final String CHECK_URL = "http://pubstatic.b0.upaiyun.com/check2.jpg";
    private Callback<String> mCallback;

    public void start() {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                long startTime = System.currentTimeMillis();
                long lastTime = startTime;
                long length = 0;
                byte[] readBuffer = new byte[1024];
                while (i < 25) {
                    Response response = HttpHelper.getInstance().getResponse(CHECK_URL);
                    InputStream inputStream = response.body().byteStream();
                    int readLen;
                    try {
                        while ((readLen = inputStream.read(readBuffer)) > 0 && i < 25) {
                            length += readLen;
                            if (System.currentTimeMillis() - lastTime > 200) {
                                float speed = length / (System.currentTimeMillis() - startTime);
                                if (mCallback != null) {
                                    mCallback.onSuccess(String.valueOf(speed));
                                }
                                lastTime = System.currentTimeMillis();
                                i++;
                                Log.e("big", "length：" + length + ",speed:" + speed);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("big", "go2Result");

            }
        });
    }
}

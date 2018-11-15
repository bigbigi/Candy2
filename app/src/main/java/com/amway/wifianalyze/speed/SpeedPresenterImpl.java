package com.amway.wifianalyze.speed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedPresenterImpl extends SpeedContract.SpeedPresenter {
    //    private static final String CHECK_URL = "http://pubstatic.b0.upaiyun.com/check2.jpg";
    private static final String CHECK_URL = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h169_1.34.1.23_2fc1ef.apk";
    private static final String CHECK_UPLOAD = "http://health-test.b0.upaiyun.com/check2.jpg?Wed%20Nov%2014%202018%2017:39:40%20GMT+08000.05684841621583214";

    private static final int MAX_COUNT = 25;

    public SpeedPresenterImpl(SpeedContract.SpeedView view) {
        super(view);
    }

    private FragmentManager mFragmentManager;

    @Override
    public void init(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }


    @Override
    public void getSpeed() {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
//                float download = checkDownload();
                float download = 0;
                float upload = checkUpload();
                go2Result(download, upload);
            }
        });
    }

    private float checkDownload() {
        int i = 0;
        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        long length = 0;
        byte[] readBuffer = new byte[1024 * 5];
        float speed = 0;
        while (i < MAX_COUNT) {
            Response response = HttpHelper.getInstance().getResponse(CHECK_URL);
            InputStream inputStream = response.body().byteStream();
            int readLen;
            try {
                while ((readLen = inputStream.read(readBuffer)) > 0 && i < MAX_COUNT) {
                    length += readLen;
                    if (System.currentTimeMillis() - lastTime > 200) {
                        lastTime = System.currentTimeMillis();
                        speed = length / (System.currentTimeMillis() - startTime);
                        mView.updateSpeed(speed, true);
                        i++;
                        Log.e("big", "download length：" + length + ",speed:" + speed);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return speed;
    }

    private float checkUpload() {
        int count = 0;
        long length = 0;
        long startTime = System.currentTimeMillis();
        final int buffSize = 1024 * 20;
        float speed = 0;
        while (count < MAX_COUNT * 10) {
            String test = new String(new byte[buffSize]);
            RequestBody requestBody = new FormBody.Builder()
                    .add("txt", test)
                    .build();
            Response response = HttpHelper.getInstance().post(CHECK_UPLOAD, requestBody);
            if (response != null && response.code() == 404) {
                length += buffSize;
            }
            count++;//todo
            long time = (System.currentTimeMillis() - startTime);
            speed = length / time;
            mView.updateSpeed(speed, false);
            Log.d("big", "upload time:" + time + ",length:" + length + ",speed:" + speed);
        }
        return speed;
    }

    private void go2Result(final float download, final float upload) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("big", "go2Result");
                if (mView.isShow()) {
                    FragmentTransaction transaction = mFragmentManager.beginTransaction();
                    transaction.hide(mFragmentManager.findFragmentByTag(SpeedFrag.TAG));

                    Fragment resultFrag = mFragmentManager.findFragmentByTag(SpeedResultFrag.TAG);
                    if (resultFrag != null) {
                        transaction.remove(resultFrag);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putFloat("download", download);
                    bundle.putFloat("upload", upload);
                    resultFrag = SpeedResultFrag.newInstance(bundle);
                    if (!resultFrag.isAdded()) {
                        transaction.add(R.id.container, resultFrag, SpeedResultFrag.TAG);
                    } else {
                        transaction.show(resultFrag);
                    }
                    transaction.commitAllowingStateLoss();
                }
            }
        });
    }
}

package com.amway.wifianalyze.speed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedPresenterImpl extends SpeedContract.SpeedPresenter {
    private static final String CHECK_URL = "http://pubstatic.b0.upaiyun.com/check2.jpg";
    //    private static final String CHECK_URL = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h169_1.34.1.23_2fc1ef.apk";
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
                int i = 0;
                long startTime = System.currentTimeMillis();
                long lastTime = startTime;
                long length = 0;
                byte[] readBuffer = new byte[1024];
                float speed = 0;
                while (i < MAX_COUNT) {
                    Response response = HttpHelper.getInstance().getResponse(CHECK_URL);
                    InputStream inputStream = response.body().byteStream();
                    int readLen;
                    try {
                        while ((readLen = inputStream.read(readBuffer)) > 0 && i < MAX_COUNT) {
                            length += readLen;
                            if (System.currentTimeMillis() - lastTime > 200) {
                                speed = length / (System.currentTimeMillis() - startTime);
                                mView.updateSpeed(String.valueOf(speed));
                                lastTime = System.currentTimeMillis();
                                i++;
                                Log.e("big", "length：" + length + ",speed:" + speed);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                go2Result(speed);
            }
        });
    }

    private void go2Result(final float speed) {
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
                    bundle.putFloat("speed", speed);
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

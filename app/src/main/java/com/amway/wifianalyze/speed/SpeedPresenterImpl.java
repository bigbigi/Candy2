package com.amway.wifianalyze.speed;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedPresenterImpl extends SpeedContract.SpeedPresenter {
    public SpeedPresenterImpl(SpeedContract.SpeedView view) {
        super(view);
    }

    private FragmentManager mFragmentManager;
    private Handler mHandler = new Handler();

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
                while (i < 25) {
                    Response response = HttpHelper.getInstance(((Fragment) mView).getContext()).getResponse("http://pubstatic.b0.upaiyun.com/check2.jpg");
                    try {
                        byte[] bytes = response.body().bytes();
                        length += bytes.length;
                        long time = System.currentTimeMillis() - lastTime;
                        Log.e("big", "bytes：" + length + ",time:" + time);
                        if (time > 200) {
                            float speed = length / (System.currentTimeMillis() - startTime);
                            mView.updateSpeed(String.valueOf(speed));
                            lastTime = System.currentTimeMillis();
                            i++;
                            Log.e("big", "speed：" + speed);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.e("big", "go2Result");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mView.isShow()) {
                            FragmentTransaction transaction = mFragmentManager.beginTransaction();
                            transaction.hide(mFragmentManager.findFragmentByTag(SpeedFrag.TAG));

                            Fragment resultFrag = mFragmentManager.findFragmentByTag(SpeedResultFrag.TAG);
                            if (resultFrag == null) {
                                resultFrag = SpeedResultFrag.newInstance(null);
                            }
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
        });
    }
}

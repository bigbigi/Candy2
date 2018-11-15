package com.amway.wifianalyze.speed;

import android.content.Context;
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
                while (i < 10) {
                    i++;
                    long startTime = System.currentTimeMillis();
                    Response response = HttpHelper.getInstance(((Fragment) mView).getContext()).getResponse("http://pubstatic.b0.upaiyun.com/check2.jpg");
                    try {
                        byte[] bytes = response.body().bytes();
                        long time = System.currentTimeMillis() - startTime;
                        float speed = bytes.length / (time);
                        Log.e("big", "bytes：" + bytes.length + ",time:" + time);
                        mView.updateSpeed(String.valueOf(speed));
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

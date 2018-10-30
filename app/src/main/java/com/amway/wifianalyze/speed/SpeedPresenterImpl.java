package com.amway.wifianalyze.speed;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.amway.wifianalyze.R;

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
        new Thread() {//todo test
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("big", "go2Result");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
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
                });

            }

        }.start();
    }
}

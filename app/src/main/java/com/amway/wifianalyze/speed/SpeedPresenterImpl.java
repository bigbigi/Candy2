package com.amway.wifianalyze.speed;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.amway.wifianalyze.home.HomeBiz;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.ThreadManager;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedPresenterImpl extends SpeedContract.SpeedPresenter {

    public SpeedPresenterImpl(SpeedContract.SpeedView view) {
        super(view);
    }

    private FragmentManager mFragmentManager;
    private SpeedChecker mSpeedChecker;

    @Override
    public void init(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mSpeedChecker = new SpeedChecker(((Fragment) mView).getContext());
    }

    private Object mLock = new Object();
    private boolean mHasGetUrl;

    @Override
    public void getSpeed() {
        Log.d("SpeedChecker", "getSpeed：" + mHasGetUrl);
        if (mHasGetUrl) {
            checkSpeed();
        } else {
            HomeBiz.getInstance(((Fragment) mView).getContext()).getSysconfig(new Callback() {
                @Override
                public void onCallBack(boolean success, Object[] t) {
                    mHasGetUrl = success;
                    checkSpeed();
                }
            });
        }
    }

    private void checkSpeed() {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    Log.d("SpeedChecker", "inshow:" + mView.isShow());
                    if (mView.isShow()) {
                        float download = mSpeedChecker.checkDownload(new Callback<Float>() {
                            @Override
                            public void onCallBack(boolean success, Float... o) {
                                mView.updateSpeed(o[0], true);
                            }
                        });
                        float upload = mSpeedChecker.checkUpload(new Callback<Float>() {
                            @Override
                            public void onCallBack(boolean success, Float... o) {
                                mView.updateSpeed(o[0], false);
                            }

                        });
                        mView.onSpeedCheckFinish(download,upload);
                    }
                }
            }
        });
    }

    @Override
    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    @Override
    public void release() {
        mSpeedChecker.release();
    }

}

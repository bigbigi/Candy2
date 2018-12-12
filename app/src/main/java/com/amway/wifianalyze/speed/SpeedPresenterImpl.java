package com.amway.wifianalyze.speed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.amway.wifianalyze.R;
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
        mSpeedChecker = new SpeedChecker(((Fragment)mView).getContext());
    }

    private Object mLock = new Object();

    @Override
    public void getSpeed() {
        Log.d("SpeedChecker", "getSpeed");
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
                        go2Result(download, upload);
                    }
                }
            }
        });

    }

    @Override
    public void release() {
        mSpeedChecker.release();
    }


    private void go2Result(final float download, final float upload) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("big", "go2Result");
                if (mView.isShow() && HomeBiz.getInstance(((Fragment) mView).getContext()).mCurrentFrag instanceof SpeedFrag) {
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
                    HomeBiz.getInstance(((Fragment) mView).getContext()).mCurrentFrag = resultFrag;
                }
            }
        });
    }
}

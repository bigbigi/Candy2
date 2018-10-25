package com.amway.wifianalyze.home;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseActivity;
import com.amway.wifianalyze.feedback.FeedbackFrag;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.amway.wifianalyze.speed.SpeedFrag;
import com.amway.wifianalyze.utils.PermissionUtil;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
        showLauncher();
        new PermissionUtil().init(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideLauncher();
            }
        }, 5000);
        Log.d("big", "wifi:" + NetworkUtils.getWifiSetting(this));
    }

    private ViewGroup mTabLayout;
    private Fragment mCurrentFragment;
    private Fragment mDetectFragment;
    private Fragment mSpeedFragment;
    private Fragment mFeedbackFragment;

    private void init() {
        mTabLayout = (ViewGroup) findViewById(R.id.tab_layout);
        findViewById(R.id.tab_detect).setOnClickListener(this);
        findViewById(R.id.tab_speed).setOnClickListener(this);
        findViewById(R.id.tab_feedback).setOnClickListener(this);
    }

    private Handler mHandler = new Handler();

    private void showLauncher() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(LaunchFrag.TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = LaunchFrag.newInstance(null);
            transaction.add(R.id.launcher_container, fragment, LaunchFrag.TAG);
        } else {
            transaction.show(fragment);
        }
        transaction.commit();
    }


    private void hideLauncher() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(getSupportFragmentManager().findFragmentByTag(LaunchFrag.TAG));
        transaction.commit();
        findViewById(R.id.tab_detect).performClick();
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < mTabLayout.getChildCount(); i++) {
            mTabLayout.getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        if (v.getId() == R.id.tab_detect) {
            if (mDetectFragment == null) {
                mDetectFragment = HomeFrag.newInstance(null);
                new WifiPresenterImpl((HomeFrag) mDetectFragment);
                new AuthPresenterImpl((HomeFrag) mDetectFragment);
            }
            showFragment(mDetectFragment);
        } else if (v.getId() == R.id.tab_speed) {
            if (mSpeedFragment == null) {
                mSpeedFragment = new SpeedFrag();
            }
            showFragment(mSpeedFragment);
        } else if (v.getId() == R.id.tab_feedback) {
            if (mFeedbackFragment == null) {
                mFeedbackFragment = new FeedbackFrag();
            }
            showFragment(mFeedbackFragment);
        }
    }

    private void showFragment(Fragment fragment) {
        if (mCurrentFragment == fragment)
            return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
            mCurrentFragment.setUserVisibleHint(false);
        }
        if (!fragment.isAdded()) {

            transaction.add(R.id.container, fragment).commit();
        } else {
            transaction.show(fragment).commit();
        }
        mCurrentFragment = fragment;
        mCurrentFragment.setUserVisibleHint(true);
    }
}

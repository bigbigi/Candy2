package com.amway.wifianalyze.home;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseActivity;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.amway.wifianalyze.utils.PermissionUtil;

public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        go2Launcher();
        new PermissionUtil().init(this);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                go2Home();
            }
        }, 5000);
       /* new Thread() {
            @Override
            public void run() {
                boolean connect = NetworkUtils.telnet("www.baidu.com", 23);
                Log.d("big", "connect:" + connect);
            }
        }.start();*/
        Log.d("big", "wifi:" + NetworkUtils.getWifiSetting(this));
    }

    private Handler mHandler = new Handler();

    private void go2Launcher() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(LaunchFrag.TAG);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = LaunchFrag.newInstance(null);
            transaction.add(R.id.container, fragment, LaunchFrag.TAG);
        } else {
            transaction.show(fragment);
        }
        transaction.commit();
    }


    private void go2Home() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(getSupportFragmentManager().findFragmentByTag(LaunchFrag.TAG));
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(HomeFrag.TAG);
        if (fragment == null) {
            fragment = HomeFrag.newInstance(null);
            new WifiPresenterImpl((HomeFrag) fragment);
            new AuthPresenterImpl((HomeFrag) fragment);
            transaction.add(R.id.container, fragment, HomeFrag.TAG);
        } else {
            transaction.show(fragment);
        }
        transaction.commit();
    }

}

package com.amway.wifianalyze.home;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseFragment;

import java.util.List;


/**
 * Created by big on 2018/10/17.
 */

public class HomeFrag extends BaseFragment implements WifiContract.WifiView {
    public static final String TAG = "HomeFrag";

    public static HomeFrag newInstance(Bundle bundle) {
        HomeFrag fragment = new HomeFrag();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_home, container, false);
        init(content);
        return content;
    }

    public void init(View content) {
        mWifiPresenter.init(getContext());
        mWifiPresenter.scanWifi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiPresenter.release(getContext());
    }

    private WifiContract.WifiPresenter mWifiPresenter;

    @Override
    public void setPresenter(WifiContract.WifiPresenter presenter) {
        mWifiPresenter = presenter;
    }

    @Override
    public void onScanResult(List<ScanResult> list, WifiInfo currentWifi) {
        Log.d("big", "onScanResult," + "list:" + list + ",current:" + currentWifi);
    }

    @Override
    public void onWifiUnable() {
        Log.d("big", "onWifiUnable");

    }

    @Override
    public void onWifiAvailable() {
        Log.d("big", "onWifiAvailable");
    }
}

package com.amway.wifianalyze.speed;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.home.WifiContract;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.autofit.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedFrag extends BaseFragment implements WifiContract.WifiView
        , SpeedContract.SpeedView {
    public static final String TAG = "SpeedFrag";

    public static SpeedFrag newInstance(Bundle args) {
        SpeedFrag fragment = new SpeedFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_speed, container, false);
        init(content);
        return content;
    }

    private RecyclerView mRecyclerView;
    private SpeedAdapter mAdapter;
    private TextView mWifiName;
    private TextView mWifiFrequence;

    public void init(View content) {
        mRecyclerView = (RecyclerView) content.findViewById(R.id.speed_Recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SpeedAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mWifiName = (TextView) content.findViewById(R.id.wifi_name);
        mWifiFrequence = (TextView) content.findViewById(R.id.wifi_frequence);
        mWifiName.setText("");
        mWifiFrequence.setText("");
        mWifiPresenter.init(getContext());
        start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiPresenter.release(getContext());
    }

    private void start() {
        if (mWifiPresenter.isConnected()) {
            WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            onConnected(wifiManager.getConnectionInfo());
        } else {
            mWifiPresenter.scanWifi();
        }
    }

    private WifiContract.WifiPresenter mWifiPresenter;
    private SpeedContract.SpeedPresenter mSpeedPresenter;

    @Override
    public void setPresenter(BaseContract.BasePresenter presenter) {
        if (presenter instanceof WifiContract.WifiPresenter) {
            mWifiPresenter = (WifiContract.WifiPresenter) presenter;
        } else if (presenter instanceof SpeedContract.SpeedPresenter) {
            mSpeedPresenter = (SpeedContract.SpeedPresenter) presenter;
        }
    }


    @Override
    public void onConnected(WifiInfo wifiInfo) {
        if (wifiInfo.getSSID() != null) {
            mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWifiFrequence.setText(NetworkUtils.is24GHz(wifiInfo.getFrequency()) ? R.string.detect_24G : R.string.detect_5G);
        }
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<SpeedResult> list = new ArrayList();
        list.add(new SpeedResult(getString(R.string.speed_IP), NetworkUtils.intToIp(wifiInfo.getIpAddress())));
        list.add(new SpeedResult(getString(R.string.speed_MAC), NetworkUtils.getMac(getContext())));
        list.add(new SpeedResult(getString(R.string.speed_subnet), NetworkUtils.intToIp(wifiManager.getDhcpInfo().netmask)));
        mAdapter.setData(list);
        mSpeedPresenter.getSpeed();
    }


    @Override
    public void onChecking(int code) {

    }

    @Override
    public void onInfo(int code, int loss, int delay) {

    }


    @Override
    public void onError(int code, int reason) {

    }


    @Override
    public void updateSpeed(String speed) {

    }

    @Override
    public boolean isShow() {
        return isVisible();
    }
}

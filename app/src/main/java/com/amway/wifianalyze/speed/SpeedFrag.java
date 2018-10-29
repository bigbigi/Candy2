package com.amway.wifianalyze.speed;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import com.amway.wifianalyze.home.DetectAdapter;
import com.amway.wifianalyze.home.WifiContract;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.autofit.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedFrag extends BaseFragment implements WifiContract.WifiView {
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
        mWifiPresenter.scanWifi();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiPresenter.release(getContext());
    }

    private WifiContract.WifiPresenter mWifiPresenter;

    @Override
    public void setPresenter(BaseContract.BasePresenter presenter) {
        if (presenter instanceof WifiContract.WifiPresenter) {
            mWifiPresenter = (WifiContract.WifiPresenter) presenter;
        }
    }

    @Override
    public void onScanResult(List<ScanResult> list, WifiInfo currentWifi) {

    }

    @Override
    public void onWifiUnable() {

    }

    @Override
    public void onWifiAvailable() {

    }

    @Override
    public void onFoundSSID(boolean found) {

    }

    @Override
    public void onConnected(WifiInfo wifiInfo) {
        if (wifiInfo.getSSID() != null) {
            mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        }
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<SpeedResult> list = new ArrayList();
        list.add(new SpeedResult(getString(R.string.speed_IP), NetworkUtils.intToIp(wifiInfo.getIpAddress())));
        list.add(new SpeedResult(getString(R.string.speed_MAC), NetworkUtils.getWlanMac()));
        list.add(new SpeedResult(getString(R.string.speed_subnet), NetworkUtils.intToIp(wifiManager.getDhcpInfo().netmask)));
        mAdapter.setData(list);
    }

    @Override
    public void onConnectFailed() {

    }

    @Override
    public void onFailReason(int code) {

    }
}

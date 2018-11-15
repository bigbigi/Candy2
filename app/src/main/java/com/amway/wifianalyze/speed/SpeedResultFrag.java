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
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.autofit.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by big on 2018/10/25.
 */

public class SpeedResultFrag extends BaseFragment {
    public static final String TAG = "SpeedResultFrag";
    private float mDownloadSpeed;
    private float mUploadSpeed;

    public static SpeedResultFrag newInstance(Bundle args) {
        SpeedResultFrag fragment = new SpeedResultFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_speed_result, container, false);
        mDownloadSpeed = getArguments().getFloat("download");
        mUploadSpeed = getArguments().getFloat("upload");
        init(content);
        initData();
        return content;
    }

    private RecyclerView mRecyclerView;
    private SpeedAdapter mAdapter;
    private TextView mWifiName;
    private TextView mWifiFrequence;
    private TextView mDownloadValue;
    private TextView mUploadValue;
    private TextView mBandwidth;

    public void init(View content) {
        mDownloadValue = (TextView) content.findViewById(R.id.speed_download);
        mUploadValue = (TextView) content.findViewById(R.id.speed_upload);
        mBandwidth = (TextView) content.findViewById(R.id.speed_result_bandwidth);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.speed_Recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SpeedAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mWifiName = (TextView) content.findViewById(R.id.wifi_name);
        mWifiFrequence = (TextView) content.findViewById(R.id.wifi_frequence);
        mWifiName.setText("");
        mWifiFrequence.setText("");
    }

    private void initData() {
        mDownloadValue.setText(NetworkUtils.getSpeed(mDownloadSpeed));
        mUploadValue.setText(NetworkUtils.getSpeed(mUploadSpeed));
        mBandwidth.setText(String.format(getString(R.string.speed_bandwidth), NetworkUtils.getBandwidth(mDownloadSpeed)));
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSSID() != null) {
            mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWifiFrequence.setText(NetworkUtils.is24GHz(wifiInfo.getFrequency()) ? R.string.detect_24G : R.string.detect_5G);
        }
        List<SpeedResult> list = new ArrayList();
        list.add(new SpeedResult(getString(R.string.speed_IP), NetworkUtils.intToIp(wifiInfo.getIpAddress())));
        list.add(new SpeedResult(getString(R.string.speed_MAC), NetworkUtils.getMac(getContext())));
        list.add(new SpeedResult(getString(R.string.speed_subnet), NetworkUtils.intToIp(wifiManager.getDhcpInfo().netmask)));
        mAdapter.setData(list);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

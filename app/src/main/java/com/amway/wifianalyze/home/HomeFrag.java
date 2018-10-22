package com.amway.wifianalyze.home;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private RecyclerView mRecyclerView;
    private TestAdapter mAdapter;

    public void init(View content) {
        mRecyclerView = (RecyclerView) content.findViewById(R.id.wifiRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new TestAdapter();
        mRecyclerView.setAdapter(mAdapter);

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
        mAdapter.getData().add("扫描成功");
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onWifiUnable() {
        Log.d("big", "onWifiUnable");
        mAdapter.getData().add("wifi未打开，正在打开wifi...");
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onWifiAvailable() {
        Log.d("big", "onWifiAvailable");
        mAdapter.getData().add("已打开wifi，开始扫描...");
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onFoundSSID(boolean found) {
        String message = found ? "正在连接" : "未找到目标wifi";
        mAdapter.getData().add(message);
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onConnected() {
        mAdapter.getData().add("wifi连接成功");
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onConnectFailed() {
        mAdapter.getData().add("wifi连接失败，正在分析原因...");
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onFailReason(int code) {
        String message = null;
        switch (code) {
            case 1:
                message = "密码错误";
                break;
            case 2:
                message = "信道拥堵";
                break;
            case 3:
                message = "信号差";
                break;
        }
        mAdapter.getData().add(message);
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }
}

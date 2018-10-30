package com.amway.wifianalyze.home;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.home.AuthContract.AuthPresenter;
import com.amway.wifianalyze.home.DetectResult.Status;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.autofit.widget.TextView;


import java.util.List;


/**
 * Created by big on 2018/10/17.
 */

public class HomeFrag extends BaseFragment implements
        WifiContract.WifiView,
        AuthContract.AuthView {
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
    private DetectAdapter mAdapter;
    private TextView mWifiName;
    private TextView mWifiFrequence;
    private TestDialog mDialog;

    public void init(View content) {
        mRecyclerView = (RecyclerView) content.findViewById(R.id.wifiRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DetectAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mWifiName = (TextView) content.findViewById(R.id.wifi_name);
        mWifiFrequence = (TextView) content.findViewById(R.id.wifi_frequence);
        mWifiName.setText("");
        mWifiFrequence.setText("");

        mWifiPresenter.init(getContext());
//        mWifiPresenter.scanWifi();

        //todo test
        mDialog = new TestDialog(getContext());
        mDialog.setOnStartListener(new TestDialog.OnStartListener() {
            @Override
            public void onStart() {
                mAdapter.getData().clear();
                mAdapter.notifyDataSetChanged();
            }
        });
        mDialog.setPresenter((WifiPresenterImpl) mWifiPresenter);
        mDialog.show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged:" + hidden);
        if (!hidden && mWifiPresenter != null && !mWifiPresenter.isConnected()) {
            mAdapter.getData().clear();
            mAdapter.notifyDataSetChanged();
            mWifiPresenter.scanWifi();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiPresenter.release(getContext());
    }

    private WifiContract.WifiPresenter mWifiPresenter;
    private AuthContract.AuthPresenter mAuthPresenter;

    @Override
    public void setPresenter(BaseContract.BasePresenter presenter) {
        if (presenter instanceof WifiContract.WifiPresenter) {
            mWifiPresenter = (WifiContract.WifiPresenter) presenter;
        } else if (presenter instanceof AuthContract.AuthPresenter) {
            mAuthPresenter = (AuthContract.AuthPresenter) presenter;
        }
    }

    @Override
    public void onScanResult(List<ScanResult> list, WifiInfo currentWifi) {
        Log.d(TAG, "onScanResult," + "list:" + list + ",current:" + currentWifi);
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, "扫描成功"));
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onWifiUnable() {
        Log.d(TAG, "onWifiUnable");
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, "wifi未打开，正在打开wifi..."));
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onWifiAvailable() {
        Log.d(TAG, "onWifiAvailable");
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, "已打开wifi，开始扫描..."));
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }

    @Override
    public void onFoundSSID(boolean found) {
        String message = found ? "正在连接" : "未找到目标wifi";
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, message));
        mAdapter.notifyItemInserted(mAdapter.getData().size());
        if (!found) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            mDialog.show();
        }
    }

    @Override
    public void onConnected(WifiInfo wifiInfo) {
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, "wifi连接成功"));
        mAdapter.notifyItemInserted(mAdapter.getData().size());
        mAuthPresenter.startCheck(getContext());
        if (wifiInfo.getSSID() != null) {
            mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "frequence:" + wifiInfo.getFrequency());
            mWifiFrequence.setText(NetworkUtils.is24GHz(wifiInfo.getFrequency()) ? R.string.detect_24G : R.string.detect_5G);
        }
    }


    @Override
    public void onConnectFailed() {
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, "wifi连接失败，正在分析原因..."));
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
        mAdapter.getData().add(new DetectResult(Status.SUCCESS, message));
        mAdapter.notifyItemInserted(mAdapter.getData().size());
    }


    @Override
    public void onError(int code) {
        final String message;
        switch (code) {
            case AuthPresenter.INFO_STATIC_IP:
                message = "静态IP";
                break;
            case AuthPresenter.INFO_SERVER:
                message = "服务器ping不通";
                break;
            case AuthPresenter.INFO_SERVER_PORT:
                message = "服务器端口被占用";
                break;
            case AuthPresenter.INFO_INTERNET:
                message = "Internet专线不通";
                break;
            case AuthPresenter.INFO_DNS:
                message = "DNS错误";
                break;
            default:
                message = null;
                break;
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.getData().add(new DetectResult(Status.SUCCESS, message));
                    mAdapter.notifyItemInserted(mAdapter.getData().size());
                }
            });
        }
    }

    @Override
    public void onInfo(int code, int loss, int delay) {
        final String message;
        switch (code) {
            case AuthPresenter.INFO_STATIC_IP:
                message = "静态IP ----Ok";
                break;
            case AuthPresenter.INFO_SERVER:
                message = "服务器ping不通 ----Ok,丢包：" + loss + ",延迟:" + delay;
                break;
            case AuthPresenter.INFO_SERVER_PORT:
                message = "服务器端口被占用 ----Ok";
                break;
            case AuthPresenter.INFO_INTERNET:
                message = "Internet专线不通 ----Ok,丢包：" + loss + ",延迟:" + delay;
                break;
            case AuthPresenter.INFO_DNS:
                message = "DNS错误  ----Ok";
                break;
            default:
                message = null;
                break;
        }
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.getData().add(new DetectResult(Status.SUCCESS, message));
                    mAdapter.notifyItemInserted(mAdapter.getData().size());
                }
            });
        }
    }

}

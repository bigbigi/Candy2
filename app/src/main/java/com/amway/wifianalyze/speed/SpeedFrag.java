package com.amway.wifianalyze.speed;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.bean.DeviceInfo;
import com.amway.wifianalyze.home.HomeBiz;
import com.amway.wifianalyze.home.WifiContract;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.autofit.widget.TextView;
import com.permission.manager.Permission;
import com.permission.manager.PermissionCallback;
import com.permission.manager.PermissionManager;

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
    private TextView mSpeedValue;
    private TextView mDownloadValue;
    private TextView mUploadValue;
    private TextView mState;
    private TextView mApName;

    public void init(View content) {
        mSpeedValue = (TextView) content.findViewById(R.id.speed_value);
        mDownloadValue = (TextView) content.findViewById(R.id.speed_download);
        mUploadValue = (TextView) content.findViewById(R.id.speed_upload);
        mState = (TextView) content.findViewById(R.id.speed_state);
        mApName = (TextView) content.findViewById(R.id.wifi_ap);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.speed_Recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new SpeedAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mWifiName = (TextView) content.findViewById(R.id.wifi_name);
        mWifiFrequence = (TextView) content.findViewById(R.id.wifi_frequence);
        mWifiName.setText("");
        mWifiFrequence.setText("");
        mWifiPresenter.init(getContext());
        HomeBiz.getInstance(getContext()).getShopName(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, String... t) {
                if (success) {
                    ThreadManager.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) return;
                            DeviceInfo info = HomeBiz.getInstance(getContext()).getDeviceInfo();
                            mApName.setText(String.format(getString(R.string.ap_users), info.ap, HomeBiz.getInstance(getContext()).mCount));
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            start();
        } else {
            mSpeedPresenter.release();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWifiPresenter.release(getContext());
        mSpeedPresenter.release();
    }

    private void start() {
        WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null && info.getSSID() != null) {
            onConnected(wifiManager.getConnectionInfo());
        } else {
            mWifiPresenter.start();
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
    public void onConnected(final WifiInfo wifiInfo) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                if (wifiInfo.getSSID() != null) {
                    mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
                }
                if (NetworkUtils.isSupport5G(getContext()) || HomeBiz.getInstance(getContext()).mHas5G) {
                    mWifiFrequence.setText(R.string.detect_5G);
                } else {
                    mWifiFrequence.setText(R.string.detect_24G);
                }
                WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                List<SpeedResult> list = new ArrayList();
                list.add(new SpeedResult(getString(R.string.speed_IP), NetworkUtils.intToIp(wifiInfo.getIpAddress())));
                list.add(new SpeedResult(getString(R.string.speed_MAC), NetworkUtils.getMac(getContext())));
                list.add(new SpeedResult(getString(R.string.speed_subnet), NetworkUtils.intToIp(wifiManager.getDhcpInfo().netmask)));
                mAdapter.setData(list);
                if (!isHidden()) {
                    PermissionManager.check(getActivity(),  Permission.Group.STORAGE,
                            new PermissionCallback(getContext(),getString(R.string.permisson_storage)){
                                @Override
                                public void hasPermission() {
                                    super.hasPermission();
                                    mSpeedPresenter.getSpeed();
                                }
                            });
                }
            }
        });

    }

    @Override
    public void onStartCheck() {

    }


    @Override
    public void onChecking(int code) {

    }

    @Override
    public void onInfo(int code, int loss, int delay) {

    }


    @Override
    public void onError(int code, int reason, String... value) {

    }

    @Override
    public void onStopCheck() {

    }


    @Override
    public void updateSpeed(final float speed, final boolean download) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                mSpeedValue.setText(String.valueOf(speed * 8));
                if (download) {
                    mState.setText(R.string.speed_downloading);
                    mUploadValue.setText(R.string.speed_prepared);
                    mDownloadValue.setText(NetworkUtils.getSpeed(speed));
                    HomeBiz.getInstance(getContext()).mDownloadSpeed = speed;
                } else {
                    mUploadValue.setText(NetworkUtils.getSpeed(speed));
                    mState.setText(R.string.speed_uploading);
                    HomeBiz.getInstance(getContext()).mUploadSpeed = speed;
                }
            }
        });

    }

    private void go2Result(final float download, final float upload) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("big", "go2Result");
                if (isShow() && HomeBiz.getInstance(getContext()).mCurrentFrag instanceof SpeedFrag) {
                    FragmentManager fragmentManager = mSpeedPresenter.getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.hide(fragmentManager.findFragmentByTag(SpeedFrag.TAG));

                    Fragment resultFrag = fragmentManager.findFragmentByTag(SpeedResultFrag.TAG);
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
                    HomeBiz.getInstance(getContext()).mCurrentFrag = resultFrag;
                }
            }
        });
    }

    @Override
    public void onSpeedCheckFinish(float download, float upload) {
        go2Result(download, upload);
    }

    @Override
    public boolean isShow() {
        return isVisible();
    }
}

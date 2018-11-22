package com.amway.wifianalyze.home;

import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.view.animation.LinearInterpolator;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.home.DetectResult.Status;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.autofit.widget.TextView;


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
    private TextView mApName;
    private TestDialog mDialog;
    private View mRadar;

    public void init(View content) {
        mRadar = content.findViewById(R.id.detect_radar);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.wifiRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DetectAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mWifiName = (TextView) content.findViewById(R.id.wifi_name);
        mApName = (TextView) content.findViewById(R.id.wifi_ap);
        mWifiFrequence = (TextView) content.findViewById(R.id.wifi_frequence);
        mWifiName.setText("");
        mWifiFrequence.setText("");

        mWifiPresenter.init(getContext());
        mWifiPresenter.scanWifi();
        startAni();

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

    private ObjectAnimator mAni;

    private void startAni() {
        stopAni();
        mAni = ObjectAnimator.ofFloat(mRadar, "rotation", 360);
        mAni.setInterpolator(new LinearInterpolator());
        mAni.setRepeatCount(ObjectAnimator.INFINITE);
        mAni.setDuration(1200);
        mAni.start();
    }

    private void stopAni() {
        if (mAni != null) {
            mRadar.setRotation(0);
            mAni.cancel();
        }
    }

    private void checkEnd(int code) {
        if (code == Code.INFO_SKIP) {
            stopAni();
        }
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
    public void onConnected(WifiInfo wifiInfo) {
        mAuthPresenter.startCheck(getContext());
        if (wifiInfo.getSSID() != null) {
            mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "frequence:" + wifiInfo.getFrequency());
            mWifiFrequence.setText(NetworkUtils.is24GHz(wifiInfo.getFrequency()) ? R.string.detect_24G : R.string.detect_5G);
        }
        HomeBiz.getInstance(getContext()).getShopName(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, final String... t) {
                ThreadManager.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mApName.setText(t[0]);
                    }
                });
            }
        });
    }


    @Override
    public void onChecking(final int code) {
        final String message = Code.getMessage(code, -1, -1);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.getData().add(0, new DetectResult(Status.LOADING, code, message));
                    mAdapter.insert();
                }
            });
        }
    }

    @Override
    public void onInfo(final int code, int loss, int delay) {
        final String message = Code.getMessage(code, loss, delay);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkEnd(code);
                    for (int i = 0; i < mAdapter.getData().size(); i++) {
                        DetectResult result = mAdapter.getData().get(i);
                        if (result.getCode() == code) {
                            result.setContent(message);
                            result.setStatus(Status.SUCCESS);
                            mAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onError(final int code, final int reason) {
        Log.e(TAG, "onError:" + code + ",reason:" + reason);
        final String message = Code.getErrorMessage(code, reason);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkEnd(code);
                    for (int i = 0; i < mAdapter.getData().size(); i++) {
                        DetectResult result = mAdapter.getData().get(i);
                        if (result.getCode() == code) {
                            result.setStatus(Status.ERROR);
                            mAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    if (reason > 0) {
                        mAdapter.getData().add(0, new DetectResult(Status.ERROR, code, message));
                        mAdapter.insert();
                    }
                }
            });
        }
    }

}

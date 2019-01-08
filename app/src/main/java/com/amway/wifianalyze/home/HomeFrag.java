package com.amway.wifianalyze.home;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.bean.DeviceInfo;
import com.amway.wifianalyze.bean.FaqInfo;
import com.amway.wifianalyze.home.DetectResult.Status;
import com.amway.wifianalyze.lib.ToastOnPermission;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.speed.SpeedContract;
import com.amway.wifianalyze.speed.SpeedView;
import com.amway.wifianalyze.utils.UpdateBiz;
import com.autofit.widget.LinearLayout;
import com.autofit.widget.ScreenParameter;
import com.autofit.widget.TextView;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by big on 2018/10/17.
 */

public class HomeFrag extends BaseFragment implements
        WifiContract.WifiView,
        AuthContract.AuthView,
        SpeedContract.SpeedView,
        View.OnClickListener {
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
    private TextView mDetectWifiName;
    private TextView mDetectWifiFrequence;
    private TextView mDetectApName;
    private TestDialog mDialog;
    private View mRadar;
    private LinearLayout mAdviceLayout;
    private View mAdviceMan;
    private View mSpeedResultLayout;
    private RecyclerView mAdviceRecycler;
    private AdviceAdapter mAdviceAdapter;
    private View mWifiLayout;
    private TextView mDownloadValue;
    private TextView mUploadValue;
    private SpeedView mSpeedView;
    private TextView mDefinition;
    private View mSpeedLoadingLayout;
    private ScrollView mScrollView;

    public void init(View content) {
        content.findViewById(R.id.barcode).setOnClickListener(this);
        content.findViewById(R.id.scan_barcode).setOnClickListener(this);
        mScrollView = (ScrollView) content.findViewById(R.id.home_scroll);
        mRadar = content.findViewById(R.id.detect_radar);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.wifiRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mAdapter = new DetectAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mWifiName = (TextView) content.findViewById(R.id.wifi_name);
        mApName = (TextView) content.findViewById(R.id.wifi_ap);
        mWifiFrequence = (TextView) content.findViewById(R.id.wifi_frequence);
        mWifiLayout = content.findViewById(R.id.wifi);
        mDetectWifiName = (TextView) mWifiLayout.findViewById(R.id.wifi_name);
        mDetectApName = (TextView) mWifiLayout.findViewById(R.id.wifi_ap);
        mDetectWifiFrequence = (TextView) mWifiLayout.findViewById(R.id.wifi_frequence);
        mAdviceLayout = (LinearLayout) content.findViewById(R.id.advice_layout);
        mAdviceMan = content.findViewById(R.id.advice_man);
        AnimationDrawable drawable = (AnimationDrawable) mAdviceMan.getBackground();
        drawable.start();
        mSpeedResultLayout = content.findViewById(R.id.speed_result_layout);
        mAdviceRecycler = (RecyclerView) content.findViewById(R.id.advice);
        mAdviceRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdviceAdapter = new AdviceAdapter();
        mAdviceRecycler.setAdapter(mAdviceAdapter);

        mDownloadValue = (TextView) content.findViewById(R.id.speed_download);
        mUploadValue = (TextView) content.findViewById(R.id.speed_upload);
        mDefinition = (TextView) content.findViewById(R.id.speed_definition);
        mSpeedView = (SpeedView) content.findViewById(R.id.speed_level);
        mSpeedLoadingLayout = content.findViewById(R.id.speed_layout);
        mWifiName.setText("");
        mWifiFrequence.setText("");
        mDetectWifiName.setText("");
        mWifiFrequence.setText("");
        mDetectApName.setText("");
        XXPermissions.with(getActivity()).constantRequest()
                .permission(Permission.Group.LOCATION)
                .request(new ToastOnPermission(getContext(), getString(R.string.permisson_wifi)) {
                    @Override
                    public void hasPermission(List<String> list, boolean b) {
                        super.hasPermission(list, b);
                        mWifiPresenter.init(getContext());
                        mWifiPresenter.start();
                    }
                });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged:" + hidden);
        if (!hidden && mWifiPresenter != null
                && (mWifiPresenter.getStatus() == WifiContract.WifiPresenter.Status.FAILED
                || mWifiPresenter.getStatus() == WifiContract.WifiPresenter.Status.PASS)) {
            mWifiLayout.setVisibility(View.VISIBLE);
            mWifiPresenter.start();
        }
        if (!hidden && mScrollView != null) {
            Log.e(TAG, "scrollTo:" + mScrollView.getScrollY());
            mScrollView.scrollTo(0, 0);
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
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                if (mAni != null) {
                    mRadar.setRotation(0);
                    mAni.cancel();
                }
            }
        });
    }


    private WifiContract.WifiPresenter mWifiPresenter;
    private AuthContract.AuthPresenter mAuthPresenter;
    private SpeedContract.SpeedPresenter mSpeedPresenter;

    @Override
    public void setPresenter(BaseContract.BasePresenter presenter) {
        if (presenter instanceof WifiContract.WifiPresenter) {
            mWifiPresenter = (WifiContract.WifiPresenter) presenter;
        } else if (presenter instanceof AuthContract.AuthPresenter) {
            mAuthPresenter = (AuthContract.AuthPresenter) presenter;
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
                    mDetectWifiName.setText(mWifiName.getText());
                }
                if (NetworkUtils.isSupport5G(getContext()) || HomeBiz.getInstance(getContext()).mHas5G) {
                    mWifiFrequence.setText(R.string.detect_5G);
                } else {
                    mWifiFrequence.setText(R.string.detect_24G);
                }
                mDetectWifiFrequence.setText(mWifiFrequence.getText());
                mAuthPresenter.startCheck(getContext());
                checkUpdate();
            }
        });

    }

    @Override
    public void onStartCheck() {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                startAni();
                mAdviceLayout.setVisibility(View.GONE);
                mAdviceMan.setVisibility(View.GONE);
                mSpeedResultLayout.setVisibility(View.GONE);
                mSpeedLoadingLayout.setVisibility(View.GONE);
                mSpeedLoadingLayout.setVisibility(View.GONE);
                mSpeedPresenter.release();
                mAdapter.getData().clear();
                mAdapter.notifyDataSetChanged();
                mAdviceAdapter.getData().clear();
                mAdviceAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onGetAp(final String apName, final String count) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                mApName.setText(String.format(getString(R.string.ap_users), apName, count));
                mDetectApName.setText(mApName.getText());
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
                    if (isFinishing()) return;
                    for (DetectResult result : mAdapter.getData()) {
                        if (result.getCode() == code) {
                            return;
                        }
                    }
                    mAdapter.getData().add(0, new DetectResult(Status.LOADING, code, message));
                    mAdapter.insert();
                }
            });
        }
    }


    @Override
    public void onStopCheck() {
        if (mWifiPresenter.getStatus() == WifiContract.WifiPresenter.Status.CONNECTING
                || mWifiPresenter.getStatus() == WifiContract.WifiPresenter.Status.SCAN) {
            stop();
        } else {
            ThreadManager.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSpeedLoadingLayout.setVisibility(View.VISIBLE);
                }
            });
            mSpeedPresenter.getSpeed();
        }
    }


    private void showFaq(List<FaqInfo> list) {
        mWifiLayout.setVisibility(View.GONE);
        mAdviceLayout.setVisibility(View.VISIBLE);
        mAdviceMan.setVisibility(View.VISIBLE);
        mAdviceAdapter.setData(list);
    }


    @Override
    public void onInfo(final int code, int loss, int delay) {
        final String message = Code.getMessage(code, loss, delay);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    for (int i = 0; i < mAdapter.getData().size(); i++) {
                        DetectResult result = mAdapter.getData().get(i);
                        if (result.getCode() == code) {
                            result.setContent(message);
                            result.setStatus(Status.SUCCESS);
                            DetectAdapter.TextHolder holder = (DetectAdapter.TextHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                            Log.d("big", "holder:" + holder + ",i:" + i + ",size:" + mAdapter.getData().size() + ",name:" + result.getContent());
                            if (holder != null) {
                                mAdapter.onBindViewHolder(holder, i);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onError(final int code, final int reason, String... value) {
        Log.e(TAG, "onError:" + code + ",reason:" + reason);
        final String message = Code.getErrorMessage(code, reason, value);
        HomeBiz.getInstance(getContext()).mErrors.add(code);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    for (int i = 0; i < mAdapter.getData().size(); i++) {
                        DetectResult result = mAdapter.getData().get(i);
                        if (result.getCode() == code) {
                            result.setStatus(Status.ERROR);
                            result.setContent(message);
                            DetectAdapter.TextHolder holder = (DetectAdapter.TextHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                            if (holder != null) {
                                mAdapter.onBindViewHolder(holder, i);
                            }
                            break;
                        }
                    }

                }
            });
        }
    }

    private static final int REQUEST_CODE = 3;

    private void go2Capture() {
        XXPermissions.with(getActivity()).constantRequest()
                .permission(Permission.CAMERA)
                .request(new ToastOnPermission(getContext(), getString(R.string.permisson_wifi)) {
                    @Override
                    public void hasPermission(List<String> list, boolean b) {
                        super.hasPermission(list, b);
                        Intent intent = new Intent(getActivity(), CaptureActivity.class);
                        startActivityForResult(intent, REQUEST_CODE);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    try {
                        HomeBiz.getInstance(getContext()).setDeviceInfo(new DeviceInfo(new JSONObject(result)));
                        restart();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "code result:" + result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(getContext(), "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private BarcodeDialog mBarcodeDialog;

    private void showBarcode() {
        if (mBarcodeDialog == null) {
            mBarcodeDialog = new BarcodeDialog(getContext());
        }
        mBarcodeDialog.show();
    }

    @Override
    public void onClick(View v) {
        Log.d("big", "onclick");
        if (v.getId() == R.id.barcode) {
            showBarcode();
        } else if (v.getId() == R.id.scan_barcode) {
            go2Capture();
        }
    }

    private void restart() {
        ThreadManager.clearSinglTask();
        mWifiPresenter.start();
    }

    private UpdateDialog mUpdateDialog;

    private void checkUpdate() {
        UpdateBiz.getInstance().request(getContext(), new Callback<String>() {
            @Override
            public void onCallBack(final boolean success, String... t) {
                if (!TextUtils.isEmpty(t[0])) {
                    ThreadManager.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) return;
                            if (mUpdateDialog == null) {
                                mUpdateDialog = new UpdateDialog(getContext());
                            }
                            if (success) {
                                mUpdateDialog.setMust();
                            }
                            mUpdateDialog.show();
                        }
                    });
                }
            }
        });
    }

    public void updateSpeed(final float speed, final boolean download) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                if (download) {
                    mUploadValue.setText(R.string.speed_prepared);
                    mDownloadValue.setText(NetworkUtils.getSpeed(speed));
                    HomeBiz.getInstance(getContext()).mDownloadSpeed = speed;
                } else {
                    mUploadValue.setText(NetworkUtils.getSpeed(speed));
                    HomeBiz.getInstance(getContext()).mUploadSpeed = speed;
                }
            }
        });
    }


    public boolean isShow() {
        return isVisible();
    }


    public void onSpeedCheckFinish(final float download, final float upload) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSpeedView.setLevel(NetworkUtils.getLevel(download));
                mDefinition.setText(String.format(getString(R.string.speed_level1), NetworkUtils.getDefinition(download)));
//                mSpeedLoadingLayout.setVisibility(View.GONE);
                mSpeedResultLayout.setVisibility(View.VISIBLE);
            }
        });
        stop();
    }

    private void stop() {
        stopAni();
        HomeBiz.getInstance(getContext()).submitDetectResult(null);
        if (HomeBiz.getInstance(getContext()).mErrors.size() > 0) {
            mWifiPresenter.stop(WifiContract.WifiPresenter.Status.FAILED);
        } else {
            mWifiPresenter.stop(WifiContract.WifiPresenter.Status.PASS);
            HomeBiz.getInstance(getContext()).mErrors.add(0);
        }
        HomeBiz.getInstance(getContext()).getFaq(new Callback<List<FaqInfo>>() {
            @Override
            public void onCallBack(boolean success, final List<FaqInfo>[] t) {
                if (success) {
                    ThreadManager.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) return;
                            showFaq(t[0]);
                        }
                    });
                }
            }
        });
    }

}

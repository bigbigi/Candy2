package com.amway.wifianalyze.home;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
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
    private TestDialog mDialog;
    private View mRadar;

    public void init(View content) {
        content.findViewById(R.id.barcode).setOnClickListener(this);
        content.findViewById(R.id.scan_barcode).setOnClickListener(this);
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
        XXPermissions.with(getActivity()).constantRequest()
                .permission(Permission.Group.LOCATION)
                .request(new ToastOnPermission(getContext(), getString(R.string.permisson_wifi)) {
                    @Override
                    public void hasPermission(List<String> list, boolean b) {
                        super.hasPermission(list, b);
                        mWifiPresenter.init(getContext());
                        mWifiPresenter.start();
                        //todo test
                      /*  mDialog = new TestDialog(getContext());
                        mDialog.setOnStartListener(new TestDialog.OnStartListener() {
                            @Override
                            public void onStart() {
                                mAdapter.getData().clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                        mDialog.setPresenter((WifiPresenterImpl) mWifiPresenter);
                        mDialog.show();*/
                    }
                });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e(TAG, "onHiddenChanged:" + hidden);
        if (!hidden && mWifiPresenter != null && mWifiPresenter.getStatus() == WifiContract.WifiPresenter.Status.FAILED) {
            mWifiPresenter.start();
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
                if (mAni != null) {
                    mRadar.setRotation(0);
                    mAni.cancel();
                }
            }
        });
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
    public void onConnected(final WifiInfo wifiInfo) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (wifiInfo.getSSID() != null) {
                    mWifiName.setText(wifiInfo.getSSID().replaceAll("\"", ""));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.e(TAG, "frequence:" + wifiInfo.getFrequency());
                    mWifiFrequence.setText(NetworkUtils.is24GHz(wifiInfo.getFrequency()) ? R.string.detect_24G : R.string.detect_5G);
                }
                mAuthPresenter.startCheck(getContext());
            }
        });

    }

    @Override
    public void onStartCheck() {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startAni();
                mAdapter.getData().clear();
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onGetAp(final String apName,final String count) {
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mApName.setText(String.format(getString(R.string.ap_users),apName,count));
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
        stopAni();
        HomeBiz.getInstance(getContext()).submitDetectResult(null);
        if (HomeBiz.getInstance(getContext()).mErrors.size() > 0) {
            mWifiPresenter.stop(WifiContract.WifiPresenter.Status.FAILED);
            HomeBiz.getInstance(getContext()).getFaq(new Callback<List<FaqInfo>>() {
                @Override
                public void onCallBack(boolean success, final List<FaqInfo>[] t) {
                    if (success) {
                        ThreadManager.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFaq(t[0]);
                            }
                        });
                    }
                }
            });
        } else {
            mWifiPresenter.stop(WifiContract.WifiPresenter.Status.PASS);
        }
    }

    private FAQDialog mFaqDialog;

    private void showFaq(List<FaqInfo> list) {
        if (mFaqDialog == null) {
            mFaqDialog = new FAQDialog(getContext());
        }
        //TODO TEST
       /* List<FaqInfo> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            FaqInfo info = new FaqInfo();
            info.question = "test";
            info.answer = "test";
            list.add(info);
        }*/
        //TODO TEST
        mFaqDialog.showData(list);

    }


    @Override
    public void onInfo(final int code, int loss, int delay) {
        final String message = Code.getMessage(code, loss, delay);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
    public void onError(final int code, final int reason) {
        Log.e(TAG, "onError:" + code + ",reason:" + reason);
        final String message = Code.getErrorMessage(code, reason);
        HomeBiz.getInstance(getContext()).mErrors.add(code);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
}

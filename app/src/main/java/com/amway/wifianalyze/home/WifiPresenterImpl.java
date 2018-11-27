package com.amway.wifianalyze.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;


import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.lib.util.Utils;
import com.amway.wifianalyze.utils.WifiConnector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by big on 2018/10/18.
 */

public class WifiPresenterImpl extends WifiContract.WifiPresenter {
    private static final String TAG = "WifiPresenterImpl";

    private final static int MAX_SCAN_TIMES = 3;
    private final static int CONNECT_TIMEOUT = 30000;
    private final static int BUSY_CHANNEL = 3;
    private final static int LOW_LEVEL = -80;
    private static String DEFAULT_SSID = "91vst-wifi";
    private static String DEFAULT_PWD = "91vst.com";
    //    private static String DEFAULT_SSID = "big";
//    private static String DEFAULT_PWD = "";
    private HashMap<Integer, Integer> mChannelBusyMap = new HashMap<Integer, Integer>();
    private MyWifiBrocastReceiver mWifiReceiver;
    private WifiManager mWm;
    private WifiInfo mWifiInfo;
    private Context mContext;

    private int mReScanTimes = 0;
    private boolean mRefreshScanList = true;
    private boolean mCheckConnected = false;
    private boolean mWifiOff;


    public WifiPresenterImpl(WifiContract.WifiView view) {
        super(view);
    }

    @Override
    public void init(Context context) {
        mContext = context;
        if (mWm == null) {
            mWm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mWifiReceiver = new MyWifiBrocastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(mWifiReceiver, filter);
        }
    }

    public void setTestData(String name, String pwd) {
        Log.e(TAG, "name:" + name + ",pwd:" + pwd);
        DEFAULT_SSID = name;
        DEFAULT_PWD = pwd;
    }

    private static final int MSG_CONNECT_TIMEOUT = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_TIMEOUT:
                    findFailReason();
                    break;
            }
        }
    };


    @Override
    public void scanWifi() {
        if (mWm == null) {
            return;
        } else if (mWm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            mWifiOff = true;
            mView.onChecking(Code.INFO_OPEN_WIFI);
            mWm.setWifiEnabled(true);
        } else {
            mView.onChecking(Code.INFO_SCAN_WIFI);
            mWm.startScan();
            mRefreshScanList = true;
            Log.e(TAG, "开始扫描");
        }
    }

    private void connect(ScanResult scanResult) {//91vst.com
        mView.onChecking(Code.INFO_CONNECTING);
        mFailScanResult = scanResult;
        int type = scanResult.capabilities.contains("WEP") ? WifiConnector.WIFI_TYPE_WEP : WifiConnector.WIFI_TYPE_WPA;
        if (TextUtils.isEmpty(DEFAULT_PWD)) {
            type = WifiConnector.WIFI_TYPE_NOPASS;
        }
        Log.e(TAG, "TYPE:" + type);
        boolean connect = WifiConnector.connect(mWm, scanResult.SSID.replaceAll("\"", ""), DEFAULT_PWD, type);
        if (!connect) {
            mView.onError(Code.INFO_CONNECTING, Code.ERROR_PWD);
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        }
    }

    private void onInfo(int code) {
        mView.onInfo(code, 0, -1);
    }


    private void onConnected() {
        Log.d(TAG, "NETWORK-->" + "----Connected--------");
        WifiInfo info = mWm.getConnectionInfo();
        if (!mCheckConnected && !mRefreshScanList && info != null && info.getSSID() != null
                && DEFAULT_SSID.equals(info.getSSID().replaceAll("\"", ""))) {
            onInfo(Code.INFO_CONNECTING);
            mView.onChecking(Code.INFO_CONNECTED);
            onInfo(Code.INFO_CONNECTED);
            mCheckConnected = true;
            mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
            mView.onConnected(mWm.getConnectionInfo());
            getAp(mContext);
        }

    }

    public boolean isConnected() {
        if (mWm == null) return false;
        WifiInfo info = mWm.getConnectionInfo();
        return info != null && info.getSSID() != null
                && DEFAULT_SSID.equals(info.getSSID().replaceAll("\"", ""));
    }

    private void findFailReason() {
        if (mFailScanResult != null) {
            Log.e(TAG, "level:" + mFailScanResult.level + ",channelNum:" + mChannelBusyMap.get(mFailScanResult.frequency));
            boolean lowLevel = mFailScanResult.level < LOW_LEVEL;//信号差
            boolean busyChannel = mChannelBusyMap.get(mFailScanResult.frequency) > BUSY_CHANNEL;//信道拥堵
            if (!lowLevel && !busyChannel) {//其他问题
                mView.onError(Code.INFO_CONNECTING, Code.ERROR_ELSE);
            } else {
                if (lowLevel) {
                    mView.onError(Code.INFO_CONNECTING, Code.ERROR_LOW_LEVEL);
                }
                if (busyChannel) {
                    mView.onError(Code.INFO_CONNECTING, Code.ERROR_BUSY_CHANNEL);
                }
            }
        }
    }

    @Override
    public void release(Context context) {
        context.unregisterReceiver(mWifiReceiver);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void rangeList(List<ScanResult> list) {
        if (list == null || list.size() == 0)
            return;
        Iterator<ScanResult> iterator = list.iterator();
        ScanResult result = null;
        mChannelBusyMap.clear();
        while (iterator.hasNext()) {
            ScanResult temp = iterator.next();
            int record = mChannelBusyMap.get(temp.frequency) == null ? 0 : mChannelBusyMap.get(temp.frequency);
            mChannelBusyMap.put(temp.frequency, record + 1);
            if (mWifiInfo != null && !TextUtils.isEmpty(mWifiInfo.getSSID())
                    && mWifiInfo.getSSID().replaceAll("\"", "").equals(temp.SSID.replaceAll("\"", ""))) {
                result = temp;
                iterator.remove();

            }

            if (temp.SSID == null || temp.SSID.equals("")) {
                iterator.remove();
            }
        }
        if (result != null) {
            list.add(0, result);
        }
    }

    private ScanResult mFailScanResult = null;

    private class MyWifiBrocastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:" + intent.getAction());
            Log.d(TAG, "mRefreshScanList:" + mRefreshScanList);
            if (mRefreshScanList && WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                mCheckConnected = false;
                mWifiInfo = mWm.getConnectionInfo();
                List<ScanResult> list = mWm.getScanResults();
                Log.d(TAG, "list:" + list);
                Log.d(TAG, "WIFI:" + mWifiInfo);
                boolean has5G = false;
                if (list != null && !list.isEmpty()) {
                    mRefreshScanList = false;
                    mReScanTimes = 0;
                    rangeList(list);
                    if (mWifiInfo == null || mWifiInfo.getSSID() == null
                            || !DEFAULT_SSID.equals(mWifiInfo.getSSID().replaceAll("\"", ""))) {
                        Iterator<ScanResult> iterator = list.iterator();
                        ScanResult dstResult = null;
                        while (iterator.hasNext()) {
                            ScanResult temp = iterator.next();
                            has5G = has5G || NetworkUtils.is5GHz(temp.frequency);
                            if (DEFAULT_SSID.equals(temp.SSID.replaceAll("\"", ""))) {
                                dstResult = temp;
                                HomeBiz.getInstance(context).setFrequence(temp.frequency);
                            }
                        }
                        Log.d(TAG, "has5G:" + has5G);
                        if (dstResult == null) {
                            mView.onError(Code.INFO_SCAN_WIFI, Code.ERR_NO_WIFI);
                        } else {
                            onInfo(Code.INFO_SCAN_WIFI);
                            connect(dstResult);
                        }

                    } else {
                        onInfo(Code.INFO_SCAN_WIFI);
                        onConnected();
                    }

                    HomeBiz.getInstance(context).mHas5G = has5G;
                    mView.onChecking(Code.INFO_SUPPORT_5G);
                    if (NetworkUtils.isSupport5G(context) || has5G) {
                        onInfo(Code.INFO_SUPPORT_5G);
                    } else if (NetworkUtils.isOnly24G(context)) {
                        mView.onError(Code.INFO_SUPPORT_5G, Code.ERR_ONLY24G);
                    } else {
                        mView.onError(Code.INFO_SUPPORT_5G, Code.ERR_NOTFOUND_5G);
                    }
                } else if (mReScanTimes < MAX_SCAN_TIMES) {
                    mReScanTimes++;
                    scanWifi();
                } else {
                    mView.onError(Code.INFO_SCAN_WIFI, Code.ERR_NO_WIFI);
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//todo
                if (mWm.getWifiState() == WifiManager.WIFI_STATE_ENABLED && mWifiOff) {
                    mWifiOff = false;
                    onInfo(Code.INFO_OPEN_WIFI);
                    scanWifi();
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (parcelableExtra != null) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    switch (networkInfo.getState()) {
                        case CONNECTING:
                            Log.d(TAG, "NETWORK-->" + "CONNECTING");
                            break;
                        case CONNECTED:
                            onConnected();
                            break;
                        case DISCONNECTED:
                            Log.d(TAG, "NETWORK-->" + "DISCONNECTED");
                            break;
                        default:
                            break;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void getAp(Context context) {
        mView.onChecking(Code.INFO_GET_AP);
        HomeBiz.getInstance(context).getShopName(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, final String... t) {
                if (success) {
                    mView.onGetAp(t[0]);
                    int count = Utils.parseInt(t[2]);
                    if (count > 50) {
                        mView.onError(Code.INFO_GET_AP, Code.ERR_AP_USER);
                    } else {
                        mView.onInfo(Code.INFO_GET_AP, count, 0);
                    }
                } else {
                    mView.onGetAp("");
                    mView.onError(Code.INFO_GET_AP, Code.ERR_QUEST);
                }
            }
        });
    }
}

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
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;


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
    private HashMap<Integer, Integer> mChannelBusyMap = new HashMap<Integer, Integer>();
    private MyWifiBrocastReceiver mWifiReceiver;
    private WifiManager mWm;
    private WifiInfo mWifiInfo;

    private int mReScanTimes = 0;
    private boolean mRefreshScanList = false;
    private boolean mCheckConnected = false;


    public WifiPresenterImpl(WifiContract.WifiView view) {
        super(view);
    }

    @Override
    public void init(Context context) {
        mWm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new MyWifiBrocastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.registerReceiver(mWifiReceiver, filter);

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
                    mView.onConnectFailed();
                    findFailReason();
                    break;
            }
        }
    };

    @Override
    public void scanWifi() {
        if (mWm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            mView.onWifiUnable();
            mWm.setWifiEnabled(true);
        } else {
            mWm.startScan();
            mRefreshScanList = true;
            Log.e(TAG, "开始扫描");
        }
    }

    private void connect(ScanResult scanResult) {//91vst.com
        mFailScanResult = scanResult;
        int type = scanResult.capabilities.contains("WEP") ? WifiConnector.WIFI_TYPE_WEP : WifiConnector.WIFI_TYPE_WPA;
        boolean connect = WifiConnector.connect(mWm, scanResult.SSID.replaceAll("\"", ""), DEFAULT_PWD, type);
        if (!connect) {
            mView.onFailReason(ERROR_PWD);
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        }
    }


    private void onConnected() {
        Log.d(TAG, "NETWORK-->" + "----Connected--------");
        WifiInfo info = mWm.getConnectionInfo();
        if (!mCheckConnected && !mRefreshScanList && info != null && info.getSSID() != null
                && DEFAULT_SSID.equals(info.getSSID().replaceAll("\"", ""))) {
            mCheckConnected = true;
            mHandler.removeMessages(MSG_CONNECT_TIMEOUT);
            mView.onConnected(mWm.getConnectionInfo());
        }

    }

    public boolean isConnected() {
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
                mView.onFailReason(ERROR_ELSE);
            } else {
                if (lowLevel) {
                    mView.onFailReason(ERROR_LOW_LEVEL);
                }
                if (busyChannel) {
                    mView.onFailReason(ERROR_BUSY_CHANNEL);
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
            if (mRefreshScanList && WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                mCheckConnected = false;
                mWifiInfo = mWm.getConnectionInfo();
                List<ScanResult> list = mWm.getScanResults();
                if (list != null && !list.isEmpty()) {
                    mRefreshScanList = false;
                    mReScanTimes = 0;
                    rangeList(list);
                    mView.onScanResult(list, mWifiInfo);
                    if (mWifiInfo == null || mWifiInfo.getSSID() == null
                            || !DEFAULT_SSID.equals(mWifiInfo.getSSID().replaceAll("\"", ""))) {
                        Iterator<ScanResult> iterator = list.iterator();
                        ScanResult dstResult = null;
                        while (iterator.hasNext()) {
                            ScanResult temp = iterator.next();
                            if (DEFAULT_SSID.equals(temp.SSID.replaceAll("\"", ""))) {
                                dstResult = temp;
                                break;
                            }
                        }
                        if (dstResult == null) {
                            mView.onFoundSSID(false);
                        } else {
                            mView.onFoundSSID(true);
                            connect(dstResult);
                        }

                    } else {
                        onConnected();
                    }
                } else if (mReScanTimes < MAX_SCAN_TIMES) {
                    mReScanTimes++;
                    scanWifi();
                } else {
                    mView.onFoundSSID(false);
                }

            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                if (mWm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    mView.onWifiAvailable();
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
}

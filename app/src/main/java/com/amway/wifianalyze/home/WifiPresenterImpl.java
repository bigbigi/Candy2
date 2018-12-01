package com.amway.wifianalyze.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;


import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.listener.BlockCall;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
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
    private final static int SCAN_DELAY = 10000;
    private final static int CONNECT_TIMEOUT = 30000;
    private final static int BUSY_CHANNEL = 3;
    private final static int LOW_LEVEL = -80;
    private static String DEFAULT_SSID = "91vst-wifi";
    private static String DEFAULT_PWD = "91vst.com";
    private HashMap<Integer, Integer> mChannelBusyMap = new HashMap<Integer, Integer>();
    private MyWifiBrocastReceiver mWifiReceiver;
    private WifiManager mWm;
    private WifiInfo mWifiInfo;
    private Context mContext;

    private int mReScanTimes = 0;
    private Status mStatus = Status.IDLE;


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
    private static final int MSG_SCAN_TIMEOUT = 2;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT_TIMEOUT:
                    findFailReason();
                    break;
                case MSG_SCAN_TIMEOUT:
                    if (++mReScanTimes < MAX_SCAN_TIMES) {
                        Log.e(TAG, "重复扫描:" + mReScanTimes);
                        mWm.startScan();
                        sendEmptyMessageDelayed(MSG_SCAN_TIMEOUT, SCAN_DELAY);
                    } else {
                        mView.onError(Code.INFO_SCAN_WIFI, Code.ERR_NO_WIFI);
                        mView.onStopCheck();
                    }
                    break;
            }
        }
    };


    @Override
    public void start() {
        HomeBiz.getInstance(mContext).reset();
        mHandler.removeCallbacksAndMessages(null);
        mReScanTimes = 0;
        mStatus = Status.PREPARED;
        scanWifi();
    }

    @Override
    public void stop(Status status) {
        mStatus = status;
    }

    @Override
    public Status getStatus() {
        return mStatus;
    }

    private void scanWifi() {
        Log.d(TAG, "scanWifi");
        if (mWm == null || mStatus != Status.PREPARED) {
            return;
        } else {
            mStatus = Status.SCAN;
            mView.onStartCheck();
            mView.onChecking(Code.INFO_SCAN_WIFI);
            if (mWm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                mView.onError(Code.INFO_SCAN_WIFI, Code.ERR_WIFI_OPEN);
                mView.onStopCheck();
            } else {
                mWm.startScan();
                mHandler.sendEmptyMessageDelayed(MSG_SCAN_TIMEOUT, SCAN_DELAY);
                Log.e(TAG, "------开始扫描------");
            }
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


    public void checkConnect() {
        mStatus = Status.CONNECTING;
        mView.onChecking(Code.INFO_CONNECTED);
        HomeBiz.getInstance(mContext).getShopName(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, String... t) {
                if (mStatus == Status.CONNECTING) {
                    if (success) {
                        mStatus = Status.CONNECTED;
                        onInfo(Code.INFO_CONNECTED);
                        mView.onConnected(mWm.getConnectionInfo());
                    } else {
                        mView.onError(Code.INFO_CONNECTED, Code.ERR_WIFI_CONNECT);
                        mView.onStopCheck();
                    }
                }
            }
        });
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
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()) && mStatus == Status.SCAN) {
                mWifiInfo = mWm.getConnectionInfo();
                List<ScanResult> list = mWm.getScanResults();
                Log.d(TAG, "list:" + list);
                Log.d(TAG, "WIFI:" + mWifiInfo);
                boolean has5G = false;
                if (list != null && !list.isEmpty()) {
                    mHandler.removeMessages(MSG_SCAN_TIMEOUT);
                    rangeList(list);
                    if (mWifiInfo == null || mWifiInfo.getSSID() == null) {
                        mView.onError(Code.INFO_SCAN_WIFI, Code.ERR_NO_WIFI);
                    } else {
                        Iterator<ScanResult> iterator = list.iterator();
                        while (iterator.hasNext()) {
                            ScanResult temp = iterator.next();
                            has5G = has5G || NetworkUtils.is5GHz(temp.frequency);
                            if ((temp.SSID.replaceAll("\"", "")).equals
                                    (mWifiInfo.getSSID().replaceAll("\"", ""))) {
                                HomeBiz.getInstance(context).mScanResult = temp;
                            }
                        }
                        HomeBiz.getInstance(context).mHas5G = has5G;
                        Log.d(TAG, "has5G:" + has5G);
                        onInfo(Code.INFO_SCAN_WIFI);
                        checkConnect();
                    }
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
                            Log.d(TAG, "NETWORK-->" + "----Connected--------");
                            WifiInfo info = mWm.getConnectionInfo();
                            if (info != null && info.getSSID() != null
                                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                                scanWifi();
                            }
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

package com.amway.wifianalyze.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by big on 2018/10/18.
 */

public class WifiPresenterImpl extends WifiContract.WifiPresenter {

    private HashMap<Integer, Integer> mChannelBusyMap = new HashMap<Integer, Integer>();
    private MyWifiBrocastReceiver mWifiReceiver;
    private WifiManager mWm;
    private WifiInfo mWifiInfo;

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
        context.registerReceiver(mWifiReceiver, filter);
    }

    @Override
    public void release(Context context) {
        context.unregisterReceiver(mWifiReceiver);
    }

    @Override
    public void scanWifi() {
        if (mWm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            mView.onWifiUnable();
        } else {
            mWm.startScan();
        }
    }


    private class MyWifiBrocastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                mWifiInfo = mWm.getConnectionInfo();
                List<ScanResult> list = mWm.getScanResults();
                rangeList(list);
                mView.onScanResult(list, mWifiInfo);

                return;
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                if (mWm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    mView.onWifiAvailable();
                }
            }

        }
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
}

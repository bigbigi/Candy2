package com.amway.wifianalyze.home;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

import java.util.List;

/**
 * Created by big on 2018/10/18.
 */

public interface WifiContract extends BaseContract {

    abstract class WifiPresenter extends BasePresenterImpl<WifiView> {

        public WifiPresenter(WifiView view) {
            super(view);
        }

        abstract void init(Context context);

        abstract void release(Context context);

        abstract void scanWifi();
    }

    interface WifiView extends BaseView<WifiPresenter> {
        void onScanResult(List<ScanResult> list, WifiInfo currentWifi);

        void onWifiUnable();

        void onWifiAvailable();
    }

}

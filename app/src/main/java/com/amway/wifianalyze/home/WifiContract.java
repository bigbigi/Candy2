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

    interface WifiView extends BaseView {

        void onScanResult(List<ScanResult> list, WifiInfo currentWifi);

        void onWifiUnable();

        void onWifiAvailable();

        void onFoundSSID(boolean found);

        void onConnected(WifiInfo wifiInfo);

        void onConnectFailed();

        void onFailReason(int code);
    }

    abstract class WifiPresenter extends BasePresenterImpl<WifiView> {
        public static final int ERROR_PWD = 1;//密码错误
        public static final int ERROR_BUSY_CHANNEL = 2;//信道拥堵
        public static final int ERROR_LOW_LEVEL = 3;//信号差
        public static final int ERROR_ELSE = 4;//其他问题，AP人数过多等，找客服

        public WifiPresenter(WifiView view) {
            super(view);
        }

        public abstract void init(Context context);

        public abstract void release(Context context);

        public abstract void scanWifi();

        public abstract boolean isConnected();
    }
}

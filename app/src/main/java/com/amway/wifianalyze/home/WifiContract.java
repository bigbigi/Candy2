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

        void onConnected(WifiInfo wifiInfo);

        void onStartCheck();

        void onChecking(int code);

        void onInfo(int code, int loss, int delay);

        void onError(int code, int reason, String... value);

        void onStopCheck();
    }

    abstract class WifiPresenter extends BasePresenterImpl<WifiView> {
        public enum Status {IDLE, PREPARED, SCAN, CONNECTING, CONNECTED, PASS, FAILED, RESTART}

        public WifiPresenter(WifiView view) {
            super(view);
        }

        public abstract void init(Context context);

        public abstract void release(Context context);

        public abstract void start();

        public abstract void stop(Status status);

        public abstract Status getStatus();

    }
}

package com.amway.wifianalyze.home;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;
import com.amway.wifianalyze.lib.listener.Callback;

/**
 * Created by big on 2018/10/22.
 */

public interface AuthContract extends BaseContract {

    interface AuthView extends BaseView {
        void onGetAp(String apName);

        void onError(int code, int reason);

        void onInfo(int code, int loss, int delay);

        void onChecking(int code);

        void onStopCheck();
    }

    abstract class AuthPresenter extends BasePresenterImpl<AuthView> {

        public AuthPresenter(AuthView view) {
            super(view);
        }

        public abstract void startCheck(Context context);

//        public abstract void checkDhcp(final Callback callback);
//
//        public abstract void checkPort(final Callback callback);
//
//        public abstract void checkServer(final Callback callback);
//
//        public abstract void checkLocalnetLoad(final Callback callback);
//
//        public abstract void checkInternetLoad(final Callback callback);
//
//        public abstract void checkDns(final Callback callback);

    }
}

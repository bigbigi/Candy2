package com.amway.wifianalyze.home;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/10/22.
 */

public interface AuthContract extends BaseContract {

    interface AuthView extends BaseView {
        void onError(int code, int reason);

        void onInfo(int code, int loss, int delay);

        void onChecking(int code);
    }

    abstract class AuthPresenter extends BasePresenterImpl<AuthView> {

        public AuthPresenter(AuthView view) {
            super(view);
        }

        public abstract void startCheck(Context context);

        public abstract boolean checkDhcp();

        public abstract boolean checkPort();

        public abstract void checkServer();

        public abstract void checkInternet();

        public abstract void checkDns();

        public abstract void skipBrowser();
    }
}

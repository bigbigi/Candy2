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
        void onError(int code, int reason);

        void onInfo(int code, int loss, int delay);

        void onChecking(int code);
    }

    abstract class AuthPresenter extends BasePresenterImpl<AuthView> {

        public AuthPresenter(AuthView view) {
            super(view);
        }

        public abstract void startCheck(Context context);

        public abstract void checkDhcp(final Callback callback);

        public abstract void checkPort(final Callback callback);

        public abstract void checkServer(final Callback callback);

        public abstract void checkLocalnet(final Callback callback);

        public abstract void checkInternet(final Callback callback);

        public abstract void checkDns(final Callback callback);

        public abstract void skipBrowser(final Callback callback);
    }
}

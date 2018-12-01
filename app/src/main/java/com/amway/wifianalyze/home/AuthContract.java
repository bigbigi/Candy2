package com.amway.wifianalyze.home;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;
import com.amway.wifianalyze.lib.listener.Callback;

import java.util.ArrayList;

/**
 * Created by big on 2018/10/22.
 */

public interface AuthContract extends BaseContract {

    interface AuthView extends BaseView {
        void onGetAp(String apName,String count);

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

    }
}

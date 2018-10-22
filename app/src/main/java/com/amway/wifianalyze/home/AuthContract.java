package com.amway.wifianalyze.home;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/10/22.
 */

public interface AuthContract extends BaseContract {

    interface AuthView extends BaseView {
        void onError(int code);

        void onInfo(int code, int loss, int delay);
    }

    abstract class AuthPresenter extends BasePresenterImpl<AuthView> {
        public static final int FLAG_ERROR = 0x000000ff;
        public static final int FLAG_INFO = 0x0000ff00;

        public static final int INFO_STATIC_IP = 0x00000001;//静态IP
        public static final int INFO_EMPTY_IP = 0x00000002;//IP空
        public static final int INFO_SERVER = 0x00000003;//服务器ping不通
        public static final int INFO_SERVER_PORT = 0x00000004;//服务器端口被占用
        public static final int INFO_INTERNET = 0x00000005;//Internet专线不通
        public static final int INFO_DNS = 0x00000006;//DNS错误

        public static final int INFO_WEIXIN = 0x00000007;//微信无法认证
        public static final int INFO_CARD = 0x00000008;//卡号无法认证
        public static final int INFO_SMS = 0x00000009;//短信无法认证

        public AuthPresenter(AuthView view) {
            super(view);
        }

        public abstract void startCheck(Context context);

        public abstract boolean checkDhcp();

        public abstract boolean checkPort();

        public abstract void checkServer();

        public abstract void checkInternet();

        public abstract boolean checkDns();

        public abstract void skipBrowser();
    }
}

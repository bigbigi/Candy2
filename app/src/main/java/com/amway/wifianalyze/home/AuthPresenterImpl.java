package com.amway.wifianalyze.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.TracerouteWithPing;

import okhttp3.Response;

/**
 * Created by big on 2018/10/22.
 */

public class AuthPresenterImpl extends AuthContract.AuthPresenter implements TracerouteWithPing.OnTraceRouteListener {
    private static final String TAG = "AuthPresenterImpl";

    private static final String INTERNET = "114.114.114.114";
    private static final String SERVER_URL = "www.baidu.com";
    private static final String AUTO_SERVER = "http://www.baidu.com/generate_204 ";
    private TracerouteWithPing mTraceroute;
    private Context mContext;

    public AuthPresenterImpl(AuthContract.AuthView view) {
        super(view);
    }

    @Override
    public void startCheck(Context context) {
        mContext = context;
        if (mTraceroute == null) {
            mTraceroute = new TracerouteWithPing(context);
            mTraceroute.setOnTraceRouteListener(this);
        }
        checkDhcp();
        checkServer();
    }


    @Override
    public boolean checkDhcp() {
        mView.onChecking(Code.INFO_STATIC_IP);
        boolean staticIp = NetworkUtils.isStaticIp(mContext);
        if (staticIp) {
            mView.onError(Code.INFO_STATIC_IP, -1);
        } else {
            mView.onInfo(Code.INFO_STATIC_IP, 0, 0);
        }
        return !staticIp;
    }

    @Override
    public boolean checkPort() {
        mView.onChecking(Code.INFO_SERVER_PORT);
        return NetworkUtils.telnet(SERVER_URL, 80);
    }


    @Override
    public void checkServer() {
        mView.onChecking(Code.INFO_SERVER);
        mTraceroute.executeTraceroute(SERVER_URL, Code.INFO_SERVER);
    }


    @Override
    public void checkInternet() {
        mView.onChecking(Code.INFO_INTERNET);
        mTraceroute.executeTraceroute(INTERNET, Code.INFO_INTERNET);
    }

    @Override
    public boolean checkDns() {
        mView.onChecking(Code.INFO_DNS);
        return !TextUtils.isEmpty(NetworkUtils.getIp(SERVER_URL));
    }

    @Override
    public void skipBrowser() {
        mView.onChecking(Code.INFO_SKIP);
        Response response = HttpHelper.getInstance().getResponse(AUTO_SERVER);
        if (response != null) {
            Log.d(TAG, "skipBrowser:" + response.code());
            if (response.code() != 204) {
                Intent intent = new Intent();
                intent.setPackage("com.android.browser");
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.baidu.com");
                intent.setData(content_url);
                mContext.startActivity(intent);
                mView.onError(Code.INFO_SKIP, -1);
            } else {
                mView.onInfo(Code.INFO_SKIP, 0, 0);
            }
            response.close();
        } else {
            mView.onError(Code.INFO_SKIP, -1);
        }
    }


    @Override
    public void onResult(int what, int loss, int delay) {
        Log.e("big", "onResult:" + what);
        mView.onInfo(what, loss, delay);
        if (what == Code.INFO_SERVER) {
            if (checkPort()) {
                mView.onInfo(Code.INFO_SERVER_PORT, 0, 0);
                checkInternet();
            } else {
                mView.onError(Code.INFO_SERVER_PORT, -1);
            }
        } else if (what == Code.INFO_INTERNET) {
            if (checkDns()) {
                mView.onInfo(Code.INFO_DNS, 0, 0);
                skipBrowser();
            } else {
                mView.onError(Code.INFO_DNS, -1);
            }
        }
    }

    @Override
    public void onTimeout(int what) {
        Log.e(TAG, "onTimeout:" + what);
        mView.onError(what, -1);
    }

    @Override
    public void onException(int what) {
        Log.e(TAG, "onException:" + what);
        mView.onError(what, -1);
    }
}

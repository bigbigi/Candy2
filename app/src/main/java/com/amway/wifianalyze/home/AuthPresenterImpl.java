package com.amway.wifianalyze.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.lib.NetworkUtils;
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
        boolean staticIp = NetworkUtils.isStaticIp(mContext);
        if (staticIp) {
            mView.onError(INFO_STATIC_IP);
        }
        return !staticIp;
    }

    @Override
    public boolean checkPort() {
        return NetworkUtils.telnet(SERVER_URL, 80);
    }


    @Override
    public void checkServer() {
        mTraceroute.executeTraceroute(SERVER_URL, INFO_SERVER);
    }


    @Override
    public void checkInternet() {
        mTraceroute.executeTraceroute(INTERNET, INFO_INTERNET);
    }

    @Override
    public boolean checkDns() {
        return !TextUtils.isEmpty(NetworkUtils.getIpAndDns(SERVER_URL));
    }

    @Override
    public void skipBrowser() {
        Response response = HttpHelper.getInstance(mContext).getResponse(AUTO_SERVER);
        if (response != null) {
            Log.d(TAG, "skipBrowser:" + response.code());
            if (response.code() != 204) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.baidu.com");
                intent.setData(content_url);
                mContext.startActivity(intent);
            }
        }
    }


    @Override
    public void onResult(int what, int loss, int delay) {
        Log.e("big", "onResult:" + what);
        mView.onInfo(what, loss, delay);
        if (what == INFO_SERVER) {
            if (checkPort()) {
                mView.onInfo(INFO_SERVER_PORT, 0, 0);
                checkInternet();
            } else {
                mView.onError(INFO_SERVER_PORT);
            }
        } else if (what == INFO_INTERNET) {
            if (checkDns()) {
                mView.onInfo(INFO_DNS, 0, 0);
                skipBrowser();
            } else {
                mView.onError(INFO_DNS);
            }
        }
    }

    @Override
    public void onTimeout(int what) {
        mView.onError(what);
    }

    @Override
    public void onException(int what) {
        mView.onError(what);
    }
}

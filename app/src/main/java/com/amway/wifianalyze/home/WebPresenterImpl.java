package com.amway.wifianalyze.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by big on 2018/10/23.
 */

public class WebPresenterImpl extends WebContract.WebPresenter {
    private static final String WEB_URL = "http://www.baidu.com";
    private static final int WEB_DELAY = 15000;
    private WebView mWebView;
    private Context mContext;

    public WebPresenterImpl(WebContract.WebView view) {
        super(view);
    }

    private static final int MSG_WEB_TIMEOUT = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WEB_TIMEOUT:
                    mView.onError(INFO_WEBSITE);
                    break;
            }
        }
    };

    @Override
    public void startCheck(Context context) {
        mContext = context;
    }

    @Override
    public void release() {
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void checkFirewall() {

    }

    @Override
    public void checkFrequence() {

    }

    @Override
    public void checkChannel() {

    }

    @Override
    public void checkBandwidth() {

    }

    @Override
    public void checkLossPacket() {

    }

    @Override
    public boolean checkDns() {
        return false;
    }

    @Override
    public boolean checkWebPort() {
        return false;
    }

    @Override
    public boolean checkWebSite() {
        if (mWebView == null) {
            mWebView = new WebView(mContext);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    mView.onInfo(INFO_WEBSITE);
                }
            });
            mWebView.loadUrl(WEB_URL);
            mHandler.sendEmptyMessageDelayed(MSG_WEB_TIMEOUT, WEB_DELAY);
        }
        return false;
    }

    @Override
    public boolean checFullLoad() {
        return false;
    }

    @Override
    public void networkOff() {

    }
}

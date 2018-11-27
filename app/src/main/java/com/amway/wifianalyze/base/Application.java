package com.amway.wifianalyze.base;

import android.webkit.WebSettings;
import android.webkit.WebView;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

/**
 * Created by big on 2018/11/22.
 */

public class Application extends android.app.Application {
    public static String USER_AGENT;

    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);
        WebView webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        USER_AGENT = settings.getUserAgentString();
    }
}

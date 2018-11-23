package com.amway.wifianalyze.base;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

/**
 * Created by big on 2018/11/22.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);
    }
}

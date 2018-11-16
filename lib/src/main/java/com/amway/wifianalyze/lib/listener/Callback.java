package com.amway.wifianalyze.lib.listener;

/**
 * Created by big on 2018/11/15.
 */

public interface Callback<T> {
    void onCallBack(boolean success, T t);
}

package com.amway.wifianalyze.lib.listener;

import android.util.Log;

/**
 * Created by big on 2018/11/15.
 */

public abstract class BlockCall<T> implements Runnable, Callback<T> {
    private boolean lock = true;

    @Override
    public void run() {
        try {
            synchronized (this) {
                if (lock) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            Log.i("BlockCall", "InterruptedException:" + e);
        }
    }

    @Override
    public void onCallBack(boolean success, T[] t) {
        try {
            synchronized (this) {
                notify();
                lock = false;
            }
        } catch (Exception e) {
            Log.i("BlockCall", "Exception:" + e.getMessage());
        }
    }
}

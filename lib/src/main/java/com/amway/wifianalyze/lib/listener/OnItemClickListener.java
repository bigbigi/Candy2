package com.amway.wifianalyze.lib.listener;

import android.view.View;

/**
 * Created by big on 2018/11/9.
 */

public interface OnItemClickListener<T> {
    void onItemClick(View v, T info, int position);
}

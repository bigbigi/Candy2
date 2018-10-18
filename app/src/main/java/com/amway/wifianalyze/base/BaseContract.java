package com.amway.wifianalyze.base;

import android.view.View;

/**
 * Created by big on 2018/10/17.
 */

public interface BaseContract {
    interface BaseView<T extends BasePresenter> {
        void setPresenter(T presenter);
    }

    interface BasePresenter {

    }
}

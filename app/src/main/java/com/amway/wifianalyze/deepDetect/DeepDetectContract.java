package com.amway.wifianalyze.deepDetect;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/12/29.
 */

public interface DeepDetectContract extends BaseContract {

    interface DeepDetectView extends BaseView {

        void onCheckStart();

        void onCheckStop(int code,int reason);

        void onChecking(int code);

        void onInfo(int code, int loss, int delay);

        void onError(int code, int reason, String... value);
    }

    abstract class DeepDetectPresenter extends BasePresenterImpl<DeepDetectView> {

        public DeepDetectPresenter(DeepDetectView view) {
            super(view);
        }

        abstract void start(Context context, String url);
    }
}

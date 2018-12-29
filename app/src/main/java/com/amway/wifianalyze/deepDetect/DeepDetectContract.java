package com.amway.wifianalyze.deepDetect;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/12/29.
 */

public interface DeepDetectContract extends BaseContract {

    interface DeepDetectView extends BaseView {

    }

    abstract class DeepDetectPresenter extends BasePresenterImpl<DeepDetectView> {

        public DeepDetectPresenter(DeepDetectView view) {
            super(view);
        }
    }
}

package com.amway.wifianalyze.speed;

import android.support.v4.app.FragmentManager;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/10/24.
 */

public interface SpeedContract extends BaseContract {
    interface SpeedView extends BaseView {
        void updateSpeed(String speed);
    }

    abstract class SpeedPresenter extends BasePresenterImpl<SpeedView> {
        public SpeedPresenter(SpeedView view) {
            super(view);
        }

        public abstract void init(FragmentManager fragmentManager);

        public abstract void getSpeed();
    }
}

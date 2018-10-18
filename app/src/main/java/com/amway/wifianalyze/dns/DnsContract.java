package com.amway.wifianalyze.dns;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/10/18.
 */

public interface DnsContract extends BaseContract {

    abstract class DnsPresenter extends BasePresenterImpl<DnsView> {

        public DnsPresenter(DnsView view) {
            super(view);
        }

        abstract void init(Context context);

        abstract void release(Context context);

    }

    interface DnsView extends BaseView<DnsPresenter> {

    }
}

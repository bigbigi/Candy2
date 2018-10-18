package com.amway.wifianalyze.base;

/**
 * Created by big on 2018/10/18.
 */

public class BasePresenterImpl<T extends BaseContract.BaseView> implements BaseContract.BasePresenter {
    protected T mView;

    public BasePresenterImpl(T view) {
        mView = view;
        mView.setPresenter(this);
    }
}

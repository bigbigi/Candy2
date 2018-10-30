package com.amway.wifianalyze.home;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.amway.wifianalyze.R;
import com.autofit.widget.EditText;

/**
 * Created by big on 2018/10/30.
 */

public class TestDialog extends Dialog {
    public TestDialog(@NonNull Context context) {
        super(context);
        init();
    }

    private EditText mName;
    private EditText mPwd;
    private OnStartListener mOnStartListener;

    public void setOnStartListener(OnStartListener listener) {
        mOnStartListener = listener;
    }

    interface OnStartListener {
        void onStart();
    }

    private void init() {
        setContentView(R.layout.dialog_test);
        mName = (EditText) findViewById(R.id.test_name);
        mPwd = (EditText) findViewById(R.id.test_pwd);
        findViewById(R.id.test_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnStartListener != null) {
                    mOnStartListener.onStart();
                }
                start();
                dismiss();
            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void start() {
        mWifiPresenterImpl.init(getContext());
        mWifiPresenterImpl.setTestData(String.valueOf(mName.getText()), String.valueOf(mPwd.getText()));
        mWifiPresenterImpl.scanWifi();
    }

    private WifiPresenterImpl mWifiPresenterImpl;

    public void setPresenter(WifiPresenterImpl wifiPresenter) {
        mWifiPresenterImpl = wifiPresenter;
    }

}


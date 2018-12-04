package com.amway.wifianalyze.home;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.utils.UpdateBiz;
import com.autofit.widget.EditText;
import com.autofit.widget.ProgressBar;

/**
 * Created by big on 2018/10/30.
 */

public class UpdateDialog extends Dialog implements View.OnClickListener {
    public UpdateDialog(@NonNull Context context) {
        super(context);
        init();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.confirm) {
            UpdateBiz.getInstance().download(mProgressBar);
            mConfirmLayout.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            dismiss();
        }
    }


    private ProgressBar mProgressBar;
    private View mCancel;
    private View mConfirmLayout;

    private void init() {
        setContentView(R.layout.dialog_update);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mCancel = findViewById(R.id.cancel);
        mConfirmLayout = findViewById(R.id.confirm_layout);
        mCancel.setOnClickListener(this);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    public void setMust() {
        mCancel.setVisibility(View.GONE);
        setCancelable(false);
    }

}


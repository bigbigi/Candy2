package com.amway.wifianalyze.deepDetect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;

/**
 * Created by big on 2018/12/29.
 */

public class DeepDetectFragment extends BaseFragment implements DeepDetectContract.DeepDetectView {

    public static final String TAG = "DeepDetectFragment";

    public static DeepDetectFragment newInstance(Bundle args) {
        DeepDetectFragment fragment = new DeepDetectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_deep_detect, container, false);
        init(content);
        return content;
    }

    private void init(View content) {
    }

    private BaseContract.BasePresenter mPresenter;

    @Override
    public void setPresenter(BaseContract.BasePresenter presenter) {
        mPresenter = presenter;
    }
}

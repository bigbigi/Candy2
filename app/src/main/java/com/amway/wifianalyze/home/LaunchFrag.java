package com.amway.wifianalyze.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseFragment;

/**
 * Created by big on 2018/10/17.
 */

public class LaunchFrag extends BaseFragment {
    public static final String TAG = "LaunchFrag";

    public static LaunchFrag newInstance(Bundle bundle) {
        LaunchFrag fragment = new LaunchFrag();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_launcher, container, false);
        init(content);
        return content;
    }

    public void init(View content) {

    }
}

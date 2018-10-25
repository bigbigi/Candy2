package com.amway.wifianalyze.speed;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseFragment;

/**
 * Created by big on 2018/10/25.
 */

public class SpeedFrag extends BaseFragment {
    public static SpeedFrag newInstance(Bundle args) {
        SpeedFrag fragment = new SpeedFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_speed, container, false);
        init(content);
        return content;
    }

    public void init(View content) {

    }
}

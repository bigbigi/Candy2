package com.amway.wifianalyze.feedback;

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

public class FeedbackFrag extends BaseFragment {
    public static final String TAG = "FeedbackFrag";

    public static FeedbackFrag newInstance(Bundle args) {
        FeedbackFrag fragment = new FeedbackFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_feedback, container, false);
        init(content);
        return content;
    }

    public void init(View content) {

    }
}

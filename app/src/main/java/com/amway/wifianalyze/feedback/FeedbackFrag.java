package com.amway.wifianalyze.feedback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.amway.wifianalyze.utils.HttpHelper;

/**
 * Created by big on 2018/10/25.
 */

public class FeedbackFrag extends BaseFragment implements View.OnClickListener {
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
        content.findViewById(R.id.feedback_submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
        String phoneNum = NetworkUtils.getPhoneNumber(getContext());
        if (TextUtils.isEmpty(phoneNum)) {//Dialog

        } else {//
            HttpHelper.getInstance(getContext()).post("", "");
        }
    }
}

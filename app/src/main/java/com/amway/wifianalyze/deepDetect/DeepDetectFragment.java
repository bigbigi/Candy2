package com.amway.wifianalyze.deepDetect;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.graphics.drawable.AnimationDrawable;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.home.DetectAdapter;
import com.amway.wifianalyze.home.DetectResult;
import com.amway.wifianalyze.home.HomeBiz;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.autofit.widget.ScreenParameter;

import java.util.ArrayList;
import java.util.List;

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

    private RecyclerView mRecyclerView;
    private RecyclerView mUrlRecycler;
    private DetectAdapter mAdapter;
    private UrlAdapter mUrlAdapter;
    private View mAdviceLayout;
    private View mLoadingLayout;
    private EditText mUrlEditTextView;
    private TextView mAdviceText;
    private TextView mDestinationText;
    private TextView mStartTrriger;

    private void init(View content) {
        mAdviceLayout = content.findViewById(R.id.advice_layout);
        mLoadingLayout = content.findViewById(R.id.deep_loading_layout);
        mUrlEditTextView = (EditText) content.findViewById(R.id.deep_url);
        mAdviceText = (TextView) content.findViewById(R.id.advice);
        mDestinationText = (TextView) content.findViewById(R.id.deep_destination);
        AnimationDrawable drawable = (AnimationDrawable) content.findViewById(R.id.deep_loading).getBackground();
        drawable.start();
        mRecyclerView = (RecyclerView) content.findViewById(R.id.wifiRecycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new DetectAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mStartTrriger = (TextView) content.findViewById(R.id.deep_check);
        mStartTrriger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.start(getContext(), String.valueOf(mUrlEditTextView.getText()));
            }
        });
        mUrlRecycler = (RecyclerView) content.findViewById(R.id.url_recycler);
        mUrlRecycler.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mUrlAdapter = new UrlAdapter();
        mUrlRecycler.setAdapter(mUrlAdapter);
        mUrlRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
                int height = ScreenParameter.getFitWidth(mRecyclerView, 7);
                outRect.set(0, height, 0, height);
            }
        });
        mUrlAdapter.setOnItemClickListener(new UrlAdapter.OnItemClickListener() {
            @Override
            public void onClick(WebInfo info) {
                mUrlEditTextView.setText(info.url);
            }
        });
        List<WebInfo> list = new ArrayList<>();
        list.add(new WebInfo("安利官网", "www.amway.com.cn"));
        list.add(new WebInfo("安利云购", "mall.amway.com.cn"));
        list.add(new WebInfo("安利易联网", "www.amwaynet.com.cn"));
        list.add(new WebInfo("百度", "www.baidu.com"));
        list.add(new WebInfo("淘宝", "www.taobao.com.cn"));
//        list.add(new WebInfo("腾讯", "v.qq.com"));
        mUrlAdapter.setData(list);
    }

    private DeepDetectPresenterImpl mPresenter;

    @Override
    public void setPresenter(BaseContract.BasePresenter presenter) {
        mPresenter = (DeepDetectPresenterImpl) presenter;
    }

    @Override
    public void onCheckStart() {
        mStartTrriger.setText("正在检测");
        mStartTrriger.setEnabled(false);
        mAdapter.getData().clear();
        mAdapter.notifyDataSetChanged();
        mLoadingLayout.setVisibility(View.VISIBLE);
        mAdviceLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCheckStop(final int code, int reason) {
        final String message;
        if (code == 0) {
            message = "您的网络正常";
        } else if (code == Code.INFO_VIDEO &&
                !TextUtils.isEmpty(HomeBiz.getInstance(getContext()).mVideoError)) {
            message = HomeBiz.getInstance(getContext()).mVideoError;
        } else {
            message = Code.getErrorMessage(code, reason);
        }
        ThreadManager.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("big", "code:" + code + ",message:" + message);
                mStartTrriger.setEnabled(true);
                mStartTrriger.setText("开始检测");
                mAdviceText.setText(message);
                mLoadingLayout.setVisibility(View.GONE);
                mAdviceLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onChecking(final int code) {
        final String message = Code.getMessage(code, -1, -1);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    switch (code) {
                        case Code.INFO_PING_AP:
                            mDestinationText.setText("WIFI");
                            break;
                        case Code.INFO_PING_ROUTER:
                            mDestinationText.setText("路由器");
                            break;
                        case Code.INFO_PING_SANGFOR:
                            mDestinationText.setText("深信服");
                            break;
                        case Code.INFO_PING_ISP:
                            mDestinationText.setText("运营商");
                            break;
                        case Code.INFO_PING_WEB:
                            mDestinationText.setText("网站");
                            break;
                    }
                    for (DetectResult result : mAdapter.getData()) {
                        if (result.getCode() == code) {
                            return;
                        }
                    }
                    mAdapter.getData().add(0, new DetectResult(DetectResult.Status.LOADING, code, message));
                    mAdapter.insert();
                }
            });
        }
    }

    @Override
    public void onInfo(final int code, int loss, int delay) {
        final String message = Code.getMessage(code, loss, delay);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    for (int i = 0; i < mAdapter.getData().size(); i++) {
                        DetectResult result = mAdapter.getData().get(i);
                        if (result.getCode() == code) {
                            result.setContent(message);
                            result.setStatus(DetectResult.Status.SUCCESS);
                            DetectAdapter.TextHolder holder = (DetectAdapter.TextHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                            Log.d("big", "holder:" + holder + ",i:" + i + ",size:" + mAdapter.getData().size() + ",name:" + result.getContent());
                            if (holder != null) {
                                mAdapter.onBindViewHolder(holder, i);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onError(final int code, final int reason, String... value) {
        Log.e(TAG, "onError:" + code + ",reason:" + reason);
        final String message = Code.getErrorMessage(code, reason, value);
        HomeBiz.getInstance(getContext()).mErrors.add(code);
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing()) return;
                    for (int i = 0; i < mAdapter.getData().size(); i++) {
                        DetectResult result = mAdapter.getData().get(i);
                        if (result.getCode() == code) {
                            result.setStatus(DetectResult.Status.ERROR);
                            result.setContent(message);
                            DetectAdapter.TextHolder holder = (DetectAdapter.TextHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                            if (holder != null) {
                                mAdapter.onBindViewHolder(holder, i);
                            }
                            break;
                        }
                    }

                }
            });
        }
    }
}

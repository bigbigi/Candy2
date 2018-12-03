package com.amway.wifianalyze.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseAdapter;
import com.amway.wifianalyze.bean.DeviceInfo;
import com.amway.wifianalyze.bean.FaqInfo;
import com.autofit.widget.RecyclerView;
import com.autofit.widget.ScreenParameter;
import com.autofit.widget.TextView;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.List;

/**
 * Created by big on 2018/10/30.
 */

public class FAQDialog extends Dialog {
    private RecyclerView mRecycler;
    private FaqAdapter mAdapter;

    public FAQDialog(@NonNull Context context) {
        super(context, R.style.transparent_dialog);
        init();
    }

    private void init() {
        View contentView = View.inflate(getContext(), R.layout.dialog_faq, null);
        setContentView(contentView, new WindowManager.LayoutParams(-1, -1));
        getWindow().setLayout(-1, -1);
        mRecycler = (RecyclerView) contentView.findViewById(R.id.faq_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FaqAdapter();
        mRecycler.setAdapter(mAdapter);
    }

    public void showData(List<FaqInfo> list) {
        mAdapter.setData(list);
        show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    class FaqHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
        private TextView question;
        private TextView answer;

        public FaqHolder(View itemView) {
            super(itemView);
            question = (TextView) itemView.findViewById(R.id.item_question);
            answer = (TextView) itemView.findViewById(R.id.item_answer);
        }
    }

    class FaqAdapter extends BaseAdapter<FaqInfo, FaqHolder> {

        @Override
        public FaqHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View content = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new FaqHolder(content);
        }

        @Override
        public void onBindViewHolder(FaqHolder holder, int position) {
            if (holder.question != null) {
                FaqInfo info = getData().get(position);
                holder.question.setText(info.question);
                holder.answer.setText(info.answer);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return R.layout.item_faq_title;
            } else {
                return R.layout.item_faq;
            }
        }
    }

}


package com.amway.wifianalyze.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseAdapter;
import com.amway.wifianalyze.bean.FaqInfo;
import com.amway.wifianalyze.lib.ColorPhrase;

/**
 * Created by big on 2018/10/19.
 */

public class AdviceAdapter extends BaseAdapter<FaqInfo, AdviceAdapter.TextHolder> {

    public AdviceAdapter() {
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advice, parent, false);
        return new TextHolder(content);
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        FaqInfo result = mList.get(position);
        holder.text.setText(result.answer);
    }

    public class TextHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public TextHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_text);
            ;
        }
    }
}

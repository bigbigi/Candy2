package com.amway.wifianalyze.home;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseAdapter;

/**
 * Created by big on 2018/10/19.
 */

public class TestAdapter extends BaseAdapter<String, TestAdapter.TextHolder> {


    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_txt, parent, false);
        return new TextHolder(content);
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        holder.text.setText(mList.get(position));
    }


    class TextHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public TextHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_text);
        }
    }
}

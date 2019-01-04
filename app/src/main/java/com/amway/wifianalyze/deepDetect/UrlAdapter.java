package com.amway.wifianalyze.deepDetect;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseAdapter;
import com.amway.wifianalyze.bean.FaqInfo;

/**
 * Created by big on 2018/10/19.
 */

public class UrlAdapter extends BaseAdapter<WebInfo, UrlAdapter.TextHolder> {

    interface OnItemClickListener {
        void onClick(WebInfo info);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_url, parent, false);
        return new TextHolder(content);
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        WebInfo result = mList.get(position);
        holder.text.setText(result.title);
    }

    public class TextHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public TextHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onClick(getData().get(getAdapterPosition()));
                    }
                }
            });

        }
    }
}

package com.amway.wifianalyze.home;

import android.content.Context;
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

public class DetectAdapter extends BaseAdapter<DetectResult, DetectAdapter.TextHolder> {

    private Context mContext;

    public DetectAdapter(Context context) {
        mContext = context;
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detect, parent, false);
        return new TextHolder(content);
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        DetectResult result = mList.get(position);
        holder.text.setText(result.getContent());
        switch (result.getStatus()) {
            case SUCCESS:
                holder.result.setText(mContext.getString(R.string.detect_result_ok));
                holder.result.setSelected(false);
                break;
            case WARN:
                break;
            case ERROR:
                holder.result.setText(mContext.getString(R.string.detect_result_error));
                holder.result.setSelected(true);
                break;
        }
    }


    class TextHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public TextView result;

        public TextHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_text);
            result = (TextView) itemView.findViewById(R.id.item_result);
        }
    }
}

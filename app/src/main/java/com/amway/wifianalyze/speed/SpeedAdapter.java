package com.amway.wifianalyze.speed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseAdapter;
import com.amway.wifianalyze.home.DetectResult;

/**
 * Created by big on 2018/10/19.
 */

public class SpeedAdapter extends BaseAdapter<SpeedResult, SpeedAdapter.TextHolder> {

    private Context mContext;

    public SpeedAdapter(Context context) {
        mContext = context;
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View content = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_speed, parent, false);
        return new TextHolder(content);
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        SpeedResult result = mList.get(position);
        holder.text.setText(result.getName());
        holder.result.setText(result.getResult());
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

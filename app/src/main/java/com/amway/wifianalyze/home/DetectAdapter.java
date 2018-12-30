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
import com.amway.wifianalyze.lib.ColorPhrase;

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
        Log.d("big","result code:"+result.getCode()+",title:"+result.getContent()+",status:"+result.getStatus());
        holder.text.setText(ColorPhrase.from(result.getContent()).withSeparator("{}").
                innerColor(0xfff5000c).outerColor(0xff2b2b2b).format());
        changeStutus(holder, result.getStatus());
        if (result.getStatus() == DetectResult.Status.LOADING) {
            holder.loading.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.INVISIBLE);
            holder.result.setVisibility(View.INVISIBLE);
        } else {
            holder.loading.setVisibility(View.INVISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
            holder.result.setVisibility(View.VISIBLE);
        }
    }

    private void changeStutus(TextHolder holder, DetectResult.Status status) {
        switch (status) {
            case SUCCESS:
                holder.result.setText(mContext.getString(R.string.detect_result_ok));
                holder.result.setSelected(false);
                holder.icon.setImageResource(R.drawable.ic_gz_zc);
                break;
            case WARN:
                break;
            case ERROR:
                holder.result.setText(mContext.getString(R.string.detect_result_error));
                holder.result.setSelected(true);
                holder.icon.setImageResource(R.drawable.ic_gz_gz);
                break;
        }
    }

    private RecyclerView mRecycler;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecycler = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert() {
        mRecycler.scrollToPosition(0);
        notifyItemInserted(0);
    }


    public class TextHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public TextView result;
        public ImageView icon;
        public View loading;

        public TextHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_text);
            result = (TextView) itemView.findViewById(R.id.item_result);
            icon = (ImageView) itemView.findViewById(R.id.item_icon);
            loading = itemView.findViewById(R.id.item_loading);
        }
    }
}

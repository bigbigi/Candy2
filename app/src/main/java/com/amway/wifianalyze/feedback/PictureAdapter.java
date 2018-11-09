package com.amway.wifianalyze.feedback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseAdapter;
import com.amway.wifianalyze.lib.listener.OnItemClickListener;
import com.bumptech.glide.Glide;

/**
 * Created by big on 2018/10/19.
 */

public class PictureAdapter extends BaseAdapter<String, PictureAdapter.TextHolder> {

    private Context mContext;

    public PictureAdapter(Context context) {
        mContext = context;
    }

    @Override
    public TextHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View content = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new TextHolder(content);
    }

    @Override
    public int getItemViewType(int position) {
        if ("add_head".equals(mList.get(position))) {
            return R.layout.item_head;
        } else {
            return R.layout.item_picture;
        }
    }

    @Override
    public void onBindViewHolder(TextHolder holder, int position) {
        String result = mList.get(position);
        if(holder.img!=null){
            Glide.with(mContext).load(result).into(holder.img);
        }
    }

    private OnItemClickListener<String> mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener<String> listener) {
        mOnItemClickListener = listener;
    }

    class TextHolder extends RecyclerView.ViewHolder {
        public ImageView img;

        public TextHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.item_img);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    mOnItemClickListener.onItemClick(v, mList.get(position), position);
                }
            });
        }
    }
}

package com.amway.wifianalyze.base;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by big on 2018/10/19.
 */

public abstract class BaseAdapter<INFO, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<INFO> mList = new ArrayList<>();

    public void setData(List<INFO> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public List<INFO> getData() {
        return mList;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}

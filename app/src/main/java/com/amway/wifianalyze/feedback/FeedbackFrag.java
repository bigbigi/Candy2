package com.amway.wifianalyze.feedback;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.lib.NetworkUtils;
import com.amway.wifianalyze.lib.listener.OnItemClickListener;
import com.amway.wifianalyze.utils.HttpHelper;
import com.autofit.widget.RecyclerView;
import com.autofit.widget.ScreenParameter;

/**
 * Created by big on 2018/10/25.
 */

public class FeedbackFrag extends BaseFragment implements View.OnClickListener,
        OnItemClickListener<String> {
    public static final String TAG = "FeedbackFrag";

    public static FeedbackFrag newInstance(Bundle args) {
        FeedbackFrag fragment = new FeedbackFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.frag_feedback, container, false);
        init(content);
        return content;
    }

    private RecyclerView mRecycler;
    private PictureAdapter mAdapter;

    public void init(View content) {
        content.findViewById(R.id.feedback_submit).setOnClickListener(this);
        mRecycler = (RecyclerView) content.findViewById(R.id.feedback_recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new PictureAdapter(getContext());
        mAdapter.getData().add("add_head");
        mAdapter.setOnItemClickListener(this);
        mRecycler.setAdapter(mAdapter);
        mRecycler.addItemDecoration(new android.support.v7.widget.RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, int itemPosition, android.support.v7.widget.RecyclerView parent) {
                if (itemPosition != 0) {
                    outRect.left = ScreenParameter.getFitWidth(getContext(), 5);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
        String phoneNum = NetworkUtils.getPhoneNumber(getContext());
        if (TextUtils.isEmpty(phoneNum)) {//Dialog
        } else {//
            HttpHelper.getInstance(getContext()).post("", "");
        }

    }

    @Override
    public void onItemClick(View v, String info, int position) {
        if (position == 0) {
            addPicture();
        } else {//todo
            mAdapter.getData().remove(position);
            mAdapter.notifyItemRemoved(position);
        }
    }

    private static final int REQUESTCODE_ALBUM = 2;

    private void addPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUESTCODE_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUESTCODE_ALBUM:
                //获取图片路径
                Uri selectedImage = data.getData();
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getContext().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String photoPath = c.getString(columnIndex);
                c.close();
                Log.d("big", "photoPath:" + photoPath);
                mAdapter.getData().add(1, photoPath);
                mAdapter.notifyItemInserted(1);
                mRecycler.scrollToPosition(0);
                break;
        }
    }


}

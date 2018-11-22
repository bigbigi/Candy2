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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BaseFragment;
import com.amway.wifianalyze.lib.ToastOnPermission;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.listener.OnItemClickListener;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.PermissionUtil;
import com.autofit.widget.RecyclerView;
import com.autofit.widget.ScreenParameter;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * Created by big on 2018/10/25.
 */

public class FeedbackFrag extends BaseFragment implements FeedbackContract.FeedbackView,
        View.OnClickListener,
        OnItemClickListener<String>,
        View.OnLongClickListener,
        View.OnTouchListener {
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
        checkStoragePermisson();
        return content;
    }

    private RecyclerView mRecycler;
    private PictureAdapter mAdapter;
    private TextView mVoiceText;
    private FeedbackContract.FeedbackPresenter mPresenter;
    private View mTimeLayout;
    private TextView mVoiceTime;
    private EditText mContent;

    public void init(View content) {
        content.findViewById(R.id.feedback_submit).setOnClickListener(this);
        content.findViewById(R.id.feedback_voice_retry).setOnClickListener(this);
        mContent = (EditText) content.findViewById(R.id.feedback_content);
        mVoiceText = (TextView) content.findViewById(R.id.feedback_voice);
        mTimeLayout = content.findViewById(R.id.feedback_voice_time_layout);
        mVoiceTime = (TextView) content.findViewById(R.id.feedback_voice_time);
        mVoiceText.setOnLongClickListener(this);
        mVoiceText.setOnTouchListener(this);
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
    public void setPresenter(BaseContract.BasePresenter presenter) {
        mPresenter = (FeedbackContract.FeedbackPresenter) presenter;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.feedback_submit) {
            mPresenter.submit(getContext(), mAdapter.getData(), String.valueOf(mContent.getText()), new Callback() {
                @Override
                public void onCallBack(final boolean success, Object[] t) {
                    ThreadManager.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "提交失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } else if (v.getId() == R.id.feedback_voice_retry) {
            mVoiceText.setEnabled(true);
            mVoiceText.setText(R.string.feedback_voice);
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

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick");
        if (v.getId() == R.id.feedback_voice && !v.isSelected()) {
            mPresenter.startRecord(getActivity());
        }
        return true;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.feedback_voice && event.getAction() == MotionEvent.ACTION_UP) {
            mPresenter.stopRecord();
        }
        return false;
    }

    @Override
    public void onRecordStart() {
        mVoiceText.setText(R.string.feedback_voice_pressed);
        mTimeLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRecordStop() {
        mVoiceText.setText(mVoiceTime.getText());
        mVoiceText.setEnabled(false);
        mTimeLayout.setVisibility(View.GONE);
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

    @Override
    public void updateTime(String s) {
        mVoiceTime.setText(s);
    }

    public void checkStoragePermisson() {
        XXPermissions.with(getActivity()).constantRequest()
                .permission(Permission.Group.STORAGE)
                .request(new ToastOnPermission(getContext(), getString(R.string.permisson_storage)) {
                    @Override
                    public void hasPermission(List<String> list, boolean b) {
                        super.hasPermission(list, b);
                    }
                });
    }
}

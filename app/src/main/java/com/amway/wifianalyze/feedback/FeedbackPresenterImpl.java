package com.amway.wifianalyze.feedback;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;


import java.util.List;

/**
 * Created by big on 2018/11/14.
 */

public class FeedbackPresenterImpl extends FeedbackContract.FeedbackPresenter {


    public FeedbackPresenterImpl(FeedbackContract.FeedbackView view) {
        super(view);
    }

    private int mTime;
    private static final int MSG_UPDATE_TIME = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TIME:
                    mView.updateTime(mTime + "s");
                    sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000);
                    mTime++;
                    break;
            }
        }
    };

    @Override
    public void submit(List<Bitmap> list, String content) {

    }

    @Override
    public void addPicture() {

    }

    @Override
    public void startRecord() {
        mIsRecording = true;
        mTime = 1;
        mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        mView.onRecordStart();
    }

    @Override
    public void stopRecord() {
        if (mIsRecording) {
            mIsRecording = false;
            mHandler.removeMessages(MSG_UPDATE_TIME);
            mTime = 1;
            mView.onRecordStop();
        }
    }
    private boolean mIsRecording = false;

}

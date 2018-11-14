package com.amway.wifianalyze.feedback;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;


import com.amway.wifianalyze.lib.util.FileUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.PermissionUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        if (PermissionUtil.checkPermissions(((Fragment) mView).getActivity(), PermissionUtil.PER_AUDIO, PermissionUtil.PERMISSIONS_AUDIO, PermissionUtil.RESULT_AUDIO)) {
            mIsRecording = true;
            mTime = 1;
            mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            mView.onRecordStart();
            record();
        }

    }

    @Override
    public void stopRecord() {
        if (mIsRecording) {
            mIsRecording = false;
            mHandler.removeMessages(MSG_UPDATE_TIME);
            mTime = 1;
            mView.onRecordStop();
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    private boolean mIsRecording = false;


    private MediaRecorder mRecorder;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
    private String mPath;

    public void record() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/amway/";
        mPath = dir + mFormat.format(new Date(System.currentTimeMillis())) + ".mp4";
        FileUtils.mkdirs(dir);
        Log.d("record", "path:" + mPath);
        FileUtils.clearDir(dir);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //录音文件保存的格式，这里保存为 mp4
        mRecorder.setOutputFile(mPath); // 设置录音文件的保存路径
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        // 设置录音文件的清晰度
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(192000);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e("Feedback", "prepare() failed");
        }
    }
}

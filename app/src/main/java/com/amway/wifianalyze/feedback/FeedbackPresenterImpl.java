package com.amway.wifianalyze.feedback;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;


import com.amway.wifianalyze.R;
import com.amway.wifianalyze.base.Application;
import com.amway.wifianalyze.home.HomeBiz;
import com.amway.wifianalyze.lib.ToastOnPermission;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.FileUtils;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.PermissionUtil;
import com.amway.wifianalyze.utils.Server;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

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

    private final String URL = "%s/checkwifi-api/addNetFeedback.dat";

    @Override
    public void submit(final Context context, final List<String> list, final String content, final Callback callback) {
        HomeBiz.getInstance(context).getShopName(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, String... t) {
                final String shopName = t[1];
                WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wm.getConnectionInfo();
                if (!TextUtils.isEmpty(shopName) && wifiInfo != null) {
                    post(shopName, content, list, context, callback);
                }
            }
        });
    }

    private void post(final String shopName, final String content, final List<String> list, final Context context, final Callback callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                final JSONObject json = new JSONObject();
                try {
                    json.put("content", content);//文字
                    if (list != null && list.size() > 0) {//图片
                        JSONArray imgArray = new JSONArray();
                        for (String path : list) {
                            if (!"add_head".equals(path)) {
                                Log.d("path", "path:" + path);
                                JSONObject imgItem = new JSONObject();
                                imgItem.put("value", Base64.encodeToString(FileUtils.File2byte(path), 0));
                                imgItem.put("postfix", "jpg");
                                imgArray.put(imgItem);
                            }
                        }
                        json.put("imgs", imgArray);
                    }
                    if (!TextUtils.isEmpty(mRecordPath)) {//语音
                        JSONArray recordArray = new JSONArray();
                        JSONObject record = new JSONObject();
                        record.put("value", Base64.encodeToString(FileUtils.File2byte(mRecordPath), 0));
                        record.put("postfix", "wav");
                        recordArray.put(record);
                        json.put("voices", recordArray);
                    }
                    //设备信息
                    WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wm.getConnectionInfo();
                    if (wifiInfo != null) {//todo ssid must be same
                        if (!TextUtils.isEmpty(wifiInfo.getSSID())) {
                            json.put("ssid", wifiInfo.getSSID().replaceAll("\"", ""));
                        }
                        json.put("ip", NetworkUtils.intToIp(wifiInfo.getIpAddress()));
                        json.put("mac", NetworkUtils.getMac(context));
                        json.put("dns", NetworkUtils.getDns1());
                        json.put("phoneType", Build.MODEL);
                        json.put("system", "Android_" + Build.VERSION.SDK_INT);
                        json.put("browser", Application.USER_AGENT);
                        int channel = NetworkUtils.isSupport5G(context) || HomeBiz.getInstance(context).mHas5G ? 2 : 1;
                        json.put("wifiChannel", channel);
                        json.put("shop", shopName);
                        json.put("ap", HomeBiz.getInstance(context).mApName);
                        json.put("processor", "处理人");//todo
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RequestBody body = new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/json; charset=utf-8");
                    }

                    @Override
                    public void writeTo(BufferedSink bufferedSink) throws IOException {
                        bufferedSink.writeUtf8(json.toString());
                    }
                };
                boolean success = HttpHelper.getInstance().post(String.format(URL, Server.HOST), body);
                if (callback != null) {
                    if (success) {
                        callback.onCallBack(true);
                    } else {
                        callback.onCallBack(false);
                    }
                }

            }
        });
    }


    @Override
    public void startRecord(final Activity context) {
        if (XXPermissions.isHasPermission(context, Permission.RECORD_AUDIO)) {
            mIsRecording = true;
            mTime = 1;
            mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            mView.onRecordStart();
            record();
        } else {
            XXPermissions.with(context).constantRequest()
                    .permission(Permission.RECORD_AUDIO)
                    .request(new ToastOnPermission(context, context.getString(R.string.permisson_storage)) {
                        @Override
                        public void hasPermission(List<String> list, boolean b) {
                            super.hasPermission(list, b);

                        }
                    });
        }
    }

    @Override
    public void stopRecord() {
        if (mIsRecording) {
            mIsRecording = false;
            mHandler.removeMessages(MSG_UPDATE_TIME);
            mTime = 1;
            mView.onRecordStop();
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean mIsRecording = false;


    private MediaRecorder mRecorder;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
    private String mRecordPath;

    public void record() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/amway/";
        mRecordPath = dir + mFormat.format(new Date(System.currentTimeMillis())) + ".wav";
        FileUtils.mkdirs(dir);
        Log.d("record", "path:" + mRecordPath);
        FileUtils.clearDir(dir);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //录音文件保存的格式，这里保存为 mp4
        mRecorder.setOutputFile(mRecordPath); // 设置录音文件的保存路径
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

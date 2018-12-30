package com.amway.wifianalyze.deepDetect;

import android.content.Context;
import android.text.TextUtils;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.home.HomeBiz;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.Server;
import com.amway.wifianalyze.utils.TracerouteWithPing;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import com.amway.wifianalyze.lib.util.Utils;

/**
 * Created by big on 2018/12/29.
 */

public class DeepDetectPresenterImpl extends DeepDetectContract.DeepDetectPresenter implements TracerouteWithPing.OnTraceRouteListener {
    private static final String URL = "%s/checkwifi-api/shop/ipNodes_mac_%s.dat";
    private TracerouteWithPing mTraceroute;
    private Context mContext;
    private String mCheckUrl;

    public DeepDetectPresenterImpl(DeepDetectContract.DeepDetectView view) {
        super(view);
    }

    public void start(Context context, String url) {
        mContext = context;
        mCheckUrl = url;
        if (mTraceroute == null) {
            mTraceroute = new TracerouteWithPing(context);
            mTraceroute.setOnTraceRouteListener(this);
        }
        mView.onCheckStart();
        if (!TextUtils.isEmpty(url) && !Utils.isUrl(url)) {
            mView.onCheckStop(Code.INFO_PING_WEB, Code.ERR_MSG);
            return;
        }
        HomeBiz.getInstance(context).getDeepData(new Callback() {
            @Override
            public void onCallBack(boolean success, Object[] t) {
                if (success) {
                    HomeBiz.getInstance(mContext).getVideo(null);
                    pingAp();
                } else {
                    mView.onCheckStop(Code.INFO_PING_AP, Code.ERR_MSG);
                }
            }
        });
    }

    private void pingAp() {
        mView.onChecking(Code.INFO_PING_AP);
        mTraceroute.executeTraceroute(Server.PING_AP, Code.INFO_PING_AP, null);
    }

    private void pingRouter() {
        mView.onChecking(Code.INFO_PING_ROUTER);
        mTraceroute.executeTraceroute(Server.PING_ROUTER, Code.INFO_PING_ROUTER, null);
    }

    private void pingSangfor() {
        mView.onChecking(Code.INFO_PING_SANGFOR);
        mTraceroute.executeTraceroute(Server.PING_SANGFOR, Code.INFO_PING_SANGFOR, null);
    }

    private void pingIsp() {
        mView.onChecking(Code.INFO_PING_ISP);
        mTraceroute.executeTraceroute(Server.PING_ISP, Code.INFO_PING_ISP, null);
    }

    private void pingWeb() {
        if (!TextUtils.isEmpty(mCheckUrl)) {
            mView.onChecking(Code.INFO_PING_WEB);
            final long startTime = System.currentTimeMillis();
            Response response = HttpHelper.getInstance().getResponse(mCheckUrl);
            if (response != null && response.isSuccessful()) {
                int delay = (int) (System.currentTimeMillis() - startTime);
                onResult(Code.INFO_PING_WEB, 0, delay);
            } else {
                mView.onError(Code.INFO_PING_WEB, Code.ERR_NONE);
            }
        } else {
            mView.onCheckStop(0, 0);
        }
    }

    public void checkVideoWeb() {
        if (!TextUtils.isEmpty(Server.PING_SANGFOR) && !TextUtils.isEmpty(mCheckUrl)) {
            mView.onChecking(Code.INFO_VIDEO);
            ThreadManager.execute(new Runnable() {
                @Override
                public void run() {
                    final JSONObject object = new JSONObject();
                    try {
                        object.put("httpUrl", "www.baofeng.com");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestBody body = new RequestBody() {
                        @Override
                        public MediaType contentType() {
                            return MediaType.parse("application/json; charset=utf-8");
                        }

                        @Override
                        public void writeTo(BufferedSink bufferedSink) throws IOException {
                            bufferedSink.writeUtf8(object.toString());
                        }
                    };
                    String ret = HttpHelper.getInstance().postResponse("http://10.0.0.12:8096/checkwifi-api/checkUrl.dat", body);
                    if (!TextUtils.isEmpty(ret)) {
                        try {
                            JSONObject obj = new JSONObject(ret);
                            JSONObject data = obj.getJSONObject("data");
                            if (data.optBoolean("isSPUrl")) {
                                mView.onError(Code.INFO_VIDEO, Code.ERR_NONE);
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mView.onInfo(Code.INFO_VIDEO, 0, 0);
                }
            });
        }
        mView.onCheckStop(0, 0);
    }

//    private void onStop() {
//        mView.onCheckStop();
//    }

    @Override
    public void onResult(int what, int loss, int delay) {
        mView.onInfo(what, loss, delay);
        switch (what) {
            case Code.INFO_PING_AP:
                pingRouter();
                break;
            case Code.INFO_PING_ROUTER:
                pingSangfor();
                break;
            case Code.INFO_PING_SANGFOR:
                pingIsp();
                break;
            case Code.INFO_PING_ISP:
                pingWeb();
                break;
            case Code.INFO_PING_WEB:
                checkVideoWeb();
                break;
        }
    }

    @Override
    public void onTimeout(int what) {
        onException(what);
    }

    @Override
    public void onException(int what) {
        mView.onError(what, Code.ERR_NONE);
        mView.onCheckStop(what, Code.ERR_MSG);
        //todo test
//        onResult(what, 0, 0);
        //todo test

    }
}

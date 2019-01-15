package com.amway.wifianalyze.deepDetect;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.home.HomeBiz;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
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
    private static final String VIDEO_URL = "%s/checkwifi-api/checkUrl.dat";
    private static final String FIREWALL_URL = "%s/checkwifi-api/shop/filterFirewall.dat?addr=%s";

    private TracerouteWithPing mTraceroute;
    private Context mContext;
    private String mCheckUrl;

    public DeepDetectPresenterImpl(DeepDetectContract.DeepDetectView view) {
        super(view);
    }

    public void start(Context context, String url) {
        mContext = context;
        if (mTraceroute == null) {
            mTraceroute = new TracerouteWithPing(context);
            mTraceroute.setOnTraceRouteListener(this);
        }
        mView.onCheckStart();
        if (!TextUtils.isEmpty(url)) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            if (!Utils.isUrl(url)) {
                mView.onCheckStop(Code.INFO_LOAD_WEB, Code.ERR_MSG);
                return;
            }
        }
        mCheckUrl = url;
        Log.d("big", "url:" + url);
        HomeBiz.getInstance(context).getDeepData(new Callback() {
            @Override
            public void onCallBack(boolean success, Object[] t) {
                if (success) {
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
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mView.onChecking(Code.INFO_PING_ROUTER);
        mTraceroute.executeTraceroute(NetworkUtils.intToIp(wifiManager.getDhcpInfo().gateway), Code.INFO_PING_ROUTER, null);
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
            Uri uri = Uri.parse(mCheckUrl);
            mTraceroute.executeTraceroute(uri.getHost(), Code.INFO_PING_WEB, null);
        } else {
            mView.onCheckStop(Code.INFO_DEEP_SUCCESS, 0);
        }
    }

    private void loadWeb() {
        if (!TextUtils.isEmpty(mCheckUrl)) {
            mView.onChecking(Code.INFO_LOAD_WEB);
            final long startTime = System.currentTimeMillis();
            Response response = HttpHelper.getInstance().getResponse(mCheckUrl);
            if (response != null && response.isSuccessful()) {
                int delay = (int) (System.currentTimeMillis() - startTime);
                onResult(Code.INFO_LOAD_WEB, 0, delay);
            } else {
                mView.onError(Code.INFO_LOAD_WEB, Code.ERR_NONE);
                mView.onCheckStop(Code.INFO_LOAD_WEB, Code.ERR_WEB_NORESPONSE);
            }
        } else {
            mView.onCheckStop(Code.INFO_DEEP_SUCCESS, 0);
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
                        object.put("httpUrl", mCheckUrl);
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
                    String ret = HttpHelper.getInstance().postResponse(String.format(VIDEO_URL, Server.HOST), body);
                    if (!TextUtils.isEmpty(ret)) {
                        try {
                            JSONObject obj = new JSONObject(ret);
                            JSONObject data = obj.getJSONObject("data");
                            if (data.optBoolean("isSPUrl") && checkFireWall()) {
                                onException(Code.INFO_VIDEO);
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
        mView.onCheckStop(Code.INFO_DEEP_SUCCESS, 0);

    }

    private boolean checkFireWall() {
        boolean block = false;
        String ret = HttpHelper.getInstance().get(String.format(FIREWALL_URL, Server.HOST, mCheckUrl));
        if (!TextUtils.isEmpty(ret)) {
            try {
                JSONObject json = new JSONObject(ret);
                if (json.optInt("code") == 100) {
                    JSONObject data = json.getJSONObject("data");
                    block = data.optBoolean(mCheckUrl);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return block;
    }


    @Override
    public void onResult(int what, int loss, int delay) {
        if (what != Code.INFO_LOAD_WEB && (loss >= 100 || delay > 100)) {
            onException(what);
            return;
        }
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
                loadWeb();
                break;
            case Code.INFO_LOAD_WEB:
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
    }
}

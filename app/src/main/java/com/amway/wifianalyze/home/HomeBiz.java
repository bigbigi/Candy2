package com.amway.wifianalyze.home;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.amway.wifianalyze.base.Application;
import com.amway.wifianalyze.bean.DeviceInfo;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by big on 2018/11/21.
 */

public class HomeBiz {
    private static final String SHOP_URL = "%s/checkwifi-api/shop/getShopInfo_mac_%s_ip_.dat";
    private static final String LOCALNET_URL = "%s/checkwifi-api/shop/getCisco2901Load_mac_%s_ip_.dat";
    private static final String INTERNET_URL = "%s/checkwifi-api/shop/getSangforLoad_mac_%s_ip_.dat";
    private static final String SUBMIT_URL = "%s/checkwifi-api/addUserAutoSubmit.dat";
    private static final String UTILIZE_URL = "%s/checkwifi-api/shop/getApInfo_mac_%s_ip_.dat";
    private static final String AUTH_URL = "%s/checkwifi-api/checkLogin/mac_%s.dat";

    private static volatile HomeBiz mInstance;
    private Context mContext;

    public synchronized static HomeBiz getInstance(Context context) {
        if (mInstance == null) {
            synchronized (HomeBiz.class) {
                if (mInstance == null) {
                    mInstance = new HomeBiz(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    public String mShopName;
    public String mApName;
    public String mUserCount;
    public int mFrequence;
    public boolean mHas5G;
    public DeviceInfo mDevicesInfo;


    public HomeBiz(Context context) {
        mContext = context;
    }


    //获取门店信息
    public void getShopName(final Callback<String> callback) {
        Log.d("big", "getShopName:" + mShopName + ",ap:" + mApName);
        if (!TextUtils.isEmpty(mShopName) && !TextUtils.isEmpty(mApName)) {
            if (callback != null) {
                callback.onCallBack(true, mApName, mShopName, mUserCount);
            }
        } else {
            ThreadManager.execute(new Runnable() {
                @Override
                public void run() {
                    boolean success = false;
                    String result = HttpHelper.getInstance().get(String.format(SHOP_URL, Server.HOST,
                           /*NetworkUtils.getMac(mContext)*/"f0:99:bf:df:5e:64"));//todo
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            JSONObject data = obj.getJSONObject("data");
                            success = 100 == obj.getInt("code");
                            mApName = data.optString("apName");
                            mShopName = data.optString("shopName");
                            mUserCount = data.optString("users");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (callback != null) {
                        callback.onCallBack(success, mApName, mShopName, mUserCount);
                    }
                }
            });
        }
    }

    //检测专线满载
    private void checkNetLoad(final String url, final Callback<Boolean> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                boolean input = false;
                boolean output = false;
                String result = HttpHelper.getInstance().get(String.format(url, Server.HOST,
                        /*NetworkUtils.getMac(mContext)*/"f0:99:bf:df:5e:64"));//todo
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject data = obj.getJSONObject("data");
                        input = data.optBoolean("input");
                        output = data.optBoolean("output");
                        success = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (callback != null) {
                    callback.onCallBack(success, input, output);
                }
            }
        });
    }

    public void checkLocalnetLoad(final Callback<Boolean> callback) {
        checkNetLoad(LOCALNET_URL, callback);
    }

    public void checkInternetLoad(final Callback<Boolean> callback) {
        checkNetLoad(INTERNET_URL, callback);
    }

    public void submitDetectResult(final Callback callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                final JSONObject json;
                if (mDevicesInfo != null) {
                    json = mDevicesInfo.toJson();
                } else {
                    DeviceInfo info = new DeviceInfo();
                    WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wm.getConnectionInfo();
                    if (!TextUtils.isEmpty(wifiInfo.getSSID())) {
                        info.ssid = wifiInfo.getSSID().replaceAll("\"", "");
                    }
                    info.ap = mApName;
                    info.browser = Application.USER_AGENT;
                    info.wifiChannel = NetworkUtils.isSupport5G(mContext) || mHas5G ? 2 : 1;
                    info.ip = NetworkUtils.intToIp(wifiInfo.getIpAddress());
                    info.mac = NetworkUtils.getMac(mContext);
                    info.dns = NetworkUtils.getDns1();
                    info.phoneType = Build.MODEL;
                    info.system = "Android_" + Build.VERSION.SDK_INT;
                    json = info.toJson();
                }
                Log.d("big", "detect submit:" + json);
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
                boolean success = HttpHelper.getInstance().post(String.format(SUBMIT_URL, Server.HOST), body);
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });

    }

    public void getUtilization(final Callback<Integer> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                int utilization = 0;
                boolean success = false;
                String result = HttpHelper.getInstance().get(String.format(UTILIZE_URL, Server.HOST,
                        "48:43:7c:bd:37:e0"/*NetworkUtils.getMac(mContext)*/));//todo change mac
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject json = new JSONObject(result);
                        JSONObject data = json.getJSONObject("data");
                        int channel = data.getInt("wifi");
                        if (channel == 2) {
                            utilization = data.optInt("utilization_5g");
                        } else {
                            utilization = data.optInt("utilization_24g");
                        }
                        success = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.onCallBack(success, utilization);
                }
            }
        });
    }

    public void getAuth(final Callback<Integer> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                int code = 0;
                int authType = 0;
                String result = HttpHelper.getInstance().get(String.format(AUTH_URL, Server.HOST,
                        "10.0.0.4"/*NetworkUtils.getMac(mContext)*/));//todo change mac
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject json = new JSONObject(result);
                        code = json.getInt("code");
                        JSONObject info = json.getJSONObject("info");
                        authType = info.getInt("auth");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.onCallBack(code == 100, authType);
                }
            }
        });

    }

    public int getFrequence() {
        return mFrequence;
    }

    public void setFrequence(int frequence) {
        this.mFrequence = frequence;
    }

}

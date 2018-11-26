package com.amway.wifianalyze.home;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.Server;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by big on 2018/11/21.
 */

public class HomeBiz {
    private static final String SHOP_URL = "%s/checkwifi-api/shop/getShopInfo_mac_%s_ip_.dat";
    private static final String LOCALNET_URL = "%s/checkwifi-api/shop/getCisco2901Load_mac_%s_ip_.dat";
    private static final String INTERNET_URL = "%s/checkwifi-api/shop/getSangforLoad_mac_%s_ip_.dat";

    private static volatile HomeBiz mInstance;

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

    private String mShopName;
    private String mApName;
    private Context mContext;
    private int mFrequence;
    private String mUserCount;

    public HomeBiz(Context context) {
        mContext = context;
    }


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
                    String result = HttpHelper.getInstance().get(String.format(SHOP_URL, Server.HOST,
                           /*NetworkUtils.getMac(mContext)*/"f0:99:bf:df:5e:64"));//todo
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            JSONObject data = obj.getJSONObject("data");
                            mApName = data.optString("apName");
                            mShopName = data.optString("shopName");
                            mUserCount = data.optString("users");
                            if (callback != null) {
                                callback.onCallBack(true, mApName, mShopName, mUserCount);
                            }
                            return;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (callback != null) {
                        callback.onCallBack(false);
                    }
                }
            });
        }
    }

    private void checkNetLoad(final String url, final Callback<Boolean> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                String result = HttpHelper.getInstance().get(String.format(url, Server.HOST,
                        /*NetworkUtils.getMac(mContext)*/"f0:99:bf:df:5e:64"));//todo
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject data = obj.getJSONObject("data");
                        boolean input = data.optBoolean("input");
                        boolean output = data.optBoolean("output");
                        if (callback != null) {
                            callback.onCallBack(true, input, output);
                        }
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                callback.onCallBack(false);
            }
        });
    }

    public void checkLocalnetLoad(final Callback<Boolean> callback) {
        checkNetLoad(LOCALNET_URL, callback);
    }

    public void checkInternetLoad(final Callback<Boolean> callback) {
        checkNetLoad(INTERNET_URL, callback);
    }

    public int getFrequence() {
        return mFrequence;
    }

    public void setFrequence(int frequence) {
        this.mFrequence = frequence;
    }

    public String getUserCount() {
        return mUserCount;
    }
}

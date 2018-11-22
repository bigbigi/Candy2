package com.amway.wifianalyze.home;

import android.content.Context;
import android.text.TextUtils;

import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by big on 2018/11/21.
 */

public class HomeBiz {
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

    public HomeBiz(Context context) {
        mContext = context;
    }

    private static final String SERVER = "http://10.0.0.171";
    private static final String SHOP_URL = "%s/checkwifi-api/shop/getShopInfo_%s.dat";

    public void getShopName(final Callback<String> callback) {
        if (!TextUtils.isEmpty(mShopName) && !TextUtils.isEmpty(mApName)) {
            if (callback != null) {
                callback.onCallBack(true, mApName, mShopName);
            }
        } else {
            ThreadManager.execute(new Runnable() {
                @Override
                public void run() {
                    String result = HttpHelper.getInstance().get(String.format(SHOP_URL, SERVER,
                            NetworkUtils.getMac(mContext)));
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            JSONObject data = obj.getJSONObject("data");
                            mApName = data.optString("apName");
                            mShopName = data.optString("shopName");
                            if (callback != null) {
                                callback.onCallBack(true, mApName, mShopName);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}

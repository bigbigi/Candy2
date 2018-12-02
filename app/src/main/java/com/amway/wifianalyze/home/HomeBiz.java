package com.amway.wifianalyze.home;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.bean.DeviceInfo;
import com.amway.wifianalyze.bean.FaqInfo;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.lib.util.Utils;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by big on 2018/11/21.
 */

public class HomeBiz {
    private static final String SUBMIT_URL = "%s/checkwifi-api/addUserAutoSubmit.dat";
    private static final String FIREWALL_URL = "%s/checkwifi-api/shop/filterFirewall.dat";
    private static final String SHOP_URL = "%s/checkwifi-api/shop/getShopInfo_mac_%s_ip_.dat";
    private static final String LOCALNET_URL = "%s/checkwifi-api/shop/getCisco2901Load_mac_%s_ip_.dat";
    private static final String INTERNET_URL = "%s/checkwifi-api/shop/getSangforLoad_mac_%s_ip_.dat";
    private static final String UTILIZE_URL = "%s/checkwifi-api/shop/getApInfo_mac_%s_ip_.dat";
    private static final String SYS_URL = "%s/checkwifi-api/shop/getSysConfig_ping_domain,auth_domain,download_domain,firewall_domain,order_domain,upload_domain,auth_port,order_port.dat";

    private static final String AUTH_URL = "%s/checkwifi-api/auth/checkLogin/mac_%s.dat";
    private static final String NETWORK_ACCESS_URL = "%s/checkwifi-api/auth/disconnect/mac_%s.dat";
    private static final String FAQ_URL = "%s/checkwifi-api/getFaqByCode/code_%s.dat";


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

    private DeviceInfo mDeviceInfo;
    public String mRouterIp;
    public String mTaobaoIp;
    public float mDownloadSpeed;
    public float mUploadSpeed;
    public boolean mHas5G;
    public String mCount;
    public ScanResult mScanResult;
    public Fragment mCurrentFrag;
    public String mTempInputUse;
    public String mTempoutputUse;
    public ArrayList<Integer> mErrors = new ArrayList<>();


    public HomeBiz(Context context) {
        mContext = context;
    }

    public void getSysconfig(final Callback callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                String ret = HttpHelper.getInstance().get(String.format(SYS_URL, Server.HOST));
                if (!TextUtils.isEmpty(ret)) {
                    try {
                        JSONObject json = new JSONObject(ret);
                        if (100 == json.optInt("code")) {
                            JSONObject data = json.getJSONObject("data");
                            if (!TextUtils.isEmpty(data.optString("order_domain"))) {
                                Server.ORDER_SERVER = data.optString("order_domain");
                            }
                          /*  if (!TextUtils.isEmpty(data.optString("upload_domain"))) {
                                Server.UPLOAD_SERVER = data.optString("upload_domain");
                            }
                            if (!TextUtils.isEmpty(data.optString("download_domain"))) {
                                Server.DOWNLOAD_SERVER = data.optString("download_domain");
                            }*/
                            if (!TextUtils.isEmpty(data.optString("auth_domain"))) {
                                Server.AUTH_SERVER = data.optString("auth_domain");
                            }
                            if (!TextUtils.isEmpty(data.optString("ping_domain"))) {
                                Server.INTERNET = data.optString("ping_domain").split(",")[0];
                            }
                            if (!TextUtils.isEmpty(data.optString("auth_port"))) {
                                Server.AUTH_PORT = Utils.parseInt(data.optString("auth_port"));
                            }
                            if (!TextUtils.isEmpty(data.optString("order_port"))) {
                                Server.ORDER_PORT = Utils.parseInt(data.optString("order_port"));
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }


    public void getFilewall(final Callback<Boolean> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                String ret = HttpHelper.getInstance().get(String.format(FIREWALL_URL, Server.HOST));
                boolean success = false;
                boolean filter = false;
                if (!TextUtils.isEmpty(ret)) {
                    try {
                        JSONObject json = new JSONObject(ret);
                        if (json.optInt("code") == 100) {
                            JSONObject data = json.getJSONObject("data");
                            filter = data.optBoolean("filter");
                            success = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.onCallBack(success, filter);
                }
            }
        });
    }
    private Object mLock=new Object();
    //获取门店信息
    public void getShopName(final Callback<String> callback) {
        Log.d("big", "getShopName:");
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (mLock) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mDeviceInfo == null) {
                        mDeviceInfo = createDeviceInfo();
                    }
                    boolean success = false;
                    String result = HttpHelper.getInstance().get(String.format(SHOP_URL, Server.HOST, mDeviceInfo.mac));//todo
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            JSONObject data = obj.getJSONObject("data");
                            success = 100 == obj.getInt("code");
                            mCount = data.optString("users");
                            mDeviceInfo.ap = data.optString("apName");
                            mDeviceInfo.shop = data.optString("shopName");
                            mRouterIp = data.optString("sangfor_outer");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (callback != null) {
                        callback.onCallBack(success, mDeviceInfo.ap, mDeviceInfo.shop, mCount);
                    }
                }

            }
        });
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
                        mDeviceInfo.mac));//todo
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONObject data = obj.getJSONObject("data");
                        input = data.optBoolean("input");
                        output = data.optBoolean("output");
                        mTempInputUse=float2Int(data.optString("input_use"));
                        mTempoutputUse=float2Int(data.optString("output_use"));
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

    private String float2Int(String f){
        float temp=Utils.parseFloat(f);
        if(temp==-1){
          return "-1";
        }else{
            return temp*100+"";
        }
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
                if (mDeviceInfo != null) {
                    json = mDeviceInfo.toJson();
                } else {
                    json = createDeviceInfo().toJson();
                }
                try {
                    json.put("downSpeed", mDownloadSpeed);
                    json.put("uploadSpeed", mUploadSpeed);
                    JSONArray errorArray = new JSONArray();
                    for (Integer code : mErrors) {
                        errorArray.put(code);
                    }
                    json.put("nfCode", errorArray);
                } catch (JSONException e) {
                    e.printStackTrace();
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
                        mDeviceInfo.mac));//todo change mac
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
                        mDeviceInfo.mac));//todo change mac
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

    public void checkCustomPick(final Callback<String> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                int cost = 0;
                while (TextUtils.isEmpty(mTaobaoIp) && count++ < 3) {
                    long startTime = System.currentTimeMillis();
                    String result = HttpHelper.getInstance().getChome(Server.MY_IP);
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject json = new JSONObject(result);
                            JSONObject data = json.getJSONObject("data");
                            mTaobaoIp = data.getString("ip");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    cost = (int) (System.currentTimeMillis() - startTime);
                }

                if (callback != null) {
                    callback.onCallBack(!TextUtils.isEmpty(mTaobaoIp), mTaobaoIp, String.valueOf(cost));
                }
            }
        });
    }

    public void checkNetworAccess(final Callback<Boolean> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                boolean access = true;
                String result = HttpHelper.getInstance().get(String.format(NETWORK_ACCESS_URL, Server.HOST,
                        mDeviceInfo.mac));//todo test mac
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONObject json = new JSONObject(result);
                        if (100 == json.optInt("code")) {
                            JSONObject data = json.getJSONObject("data");
                            access = data.optBoolean("isFollow") || !data.optBoolean("wxAuth");
                            success = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) {
                    callback.onCallBack(success, access);
                }
            }
        });
    }

    public void getFaq(final Callback<List<FaqInfo>> callback) {
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                if (mErrors.size() > 0) {
                    String value = "";
                    for (Integer code : mErrors) {
                        value = value + code + "_";
                    }
                    String ret = HttpHelper.getInstance().get(String.format(FAQ_URL, Server.HOST, value));
                    if (!TextUtils.isEmpty(ret)) {
                        try {
                            JSONObject obj = new JSONObject(ret);
                            if (100 == obj.getInt("code")) {
                                List<FaqInfo> list = new ArrayList<>();
                                list.add(new FaqInfo());
                                JSONArray data = obj.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    try {
                                        JSONObject json = data.getJSONObject(i);
                                        FaqInfo faqInfo = new FaqInfo();
                                        faqInfo.question = "故障：" + Code.getMessage(Utils.parseInt(json.optString("code")), -1, 0);
                                        faqInfo.answer = "方法：" + json.optString("content");
                                        list.add(faqInfo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (callback != null) {
                                    callback.onCallBack(true, list);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        });
    }

    //54:33:cb:66:b4:1f
//    ac:cf:5c:18:ff:d5
    public DeviceInfo createDeviceInfo() {
        DeviceInfo info = new DeviceInfo();
        WifiManager wm = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID())) {
            info.ssid = wifiInfo.getSSID().replaceAll("\"", "");
            info.ip = NetworkUtils.intToIp(wifiInfo.getIpAddress());
        }
        info.wifiChannel = NetworkUtils.isSupport5G(mContext) || mHas5G ? 2 : 1;
        info.mac = NetworkUtils.getMac(mContext);//todo test mac
        info.dns = NetworkUtils.getDns1();
        info.phoneType = Build.MODEL;
        info.system = "Android_" + Build.VERSION.SDK_INT;
        return info;
    }

    public void setDeviceInfo(DeviceInfo mDeviceInfo) {
        this.mDeviceInfo = mDeviceInfo;
    }

    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    public void reset() {
        mErrors.clear();
        mTaobaoIp = "";
    }
}

package com.amway.wifianalyze.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.HttpHelper;
import com.amway.wifianalyze.utils.TracerouteWithPing;

import okhttp3.Response;

/**
 * Created by big on 2018/10/22.
 */

public class AuthPresenterImpl extends AuthContract.AuthPresenter implements TracerouteWithPing.OnTraceRouteListener {
    private static final String TAG = "AuthPresenterImpl";

    private static final String INTERNET = "www.baidu.com";
    private static final String SERVER_URL = "www.baidu.com";//todo 认证服务器地址
    private static final String AUTO_SERVER = "http://www.baidu.com/generate_204 ";//todo 认证204返回
    private TracerouteWithPing mTraceroute;
    private Context mContext;

    public AuthPresenterImpl(AuthContract.AuthView view) {
        super(view);
    }

    @Override
    public void startCheck(Context context) {
        mContext = context;
        if (mTraceroute == null) {
            mTraceroute = new TracerouteWithPing(context);
            mTraceroute.setOnTraceRouteListener(this);
        }
        //获取ap人数--信道利用率-静态ip-内网满载-外网满载-dns-ping服务器-服务器端口-认证-ping外网
        ThreadManager.execute(new Runnable() {
            @Override
            public void run() {
                checkUtilization(new Callback<Integer>() {
                    @Override
                    public void onCallBack(boolean success, Integer... t) {
                        checkDhcp(new Callback() {
                            @Override
                            public void onCallBack(boolean success, Object[] t) {
                                checkLocalnet(new Callback() {
                                    @Override
                                    public void onCallBack(boolean success, Object[] t) {
                                        checkInternet(new Callback() {
                                            @Override
                                            public void onCallBack(boolean success, Object[] t) {
                                                checkDns(new Callback() {
                                                    @Override
                                                    public void onCallBack(boolean success, Object[] t) {
                                                        checkServer(new Callback() {
                                                            @Override
                                                            public void onCallBack(boolean success, Object[] t) {
                                                                checkPort(new Callback() {
                                                                    @Override
                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                        skipBrowser(new Callback() {
                                                                            @Override
                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                pingInternet(new Callback() {
                                                                                    @Override
                                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                                        mView.onStopCheck();
                                                                                        HomeBiz.getInstance(mContext).submitDetectResult(null);
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void checkUtilization(final Callback<Integer> callback) {
        mView.onChecking(Code.INFO_UTILIZATION);
        HomeBiz.getInstance(mContext).getUtilization(new Callback<Integer>() {
            @Override
            public void onCallBack(boolean success, Integer... t) {
                if (success) {
                    mView.onInfo(Code.INFO_UTILIZATION, t[0], 0);
                    if (t[0] >= 80) {
                        mView.onError(Code.INFO_UTILIZATION, Code.ERR_NONE);
                    }
                } else {
                    mView.onError(Code.INFO_UTILIZATION, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(true);
                }
            }
        });
    }

    @Override
    public void checkDhcp(Callback callback) {
        mView.onChecking(Code.INFO_STATIC_IP);
        boolean staticIp = NetworkUtils.isStaticIp(mContext);
        if (staticIp) {
            mView.onError(Code.INFO_STATIC_IP, Code.ERR_NONE);
        } else {
            mView.onInfo(Code.INFO_STATIC_IP, 0, 0);
        }
        if (callback != null) {
            callback.onCallBack(!staticIp);
        }
    }

    @Override
    public void checkPort(Callback callback) {
        boolean success = false;
        mView.onChecking(Code.INFO_SERVER_PORT);
        if (NetworkUtils.telnet(SERVER_URL, 80)) {
            mView.onInfo(Code.INFO_SERVER_PORT, 0, 0);
            success = true;
        } else {
            mView.onError(Code.INFO_SERVER_PORT, Code.ERR_NONE);
        }
        if (callback != null) {
            callback.onCallBack(success);
        }
    }

    public void pingInternet(Callback callback) {
        mView.onChecking(Code.INFO_PING_INTERNET);
        mTraceroute.executeTraceroute(INTERNET, Code.INFO_PING_INTERNET, callback);
    }

    @Override
    public void checkServer(Callback callback) {
        mView.onChecking(Code.INFO_SERVER);
        mTraceroute.executeTraceroute(SERVER_URL, Code.INFO_SERVER, callback);
    }

    @Override
    public void checkLocalnet(final Callback callback) {
        mView.onChecking(Code.INFO_LOCALNET);
        HomeBiz.getInstance(mContext).checkLocalnetLoad(new Callback<Boolean>() {
            @Override
            public void onCallBack(boolean success, Boolean... t) {
                if (success) {
                    boolean input = t[0];
                    boolean output = t[1];
                    if (!input && !output) {
                        mView.onInfo(Code.INFO_LOCALNET, 0, 0);
                    } else {
                        mView.onError(Code.INFO_LOCALNET, input ? Code.ERR_INTERNET_INPUT : Code.ERR_INTERNET_OUTPUT);
                    }
                } else {
                    mView.onError(Code.INFO_LOCALNET, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }


    @Override
    public void checkInternet(final Callback callback) {
        mView.onChecking(Code.INFO_INTERNET);
        HomeBiz.getInstance(mContext).checkInternetLoad(new Callback<Boolean>() {
            @Override
            public void onCallBack(boolean success, Boolean... t) {
                if (success) {
                    boolean input = t[0];
                    boolean output = t[1];
                    if (!input && !output) {
                        mView.onInfo(Code.INFO_INTERNET, 0, 0);
                    } else {
                        mView.onError(Code.INFO_INTERNET, input ? Code.ERR_INTERNET_INPUT : Code.ERR_INTERNET_OUTPUT);
                    }
                } else {
                    mView.onError(Code.INFO_INTERNET, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }

    @Override
    public void checkDns(Callback callback) {
        mView.onChecking(Code.INFO_DNS);
        boolean success = false;
        if (!TextUtils.isEmpty(NetworkUtils.getIp(SERVER_URL))) {
            mView.onInfo(Code.INFO_DNS, 0, 0);
            success = true;
        } else {
            mView.onError(Code.INFO_DNS, Code.ERR_NONE);
        }
        if (callback != null) {
            callback.onCallBack(success);
        }
    }

    @Override
    public void skipBrowser(Callback callback) {
        boolean success = false;
        mView.onChecking(Code.INFO_SKIP);
        Response response = HttpHelper.getInstance().getResponse(AUTO_SERVER);
        if (response != null) {
            Log.d(TAG, "skipBrowser:" + response.code());
            if (response.code() != 204) {
                Intent intent = new Intent();
                intent.setPackage("com.android.browser");
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://www.baidu.com");
                intent.setData(content_url);
//                mContext.startActivity(intent);
                mView.onError(Code.INFO_SKIP, Code.ERR_NONE);
            } else {
                mView.onInfo(Code.INFO_SKIP, 0, 0);
            }
            success = true;
            response.close();
        } else {
            mView.onError(Code.INFO_SKIP, Code.ERR_NONE);
        }
        if (callback != null) {
            callback.onCallBack(success);
        }
    }


    @Override
    public void onResult(int what, int loss, int delay) {
        Log.e("big", "onResult:" + what);
        mView.onInfo(what, loss, delay);
    }

    @Override
    public void onTimeout(int what) {
        Log.e(TAG, "onTimeout:" + what);
        mView.onError(what, Code.ERR_NONE);
    }

    @Override
    public void onException(int what) {
        Log.e(TAG, "onException:" + what);
        mView.onError(what, Code.ERR_NONE);
    }
}

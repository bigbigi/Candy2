package com.amway.wifianalyze.home;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.utils.Server;
import com.amway.wifianalyze.utils.TracerouteWithPing;

/**
 * Created by big on 2018/10/22.
 */

public class AuthPresenterImpl extends AuthContract.AuthPresenter implements TracerouteWithPing.OnTraceRouteListener {
    private static final String TAG = "AuthPresenterImpl";

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
                                                                        checkAuth(new Callback() {
                                                                            @Override
                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                checkFilewall(new Callback() {
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

    public void checkFilewall(final Callback callback) {
        mView.onChecking(Code.INFO_FILEWALL);
        HomeBiz.getInstance(mContext).getFilewall(new Callback<Boolean>() {
            @Override
            public void onCallBack(boolean success, Boolean... t) {
                if (success) {
                    if (!t[0]) {
                        onInfo(Code.INFO_FILEWALL);
                    } else {
                        mView.onError(Code.INFO_FILEWALL, Code.ERR_NONE);
                    }
                } else {
                    mView.onError(Code.INFO_FILEWALL, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
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
            onInfo(Code.INFO_STATIC_IP);
        }
        if (callback != null) {
            callback.onCallBack(!staticIp);
        }
    }

    @Override
    public void checkPort(Callback callback) {
        boolean success = false;
        mView.onChecking(Code.INFO_SERVER_PORT);
        if (NetworkUtils.telnet(Server.AUTH_SERVER, Server.AUTH_PORT)) {
            onInfo(Code.INFO_SERVER_PORT);
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
        mTraceroute.executeTraceroute(Server.INTERNET, Code.INFO_PING_INTERNET, callback);
    }

    @Override
    public void checkServer(Callback callback) {
        mView.onChecking(Code.INFO_SERVER);
        mTraceroute.executeTraceroute(Server.AUTH_SERVER, Code.INFO_SERVER, callback);
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
                        onInfo(Code.INFO_LOCALNET);
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
                        onInfo(Code.INFO_INTERNET);
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
        if (!TextUtils.isEmpty(NetworkUtils.getIp(Server.DNS_SERVER))) {
            onInfo(Code.INFO_DNS);
            success = true;
        } else {
            mView.onError(Code.INFO_DNS, Code.ERR_NONE);
        }
        if (callback != null) {
            callback.onCallBack(success);
        }
    }

    private void checkAuth(final Callback callback) {
        mView.onChecking(Code.INFO_AUTH);
        HomeBiz.getInstance(mContext).getAuth(new Callback<Integer>() {
            @Override
            public void onCallBack(boolean success, Integer... t) {
                int code = t[0];
                if (success) {
                    onInfo(Code.INFO_AUTH);
                } else if (code == 103) {
                    mView.onError(Code.INFO_AUTH, Code.ERR_WEIXIN);
                } else if (code == 102) {
                    mView.onError(Code.INFO_AUTH, Code.ERR_SMS);
                } else if (code == 101) {
                    mView.onError(Code.INFO_AUTH, Code.ERR_CARD);
                } else {
                    mView.onError(Code.INFO_AUTH, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
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

    public void onInfo(int code) {
        mView.onInfo(code, 0, 0);
    }
}

package com.amway.wifianalyze.home;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.listener.Callback;
import com.amway.wifianalyze.lib.util.NetworkUtils;
import com.amway.wifianalyze.lib.util.ThreadManager;
import com.amway.wifianalyze.lib.util.Utils;
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
        //信道利用率-静态ip-5g-获取ap人数--ping认证服务器-认证服务器端口-ping114-dns-认证-防火墙- 内网满载-外网满载
        //-ping Interner丢包-ping支付网站--下单网站 -下单网站端口--店铺自提
        HomeBiz.getInstance(mContext).getSysconfig(new Callback() {
            @Override
            public void onCallBack(boolean success, Object[] t) {
                checkUtilization(new Callback<Integer>() {
                    @Override
                    public void onCallBack(boolean success, Integer... t) {
                        checkDhcp(new Callback() {
                            @Override
                            public void onCallBack(boolean success, Object[] t) {
                                checkSupport5G(new Callback() {
                                    @Override
                                    public void onCallBack(boolean success, Object[] t) {
                                        checkAp(new Callback() {
                                            @Override
                                            public void onCallBack(boolean success, Object[] t) {
                                                checkAuthServer(new Callback() {
                                                    @Override
                                                    public void onCallBack(boolean success, Object[] t) {
                                                        checkAuthPort(new Callback() {
                                                            @Override
                                                            public void onCallBack(boolean success, Object[] t) {
                                                                pingIp114(new Callback() {
                                                                    @Override
                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                        checkDns(new Callback() {
                                                                            @Override
                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                checkAuth(new Callback() {
                                                                                    @Override
                                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                                        checkFilewall(new Callback() {
                                                                                            @Override
                                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                                checkLocalnetLoad(new Callback() {
                                                                                                    @Override
                                                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                                                        checkInternetLoad(new Callback() {
                                                                                                            @Override
                                                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                                                pingInternet(new Callback() {
                                                                                                                    @Override
                                                                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                                                                        pingPayWX(new Callback() {
                                                                                                                            @Override
                                                                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                                                                pingPayZhifubao(new Callback() {
                                                                                                                                    @Override
                                                                                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                                                                                        pingOrder(new Callback() {
                                                                                                                                            @Override
                                                                                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                                                                                checkOrderPort(new Callback() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onCallBack(boolean success, Object[] t) {
                                                                                                                                                        checkCustomPick(new Callback() {
                                                                                                                                                            @Override
                                                                                                                                                            public void onCallBack(boolean success, Object[] t) {
                                                                                                                                                                mView.onStopCheck();
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

    public void checkSupport5G(Callback callback) {
        mView.onChecking(Code.INFO_SUPPORT_5G);
        if (NetworkUtils.isSupport5G(mContext) || HomeBiz.getInstance(mContext).mHas5G) {
            onInfo(Code.INFO_SUPPORT_5G);
        } else if (NetworkUtils.isOnly24G(mContext)) {
            mView.onError(Code.INFO_SUPPORT_5G, Code.ERR_ONLY24G);
        } else {
            mView.onError(Code.INFO_SUPPORT_5G, Code.ERR_NOTFOUND_5G);
        }
        if (callback != null) {
            callback.onCallBack(true);
        }
    }

    public void checkAp(final Callback callback) {
        mView.onChecking(Code.INFO_GET_AP);
        HomeBiz.getInstance(mContext).getShopName(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, final String... t) {
                if (success) {
                    mView.onGetAp(t[0]);
                    int count = Utils.parseInt(t[2]);
                    if (count > 50) {
                        mView.onError(Code.INFO_GET_AP, Code.ERR_AP_USER);
                    } else {
                        mView.onInfo(Code.INFO_GET_AP, count, 0);
                    }
                } else {
                    mView.onGetAp("");
                    mView.onError(Code.INFO_GET_AP, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }

    public void checkAuthServer(Callback callback) {
        mView.onChecking(Code.INFO_SERVER);
        mTraceroute.executeTraceroute(Server.AUTH_SERVER, Code.INFO_SERVER, callback);
    }

    public void checkAuthPort(Callback callback) {
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

    public void pingIp114(Callback callback) {
        mView.onChecking(Code.INFO_IP_114);
        mTraceroute.executeTraceroute(Server.IP_114, Code.INFO_IP_114, callback);
    }

    public void pingPayWX(Callback callback) {
        mView.onChecking(Code.INFO_PAY_WEIXIN);
        mTraceroute.executeTraceroute(Server.PAY_WEI_XIN, Code.INFO_PAY_WEIXIN, callback);
    }

    public void pingInternet(Callback callback) {
        mView.onChecking(Code.INFO_PING_INTERNET);
        mTraceroute.executeTraceroute(Server.INTERNET, Code.INFO_PING_INTERNET, callback);
    }

    public void pingPayZhifubao(Callback callback) {
        mView.onChecking(Code.INFO_PAY_ZHIFUBAO);
        mTraceroute.executeTraceroute(Server.PAY_ZHIFUBAO, Code.INFO_PAY_ZHIFUBAO, callback);
    }

    public void pingOrder(Callback callback) {
        mView.onChecking(Code.INFO_PING_ORDER);
        mTraceroute.executeTraceroute(Server.ORDER_SERVER, Code.INFO_PING_ORDER, callback);
    }

    public void checkOrderPort(Callback callback) {
        boolean success = false;
        mView.onChecking(Code.INFO_ORDER_PORT);
        if (NetworkUtils.telnet(Server.ORDER_SERVER, Server.ORDER_PORT)) {
            onInfo(Code.INFO_ORDER_PORT);
            success = true;
        } else {
            mView.onError(Code.INFO_ORDER_PORT, Code.ERR_NONE);
        }
        if (callback != null) {
            callback.onCallBack(success);
        }
    }

    public void checkCustomPick(Callback callback) {//todo
        boolean success = false;
        mView.onChecking(Code.INFO_CUSTOMER_PICK);

        if (callback != null) {
            callback.onCallBack(success);
        }
    }

    public void checkLocalnetLoad(final Callback callback) {
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


    public void checkInternetLoad(final Callback callback) {
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

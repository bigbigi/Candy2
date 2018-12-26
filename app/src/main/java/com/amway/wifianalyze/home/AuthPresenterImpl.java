package com.amway.wifianalyze.home;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.amway.wifianalyze.base.Code;
import com.amway.wifianalyze.lib.listener.BlockCall;
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

    private final static int LOW_LEVEL = -80;
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
        //信号弱-ping Interner丢包-运营商检测-ping支付网站--下单网站 -下单网站端口--店铺自提
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                HomeBiz.getInstance(mContext).getSysconfig(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkUtilization(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkDhcp(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                Log.d("big", "checkSupport5G");
                checkSupport5G(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkAp(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                pingGateway(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkAuthServer(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkAuthPort(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                pingIp114(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkDns(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkAuth(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkFilewall(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkLocalnetLoad(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkInternetLoad(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkWifiLevel(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                pingInternet(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkIsp(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                pingPayWX(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                pingPayZhifubao(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                pingOrder(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkOrderPort(this);
                super.run();
            }

        });
        ThreadManager.single(new BlockCall() {
            @Override
            public void run() {
                checkCustomPick(this);
                super.run();
            }

        });
        ThreadManager.single(new Runnable() {
            @Override
            public void run() {
                mView.onStopCheck();
            }
        });

    }


    public void checkUtilization(final Callback callback) {
        mView.onChecking(Code.INFO_UTILIZATION);
        HomeBiz.getInstance(mContext).getUtilization(new Callback<Integer>() {
            @Override
            public void onCallBack(boolean success, Integer... t) {
                if (success) {
                    mView.onInfo(Code.INFO_UTILIZATION, t[0], 0);
                    if (t[0] >= 80) {
                        mView.onError(Code.INFO_UTILIZATION, Code.ERR_MSG, t[0] + "");
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
                    mView.onGetAp(t[0], t[2]);
                    int count = Utils.parseInt(t[2]);
                    if (count > 50) {
                        mView.onError(Code.INFO_GET_AP, Code.ERR_AP_USER);
                    } else {
                        mView.onInfo(Code.INFO_GET_AP, count, 0);
                    }
                } else {
                    mView.onGetAp("", "");
                    mView.onError(Code.INFO_GET_AP, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }

    public void checkAuthServer(Callback callback) {
        mView.onChecking(Code.INFO_AUTH_SERVER);
        mTraceroute.executeTraceroute(Server.AUTH_SERVER, Code.INFO_AUTH_SERVER, callback);
    }

    public void checkAuthPort(Callback callback) {
        boolean success = false;
        mView.onChecking(Code.INFO_AUTH_SERVER_PORT);
        if (NetworkUtils.telnet(Server.AUTH_SERVER, Server.AUTH_PORT)) {
            onInfo(Code.INFO_AUTH_SERVER_PORT);
            success = true;
        } else {
            mView.onError(Code.INFO_AUTH_SERVER_PORT, Code.ERR_NONE);
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

    public void pingGateway(Callback callback) {
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mView.onChecking(Code.INFO_GATEWAY);
        mTraceroute.executeTraceroute(NetworkUtils.intToIp(wifiManager.getDhcpInfo().gateway), Code.INFO_GATEWAY, callback);
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

    public void checkIsp(final Callback callback) {
        mView.onChecking(Code.INFO_ISP);
        HomeBiz.getInstance(mContext).checkCustomPick(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, String... t) {
                if (success) {
                    if (HomeBiz.getInstance(mContext).mRouterIp.contains(t[0])) {
                        onInfo(Code.INFO_ISP);
                        mView.onInfo(Code.INFO_ISP, 0, Utils.parseInt(t[1]));
                    } else {
                        mView.onError(Code.INFO_ISP, Code.ERR_NONE);
                    }
                } else {
                    mView.onError(Code.INFO_ISP, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });

    }

    public void checkCustomPick(final Callback callback) {
        mView.onChecking(Code.INFO_CUSTOMER_PICK);
        HomeBiz.getInstance(mContext).checkCustomPick(new Callback<String>() {
            @Override
            public void onCallBack(boolean success, String... t) {
                if (success) {
                    if (HomeBiz.getInstance(mContext).mRouterIp.contains(t[0])) {
                        onInfo(Code.INFO_CUSTOMER_PICK);
                    } else {
                        mView.onError(Code.INFO_CUSTOMER_PICK, Code.ERR_NONE);
                    }
                } else {
                    mView.onError(Code.INFO_CUSTOMER_PICK, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }

    public void checkLocalnetLoad(final Callback callback) {
        mView.onChecking(Code.INFO_LOCALNET_LOAD);
        HomeBiz.getInstance(mContext).checkLocalnetLoad(new Callback<Boolean>() {
            @Override
            public void onCallBack(boolean success, Boolean... t) {
                if (success) {
                    boolean input = t[0];
                    boolean output = t[1];
                    if (!input && !output) {
                        final String inputUse = HomeBiz.getInstance(mContext).mTempInputUse;
                        final String outputUse = HomeBiz.getInstance(mContext).mTempoutputUse;
                        mView.onInfo(Code.INFO_LOCALNET_LOAD, Utils.parseInt(inputUse), Utils.parseInt(outputUse));
                    } else {
                        if (input && output) {
                            mView.onError(Code.INFO_LOCALNET_LOAD, Code.ERR_ALLPUT, HomeBiz.getInstance(mContext).mTempInputUse, HomeBiz.getInstance(mContext).mTempoutputUse);
                        } else if (input) {
                            mView.onError(Code.INFO_LOCALNET_LOAD, Code.ERR_INPUT, HomeBiz.getInstance(mContext).mTempInputUse, HomeBiz.getInstance(mContext).mTempoutputUse);
                        } else {
                            mView.onError(Code.INFO_LOCALNET_LOAD, Code.ERR_OUTPUT, HomeBiz.getInstance(mContext).mTempInputUse, HomeBiz.getInstance(mContext).mTempoutputUse);
                        }
                    }
                } else {
                    mView.onError(Code.INFO_LOCALNET_LOAD, Code.ERR_QUEST);
                }
                if (callback != null) {
                    callback.onCallBack(success);
                }
            }
        });
    }


    public void checkInternetLoad(final Callback callback) {
        mView.onChecking(Code.INFO_INTERNET_LOAD);
        HomeBiz.getInstance(mContext).checkInternetLoad(new Callback<Boolean>() {
            @Override
            public void onCallBack(boolean success, Boolean... t) {
                if (success) {
                    boolean input = t[0];
                    boolean output = t[1];
                    if (!input && !output) {
                        final String inputUse = HomeBiz.getInstance(mContext).mTempInputUse;
                        final String outputUse = HomeBiz.getInstance(mContext).mTempoutputUse;
                        mView.onInfo(Code.INFO_INTERNET_LOAD, Utils.parseInt(inputUse), Utils.parseInt(outputUse));
                    } else {
                        if (input && output) {
                            mView.onError(Code.INFO_INTERNET_LOAD, Code.ERR_ALLPUT, HomeBiz.getInstance(mContext).mTempInputUse, HomeBiz.getInstance(mContext).mTempoutputUse);
                        } else if (input) {
                            mView.onError(Code.INFO_INTERNET_LOAD, Code.ERR_INPUT, HomeBiz.getInstance(mContext).mTempInputUse, HomeBiz.getInstance(mContext).mTempoutputUse);
                        } else {
                            mView.onError(Code.INFO_INTERNET_LOAD, Code.ERR_OUTPUT, HomeBiz.getInstance(mContext).mTempInputUse, HomeBiz.getInstance(mContext).mTempoutputUse);
                        }
                    }
                } else {
                    mView.onError(Code.INFO_INTERNET_LOAD, Code.ERR_QUEST);
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

    public void checkWifiLevel(Callback callback) {
        mView.onChecking(Code.INFO_WIFI_LEVEL);
        if (HomeBiz.getInstance(mContext).mScanResult != null
                && HomeBiz.getInstance(mContext).mScanResult.level < LOW_LEVEL) {
            mView.onError(Code.INFO_WIFI_LEVEL, Code.ERR_MSG, String.valueOf(HomeBiz.getInstance(mContext).mScanResult.level));
        } else {
            mView.onInfo(Code.INFO_WIFI_LEVEL, HomeBiz.getInstance(mContext).mScanResult.level, 0);
        }
        if (callback != null) {
            callback.onCallBack(true);
        }
    }

    public void checkNetworAccess(final Callback callback) {
        HomeBiz.getInstance(mContext).checkNetworAccess(new Callback<Boolean>() {
            @Override
            public void onCallBack(boolean success, Boolean... t) {
                if (success && !t[0]) {
                    mView.onChecking(Code.INFO_NETWORK_ACCESS);
                    mView.onError(Code.INFO_NETWORK_ACCESS, Code.ERR_NONE);
                }
                if (callback != null) {
                    callback.onCallBack(true);
                }
            }
        });
    }

    @Override
    public void onResult(int what, int loss, int delay) {
        Log.e("big", "onResult:" + what);
        if (loss >= 100) {
            mView.onError(what, Code.ERR_NONE);
        } else {
            mView.onInfo(what, loss, delay);
        }
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

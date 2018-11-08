package com.amway.wifianalyze.utils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

public class WifiConnector {
    private static final String TAG = "WifiConnector";

    public static final int WIFI_TYPE_WEP = 0;
    public static final int WIFI_TYPE_WPA = 1;
    public static final int WIFI_TYPE_NOPASS = 2;

    public static boolean connect(WifiManager wm, String ssid, String pwd, int type) {
        boolean ret;
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            WifiConfiguration configuration = checkExist(wm, ssid);
            if (configuration == null) {
                configuration = createWifiConfiguration(ssid, pwd, type);
                configuration.networkId = wm.addNetwork(configuration);
                wm.saveConfiguration();
            }
            ret = connectWifiByReflectMethod(wm, configuration.networkId) != null;
            Log.e(TAG, "networkId :" + configuration.networkId);
        } else {
            ret = connectByCofiguration(wm, createWifiConfiguration(ssid, pwd, type));
        }
        Log.e(TAG, "connect ret:" + ret);
        return ret;
    }


    private static boolean connectByCofiguration(WifiManager wm, WifiConfiguration configuration) {
        boolean isExist = isWifiConfigExist(wm, configuration);
        configuration.networkId = wm.addNetwork(configuration);
        wm.saveConfiguration();
        wm.disconnect();
        boolean ret = wm.enableNetwork(configuration.networkId, true);
        wm.reconnect();
        Log.e(TAG, "connectByCofiguration-->" + ret + "--ssid-->" + configuration.SSID + "--netID-->"
                + configuration.networkId + "--isExist-->" + isExist);

        return ret;
    }


    private static WifiConfiguration createWifiConfiguration(String SSID, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == WIFI_TYPE_NOPASS) { // WIFICIPHER_NOPASS
//            config.wepKeys[0] = "";
//            config.wepTxKeyIndex = 0;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == WIFI_TYPE_WEP) { // WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFI_TYPE_WPA) { // WIFICIPHER_WPA
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private static WifiConfiguration checkExist(WifiManager wm, String ssid) {
        WifiConfiguration exitConfig = null;
        List<WifiConfiguration> list = wm.getConfiguredNetworks();
        if (list != null) {
            for (WifiConfiguration exist : list) {
                if (exist == null || exist.SSID == null)
                    continue;
                Log.e(TAG, "EXIST-->" + exist.SSID + ",SSID:" + ssid);
                if (exist.SSID.equals("\"" + ssid + "\"")) {
                    exitConfig = exist;
                    Log.e(TAG, "checkExist-->" + exist.networkId);
                }
            }
        }
        return exitConfig;
    }

    private static boolean isWifiConfigExist(WifiManager wm, WifiConfiguration config) {
        boolean isExist = false;
        List<WifiConfiguration> list = wm.getConfiguredNetworks();
        if (list == null)
            return false;
        for (WifiConfiguration exist : list) {
            if (exist == null || exist.SSID == null)
                continue;
            if (exist.SSID.equals(config.SSID)) {
                isExist = true;
                boolean ret = wm.removeNetwork(exist.networkId);
                wm.saveConfiguration();
                Log.e(TAG, "remove-->" + exist.networkId + "--ret-->" + ret);
            }
        }
        return isExist;
    }

    private static Method connectWifiByReflectMethod(WifiManager wm, int netId) {
        Method connectMethod = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 反射方法： connect(int, listener) , 4.2 <= phone's android version
            for (Method methodSub : wm.getClass()
                    .getDeclaredMethods()) {
                if ("connect".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wm, netId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            // 反射方法: connect(Channel c, int networkId, ActionListener listener)
            // 暂时不处理4.1的情况 , 4.1 == phone's android version
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // 反射方法：connectNetwork(int networkId) ,
            // 4.0 <= phone's android version < 4.1
            for (Method methodSub : wm.getClass()
                    .getDeclaredMethods()) {
                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wm, netId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else {
            // < android 4.0
            return null;
        }
        return connectMethod;
    }
}

package com.amway.wifianalyze.lib.util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by big on 2018/10/18.
 */

public class NetworkUtils {
    private final static String TAG = "NetworkUtils";

    public static String getIp(String url) {
        try {
            ///Found NSLookup from http://www.coderanch.com/t/328875/java/java/nslookup-Java
            ///More on inet addresses from http://download.java.net/jdk7/archive/b123/docs/api/java/net/InetAddress.html

            InetAddress ipAddresses[] = InetAddress.getAllByName(url);
            StringBuffer strbuf = new StringBuffer("");

            for (int i = 0; i < ipAddresses.length; i++) {
                strbuf.append(ipAddresses[i].getHostAddress() + ";");
            }
            return strbuf.toString();

        } catch (UnknownHostException e) {
            Log.e("TAG", "UnknownHostException:" + e);
            return null;
        }

    }

    public static boolean checkDnsWithIp(String url, String dns) {
        return TextUtils.equals(dns, getIp(url));
    }

    public static void checkSocket() {
        try {
            DatagramSocket socket = new DatagramSocket(80);
            InetAddress serverAddress = InetAddress.getByName("www.baidu.com");
            String str = "hello";
            byte data[] = str.getBytes();
            DatagramPacket packages = new DatagramPacket(data, data.length, serverAddress, 80);
            socket.send(packages);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean telnet(String ip, int port) {
        boolean isConnected = false;
        TelnetClient client = new TelnetClient();
        try {
            Log.e("TelnetConnection", "ip:" + ip + ",--port:" + port);
            client.connect(ip, port);
            isConnected = client.isConnected();
            Log.e("TelnetConnection", "isConnected:" + isConnected);
            if (isConnected) {
                client.disconnect();
            }
        } catch (SocketException ex) {
            Log.e("TelnetConnection", "error:" + ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    public static String getWifiSetting(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo.leaseDuration == 0) {//静态IP配置方式
            return "StaticIP";
        } else {                         //动态IP配置方式
            return "DHCP";
        }
    }

    public static boolean isStaticIp(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        return dhcpInfo.leaseDuration == 0;
    }

    public static boolean is24GHz(int freq) {
        return freq > 2400 && freq < 2500;
    }

    public static boolean is5GHz(int freq) {
        return freq > 4900 && freq < 5900;
    }

    public static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public static String getMac(Context context) {
        Log.e(TAG, "VERSION:" + Build.VERSION.SDK_INT);
        String mac = getWlanMac();
        if (TextUtils.isEmpty(mac) || "02:00:00:00:00:00".equals(mac.trim())) {
            mac = getWifiInfoMac(context);
        }
        if (TextUtils.isEmpty(mac) || "02:00:00:00:00:00".equals(mac.trim())) {
            mac = getLocalMacAddressFromIp();
        }
        return mac;
    }

    /**
     * 根据wifi信息获取本地mac
     *
     * @param context
     * @return
     */
    public static String getWifiInfoMac(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String mac = winfo.getMacAddress();
        return mac;
    }

    public static String getWlanMac() {

        String mac = "";

        try {
            String temp = "";
            Process pro = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pro.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            while (null != temp) {
                temp = input.readLine();
                if (temp != null) {
                    mac = temp.trim();
                    break;
                }
            }
        } catch (Throwable var5) {
            var5.printStackTrace();
        }

        return mac;
    }

    /**
     * 根据IP地址获取MAC地址
     *
     * @return
     */
    private static String getLocalMacAddressFromIp() {
        String strMacAddr = null;
        try {
            //获得IpD地址
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toUpperCase();
        } catch (Exception e) {

        }

        return strMacAddr;
    }

    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            //列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();//得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }

    public static String getPhoneNumber(Context context) {
        String phoneNum = PreferenceUtil.getString(context, PreferenceUtil.PHONE_NUM);
        if (TextUtils.isEmpty(phoneNum)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNum = telephonyManager.getLine1Number();
            if (!TextUtils.isEmpty(phoneNum)) {
                PreferenceUtil.putString(context, PreferenceUtil.PHONE_NUM, phoneNum);
            }
        }
        return phoneNum;
    }


    public static String getDns1() {
        return getProp("getprop net.dns1");
    }

    public static String getProp(String prop) {
        String dns = "";
        try {
            Process localProcess = Runtime.getRuntime().exec(prop);
            InputStream is = localProcess.getInputStream();
            byte buf[] = new byte[1024 * 4];
            int len = -1;
            StringBuffer sb = new StringBuffer();
            while ((len = is.read(buf, 0, buf.length)) != -1) {
                sb.append(new String(buf, 0, len));
            }
            is.close();
            localProcess.destroy();
            dns = sb.toString().trim();
            Log.d("big", "dns-->" + sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dns;
    }

    public static String getSpeed(float speed) {
        speed = speed * 8;
        if (speed > 1000) {
            return String.format("%.1fMb/S", speed / 1000);
        } else {
            return String.format("%.1fKb/S", speed);
        }
    }

    public static int getBandwidth(float speed) {
        int bandwidth = (int) (speed * 8 / 1000);
        return bandwidth < 1 ? 1 : bandwidth;
    }

    public static int getLevel(float speed) {
        if (speed > 750) {//800
            return 3;
        } else if (speed > 450) {//500
            return 2;
        } else if (speed > 220) {//250
            return 1;
        } else {
            return 0;
        }
    }

    private static String[] tags = new String[]{"自行车", "汽车", "飞机", "火箭"};

    public static String getDefinition(float speed) {
        return tags[getLevel(speed)];
    }

    public static boolean isSupport5G(Context context) {
        boolean support = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            support = wm.is5GHzBandSupported();
        }
        return support;
    }

    public static boolean isOnly24G(Context context) {
        boolean only = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            only = !wm.is5GHzBandSupported();
        }
        return only;
    }
}

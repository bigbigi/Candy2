package com.amway.wifianalyze.lib;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by big on 2018/10/18.
 */

public class NetworkUtils {

    public static String getIpAndDns(String url) {
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
            Log.e("bigbig", "UnknownHostException:" + e);
            return null;
        }

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
}

package com.amway.wifianalyze.base;

/**
 * Created by big on 2018/11/5.
 */

public class Code {
    public static final int FLAG_ERROR = 0x000000ff;
    public static final int FLAG_INFO = 0x0000ff00;

    public static final int INFO_OPEN_WIFI = 0x00000001;//正在打开WIFI
    public static final int INFO_SCAN_WIFI = 0x00000002;//扫描WIFI...
    public static final int INFO_CONNECTING = 0x00000003;//正在连接
    public static final int INFO_CONNECTED = 0x00000004;//连接成功

    public static final int INFO_STATIC_IP = 0x00000011;//静态IP
    public static final int INFO_EMPTY_IP = 0x00000012;//IP空
    public static final int INFO_SERVER = 0x00000013;//服务器ping不通
    public static final int INFO_SERVER_PORT = 0x00000014;//服务器端口被占用
    public static final int INFO_INTERNET = 0x00000005;//Internet专线不通
    public static final int INFO_DNS = 0x00000016;//DNS错误
    public static final int INFO_SKIP = 0x00000017;//自动跳转

    public static final int INFO_WEIXIN = 0x00000018;//微信无法认证
    public static final int INFO_CARD = 0x00000019;//卡号无法认证
    public static final int INFO_SMS = 0x0000001a;//短信无法认证

    public static String getMessage(int code, int loss, int delay) {
        String message;
        switch (code) {
            case Code.INFO_OPEN_WIFI:
                message = "正在打开wifi...";
                break;
            case Code.INFO_SCAN_WIFI:
                message = "开始扫描附近wifi";
                break;
            case Code.INFO_CONNECTING:
                message = "正在连接wifi...";
                break;
            case Code.INFO_CONNECTED:
                message = "连接成功";
                break;
            case Code.INFO_STATIC_IP:
                message = "检查静态IP";
                break;
            case Code.INFO_SERVER:
                if (loss == -1) {
                    message = "检查服务器延迟";
                } else {
                    message = "检查服务器延迟，丢包：" + loss + ",延迟:" + delay;
                }
                break;
            case Code.INFO_SERVER_PORT:
                message = "检查服务器端口被占用";
                break;
            case Code.INFO_INTERNET:
                if (loss == -1) {
                    message = "检查Internet专线延迟";
                } else {
                    message = "检查Internet专线延迟，丢包：" + loss + ",延迟:" + delay;
                }
                break;
            case Code.INFO_DNS:
                message = "检查DNS配置";
                break;
            case Code.INFO_SKIP:
                message = "是否已认证";
                break;
            default:
                message = null;
                break;
        }
        return message;
    }

    public static String getErrorMessage(int code, int reason) {
        if (reason < 0) {
            return getMessage(code, -1, -1);
        }
        String message = null;
        switch (reason) {
            case 1:
                message = "密码错误";
                break;
            case 2:
                message = "信道拥堵";
                break;
            case 3:
                message = "信号差";
                break;
        }
        return message;
    }
}

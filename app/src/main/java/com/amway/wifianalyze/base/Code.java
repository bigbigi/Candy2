package com.amway.wifianalyze.base;

/**
 * Created by big on 2018/11/5.
 */

public class Code {
    public static final int FLAG_ERROR = 0x0000ff00;
    public static final int FLAG_INFO = 0x000000ff;

    public static final int INFO_OPEN_WIFI = 0x00000001;//正在打开WIFI
    public static final int INFO_SCAN_WIFI = 0x00000002;//扫描WIFI...
    public static final int INFO_CONNECTING = 0x00000003;//正在连接
    public static final int INFO_CONNECTED = 0x00000004;//连接成功

    public static final int INFO_STATIC_IP = 0x00000011;//静态IP
    public static final int INFO_IP_114 = 0x00000012;//ping 114网络中断
    public static final int INFO_SERVER = 0x00000013;//服务器ping不通
    public static final int INFO_SERVER_PORT = 0x00000014;//服务器端口被占用
    public static final int INFO_INTERNET = 0x00000015;//检查外网专线带宽
    public static final int INFO_DNS = 0x00000016;//DNS错误
    public static final int INFO_AUTH = 0x00000017;//自动跳转
    public static final int INFO_FILEWALL = 0x00000018;//视频网站访问被拦截
    public static final int INFO_PAY_WEIXIN = 0x00000019;//检测微信支付
    public static final int INFO_PAY_ZHIFUBAO = 0x0000001a;//检测支付宝
    public static final int INFO_LOCALNET = 0x0000001b;//内网专线带宽
    public static final int INFO_PING_INTERNET = 0x0000001c;//ping外网
    public static final int INFO_GET_AP = 0x0000001d;//获取ap人数
    public static final int INFO_SUPPORT_5G = 0x0000001e;//是否支持5G;
    public static final int INFO_UTILIZATION = 0x0000001f;//检测信道利用率;

    public static final int INFO_PING_ORDER = 0x00000020;//检测下单网站;
    public static final int INFO_ORDER_PORT = 0x00000021;//检测下单网站端口;
    public static final int INFO_CUSTOMER_PICK = 0x00000022;//检测店铺自提;


    public static final int CHECKING = -1;

    public static String getMessage(int code, int loss, int delay) {
        String message;
        switch (code) {
            case Code.INFO_OPEN_WIFI:
                message = "正在打开WIFI...";
                break;
            case Code.INFO_SCAN_WIFI:
                message = "开始扫描附近WIFI";
                break;
            case Code.INFO_CONNECTING:
                message = "正在连接WIFI...";
                break;
            case Code.INFO_CONNECTED:
                message = "连接成功";
                break;
            case Code.INFO_STATIC_IP:
                message = "检查静态IP";
                break;
            case Code.INFO_IP_114:
                message = "检查互联网连接";//"网络中断"
                break;

            case Code.INFO_SERVER:
                if (loss == CHECKING) {
                    message = "检查服务器延迟";
                } else {
                    message = "检查服务器延迟，丢包：" + loss + "%，延迟:" + delay + "ms";
                }
                break;
            case Code.INFO_SERVER_PORT:
                message = "检查服务器端口被占用";
                break;
            case Code.INFO_INTERNET:
                message = "检查外网专线带宽";
                break;
            case Code.INFO_DNS:
                message = "检查DNS配置";
                break;
            case Code.INFO_AUTH:
                message = "是否已认证";
                break;
            case Code.INFO_LOCALNET:
                message = "检查内网专线带宽";
                break;
            case Code.INFO_PING_INTERNET:
                if (loss == CHECKING) {
                    message = "检查互联网延迟";
                } else {
                    message = "互联网延迟，丢包：" + loss + "%，延迟:" + delay + "ms";
                }
                break;
            case Code.INFO_GET_AP:
                if (loss == CHECKING) {
                    message = "检测WIFI在线人数";
                } else {
                    message = "WIFI在线人数：" + loss;
                }
                break;
            case INFO_SUPPORT_5G:
                message = "是否支持5G";
                break;
            case INFO_UTILIZATION:
                if (loss == CHECKING) {
                    message = "检测信道利用率";
                } else {
                    message = "信道利用率：" + loss + "%";
                }
                break;
            case INFO_FILEWALL:
                message = "视频网站访问被拦截";
                break;
            case INFO_PAY_WEIXIN:
                if (loss == CHECKING) {
                    message = "检测微信支付";
                } else {
                    message = "检测微信支付，丢包：" + loss + "%，延迟:" + delay + "ms";
                }
                break;
            case INFO_PAY_ZHIFUBAO:
                if (loss == CHECKING) {
                    message = "检测支付宝";
                } else {
                    message = "检测支付宝，丢包：" + loss + "%，延迟:" + delay + "ms";
                }
                break;
            case INFO_PING_ORDER:
                message = "检测下单网站";
                break;
            case INFO_ORDER_PORT:
                message = "检测下单网站端口";
                break;
            case INFO_CUSTOMER_PICK:
                message = "检测店铺自提";
                break;
            default:
                message = null;
                break;
        }
        return message;
    }

    //错误
    public static final int ERR_NONE = -1;//无
    public static final int ERR_QUEST = 0x0000ff00;//请求失败
    public static final int ERR_NO_WIFI = 0x0000ff01;//未找到目标WIFI
    public static final int ERROR_PWD = 0x0000ff02;//密码错误
    public static final int ERROR_BUSY_CHANNEL = 0x0000ff03;//信道拥堵
    public static final int ERROR_LOW_LEVEL = 0x0000ff04;//信号差
    public static final int ERROR_ELSE = 0x0000ff05;//其他问题，AP人数过多等，找客服
    public static final int ERR_ONLY24G = 0x0000ff06;//不支持5G
    public static final int ERR_NOTFOUND_5G = 0x0000ff06;//未找到5G信号
    public static final int ERR_INTERNET_INPUT = 0x0000ff07;//入口带宽异常
    public static final int ERR_INTERNET_OUTPUT = 0x0000ff08;//出口带宽异常

    public static final int ERR_WEIXIN = 0x00000009;//微信无法认证
    public static final int ERR_CARD = 0x0000000a;//卡号无法认证
    public static final int ERR_SMS = 0x0000000b;//短信无法认证
    public static final int ERR_AP_USER = 0x0000000c;//WIFI连接人数过多

    public static String getErrorMessage(int code, int reason) {
        if (reason < 0) {
            return getMessage(code, CHECKING, -1);
        }
        String message = null;
        switch (reason) {
            case ERR_QUEST:
                message = getMessage(code, CHECKING, -1) + "（请求失败）";
                break;
            case ERR_NO_WIFI:
                message = "未找到目标WIFI";
                break;
            case ERROR_PWD:
                message = "WIFI密码错误";
                break;
            case ERROR_BUSY_CHANNEL:
                message = "WIFI信号干扰严重";
                break;
            case ERROR_LOW_LEVEL:
                message = "WIFI信号弱";
                break;
            case ERROR_ELSE:
                message = "AP人数过多等，找客服";
                break;
            case ERR_ONLY24G:
                message = "不支持5G";
                break;
            case ERR_INTERNET_INPUT:
                message = "入口带宽异常";
                break;
            case ERR_INTERNET_OUTPUT:
                message = "出口带宽异常";
                break;
            case ERR_WEIXIN:
                message = "微信无法认证";
                break;
            case ERR_CARD:
                message = "卡号无法认证";
                break;
            case ERR_SMS:
                message = "短信无法认证";
                break;
            case ERR_AP_USER:
                message = "WIFI连接人数过多";
                break;
        }
        return message;
    }
}

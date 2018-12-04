package com.amway.wifianalyze.utils;

/**
 * Created by big on 2018/11/22.
 */

public class Server {
    //192.168.101.207
//     public static final String HOST = "http://10.0.0.12:8096";//todo 更改地址
    public static final String HOST = "http://192.168.101.207";//todo 更改地址
    //认证服务器
    public static String AUTH_SERVER = "www.baidu.com";//todo 认证服务器地址
    public static int AUTH_PORT = 80;//todo 认证服务器端口
    //下单网站
    public static String ORDER_SERVER = "www.baidu.com";
    public static int ORDER_PORT = 80;
    //上传、下载
    public static String DOWNLOAD_SERVER = "http://pubstatic.b0.upaiyun.com/check2.jpg";
    public static String UPLOAD_SERVER = "http://health-test.b0.upaiyun.com/check2.jpg?t=1543397663809";
    //外网
    public static String INTERNET = "www.baidu.com";
    //外网IP
    public static String IP_114 = "114.114.114.114";
    //DNS
    public static String DNS_SERVER = "www.baidu.com";
    //检测微信支付
    public static String PAY_WEI_XIN = "pay.weixin.qq.com";
    //检测检测支付宝
    public static String PAY_ZHIFUBAO = "alipay.com";
    //获取公网IP
    public static String MY_IP = "http://ip.taobao.com/service/getIpInfo.php?ip=myip";
    //升级
    public static String UPDATE = "%s/checkwifi-api/appUpdate/name_Amwayapk/apkType_2.dat";
}

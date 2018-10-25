package com.amway.wifianalyze.home;

import android.content.Context;

import com.amway.wifianalyze.base.BaseContract;
import com.amway.wifianalyze.base.BasePresenterImpl;

/**
 * Created by big on 2018/10/23.
 */

public interface WebContract extends BaseContract {

    interface WebView extends BaseView {
        void onInfo(int code);

        void onError(int code);

    }

    abstract class WebPresenter extends BasePresenterImpl<WebView> {
        public static final int INFO_FIREWALL = 0x00000001;//防火墙限制
        public static final int INFO_FREQUENCE = 0x00000002;//2.4g无线
        public static final int INFO_CHANNEL = 0x00000003;//信道满
        public static final int INFO_BANDWIDTH = 0x00000004;//带宽满
        public static final int INFO_LOSS_PACKET = 0x00000005;//Internet专线丢包
        public static final int INFO_DNS = 0x00000006;//DNS问题
        public static final int INFO_WEBPORT = 0x00000007;//网站端口占用
        public static final int INFO_WEBSITE = 0x00000008;//网站问题
        public static final int INFO_FULLLOAD = 0x00000009;//内/外网专线满
        public static final int INFO_NETWORKOFF = 0x0000000a;//5分钟断网，未关注安利云服务公众号

        public WebPresenter(WebView view) {
            super(view);
        }

        public abstract void startCheck(Context context);

        public abstract void release();

        public abstract void checkFirewall();//查看防火墙策略限制（后台服务器登录当地店铺深信服检查策略）

        public abstract void checkFrequence();//用户连着的是2.4G无线（后台服务器登录WLC用命令查看）；

        public abstract void checkChannel();//无线信道利用率满（后台服务器登录WLC用命令查看）

        public abstract void checkBandwidth();//internet专线带宽满（后台服务器登录当地店铺路由器用命令查看）

        public abstract void checkLossPacket();//internet专线丢包（后台服务器登录当地店铺路由器用ping命令看延时）；

        //DNS问题（检查DNS配置是否正确，用APP检查解析的IP和专线的IP是否同一运营商以及解析耗时）
        //DNS问题（检查DNS配置是否正确、DNS配置正确时用APP去解析该网站是否为预设IP）；
        public abstract boolean checkDns();

        public abstract boolean checkWebPort();//网站端口不通（telnet网站端口）

        public abstract boolean checkWebSite();//webview 检测网站

        public abstract boolean checFullLoad(); //内/外网专线满（后台服务器登录当地店铺路由器用命令查看）；

        public abstract void networkOff();//未关注安利云服务公众号（直接建议用户关注安利云服务公众号）
    }
}

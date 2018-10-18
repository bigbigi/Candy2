package com.amway.wifianalyze.dns;

import android.content.Context;
import android.util.Log;

import com.amway.wifianalyze.dns.DnsContract.DnsPresenter;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by big on 2018/10/18.
 */

public class DnsPresenterImpl extends DnsPresenter {
    public DnsPresenterImpl(DnsContract.DnsView view) {
        super(view);
    }

    @Override
    void init(Context context) {

    }

    @Override
    void release(Context context) {

    }


}

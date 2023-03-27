package com.cc.wasu.softap.lib.udp;

public interface SocketResultListener {

    void onDataReceived(String data);

    void onReceivedDataCheckError();
}

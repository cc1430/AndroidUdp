package com.cc.wasu.softap.lib.wifiUtils.wifiConnect;

import androidx.annotation.NonNull;


public interface WifiConnectionCallback {
    void successfulConnect();

    void errorConnect(@NonNull ConnectionErrorCode connectionErrorCode);

    void onDisconnect();
}

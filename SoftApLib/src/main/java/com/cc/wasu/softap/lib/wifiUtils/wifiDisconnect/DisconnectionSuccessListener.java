package com.cc.wasu.softap.lib.wifiUtils.wifiDisconnect;

import androidx.annotation.NonNull;


public interface DisconnectionSuccessListener {
    void success();

    void failed(@NonNull DisconnectionErrorCode errorCode);
}
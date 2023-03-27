package com.cc.wasu.softap.lib.wifiUtils.wifiConnect;

import androidx.annotation.NonNull;

public interface ConnectionSuccessListener {
    void success();

    void failed(@NonNull ConnectionErrorCode errorCode);
}

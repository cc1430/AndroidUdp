package com.cc.wasu.softap.lib.wifiUtils.wifiRemove;

import androidx.annotation.NonNull;


public interface RemoveSuccessListener {
    void success();

    void failed(@NonNull RemoveErrorCode errorCode);
}

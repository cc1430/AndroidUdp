package com.cc.wasu.softap.lib.wifiUtils.wifiConnect;


import android.net.wifi.ScanResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface ConnectionScanResultsListener {
    @Nullable
    ScanResult onConnectWithScanResult(@NonNull List<ScanResult> scanResults);
}

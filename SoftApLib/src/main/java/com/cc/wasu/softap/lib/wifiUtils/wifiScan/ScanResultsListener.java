package com.cc.wasu.softap.lib.wifiUtils.wifiScan;


import android.net.wifi.ScanResult;

import androidx.annotation.NonNull;

import java.util.List;

public interface ScanResultsListener {
    void onScanResults(@NonNull List<ScanResult> scanResults);
}

package com.cc.wasu.softap.lib.wifiUtils.wifiConnect;

import static com.cc.wasu.softap.lib.wifiUtils.ConnectorUtils.isAlreadyConnected;
import static com.cc.wasu.softap.lib.wifiUtils.ConnectorUtils.reEnableNetworkIfPossible;
import static com.cc.wasu.softap.lib.wifiUtils.WifiUtils.wifiLog;
import static com.cc.wasu.softap.lib.wifiUtils.utils.Elvis.of;
import static com.cc.wasu.softap.lib.wifiUtils.utils.VersionUtils.isAndroidQOrLater;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import androidx.annotation.NonNull;

import com.cc.wasu.softap.lib.wifiUtils.WeakHandler;


public class TimeoutHandler {
    private final WifiManager mWifiManager;
    private final WeakHandler mHandler;
    private final WifiConnectionCallback mWifiConnectionCallback;
    private ScanResult mScanResult;

    private final Runnable timeoutCallback = new Runnable() {
        @Override
        public void run() {
            wifiLog("Connection Timed out...");

            if (!isAndroidQOrLater()) {
                reEnableNetworkIfPossible(mWifiManager, mScanResult);
            }
            if (isAlreadyConnected(mWifiManager, of(mScanResult).next(scanResult -> scanResult.BSSID).get())) {
                mWifiConnectionCallback.successfulConnect();
            } else {
                mWifiConnectionCallback.errorConnect(ConnectionErrorCode.TIMEOUT_OCCURRED);
            }

            mHandler.removeCallbacks(this);
        }
    };

    public TimeoutHandler(@NonNull WifiManager wifiManager, @NonNull WeakHandler handler, @NonNull final WifiConnectionCallback wifiConnectionCallback) {
        this.mWifiManager = wifiManager;
        this.mHandler = handler;
        this.mWifiConnectionCallback = wifiConnectionCallback;
    }

    public void startTimeout(final ScanResult scanResult, final long timeout) {
        // cleanup previous connection timeout handler
        mHandler.removeCallbacks(timeoutCallback);

        mScanResult = scanResult;
        mHandler.postDelayed(timeoutCallback, timeout);
    }

    public void stopTimeout() {
        mHandler.removeCallbacks(timeoutCallback);
    }
}

package com.cc.wasu.softap.lib.utils;

import android.util.Log;

public class ApLog {

    public static boolean enableLog = false;
    public static final String TAG = "chenchen";

    public static void d(String tag, Object msg) {
        if (enableLog) {
            Log.d(tag, "" + msg);
        }
    }

    public static void i(String tag, Object msg) {
        if (enableLog) {
            Log.i(tag, "" + msg);
        }
    }

    public static void v(String tag, Object msg) {
        if (enableLog) {
            Log.v(tag, "" + msg);
        }
    }

    public static void e(String tag, Object msg) {
        if (enableLog) {
            Log.e(tag, "" + msg);
        }
    }

}

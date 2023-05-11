package com.cc.wasu.softap.lib.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static String getEncode(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bs = md.digest(input.getBytes());
            return toMD5String(bs);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toMD5String(byte[] bs) {
        char[] chars = new char[bs.length];
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            int val = ((int) bs[i]) & 0xff;
            if (val < 16)
                buffer.append("0");
            buffer.append(Integer.toHexString(val));
        }
        return buffer.toString();
    }
}

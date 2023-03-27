package com.cc.wasu.softap.lib.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.cc.wasu.softap.lib.GlobalDef;
import com.cc.wasu.softap.lib.WasuSDK_SoftAPConfig;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DataUtils {

    public static final String TAG = ApLog.TAG;

    /**
     * 对发送的报文进行不加密组装
     * @param payload 未加密负载
     * @return 报文
     */
    public static byte[] buildDataPacket(String payload) {
        String encodedPayload = Base64.encodeToString(payload.getBytes(), Base64.DEFAULT);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(GlobalDef.PROTOCOL_HEAD);
        byte[] contentLength = getContentLength(encodedPayload);
        byteBuffer.put(contentLength);
        byteBuffer.put(GlobalDef.FUNCTION_CODE_DOWN);
        byteBuffer.put(encodedPayload.getBytes());
        byteBuffer.put(getCheckSum(encodedPayload, GlobalDef.FUNCTION_CODE_DOWN));
        return byteBuffer.array();
    }

    /**
     * 对发送的报文进行加密组装
     * @param payload 未加密负载
     * @param aesKey 加密秘钥
     * @return 报文
     */
    public static byte[] buildEncryptDataPacket(String payload, String aesKey) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        String encryptPayload = AESUtils.encryptCBC(payload, aesKey);
        byteBuffer.put(GlobalDef.PROTOCOL_HEAD);
        byte[] contentLength = getContentLength(encryptPayload);
        byteBuffer.put(contentLength);
        byteBuffer.put(GlobalDef.FUNCTION_CODE_DOWN);
        byteBuffer.put(encryptPayload.getBytes());
        byteBuffer.put(getCheckSum(encryptPayload, GlobalDef.FUNCTION_CODE_DOWN));
        return byteBuffer.array();
    }

    /**
     * 负载长度
     * @param payload 加密后负载
     * @return 负载长度
     */
    private static int getPayloadLength(String payload) {
        if (TextUtils.isEmpty(payload)) {
            return 0;
        }

        return payload.getBytes().length;
    }

    /**
     * 获取报文长度 功能码+负载+和校验
     * @param payload 加密后的负载
     * @return 报文长度
     */
    private static byte[] getContentLength(String payload) {
        int contentLength = 1 + getPayloadLength(payload) + 2;
        return int2ByteArray(contentLength);
    }

    private static byte[] getCheckSum(String payload, byte functionCode) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.put(GlobalDef.PROTOCOL_HEAD);
        byteBuffer.put(getContentLength(payload));
        byteBuffer.put(functionCode);
        byteBuffer.put(payload.getBytes());
        byte[] bytes = byteBuffer.array();

        int sum = 0;
        for (int i = 0; i < bytes.length; i++) {
            sum += bytes[i]&0xff;
        }
        return int2ByteArray(sum);
    }

    private static byte[] int2ByteArray(int digit) {
        String sumHex = String.format("%04x", digit);
        int a = Integer.parseInt(sumHex.substring(0, 2), 16);
        int b = Integer.parseInt(sumHex.substring(2, 4), 16);
        return new byte[]{(byte) a, (byte) b};
    }

    /**
     * 对设备返回的源数据进行和校验，确保数据完整性和正确性
     * @param receivedData 源数据
     * @return 成功或失败
     */
    public static boolean checkReceivedDataSum(byte[] receivedData) {
        ApLog.d(TAG, "校验接收数据: <---------");
        boolean res = false;
        try {
            ApLog.d(TAG, "协议头: " + String.format("%02x", receivedData[0]) + " " + String.format("%02x", receivedData[1]));
            ApLog.d(TAG, "报文长度: " + String.format("%02x", receivedData[2]) + " " + String.format("%02x", receivedData[3]));

            String len = String.format("%02x", receivedData[2]) + String.format("%02x", receivedData[3]);
            ApLog.d(TAG, "报文长度: " + Integer.parseInt(len, 16));
            int payloadLength = Integer.parseInt(len, 16) - 3;

            ApLog.d(TAG, "功能码: " + String.format("%02x", receivedData[4]));

            int totalLength = Integer.parseInt(len, 16) + 4;
            ApLog.d(TAG, "和校验: " + String.format("%02x", receivedData[totalLength - 2]) + " " + String.format("%02x", receivedData[totalLength - 1]));
            String sumStr = String.format("%02x", receivedData[totalLength - 2]) + String.format("%02x", receivedData[totalLength - 1]);
            int sumInt = Integer.parseInt(sumStr, 16);
            ApLog.d(TAG, "和校验: " + sumInt);
            
            String payload = new String(receivedData, 5, payloadLength, StandardCharsets.UTF_8);
            ApLog.d(TAG, "payload: " + payload);

            int total = 0;
            for (int i = 0; i < totalLength - 2; i++) {
                total += receivedData[i]&0xff;
            }
            ApLog.d(TAG, "SDK计算和校验值: " + total);
            if (sumInt == total) {
                res = true;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        ApLog.d(TAG, "校验接收数据: ------> result = " + res);
        return res;
    }

    /**
     * 获取负载-校验是否是加密数据，如果加密需要解密后返回
     * @param receivedData 源数据
     * @return 解密后数据
     */
    public static String getPayload(byte[] receivedData) {
        String len = String.format("%02x", receivedData[2]) + String.format("%02x", receivedData[3]);
        int payloadLength = Integer.parseInt(len, 16) - 3;
        String encodedPayload = new String(receivedData, 5, payloadLength, StandardCharsets.UTF_8);
        byte[] bytePayload = Base64.decode(encodedPayload, Base64.DEFAULT);
        String payload = new String(bytePayload, 0, bytePayload.length);
        //如果是json格式的不用解密直接返回
        if (payload.startsWith("{") && payload.endsWith("}")) {
            return payload;
        } else {
            return AESUtils.decryptCBC(encodedPayload, DataUtils.getMD5AesKey(WasuSDK_SoftAPConfig.deviceId));
        }
    }

    /**
     * DeviceId MD5以后取前16位作为AES加密密钥
     * @param deviceId 设备ID
     * @return 加密密钥
     */
    public static String getMD5AesKey(String deviceId) {
        String aesKey = deviceId;
        aesKey = MD5.getEncode(aesKey);
        if (!TextUtils.isEmpty(aesKey) && aesKey.length() > 16) {
            aesKey = aesKey.substring(0, 16);
        }
        return aesKey;
    }
}

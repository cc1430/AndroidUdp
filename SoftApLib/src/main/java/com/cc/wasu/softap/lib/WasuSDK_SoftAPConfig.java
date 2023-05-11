package com.cc.wasu.softap.lib;

import static com.cc.wasu.softap.lib.GlobalDef.MSG_CONFIG_NETWORK_FAILED;
import static com.cc.wasu.softap.lib.GlobalDef.MSG_CONFIG_NETWORK_SUCCESS;
import static com.cc.wasu.softap.lib.GlobalDef.MSG_CONNECT_AP_FAILED;
import static com.cc.wasu.softap.lib.GlobalDef.MSG_CONNECT_AP_SUCCESS;
import static com.cc.wasu.softap.lib.GlobalDef.MSG_RECEIVER_DATA_CHECK_ERROR;
import static com.cc.wasu.softap.lib.GlobalDef.MSG_RECEIVER_DEV_INFO;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cc.wasu.softap.lib.udp.SocketResultListener;
import com.cc.wasu.softap.lib.udp.UDPSocket;
import com.cc.wasu.softap.lib.utils.ApLog;
import com.cc.wasu.softap.lib.utils.DataUtils;
import com.cc.wasu.softap.lib.wifiUtils.WifiUtils;
import com.cc.wasu.softap.lib.wifiUtils.wifiConnect.ConnectionErrorCode;
import com.cc.wasu.softap.lib.wifiUtils.wifiConnect.ConnectionSuccessListener;


public class WasuSDK_SoftAPConfig {

    public static final String TAG = ApLog.TAG;

    private static WasuSDK_SoftAPConfig instance;
    private Context context;
    private UDPSocket udpSocket;
    private Handler handler;
    public static String deviceId;
    public static String productId;

    public static WasuSDK_SoftAPConfig getInstance() {
        if (null == instance) {
            instance = new WasuSDK_SoftAPConfig();
        }
        return instance;
    }

    private WasuSDK_SoftAPConfig() {

    }

    public void init(Context context) {
        this.context = context;
        udpSocket = new UDPSocket();
        udpSocket.setSocketResultListener(listener);
    }

    public void enableLog(boolean enable) {
        ApLog.enableLog = enable;
    }

    public void findDevice(String ssid, String pwd, Handler handler) {
        this.handler = handler;
        WifiUtils.withContext(context).enableWifi();
        WifiUtils.withContext(context)
                .connectWith(ssid, pwd)
                .setTimeout(15000)
                .onConnectionResult(new ConnectionSuccessListener() {
                    @Override
                    public void success() {
                        ApLog.i(TAG, "AP连接成功！！");
                        udpSocket.startUDPSocket();
                        if (handler != null) {
                            Message message = handler.obtainMessage(MSG_CONNECT_AP_SUCCESS);
                            message.arg1 = GlobalDef.STATUS_CODE_4000;
                            handler.sendMessage(message);
                        }
                    }

                    @Override
                    public void failed(@NonNull ConnectionErrorCode errorCode) {
                        ApLog.e(TAG, "AP连接异常: " + errorCode);
                        udpSocket.stopUDPSocket();
                        if (handler != null) {
                            Message message = handler.obtainMessage(MSG_CONNECT_AP_FAILED);
                            message.arg1 = GlobalDef.STATUS_CODE_4002;
                            handler.sendMessage(message);
                        }
                    }
                }).start();
    }

    public void startUdpSocket() {
        udpSocket.startUDPSocket();
    }

    public void stopUdpSocket() {
        udpSocket.stopUDPSocket();
    }

    public void getDeviceInfo(Handler handler) {
        this.handler = handler;
        JSONObject payloadJson = new JSONObject();
        try {
            payloadJson.put("cmdId", 1);
            payloadJson.put("timeStamp", System.currentTimeMillis());
            payloadJson.put("version", "1.0.0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String payload = payloadJson.toString();
        byte[] message = DataUtils.buildDataPacket(payload);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < message.length; i++) {
            sb.append(String.format("%02x", message[i]));
            sb.append(" ");
        }
        ApLog.d(TAG, String.valueOf(sb));

        udpSocket.sendMessage(message);
    }

    /**
     * cmdId
     * 3: 下发配网信息
     * deviceId	string	华数平台颁发的设备ID	必须
     * productId	string	华数平台颁发的产品ID	必须
     * encrypt	int	热点加密模式
     * 0:open
     * 1:wpa_psk
     * 2:wpa2_psk
     * 3:wpa_wpa2_psk
     * 4:wpa2_enterprise
     * 5:wpa3_psk
     * 6:wpa2_wpa3_psk
     * 7:wapi_psk	必须
     * @param ssid 热点名称	必须
     * @param pwd 热点密码	必须
     */
    public void startApConfig(String ssid, String pwd, String token) {
        JSONObject payloadJson = new JSONObject();
        try {
            payloadJson.put("cmdId", 3);
            payloadJson.put("timeStamp", System.currentTimeMillis());
            payloadJson.put("version", "1.0.0");
            payloadJson.put("productId", productId);
            payloadJson.put("deviceId", deviceId);
            payloadJson.put("ssid", ssid);
            payloadJson.put("pwd", pwd);
            payloadJson.put("encrypt", 3);
            payloadJson.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String payload = payloadJson.toString();
        ApLog.d(TAG, payload);
        byte[] message = DataUtils.buildEncryptDataPacket(payload, DataUtils.getMD5AesKey(deviceId));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < message.length; i++) {
            sb.append(String.format("%02x", message[i]));
            sb.append(" ");
        }
        ApLog.d(TAG, String.valueOf(sb));

        udpSocket.sendMessage(message);
    }

    private final SocketResultListener listener = new SocketResultListener() {
        @Override
        public void onDataReceived(String data) {
            if (handler != null) {
                Message message;
                JSONObject obj = JSON.parseObject(data);
                if (obj == null) {
                    ApLog.e(ApLog.TAG, "JSON数据解析失败！");
                    return;
                }
                int cmdId = obj.getInteger("cmdId");
                if (cmdId == 2) {
                    deviceId = obj.getString("deviceId");
                    productId = obj.getString("productId");
                    message = handler.obtainMessage(MSG_RECEIVER_DEV_INFO);
                    message.arg1 = GlobalDef.STATUS_CODE_2000;
                    message.obj = data;
                    handler.sendMessage(message);
                } else if (cmdId == 4) {
                    int connect = obj.getInteger("connectTest");
                    if (connect == 0) {
                        message = handler.obtainMessage(MSG_CONFIG_NETWORK_SUCCESS);
                        message.arg1 = GlobalDef.STATUS_CODE_1000;
                    } else {
                        message = handler.obtainMessage(MSG_CONFIG_NETWORK_FAILED);
                        message.arg1 = GlobalDef.STATUS_CODE_1001;
                    }
                    message.obj = data;
                    handler.sendMessage(message);
                }
            }
        }

        @Override
        public void onReceivedDataCheckError() {
            if (handler != null) {
                Message message = handler.obtainMessage(MSG_RECEIVER_DATA_CHECK_ERROR);
                message.arg1 = GlobalDef.STATUS_CODE_5000;
                handler.sendMessage(message);
            }
        }
    };

    public void unInit() {
        udpSocket.stopUDPSocket();
        udpSocket = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

}

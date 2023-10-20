package com.cc.wasu.softap.lib;

public class GlobalDef {

    public static final String AP_SSID = "WASU_OUTLET_C43D"; //"WASU_OUTLET_C43D"; //"WASU-AP-LINK";
    public static final String AP_PWD = "cb0cC43D"; //"cb0cC43D"; // "Wasu@2023";

    public static final String AP_SSID_1 = "nova"; //"H3C_46C6A3";
    public static final String AP_PWD_1 = "12345678"; //"Study@812A3";
    public static final String SERVER_IP = "192.168.4.1";
    public static final int SERVER_PORT = 10001;

    /**
     * 协议头2字节：0xa11a（固定）
     */
    public static final byte[] PROTOCOL_HEAD = new byte[]{(byte) 0xa1, 0x1a};

    /**
     * 功能码 1字节 （可变）
     * 0x01:下行通信（配网设备-》设备）
     * 0x02:上行通信（设备-》配网设备）
     */
    public static final byte FUNCTION_CODE_DOWN = 0x01;
    public static final byte FUNCTION_CODE_UP = 0x02;

    public static final int MSG_RECEIVER_DEV_INFO = 0x1001;
    public static final int MSG_CONNECT_AP_SUCCESS = 0x1002;
    public static final int MSG_CONNECT_AP_FAILED = 0x1003;
    public static final int MSG_CONFIG_NETWORK_SUCCESS = 0x1004;
    public static final int MSG_CONFIG_NETWORK_FAILED = 0x1005;
    public static final int MSG_RECEIVER_DATA_CHECK_ERROR = 0x1006;

    /**
     * SDK状态码
     */
    public static final int STATUS_CODE_1000 = 1000;
    public static final int STATUS_CODE_1001 = 1001;
    public static final int STATUS_CODE_2000 = 2000;
    public static final int STATUS_CODE_4000 = 4000;
    public static final int STATUS_CODE_4002 = 4002;
    public static final int STATUS_CODE_5000 = 5000;
}

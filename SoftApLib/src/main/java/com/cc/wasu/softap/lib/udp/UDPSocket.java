package com.cc.wasu.softap.lib.udp;

import com.cc.wasu.softap.lib.GlobalDef;
import com.cc.wasu.softap.lib.utils.ApLog;
import com.cc.wasu.softap.lib.utils.DataUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class UDPSocket {

    private static final String TAG = "SoftAp";

    // 单个CPU线程池大小
    private static final int POOL_SIZE = 5;

    private static final int BUFFER_LENGTH = 1024;
    private byte[] receiveByte = new byte[BUFFER_LENGTH];

    private boolean isThreadRunning = false;

    private DatagramSocket client;
    private DatagramPacket receivePacket;
    private SocketResultListener socketResultListener;

    private long lastReceiveTime = 0;
    private static final long TIME_OUT = 120 * 1000;
    private static final long HEARTBEAT_MESSAGE_DURATION = 10 * 1000;

    private ExecutorService mThreadPool;
    private Thread clientThread;
    private HeartbeatTimer timer;

    public UDPSocket() {
        int cpuNumbers = Runtime.getRuntime().availableProcessors();
        // 根据CPU数目初始化线程池
        mThreadPool = Executors.newFixedThreadPool(cpuNumbers * POOL_SIZE);
        // 记录创建对象时的时间
        lastReceiveTime = System.currentTimeMillis();
    }

    public void setSocketResultListener(SocketResultListener socketResultListener) {
        this.socketResultListener = socketResultListener;
    }

    public void startUDPSocket() {
        if (client != null) return;
        try {
            // 表明这个 Socket 在设置的端口上监听数据。
            client = new DatagramSocket(GlobalDef.SERVER_PORT);

            if (receivePacket == null) {
                // 创建接受数据的 packet
                receivePacket = new DatagramPacket(receiveByte, BUFFER_LENGTH);
            }

            startSocketThread();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启发送数据的线程
     */
    private void startSocketThread() {
        clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ApLog.d(TAG, "clientThread is running...");
                receiveMessage();
            }
        });
        isThreadRunning = true;
        clientThread.start();

//        startHeartbeatTimer();
    }

    /**
     * 处理接受到的消息
     */
    private void receiveMessage() {
        while (isThreadRunning) {
            try {
                if (client != null) {
                    client.receive(receivePacket);
                }
                lastReceiveTime = System.currentTimeMillis();
                ApLog.d(TAG, "receive packet success...");
            } catch (Exception e) {
                ApLog.e(TAG, "UDP数据包接收失败！线程停止");
                stopUDPSocket();
                e.printStackTrace();
                return;
            }

            if (receivePacket == null || receivePacket.getLength() == 0) {
                ApLog.e(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
                continue;
            }
            ApLog.d(TAG, "#receiveMessage: from " + receivePacket.getAddress().getHostAddress() + ":" + receivePacket.getPort());

            String strReceive = new String(receivePacket.getData(), 0, receivePacket.getLength());
            ApLog.d(TAG, "原始数据：" + strReceive);

            if (DataUtils.checkReceivedDataSum(receivePacket.getData())) {
                if (socketResultListener != null) {
                    String decryptData = DataUtils.getPayload(receivePacket.getData());
                    ApLog.d(TAG, "payload解密结果：" + decryptData);
                    socketResultListener.onDataReceived(decryptData);
                }
            } else {
                if (socketResultListener != null) {
                    socketResultListener.onReceivedDataCheckError();
                }
            }

            // 每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
            if (receivePacket != null) {
                receivePacket.setLength(BUFFER_LENGTH);
            }
        }
    }

    public void stopUDPSocket() {
        isThreadRunning = false;
        receivePacket = null;
        if (clientThread != null) {
            clientThread.interrupt();
        }
        if (client != null) {
            client.close();
            client = null;
        }
        if (timer != null) {
            timer.exit();
        }
    }

    /**
     * 启动心跳，timer 间隔十秒
     */
    private void startHeartbeatTimer() {
        timer = new HeartbeatTimer();
        timer.setOnScheduleListener(new HeartbeatTimer.OnScheduleListener() {
            @Override
            public void onSchedule() {
                ApLog.d(TAG, "timer is onSchedule...");
                long duration = System.currentTimeMillis() - lastReceiveTime;
                ApLog.d(TAG, "duration:" + duration);
                if (duration > TIME_OUT) {//若超过两分钟都没收到我的心跳包，则认为对方不在线。
                    ApLog.d(TAG, "超时，对方已经下线");
                    // 刷新时间，重新进入下一个心跳周期
                    lastReceiveTime = System.currentTimeMillis();
                } else if (duration > HEARTBEAT_MESSAGE_DURATION) {//若超过十秒他没收到我的心跳包，则重新发一个。
                    String string = "hello,this is a heartbeat message";
//                    sendMessage(string);
                }
            }

        });
        timer.startTimer(0, 1000 * 10);
    }

    /**
     * 发送心跳包
     *
     * @param message
     */
    public void sendMessage(final byte[] message) {
        if (client == null) {
            return;
        }
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress targetAddress = InetAddress.getByName(GlobalDef.SERVER_IP);
                    DatagramPacket packet = new DatagramPacket(message, message.length, targetAddress, GlobalDef.SERVER_PORT);
                    client.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
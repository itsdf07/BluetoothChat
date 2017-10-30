package com.itsdf07.bluetoothchat.bluetoothutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.itsdf07.bluetoothchat.common.log.ALog;

/**
 * 服务器连接线程
 */
public class BluetoothServerConnThread extends Thread {
    private Handler serviceHandler; // 用于同Service通信的Handler
    private BluetoothAdapter adapter;
    private BluetoothSocket socket; // 用于通信的Socket
    private BluetoothServerSocket serverSocket;

    /**
     * 构造函数
     *
     * @param handler
     */
    public BluetoothServerConnThread(Handler handler) {
        ALog.d("...");
        this.serviceHandler = handler;
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void run() {

        try {
            Thread.sleep(1000);//休眠1秒，防止出错

            serverSocket = adapter.listenUsingRfcommWithServiceRecord("Server",
                    BluetoothTools.PRIVATE_UUID);
            ALog.d("serverSocket = %s", serverSocket);
            socket = serverSocket.accept();
            ALog.d("socket = %s", socket.getRemoteDevice().getAddress());
        } catch (Exception e) {
            // 发送连接失败消息
            serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR)
                    .sendToTarget();
            ALog.e("e = %s", e.getMessage());
            // 打印连接失败信息
            ALog.e("e = %s", e.getMessage());
            return;
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                // 打印关闭socket失败信息
                ALog.e("e = %s", e.getMessage());
            }
        }
        try {
            if (socket != null) {
                // 发送连接成功消息，消息的obj字段为连接的socket
                Message msg = serviceHandler.obtainMessage();
                msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
                msg.obj = socket;
                msg.sendToTarget();
            } else {
                // 发送连接失败消息
                serviceHandler.obtainMessage(
                        BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
                ALog.e("socket = %s", socket);
                // System.out.println("socket=null");
                return;
            }
        } catch (Exception e) {
            ALog.e("e = %s", e.getMessage());
        }
    }
}
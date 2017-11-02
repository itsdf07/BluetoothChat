package com.itsdf07.bluetoothchat.bluetoothutil;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.itsdf07.bluetoothchat.common.log.ALog;

/**
 * 蓝牙客户端连接线程
 */
public class BluetoothClientConnThread extends Thread {
    /**
     * 用于向客户端Service回传消息的handler
     */
    private Handler mClientMainHandler;
    private BluetoothDevice mBluetoothDevice; // 服务器设备
    private BluetoothSocket mSocket; // 通信Socket

    /**
     * 构造函数
     */
    public BluetoothClientConnThread(Handler handler, BluetoothDevice bluetoothDevice) {
        this.mClientMainHandler = handler;
        this.mBluetoothDevice = bluetoothDevice;
    }

    @Override
    public void run() {
        try {
            //建立连接
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
            mSocket.connect();
        } catch (Exception ex) {
            try {
                mSocket.close();
            } catch (IOException e) {
                ALog.e("e = %s", ex.getMessage());
            }
            ALog.e("ex = %s", ex.getMessage());
            // 发送连接失败消息
            mClientMainHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR)
                    .sendToTarget();
            return;
        }

        // 发送连接成功消息，消息的obj参数为连接的socket
        Message msg = mClientMainHandler.obtainMessage();
        msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
        msg.obj = mSocket;
        msg.sendToTarget();
    }

}


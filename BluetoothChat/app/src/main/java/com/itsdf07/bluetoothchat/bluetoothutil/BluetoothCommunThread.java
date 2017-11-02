package com.itsdf07.bluetoothchat.bluetoothutil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

/**
 * 蓝牙通讯线程
 */
public class BluetoothCommunThread extends Thread {

    private Handler mMainHandler; // 与Service通信的Handler
    private BluetoothSocket mSocket;
    private ObjectInputStream inStream; // 对象输入流
    private ObjectOutputStream outStream; // 对象输出流
    public volatile boolean isRun = true; // 运行标志位

    /**
     * 构造函数
     *
     * @param handler 用于接收消息
     * @param socket
     */
    public BluetoothCommunThread(Handler handler, BluetoothSocket socket) {
        this.mMainHandler = handler;
        this.mSocket = socket;
        try {
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            //TODO 此处有ANR现象，需要优化：当前运行在main线程
            this.inStream = new ObjectInputStream(new BufferedInputStream(
                    socket.getInputStream()));//此处，mSocket.getInputStream()会造成ANR，详情查阅doc\notes文件记录
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            // 发送连接失败消息
            mMainHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!isRun) {
                break;
            }
            try {
                Object obj = inStream.readObject();
                // 发送成功读取到对象的消息，消息的obj参数为读取到的对象
                Message msg = mMainHandler.obtainMessage();
                msg.what = BluetoothTools.MESSAGE_READ_OBJECT;
                msg.obj = obj;
                msg.sendToTarget();
            } catch (Exception ex) {
                // 发送连接失败消息
                mMainHandler.obtainMessage(
                        BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
                ex.printStackTrace();
                return;
            }
        }

        // 关闭流
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 写入一个可序列化的对象
     *
     * @param obj
     */
    public void writeObject(Object obj) {
        try {
            outStream.flush();
            outStream.writeObject(obj);
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


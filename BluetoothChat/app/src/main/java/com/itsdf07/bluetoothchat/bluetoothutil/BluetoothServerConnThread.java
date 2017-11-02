package com.itsdf07.bluetoothchat.bluetoothutil;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.itsdf07.bluetoothchat.common.log.ALog;

/**
 * 服务器连接线程
 */
public class BluetoothServerConnThread extends Thread {
    private Handler mServiceMainHandler; // 用于同Service通信的Handler
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket mSocket; // 用于通信的Socket
    private BluetoothServerSocket mServerSocket;

    /**
     * 构造函数
     *
     * @param handler
     */
    public BluetoothServerConnThread(Handler handler) {
        ALog.d("...");
        this.mServiceMainHandler = handler;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void run() {

        try {
//            Thread.sleep(1000);//休眠1秒，防止出错

            mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord("Server",
                    BluetoothTools.PRIVATE_UUID);
            ALog.d("mServerSocket = %s", mServerSocket);
            mSocket = mServerSocket.accept();
            ALog.d("mSocket = %s", mSocket.getRemoteDevice().getAddress());
        } catch (Exception e) {
            // 发送连接失败消息
            mServiceMainHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
            // 打印连接失败信息
            ALog.e("e = %s", e.getMessage());
            return;
        } finally {
            try {
                mServerSocket.close();
            } catch (Exception e) {
                // 打印关闭socket失败信息
                ALog.e("e = %s", e.getMessage());
            }
        }
        try {
            if (mSocket != null) {
                // 发送连接成功消息，消息的obj字段为连接的socket
                Message msg = mServiceMainHandler.obtainMessage();
                msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
                msg.obj = mSocket;
                msg.sendToTarget();
            } else {
                // 发送连接失败消息
                mServiceMainHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
                ALog.e("mSocket = %s", mSocket);
                return;
            }
        } catch (Exception e) {
            ALog.e("e = %s", e.getMessage());
        }
    }
}
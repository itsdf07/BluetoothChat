package com.itsdf07.bluetoothchat.bluetoothutil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.itsdf07.bluetoothchat.common.log.ALog;

public class BluetoothClientService extends Service {

    // 搜索到的远程设备集合
    private List<BluetoothDevice> mDiscoveredDevices = new ArrayList<BluetoothDevice>();

    // 蓝牙适配器
    private final BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();

    // 蓝牙通讯线程
    private BluetoothCommunThread mCommunThread;

    private boolean TempB = false;// 判断是否是主动取消的搜索

    // 控制信息广播的接收器
    private BroadcastReceiver mControlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ALog.d("action = %s", action);
            if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
                // 选择了连接的服务器设备
                BluetoothDevice device = (BluetoothDevice) intent.getExtras()
                        .get(BluetoothTools.DEVICE);
                // 开启设备连接线程
                new BluetoothClientConnThread(handler, device).start();
            } else if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
                // 停止后台服务
                if (mCommunThread != null) {
                    mCommunThread.isRun = false;
                }
                stopSelf();
            } else if (BluetoothTools.ACTION_DATA_TO_SERVICE.equals(action)) {
                // 获取数据
                Object data = intent.getSerializableExtra(BluetoothTools.DATA);
                if (mCommunThread != null) {
                    mCommunThread.writeObject(data);
                }
            }
        }
    };

    // 蓝牙搜索广播的接收器
    private BroadcastReceiver mDiscoveryReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取广播的Action
            String action = intent.getAction();
            ALog.d("action = %s", action);
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                // 开始搜索
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 发现远程蓝牙设备
                // 获取设备
                BluetoothDevice bluetoothDevice = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String address = bluetoothDevice.getAddress();
                String name = bluetoothDevice.getName();
                Log.e("this bluetooth address", address);
                Log.e("this bluetooth name", name);

                //这里搜索到与地址匹配的手机后，发送广播，由注册了该广播的Receiver进行连接操作
                if (address.equals(BluetoothTools.BluetoothAddress)
                        || address.equals(BluetoothTools.BluetoothAddress2)) {
                    TempB = true;
                    mBTAdapter.cancelDiscovery();// 取消搜索
                    // 将广播发送出去
                    Intent selectDeviceIntent = new Intent(
                            BluetoothTools.ACTION_SELECTED_DEVICE);
                    selectDeviceIntent.putExtra(BluetoothTools.DEVICE,
                            bluetoothDevice);
                    sendBroadcast(selectDeviceIntent);
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                // 搜索结束，如果不是主动取消的搜索，就发送广播
                if (!TempB) {
                    // 若未找到设备，则发动未发现设备广播
                    Intent foundIntent = new Intent(
                            BluetoothTools.ACTION_NOT_FOUND_SERVER);
                    sendBroadcast(foundIntent);
                }
            } else if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
                // 搜索完成，未发现设备，继续调用搜索
                mBTAdapter.cancelDiscovery();// 取消搜索
                try {
                    Thread.sleep(5000);// 休眠,防止低端手机出错
                } catch (Exception e) {
                    Log.e("sleep", e.getMessage());
                }
                mBTAdapter.startDiscovery(); // 开始搜索
            }
        }
    };

    // 接收其他线程消息的Handler
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ALog.d("msg.what = %s", msg.what);
            // 处理消息
            switch (msg.what) {
                case BluetoothTools.MESSAGE_CONNECT_ERROR:
                    // 连接错误
                    // 发送连接错误广播
                    Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
                    sendBroadcast(errorIntent);
                    break;
                case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
                    // 连接成功

                    // 开启通讯线程
                    mCommunThread = new BluetoothCommunThread(handler, (BluetoothSocket) msg.obj);
                    mCommunThread.start();

                    // 发送连接成功广播
                    Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
                    sendBroadcast(succIntent);
                    break;
                case BluetoothTools.MESSAGE_READ_OBJECT:
                    // 读取到对象
                    // 发送数据广播（包含数据对象）
                    Intent dataIntent = new Intent(
                            BluetoothTools.ACTION_DATA_TO_GAME);
                    dataIntent
                            .putExtra(BluetoothTools.DATA, (Serializable) msg.obj);
                    sendBroadcast(dataIntent);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 获取通讯线程
     *
     * @return
     */
    public BluetoothCommunThread getBluetoothCommunThread() {
        return mCommunThread;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(BluetoothTools.TAG, "onStartCommand ...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Service创建时的回调函数
     */
    @Override
    public void onCreate() {
        ALog.d("...");
        // discoveryReceiver的IntentFilter
        IntentFilter discoveryFilter = new IntentFilter();
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);

        // controlReceiver的IntentFilter
        IntentFilter controlFilter = new IntentFilter();
        controlFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
        controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
        controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);

        // 注册BroadcastReceiver
        registerReceiver(mDiscoveryReceiver, discoveryFilter);
        registerReceiver(mControlReceiver, controlFilter);

//        mDiscoveredDevices.clear(); // 清空存放设备的集合
        mBTAdapter.enable(); // 打开蓝牙
        try {
            Thread.sleep(5000);// 休眠1秒
        } catch (Exception e) {
            Log.e(BluetoothTools.TAG, e.getMessage());
        }
        mBTAdapter.startDiscovery(); // 开始搜索
        super.onCreate();
    }

    /**
     * Service销毁时的回调函数
     */
    @Override
    public void onDestroy() {
        ALog.d("...");
        if (mCommunThread != null) {
            mCommunThread.isRun = false;
        }
        // 解除绑定
        unregisterReceiver(mDiscoveryReceiver);
        mBTAdapter.disable();// 关闭蓝牙
        super.onDestroy();
    }
}

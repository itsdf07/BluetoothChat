package com.itsdf07.bluetoothchat.bluetoothutil;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.itsdf07.bluetoothchat.common.TranslateUtils;
import com.itsdf07.bluetoothchat.common.log.ALog;

/**
 * 蓝牙模块服务器端主控制Service
 */
public class BluetoothServerService extends Service {

    private IServerCallback mServerCallback;

    /**
     * 客户端服务响应交互回调
     */
    public interface IServerCallback {

        /**
         * @param object
         * @param type   see BluetoothTools
         */
        void onNotifyConnectResult(Object object, String type);
    }

    public void setServerCallback(IServerCallback callback) {
        mServerCallback = callback;
    }

    // 蓝牙适配器
    private final BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();

    // 蓝牙通讯线程
    private BluetoothCommunThread mCommunThread;

    // 接收其他线程消息的Handler
    private Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            ALog.d("msg.what = %s", msg.what);
            switch (msg.what) {
                case BluetoothTools.MESSAGE_CONNECT_ERROR:
                    // 连接错误
                    // 发送连接错误广播
                    mServerCallback.onNotifyConnectResult(null, BluetoothTools.ACTION_CONNECT_ERROR);
                    break;
                case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
                    // 连接成功
                    // 开启通讯线程
                    mCommunThread = new BluetoothCommunThread(mMainHandler,
                            (BluetoothSocket) msg.obj);
                    mCommunThread.start();

                    // 发送连接成功广播
                    mServerCallback.onNotifyConnectResult( msg.obj, BluetoothTools.ACTION_CONNECT_SUCCESS);
                    break;
                case BluetoothTools.MESSAGE_READ_OBJECT:
                    // 读取到数据
                    // 发送数据广播（包含数据对象）
                    mServerCallback.onNotifyConnectResult(msg.obj, BluetoothTools.ACTION_DATA_TO_GAME);
                    break;
            }

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
    public void onCreate() {
        super.onCreate();
        ALog.d("...");

        onOpenBluetooth();
    }

    @Override
    public void onDestroy() {
        ALog.e("...");
        if (mCommunThread != null) {
            mCommunThread.isRun = false;
        }
        super.onDestroy();
    }

    /**
     * 消息发送
     *
     * @param bean 发送内容
     */
    public void onSendMsg(TransmitBean bean) {
        // 获取数据
        if (mCommunThread != null) {
            mCommunThread.writeObject(bean);
        }
    }

    /**
     * 注册蓝牙检测广播
     */
    private void registerBtReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        //蓝牙开关状态的广播
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(mBluetoothReceiver, intentFilter);
    }

    /**
     * 打开蓝牙
     */
    private void onOpenBluetooth() {
        if (mBTAdapter == null) {
            return;
        }
        if (mBTAdapter.isEnabled()) {
            onDiscoverable();
        } else {
            mBTAdapter.enable();
        }
    }

    /**
     * 开启蓝牙发现功能（300秒）
     */
    private void onDiscoverable() {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(discoveryIntent);

        // 开启后台连接线程
        new BluetoothServerConnThread(mMainHandler).start();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(mBluetoothReceiver);
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        registerBtReceiver();
        return new ServerServiceBinder();
    }

    public class ServerServiceBinder extends Binder {
        public BluetoothServerService getService() {
            return BluetoothServerService.this;
        }
    }


    /**
     * 蓝牙相关广播接收器<br>
     * 蓝牙开关状态的广播 : BluetoothAdapter.ACTION_STATE_CHANGED
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ALog.d("onReceive：action = " + action);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))//蓝牙开关状态的广播
            {
                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                Toast.makeText(context, TranslateUtils.getBluetoothStateTip(bluetoothState), Toast.LENGTH_SHORT).show();
                ALog.d("ACTION_STATE_CHANGED = " + bluetoothState + ":" + TranslateUtils.getBluetoothStateTip(bluetoothState));
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_OFF://蓝牙已关闭
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON://蓝牙打开中
                        break;
                    case BluetoothAdapter.STATE_ON://蓝牙已打开
                        onDiscoverable();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF://蓝牙关闭中
                        break;
                    default:
                        break;
                }
            }
        }
    };
}
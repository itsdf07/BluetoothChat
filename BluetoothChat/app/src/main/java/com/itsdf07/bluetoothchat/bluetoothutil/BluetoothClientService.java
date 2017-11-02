package com.itsdf07.bluetoothchat.bluetoothutil;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
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

public class BluetoothClientService extends Service {

    private IClientCallBack mClientCallback;

    /**
     * 客户端服务响应交互回调
     */
    public interface IClientCallBack {
        /**
         * 添加搜索到的蓝牙设备
         *
         * @param device    搜索到的蓝牙设备
         * @param needClear 是否需要清空列表
         */
        void addBTDevice(BluetoothDevice device, boolean needClear);

        /**
         * 搜索设备的按钮是否可用，防止恶意点击
         *
         * @param enable true：可用
         */
        void onUpdateSearchDevicesStatus(boolean enable);

        /**
         * @param object
         * @param type   see BluetoothTools
         */
        void onNotifyConnectResult(Object object, String type);
    }

    public void setClientCallback(IClientCallBack callback) {
        mClientCallback = callback;
    }

    private final BluetoothAdapter mBTAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * 蓝牙通讯线程
     */
    private BluetoothCommunThread mCommunThread;

    Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ALog.d("msg.what = %s", msg.what);
            // 处理消息
            switch (msg.what) {
                case BluetoothTools.MESSAGE_CONNECT_ERROR:// 连接错误
                    mClientCallback.onNotifyConnectResult(null, BluetoothTools.ACTION_CONNECT_ERROR);
                    break;
                case BluetoothTools.MESSAGE_CONNECT_SUCCESS:// 连接成功
                    // 开启通讯线程
                    mCommunThread = new BluetoothCommunThread(mMainHandler, (BluetoothSocket) msg.obj);
                    mCommunThread.start();

                    mClientCallback.onNotifyConnectResult(msg.obj, BluetoothTools.ACTION_CONNECT_SUCCESS);
                    break;
                case BluetoothTools.MESSAGE_READ_OBJECT: // 读取到对象
                    // 数据回调（包含数据对象）
                    mClientCallback.onNotifyConnectResult(msg.obj, BluetoothTools.ACTION_DATA_TO_GAME);
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        registerBtReceiver();
        return new ClientServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(mBluetoothReceiver);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ALog.d("...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ALog.d("...");
        return super.onStartCommand(intent, flags, startId);
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
        super.onDestroy();
    }

    /**
     * 注册蓝牙检测广播
     */
    private void registerBtReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        //蓝牙开关状态的广播
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //蓝牙连接状态的广播
        intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);

        //蓝牙扫描的广播：开始
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //蓝牙扫描的广播：结束
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // 发现搜索到的设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        // 发现搜索到的设备
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        //蓝牙配对的广播
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        //蓝牙扫描状态(SCAN_MODE)发生改变
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);

        registerReceiver(mBluetoothReceiver, intentFilter);
    }

    /**
     * 开始搜索蓝牙设备
     */
    public void onFoundBTDevice() {
        if (mBTAdapter == null) {
            ALog.e("mBTAdapter is null");
            Toast.makeText(this, "当前设备蓝牙不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mClientCallback != null) {
            mClientCallback.onUpdateSearchDevicesStatus(false);
        }
        if (mBTAdapter.isEnabled()) {
            if (mBTAdapter.isDiscovering()) {
                mBTAdapter.cancelDiscovery();
            }
            mBTAdapter.startDiscovery();
        } else {
            mBTAdapter.enable();
        }

    }

    /**
     * 停止蓝牙设备搜索
     */
    public void onCancelDiscvery() {
        if (mClientCallback != null) {
            mClientCallback.onUpdateSearchDevicesStatus(true);
        }
        if (mBTAdapter.isDiscovering()) {
            mBTAdapter.cancelDiscovery();
        }
    }

    /**
     * 连接蓝牙设备
     *
     * @param device 连接目标
     */
    public void onConnectBTDevice(BluetoothDevice device) {
        onCancelDiscvery();
        // 开启设备连接线程
        new BluetoothClientConnThread(mMainHandler, device).start();
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

    public class ClientServiceBinder extends Binder {
        public BluetoothClientService getService() {
            return BluetoothClientService.this;
        }
    }


    /**
     * 蓝牙相关广播接收器<br>
     * 1、注册监听蓝牙sco通道状态 : AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED<br>
     * 状态key：AudioManager.EXTRA_SCO_AUDIO_STATE，状态value如下：<br>
     * {@code AudioManager.SCO_AUDIO_STATE_CONNECTED }: 连接成功<br>
     * {@code AudioManager.SCO_AUDIO_STATE_CONNECTING }: 连接中<br>
     * {@code AudioManager.SCO_AUDIO_STATE_DISCONNECTED }: 连接失败<br>
     * {@code AudioManager.SCO_AUDIO_STATE_ERROR }: 连接异常<br>
     * 2、蓝牙连接状态的广播 : BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED<br>
     * 状态key：BluetoothHeadset.EXTRA_STATE，状态value如下：<br>
     * {@code BluetoothHeadset.STATE_DISCONNECTED }: 未连接<br>
     * {@code BluetoothHeadset.STATE_CONNECTING }: 连接中<br>
     * {@code BluetoothHeadset.STATE_CONNECTED }: 连接成功<br>
     * {@code BluetoothHeadset.STATE_DISCONNECTING }:<br>
     * 3、蓝牙开关状态的广播 : BluetoothAdapter.ACTION_STATE_CHANGED
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
                        onFoundBTDevice();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF://蓝牙关闭中
                        break;
                    default:
                        break;
                }
            } else if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action))//蓝牙连接状态的广播
            {
                int headsetState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_CONNECTED);
                Toast.makeText(context, TranslateUtils.getBluetoothHeadsetStateTip(headsetState), Toast.LENGTH_SHORT).show();
                ALog.d("ACTION_CONNECTION_STATE_CHANGED = " + headsetState + ":" + TranslateUtils.getBluetoothHeadsetStateTip(headsetState));
                switch (headsetState) {
                    case BluetoothHeadset.STATE_CONNECTED://连接成功
                        break;
                    case BluetoothHeadset.STATE_DISCONNECTED://未连接
                        break;
                    case BluetoothHeadset.STATE_CONNECTING://连接中
                        break;
                    case BluetoothHeadset.STATE_DISCONNECTING:
                        break;
                    default:
                        break;
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))//蓝牙扫描的广播：开始
            {
                Toast.makeText(context, "设备搜索中", Toast.LENGTH_SHORT).show();
                if (mClientCallback != null) {
                    mClientCallback.addBTDevice(null, true);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))//蓝牙扫描的广播：结束
            {
                Toast.makeText(context, "设备搜索结束", Toast.LENGTH_SHORT).show();
                if (mClientCallback != null) {
                    mClientCallback.onUpdateSearchDevicesStatus(true);
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action))//用BroadcastReceiver来取得搜索结果
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mClientCallback != null) {
                    mClientCallback.addBTDevice(device, false);
                }
                ALog.d("ACTION_FOUND = " + device.getBondState() + ",deviceName = " + device.getName() + ",deviceAddress = " + device.getAddress());
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))//蓝牙配对的广播
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(context, device.getAddress() + "配对成功", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action))//蓝牙扫描状态(SCAN_MODE)发生改变
            {
            }
        }
    };
}

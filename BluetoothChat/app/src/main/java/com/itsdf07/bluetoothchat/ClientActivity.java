package com.itsdf07.bluetoothchat;

import java.util.ArrayList;
import java.util.Date;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothClientService;
import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothTools;
import com.itsdf07.bluetoothchat.bluetoothutil.TransmitBean;
import com.itsdf07.bluetoothchat.common.DeviceAdapter;
import com.itsdf07.bluetoothchat.common.log.ALog;

public class ClientActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener,
        BluetoothClientService.IClientCallBack {

    /**
     * 服务端与客户端是否连接成功的提示
     */
    private TextView mTvServerConnStatusTip;
    /**
     * 聊天内容展示
     */
    private EditText mEtChatContent;
    /**
     * 聊天内容输入框
     */
    private EditText mEtSendMsg;
    /**
     * 聊天发送按钮
     */
    private Button mBtnSendMsg;

    /**
     * 搜索蓝牙设备
     */
    private Button mBtnSearchServer;

    /**
     * 展示搜索到蓝牙设备的列表
     */
    private ListView mLvDevices;
    private DeviceAdapter mDeviceAdapter;

    ArrayList<BluetoothDevice> mDeviceData = new ArrayList();

    private BluetoothClientService mClientService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.d("...");
        setContentView(R.layout.activity_client);
        init();
    }

    private void init() {
        initView();
        initData();
        startClientService();
    }

    private void initView() {
        mTvServerConnStatusTip = (TextView) findViewById(R.id.tv_clientConnTip);
        mEtChatContent = (EditText) findViewById(R.id.et_clientChatContent);
        mEtSendMsg = (EditText) findViewById(R.id.et_clientSendMsg);
        mBtnSendMsg = (Button) findViewById(R.id.btn_clientSendMsg);
        mBtnSendMsg.setOnClickListener(onClickListener);
        mBtnSearchServer = (Button) findViewById(R.id.btn_searchServer);
        mBtnSearchServer.setOnClickListener(onClickListener);

        mLvDevices = (ListView) findViewById(R.id.lv_devices);
        mDeviceAdapter = new DeviceAdapter(this, mDeviceData);
        mLvDevices.setAdapter(mDeviceAdapter);
        mLvDevices.setOnItemClickListener(this);
    }

    private void initData() {
    }

    /**
     * 启动服务
     */
    private void startClientService() {
        // 开启后台service
        Intent startService = new Intent(ClientActivity.this, BluetoothClientService.class);
        //绑定Service
        bindService(startService, mClientServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        if (mClientService != null) {
            mClientService.onCancelDiscvery();
            mClientService = null;
        }
        unbindService(mClientServiceConn);
        super.onDestroy();
    }


    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_clientSendMsg:
                    String msgContent = mEtSendMsg.getText().toString().trim();
                    ALog.d("msgContent = %s", msgContent);
                    // 发送消息
                    if (TextUtils.isEmpty(msgContent)) {
                        Toast.makeText(ClientActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        // 发送消息
                        TransmitBean data = new TransmitBean();
                        data.setMsg(msgContent);
                        mClientService.onSendMsg(data);

                        msgContent = "to remote " + new Date().toLocaleString()
                                + " :\r\n" + msgContent + "\r\n";
                        mEtChatContent.append(msgContent);
                    }
                    break;
                case R.id.btn_searchServer:
                    mClientService.onFoundBTDevice();
                    break;
            }
        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mClientService.onConnectBTDevice(mDeviceData.get(position));
    }

    ServiceConnection mClientServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            ALog.d(" ...");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            mClientService = ((BluetoothClientService.ClientServiceBinder) service).getService();
            if (mClientService != null) {
                mClientService.setClientCallback(ClientActivity.this);
            }
        }
    };

    @Override
    public void addBTDevice(BluetoothDevice device, boolean needClear) {
        if (needClear) {
            if (mDeviceData.size() > 0) {
                mDeviceData.clear();
            }
        } else {
            if (device == null) {
                return;
            }
            mDeviceData.add(device);
        }
        mDeviceAdapter.setData(mDeviceData);
    }

    @Override
    public void onUpdateSearchDevicesStatus(boolean enable) {

    }

    @Override
    public void onNotifyConnectResult(Object object, String type) {
        if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(type)) {
            BluetoothDevice mBluetoothDevice = ((BluetoothSocket) object).getRemoteDevice();
            // 连接成功
            mTvServerConnStatusTip.setText("连接成功:" + mBluetoothDevice.getName() + "->" + mBluetoothDevice.getAddress());
        } else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(type)) {
            // 接收数据
            TransmitBean data = (TransmitBean) object;
            String msg = "from server " + new Date().toLocaleString()
                    + " :\r\n" + data.getMsg() + "\r\n";
            mEtChatContent.append(msg);
        } else if (BluetoothTools.ACTION_CONNECT_ERROR.equals(type)) {
            mEtChatContent.append("连接失败\r\n");
        }
    }
}
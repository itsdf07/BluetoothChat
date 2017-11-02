package com.itsdf07.bluetoothchat;

import java.util.Date;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothServerService;
import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothTools;
import com.itsdf07.bluetoothchat.bluetoothutil.TransmitBean;
import com.itsdf07.bluetoothchat.common.log.ALog;


public class ServerActivity extends AppCompatActivity implements BluetoothServerService.IServerCallback {
    /**
     * 服务端与客户端是否连接成功的提示
     */
    private TextView mTvClientConnStatusTip;
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

    private BluetoothServerService mServerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ALog.d("...");
        setContentView(R.layout.activity_server);
        init();
    }

    private void init() {
        initView();
        initData();
    }

    private void initView() {
        mTvClientConnStatusTip = (TextView) findViewById(R.id.tv_serverConnTip);
        mTvClientConnStatusTip.setText("等待连接...");

        mEtChatContent = (EditText) findViewById(R.id.et_serverChatContent);

        mEtSendMsg = (EditText) findViewById(R.id.et_serverSendMsg);

        mBtnSendMsg = (Button) findViewById(R.id.btn_serverSendMsg);
        mBtnSendMsg.setOnClickListener(onSendMsgClickListener);
        mBtnSendMsg.setEnabled(false);
    }

    private void initData() {
        // 开启后台service
        onStartServerService();
    }

    @Override
    protected void onStop() {
        ALog.d("...");
        // 关闭后台Service
        unbindService(mServerServiceConn);
        super.onStop();
    }

    private OnClickListener onSendMsgClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String msgContent = mEtSendMsg.getText().toString().trim();
            ALog.d("msgContent = %s", msgContent);
            if (TextUtils.isEmpty(msgContent)) {
                Toast.makeText(ServerActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
            } else {
                // 发送消息
                TransmitBean data = new TransmitBean();
                data.setMsg(mEtSendMsg.getText().toString());
                mServerService.onSendMsg(data);

                msgContent = "to client " + new Date().toLocaleString()
                        + " :\r\n" + msgContent + "\r\n";
                mEtChatContent.append(msgContent);
            }
        }
    };


    /**
     * 启动服务
     */
    private void onStartServerService() {
        // 开启后台service
        Intent startService = new Intent(ServerActivity.this, BluetoothServerService.class);
        //绑定Service
        bindService(startService, mServerServiceConn, Context.BIND_AUTO_CREATE);
    }


    ServiceConnection mServerServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            ALog.d(" ...");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            mServerService = ((BluetoothServerService.ServerServiceBinder) service).getService();
            if (mServerService != null) {
                mServerService.setServerCallback(ServerActivity.this);
            }
        }
    };

    @Override
    public void onNotifyConnectResult(Object object, String type) {
        if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(type)) {
            BluetoothDevice mBluetoothDevice = ((BluetoothSocket) object).getRemoteDevice();
            // 连接成功
            mTvClientConnStatusTip.setText("连接成功:" + mBluetoothDevice.getName() + "->" + mBluetoothDevice.getAddress());
            mBtnSendMsg.setEnabled(true);
        } else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(type)) {
            // 接收数据
            TransmitBean data = (TransmitBean) object;
            String msg = "from client " + new Date().toLocaleString()
                    + " :\r\n" + data.getMsg() + "\r\n";
            mEtChatContent.append(msg);
        } else if (BluetoothTools.ACTION_CONNECT_ERROR.equals(type)) {
            mEtChatContent.append("连接失败\r\n");
        }
    }
}

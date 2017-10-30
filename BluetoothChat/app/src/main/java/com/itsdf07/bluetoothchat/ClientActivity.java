package com.itsdf07.bluetoothchat;

import java.util.Date;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothClientService;
import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothTools;
import com.itsdf07.bluetoothchat.bluetoothutil.TransmitBean;
import com.itsdf07.bluetoothchat.common.log.ALog;


public class ClientActivity extends AppCompatActivity {

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
     * 未搜索到设备的次数统计
     */
    int mNotFoundCount = 1;

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
    }

    private void initView() {
        mTvServerConnStatusTip = (TextView) findViewById(R.id.tv_clientConnTip);
        mEtChatContent = (EditText) findViewById(R.id.et_clientChatContent);
        mEtSendMsg = (EditText) findViewById(R.id.et_clientSendMsg);
        mBtnSendMsg = (Button) findViewById(R.id.btn_clientSendMsg);
        mBtnSendMsg.setOnClickListener(onSendMsgClickListener);
    }

    private void initData() {
        // 开启后台service
        Intent startService = new Intent(this, BluetoothClientService.class);
        startService(startService);

        // 注册BoradcasrReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);
        intentFilter.addAction(BluetoothTools.ACTION_FOUND_DEVICE);
        intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
        intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
        intentFilter.addAction(BluetoothTools.ACTION_CONNECT_ERROR);

        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        ALog.d("...");
        // 关闭后台Service
        Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
        sendBroadcast(startService);

        unregisterReceiver(mBroadcastReceiver);
        super.onStop();
    }

    private OnClickListener onSendMsgClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String msgContent = mEtSendMsg.getText().toString().trim();
            ALog.d("msgContent = %s", msgContent);
            // 发送消息
            if (TextUtils.isEmpty(msgContent)) {
                Toast.makeText(ClientActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
            } else {
                // 发送消息
                TransmitBean data = new TransmitBean();
                data.setMsg(msgContent);
                Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
                sendDataIntent.putExtra(BluetoothTools.DATA, data);
                sendBroadcast(sendDataIntent);

                msgContent = "to remote " + new Date().toLocaleString()
                        + " :\r\n" + msgContent + "\r\n";
                mEtChatContent.append(msgContent);
            }
        }
    };

    // 广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ALog.d("action = %s", action);
            if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
                // 未发现设备
                mTvServerConnStatusTip.append("未发现设备" + mNotFoundCount + "次\r\n");
                mNotFoundCount++;
            } else if (BluetoothTools.ACTION_FOUND_DEVICE.equals(action)) {
                // 获取到设备对象
                BluetoothDevice device = (BluetoothDevice) intent.getExtras().get(BluetoothTools.DEVICE);

                String address = device.getAddress();
                String name = device.getName();
                Log.e(BluetoothTools.TAG, "name =" + name + ",address = " + address);
            } else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
                // 连接成功
                mTvServerConnStatusTip.append("连接成功");
                mBtnSendMsg.setEnabled(true);
            } else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
                // 接收数据
                TransmitBean data = (TransmitBean) intent.getExtras()
                        .getSerializable(BluetoothTools.DATA);
                String msg = "from server " + new Date().toLocaleString()
                        + " :\r\n" + data.getMsg() + "\r\n";
                mEtChatContent.append(msg);

            } else if (BluetoothTools.ACTION_CONNECT_ERROR.equals(action)) {
                mEtChatContent.append("连接失败");
            }
        }
    };
}
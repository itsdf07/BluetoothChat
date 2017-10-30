package com.itsdf07.bluetoothchat;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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


public class ServerActivity extends AppCompatActivity {
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
        Intent startService = new Intent(this, BluetoothServerService.class);
        startService(startService);

        // 注册BoradcasrReceiver
        IntentFilter intentFilter = new IntentFilter();
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

    // 广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
                // 接收数据
                TransmitBean data = (TransmitBean) intent.getExtras().getSerializable(BluetoothTools.DATA);
                String msg = "from client " + new Date().toLocaleString()
                        + " :\r\n" + data.getMsg() + "\r\n";
                mEtChatContent.append(msg);

            } else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
                // 连接成功
                mTvClientConnStatusTip.setText("连接成功");
                mBtnSendMsg.setEnabled(true);
            } else if (BluetoothTools.ACTION_CONNECT_ERROR.equals(action)) {
                //连接失败
                mTvClientConnStatusTip.setText("连接失败");
            }

        }
    };


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
                Intent sendDataIntent = new Intent(
                        BluetoothTools.ACTION_DATA_TO_SERVICE);
                sendDataIntent.putExtra(BluetoothTools.DATA, data);
                sendBroadcast(sendDataIntent);

                msgContent = "to client " + new Date().toLocaleString()
                        + " :\r\n" + msgContent + "\r\n";
                mEtChatContent.append(msgContent);
            }
        }
    };
}

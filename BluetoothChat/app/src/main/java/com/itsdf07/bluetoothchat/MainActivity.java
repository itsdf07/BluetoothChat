package com.itsdf07.bluetoothchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.itsdf07.bluetoothchat.bluetoothutil.BluetoothTools;
import com.itsdf07.bluetoothchat.common.log.ALog;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initALog();
    }

    /**
     * 启动时就需要优先初始化的事情
     */
    private void initALog() {
        ALog.init()
                .setTag(BluetoothTools.TAG)
                .setLog(true)
                .setLog2Local(false);
        ALog.d("...");
    }

    /**
     * 启动聊天服务器
     *
     * @param view
     */
    public void onStartService(View view) {
        ALog.d("...");
        // 打开服务器
        Intent serverIntent = new Intent(this, ServerActivity.class);
        serverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(serverIntent);
    }

    /**
     * 启动聊天客户端
     *
     * @param view
     */
    public void onStartClient(View view) {
        ALog.d("...");
        // 打开客户端
        Intent clientIntent = new Intent(this, ClientActivity.class);
        clientIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(clientIntent);
    }
}

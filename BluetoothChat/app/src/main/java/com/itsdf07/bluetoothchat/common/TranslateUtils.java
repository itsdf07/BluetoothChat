package com.itsdf07.bluetoothchat.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.media.AudioManager;

/**
 * 转换工具类
 * 1、int转换成对应String：可以用于提示
 * 2、String转换成对应int：
 * Created by itsdf07 on 2017/9/14.
 */

public class TranslateUtils {

    public static String getBluetoothStateTip(int state) {
        String tip = "";
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                tip = "蓝牙已关闭";
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                tip = "蓝牙打开中...";
                break;
            case BluetoothAdapter.STATE_ON:
                tip = "蓝牙已打开";
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                tip = "蓝牙关闭中...";
                break;
            default:
                break;
        }
        return tip;
    }

    /**
     * 蓝牙状态String提示
     *
     * @param headsetState
     * @return
     */
    public static String getBluetoothHeadsetStateTip(int headsetState) {
        String tip = "";
        switch (headsetState) {
            case BluetoothHeadset.STATE_DISCONNECTED:
                tip = "蓝牙未连接";
                break;
            case BluetoothHeadset.STATE_CONNECTING:
                tip = "蓝牙正在连接中...";
                break;
            case BluetoothHeadset.STATE_CONNECTED:
                tip = "蓝牙已连接";
                break;
            case BluetoothHeadset.STATE_DISCONNECTING:
                tip = "蓝牙正在断开中...";
                break;
            default:
                break;
        }
        return tip;
    }

    /**
     * 蓝牙SCO状态String提示
     *
     * @param scoState
     */
    public static String getBluetoothScoAudioStateTip(int scoState) {
        String tip = "";
        switch (scoState) {
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED://连接失败
                tip = "蓝牙SCO未连接";
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTED://连接成功
                tip = "蓝牙SCO已连接";
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTING://连接过程
                tip = "蓝牙SCO正在连接中...";
                break;
            case AudioManager.SCO_AUDIO_STATE_ERROR://状态异常
                tip = "蓝牙SCO状态异常";
            default:
                break;
        }
        return tip;
    }

    /**
     * PTT按键状态String提示
     *
     * @param enablePtt
     * @return
     */
    public static String getEnablePttTip(boolean enablePtt) {
        return enablePtt ? "PTT正常" : "PTT不可用";
    }

}

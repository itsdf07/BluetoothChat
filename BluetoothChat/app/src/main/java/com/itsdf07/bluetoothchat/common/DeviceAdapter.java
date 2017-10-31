package com.itsdf07.bluetoothchat.common;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.itsdf07.bluetoothchat.R;
import com.itsdf07.bluetoothchat.common.bean.DeviceBean;

import java.util.ArrayList;

/**
 * Created by itsdf07 on 2017/9/16.
 */

public class DeviceAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<BluetoothDevice> mDeviceData = new ArrayList();

    public DeviceAdapter(Context context, ArrayList<BluetoothDevice> data) {
        mContext = context;
        setData(data);
    }

    public void setData(ArrayList<BluetoothDevice> data) {
        if (mDeviceData == null) {
            mDeviceData = new ArrayList<>();
        } else {
            mDeviceData.clear();
        }
        mDeviceData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDeviceData == null ? 0 : mDeviceData.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceData == null ? null : mDeviceData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_deviceinfo, parent, false);
        }
        BluetoothDevice device = mDeviceData.get(position);
        TextView deviceName = CommonViewHolder.get(convertView, R.id.tv_deviceName);
        TextView deviceMac = CommonViewHolder.get(convertView, R.id.tv_deviceMac);
        deviceName.setText(TextUtils.isEmpty(device.getName()) ? "null" : device.getName());
        deviceMac.setText(device.getAddress());
        return convertView;
    }
}

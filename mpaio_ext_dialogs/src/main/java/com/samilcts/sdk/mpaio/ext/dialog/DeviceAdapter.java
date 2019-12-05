package com.samilcts.sdk.mpaio.ext.dialog;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * @author mskim
 * ble device list adpater
 */

class DeviceAdapter extends BaseAdapter {
    private static final String TAG = "DeviceAdapter";
    public static final String KEY_DEVICE = "device";
    public static final String KEY_RSSI = "rssi";
    private final List<HashMap<String, ?>> deviceList;

    private final LayoutInflater inflater;

    public DeviceAdapter(Context context, List<HashMap<String, ?>> deviceList) {
        inflater = LayoutInflater.from(context);

        this.deviceList = deviceList;

    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {

        return deviceList.get(position);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewGroup vg;

        if (convertView != null) {
            vg = (ViewGroup) convertView;
        } else {
            vg = (ViewGroup) inflater.inflate(R.layout.ble_device_element, null);
        }

        HashMap<String,?> deviceMap = deviceList.get(position);


        BluetoothDevice device = (BluetoothDevice)deviceMap.get(KEY_DEVICE);
        int rssi = (Integer)deviceMap.get(KEY_RSSI);


        final TextView tvPaired = (TextView)vg.findViewById(R.id.paired);
        final TextView tvAdd = (TextView) vg.findViewById(R.id.address);
        final TextView tvName = (TextView) vg.findViewById(R.id.name);
        final TextView tvRSSI = (TextView) vg.findViewById(R.id.rssi);

        tvName.setText(device.getName());
        tvAdd.setText(device.getAddress());
        tvRSSI.setText(""+rssi);


        if (device.getBondState() == BluetoothDevice.BOND_BONDED)
             tvPaired.setVisibility(View.VISIBLE);
        else
            tvPaired.setVisibility(View.INVISIBLE);


        return vg;
    }
}

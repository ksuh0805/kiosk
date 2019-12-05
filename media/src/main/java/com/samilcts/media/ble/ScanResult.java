package com.samilcts.media.ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public class ScanResult {

    private final BluetoothDevice bleDevice;
    private final int rssi;
    private final byte[] scanRecord;

    public ScanResult(BluetoothDevice bleDevice, int rssi, byte[] scanRecords) {
        this.bleDevice = bleDevice;
        this.rssi = rssi;
        this.scanRecord = scanRecords;
    }

    /**
     * Returns {@link ScanResult} which is a handle for Bluetooth operations on a device. It may be used to establish connection,
     * get MAC address and/or get the device name.
     */
    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    /**
     * Returns signal strength indication received during scan operation.
     *
     * @return the rssi value
     */
    public int getRssi() {
        return rssi;
    }

    /**
     * The scan record of Bluetooth LE advertisement.
     *
     * @return Array of data containing full ADV packet.
     */
    public byte[] getScanRecord() {
        return scanRecord;
    }
}
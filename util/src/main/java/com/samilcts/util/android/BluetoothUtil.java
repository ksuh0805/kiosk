package com.samilcts.util.android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Set;

/**
 * Created by mskim on 2015-07-14.
 * mskim@31cts.com
 */
public class BluetoothUtil {

    private final BluetoothAdapter adapter;

    /**
     *
     * @param context context
     * @throws IllegalStateException if bt adapter can't created
     */
    public BluetoothUtil(Context context) throws IllegalStateException {

        adapter = getAdapter(context);

        if (adapter == null) {

            throw new IllegalStateException("Unable to obtain a BluetoothAdapter.");

        }

    }


    /**
     * {@link BluetoothAdapter#startDiscovery}
     * @return false, if adapter is not initiate or not enabled
     */

    public boolean startScan() {

        return adapter != null && adapter.isEnabled() && adapter.startDiscovery();

    }

    /**
     * {@link BluetoothAdapter#cancelDiscovery}
     * @return false, if adapter is not initiate or not enabled
     */

    public boolean stopScan() {

        return !(adapter != null && adapter.enable()) || adapter.cancelDiscovery();

    }

    /**
     * {@link BluetoothAdapter#isEnabled}
     */
    public boolean isBluetoothOn() {

        return adapter != null && adapter.isEnabled();
    }


    /**
     * {@link BluetoothAdapter#enable}
     */

    public boolean turnOn() {

        return adapter != null && adapter.enable();
    }

    /**
     * create bluetooth adapter by android version
     * @param context context
     * @return the default local adapter, or null if Bluetooth is not supported on this hardware platform
     */
    private BluetoothAdapter getAdapter(Context context) {

        BluetoothAdapter adapter = null;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {

            BluetoothManager bluetoothManager =  (BluetoothManager)context
                    .getSystemService(Context.BLUETOOTH_SERVICE);

            if (bluetoothManager != null) {

               adapter = bluetoothManager.getAdapter();
            }

        } else {

            adapter = BluetoothAdapter.getDefaultAdapter();
        }

        return adapter;

    }


    /**
     * request for turning on bluetooth to user
     * @param context context
     * @param requestNumber request number for {@link Activity#onActivityResult(int, int, Intent)}
     * @return false, if bluetooth already enabled or fail to request.
     */

    public boolean requestTurningBluetoothOn(Context context, int requestNumber) {

        if ( adapter.isEnabled())
            return true;

        try {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity) context).startActivityForResult(enableBtIntent, requestNumber);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }

    }

    /**
     * call etBondedDevices. if not available bluetooth, return null
     * @see BluetoothAdapter#getBondedDevices
     * @return paired bluetooth device
     */

    public Set<BluetoothDevice> getBondedDevices() {

        if (adapter != null ) {

            return adapter.getBondedDevices();
        }

        return null;
    }

    /**
     * Get filtered device with address
     * @param address find address
     * @return the device or null.
     */

    public BluetoothDevice getBondedDevice(String address) {

        for (BluetoothDevice device :
                getBondedDevices()) {

            if (null != address && address.equals(device.getAddress())) {

                return device;
            }
        }

        return null;
    }


}

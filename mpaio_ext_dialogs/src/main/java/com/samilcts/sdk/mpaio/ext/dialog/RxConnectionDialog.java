package com.samilcts.sdk.mpaio.ext.dialog;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.samilcts.media.ble.ScanResult;
import com.samilcts.sdk.mpaio.ConnectionManager;
import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.util.android.BluetoothUtil;
import com.samilcts.util.android.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;


/**
 * dialog class for connect with mpaio using usb or ble
 */
public class RxConnectionDialog extends AppCompatDialog {


    private static final String TAG = "RxConnectionDialog";
    public static int BT_REQUEST_NUMBER = 100;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 101;


    private final Context mContext;
    private final ConnectionManager mConnectionManager;

    private ProgressBar progBleScan;
    private TextView tvUsbMsg;
    private BluetoothUtil btUtil;
    private ListView bleList;
    private Button btnOk;
    private TextView tvBleMsg;
    private RadioButton rbBLE;
    private RadioButton rbUSB;
    private Subscription mScanSubscription;


    /**
     *
     * @param context application context
     *
     */
    public RxConnectionDialog(Context context, ConnectionManager connectionManager) {

        super(context, R.style.Theme_Mpos_ConnectionDialog);

        mContext = context;
        mConnectionManager = connectionManager;

        try {
            btUtil = new BluetoothUtil(context);
        } catch ( IllegalStateException e) {
            e.printStackTrace();
        }


    }

    public RxConnectionDialog(Context context, ConnectionManager connectionManager, int theme) {
        super(context, theme);

        mContext = context;
        mConnectionManager = connectionManager;


        try {
            btUtil = new BluetoothUtil(context);
        } catch ( IllegalStateException e) {
            e.printStackTrace();
        }


    }





    private OnClickListener onOkListener;

    private final Logger logger = LogTool.getLogger();
    private OnClickListener okListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            mConnectionManager.connect()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {

                            dismiss();
                        }

                        @Override
                        public void onError(Throwable e) {
                            tvUsbMsg.setText(e.getMessage());
                            tvUsbMsg.setVisibility(View.VISIBLE);
                            logger.w(TAG, "onError : " + e.getMessage());
                        }

                        @Override
                        public void onNext(Void aVoid) {

                        }
                    });

        }
    };
    AdapterView.OnItemClickListener onBleClickListener;

    AdapterView.OnItemClickListener bleClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if ( mScanSubscription != null)
                mScanSubscription.unsubscribe();

            progBleScan.setVisibility(View.INVISIBLE);

            connectBLE(position);


        }
    };


    /**
     * set usb connect button's click listener
     * @param l listener
     * @param replaceDefault if true, replace default click listener
     */

    public void setOnOkListener(OnClickListener l, boolean replaceDefault) {

        if ( replaceDefault) {
            okListener = l;
            onOkListener = null;
        } else {
            onOkListener = l;
        }

    }

    /**
     * set ble device click listener
     * @param l listener
     * @param replaceDefault if true, replace default click listener
     */
    public void setOnBleDeviceClickListener(AdapterView.OnItemClickListener l, boolean replaceDefault) {

        if ( replaceDefault) {
            bleClickListener = l;
            onBleClickListener = null;
        } else {
            onBleClickListener = l;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.connect_dialog);


        mDeviceAdapter = new DeviceAdapter(mContext, scannedMpaioList);
        //setCanceledOnTouchOutside(false);


        final RadioGroup group = (RadioGroup) findViewById(R.id.select_connection);

        rbUSB = (RadioButton) findViewById(R.id.rbUSB);
        rbBLE = (RadioButton) findViewById(R.id.rbBLE);
        bleList = (ListView) findViewById(R.id.select_connection_devices);
        btnOk = (Button) findViewById(R.id.btnOk);

        tvBleMsg = (TextView) findViewById(R.id.tvBleMsg);
        tvBleMsg.setTextColor(0xFFFF0000);

        tvUsbMsg = (TextView) findViewById(R.id.tvUsbMsg);
        tvUsbMsg.setTextColor(0xFFFF0000);

        progBleScan = (ProgressBar) findViewById(R.id.progBleScan);


        setBleMsg();


        bleList.setAdapter(mDeviceAdapter);

        bleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (onBleClickListener != null) {
                    onBleClickListener.onItemClick(parent, view, position, id);
                }

                if (bleClickListener != null) {
                    bleClickListener.onItemClick(parent, view, position, id);
                }

            }
        });




        //mContext.registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));



        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (null == mConnectionManager) return;


                if (checkedId == R.id.rbUSB) {

                    tvBleMsg.setVisibility(View.INVISIBLE);
                    bleList.setVisibility(View.GONE);
                    btnOk.setVisibility(View.VISIBLE);

                    progBleScan.setVisibility(View.INVISIBLE);

                    //connectionManager.scanLeDevice(false);

                    if ( null != mScanSubscription )
                        mScanSubscription.unsubscribe();

                    bleList.setVisibility(View.INVISIBLE);

                    scannedMpaioList.clear();
                    mDeviceAdapter.notifyDataSetChanged();

                } else if (checkedId == R.id.rbBLE) {

                    tvUsbMsg.setVisibility(View.INVISIBLE);
                    bleList.setVisibility(View.VISIBLE);
                    btnOk.setVisibility(View.GONE);

                    setBleMsg();

                    if (btUtil.isBluetoothOn()) {

                        if ( !hasPermissionForScan()) {
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            return;
                        }

                        startScan();

                    } else if (!btUtil.requestTurningBluetoothOn(mContext, BT_REQUEST_NUMBER)){

                        tvBleMsg.setText(R.string.mpos_sdk_bluetooth_fail_turn_on);
                        tvBleMsg.setVisibility(View.VISIBLE);
                        rbBLE.setEnabled(false);
                        bleList.setVisibility(View.INVISIBLE);

                    }

                }
            }
        });





        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int checkedId = group.getCheckedRadioButtonId();

                if (checkedId == R.id.rbUSB) {

                    if (onOkListener != null) {
                        onOkListener.onClick(RxConnectionDialog.this, 0);
                    }

                    if (okListener != null) {
                        okListener.onClick(RxConnectionDialog.this, 0);
                    }

                }

            }
        });





    }


    /**
     * check whether required permissions are granted.
     * @return true, if granted.
     */
    private boolean hasPermissionForScan() {

        if ( Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {

            int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            int permissionCheck2 = ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (  PackageManager.PERMISSION_GRANTED != permissionCheck &&
                    PackageManager.PERMISSION_GRANTED != permissionCheck2) {

                return false;
            }

        }

        return true;
    }

    /**
     * set ble message according to bluetooth state
     */
    private void setBleMsg() {

        if (btUtil == null) {

            tvBleMsg.setText(R.string.mpos_sdk_bluetooth_not_supported);
            tvBleMsg.setVisibility(View.VISIBLE);
            rbBLE.setEnabled(false);

            bleList.setVisibility(View.INVISIBLE);

        } else if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            //BLE not supported
            final RadioButton rbBLE = (RadioButton) findViewById(R.id.rbBLE);
            rbBLE.setEnabled(false);
            tvBleMsg.setText(R.string.mpos_sdk_ble_not_available);
            tvBleMsg.setVisibility(View.VISIBLE);
            bleList.setVisibility(View.INVISIBLE);
        } else if ( !btUtil.isBluetoothOn() ){
            scannedMpaioList.clear();
            bleList.setVisibility(View.INVISIBLE);
            progBleScan.setVisibility(View.INVISIBLE);

            tvBleMsg.setText(R.string.mpos_sdk_bluetooth_not_turn_on);
            tvBleMsg.setVisibility(View.VISIBLE);

        } else {

            tvBleMsg.setVisibility(View.INVISIBLE);
            bleList.setVisibility(View.VISIBLE);
        }
    }


    /**
     * start scan
     */
    private void startScan() {

        if ( rbBLE.isChecked()) {

            scannedMpaioList.clear();
            logger.i(TAG, "start scan");
            if ( null != mScanSubscription && !mScanSubscription.isUnsubscribed())
                mScanSubscription.unsubscribe();

            mScanSubscription = mConnectionManager.scan()
                    .throttleFirst(200, TimeUnit.MICROSECONDS)
                    .take(10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {

                            progBleScan.setVisibility(View.VISIBLE);
                        }
                    })
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {

                            progBleScan.setVisibility(View.INVISIBLE);
                        }
                    })
                    .subscribe(new Subscriber<ScanResult>() {
                        @Override
                        public void onCompleted() {

                            logger.i(TAG, "scan onCompleted");
                        }

                        @Override
                        public void onError(Throwable e) {

                            progBleScan.setVisibility(View.INVISIBLE);
                            logger.i(TAG, "scan onError : " + e.toString()+ "/" + e.getMessage());
                        }

                        @Override
                        public void onNext(ScanResult scanResult) {

                            addDevice(scanResult.getBleDevice(), scanResult.getRssi());
                        }
                    });

        }

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        mContext.registerReceiver(btReceiver, filter);

        setBleMsg();
    }

    @Override
    public void onDetachedFromWindow() {


        if ( null != mScanSubscription )
            mScanSubscription.unsubscribe();

        mContext.unregisterReceiver(btReceiver);

        // scannedMpaioList.removeKeys();

        bleList.setVisibility(View.INVISIBLE);
        progBleScan.setVisibility(View.INVISIBLE);
        tvBleMsg.setText("");
        tvBleMsg.setVisibility(View.INVISIBLE);

        super.onDetachedFromWindow();
    }


    /**
     *  connect with ble.
     *
     *  @param position device position
     */

    private void connectBLE(int position) {

        dismiss();

        BluetoothDevice device = (BluetoothDevice) scannedMpaioList.get(position).get(DeviceAdapter.KEY_DEVICE);
        mConnectionManager.connect(device.getAddress(), false)
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        logger.i(TAG, "Error : " + e.getMessage());
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });

    }


    private final ArrayList<HashMap<String, ?>> scannedMpaioList = new ArrayList<>();

    private DeviceAdapter mDeviceAdapter;

    /**
     * add device to the list
     * @param device updated device
     * @param rssi rssi of device
     */

    private void addDevice(final BluetoothDevice device, int rssi) {


        Iterator iterator = scannedMpaioList.iterator();

        int index = scannedMpaioList.size();

        while(iterator.hasNext()) {

            HashMap<String, Object> map = (HashMap<String, Object>)iterator.next();

            BluetoothDevice bleDevice = (BluetoothDevice)map.get(DeviceAdapter.KEY_DEVICE);

            if ( bleDevice.getAddress().equals(device.getAddress())) {
                index = scannedMpaioList.indexOf(map);
                updateView(index, ""+rssi);
                return;
            }

        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(DeviceAdapter.KEY_DEVICE, device);
        map.put(DeviceAdapter.KEY_RSSI, rssi);
        scannedMpaioList.add(index, map);
        mDeviceAdapter.notifyDataSetChanged();


    }


    /**
     * bt on, off receiver. change scan state and message.
     */
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                setBleMsg();

                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON) {

                    startScan();

                }
            }
        }
    };

    /**
     * according to permissions, start scan or select connection type to usb
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public void onRequestPermissionResult(int requestCode , String permissions[], final int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_ACCESS_COARSE_LOCATION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();

                } else {

                    rbUSB.setChecked(true);
                    rbBLE.setChecked(false);
                }

           }
        }
    }



    private void updateView(int index, String rssi){
        View v = bleList.getChildAt(index);

        if(v == null)
            return;

        final TextView tvRssi = (TextView) v.findViewById(R.id.rssi);
        tvRssi.setText(rssi);
    }


}
package com.samilcts.mpaio.testtool;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.ext.nice.MpaioNiceManager;
import com.samilcts.util.android.Logger;

import rx.functions.Func1;


public class MpaioService extends Service {
    private static final String TAG = "DeviceService";
    //public static final String SW_MODEL = "";
    public static final String SW_MODEL = "####KMPOSAND1001";
    private final IBinder mBinder = new LocalBinder();
    private Context mContext;

    private MpaioNiceManager mDeviceManager;

    private final Logger logger = AppTool.getLogger();

    private final byte[] key1 = new byte[]{ (byte)0xe8, (byte)0xd1, (byte)0xaf, (byte)0xbd, (byte)0x84, (byte)0x06, (byte)0xb1,
            (byte)0xfe, (byte)0xaa, (byte)0xd1, (byte)0xad, (byte)0x62, (byte)0xfe, (byte)0xcc, (byte)0x5c, (byte)0x0f};
    private final byte[] key2 = new byte[16];
    private final byte[] key3 = new byte[16];

    public MpaioService() {

        if ( BuildConfig.FLAVOR.equals("dev")) {
            System.arraycopy(new byte[]{(byte)0x46, (byte)0xe6, (byte)0xed, (byte)0xea, (byte)0x39, (byte)0xb6, (byte)0x3c, (byte)0x8a,
                    (byte)0xe6, (byte)0x22, (byte)0x91, (byte)0x95, (byte)0xfc, (byte)0x01, (byte)0x01, (byte)0xa4}, 0, key2, 0, 16);
            System.arraycopy(new byte[]{(byte)0xce, (byte)0xc7, (byte)0x01, (byte)0x52, (byte)0x31, (byte)0xfd, (byte)0xa2, (byte)0x2f,
                    (byte)0xc1, (byte)0xbc, (byte)0x47, (byte)0xfc, (byte)0xdc, (byte)0xe6, (byte)0xea, (byte)0x39}, 0, key3, 0, 16);
        }

    }

    synchronized public MpaioNiceManager getDeviceManager() {

        if ( mDeviceManager == null) {

            mDeviceManager = new MpaioNiceManager(mContext);
            mDeviceManager.setAuthenticationParameter(key1, 1, SW_MODEL);
        }

        mDeviceManager.useRealVanServer(false);
        return mDeviceManager;
    }


    public void setKey(int i) {

        if ( 1 == i)
            mDeviceManager.setAuthenticationParameter(key1, 1, SW_MODEL);
        else if ( 2 == i)
            mDeviceManager.setAuthenticationParameter(key2, 2, SW_MODEL);
        else if ( 3 == i)
            mDeviceManager.setAuthenticationParameter(key3, 3, SW_MODEL);
        else
            mDeviceManager.setAuthenticationParameter(new byte[16], 0, SW_MODEL);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.i(TAG, "onCreate()");
        mContext = getBaseContext();

        mDeviceManager = getDeviceManager();
        mDeviceManager.setSerialDeviceName("UART0");
    }

    @Override
    public void onDestroy() {

        logger.i(TAG, "onDestroy()");

        if (mDeviceManager != null) {
            mDeviceManager.close();
            mDeviceManager = null;
        }

        super.onDestroy();
    }

    public void stop() {

         getDeviceManager().rxSendMessage(getDeviceManager().getNextAid(), new MpaioCommand(MpaioCommand.STOP).getCode(), null).
                 onErrorReturn(new Func1<Throwable, byte[]>() {
                     @Override
                     public byte[] call(Throwable throwable) {
                         return new byte[0];
                     }
                 })
                 .subscribe();

    }

    public void disconnect() {

        if ( mDeviceManager != null)
            mDeviceManager.disconnect();
    }

    public boolean isConnected() {
        return getDeviceManager().isConnected();
    }

    public boolean isBleConnected() {
        return getDeviceManager().isBleConnected();
    }

    public boolean isUsbConnected() {
        return getDeviceManager().isUsbConnected();
    }

       public class LocalBinder extends Binder {
        public MpaioService getService() {
            return MpaioService.this;
        }
    }

}

package com.samilcts.printer.android;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.woosim.android.print.BluetoothPrintService;
import com.woosim.printer.WoosimCmd;
import com.woosim.printer.WoosimImage;
import com.woosim.printer.WoosimService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by mskim on 2015-11-30.
 */
public class Woosim implements Printer {

    private final static String TAG = "Woosim";

    private final BluetoothPrintService mPrinter;

    ConnectionState mState = ConnectionState.NONE;
    private StateChangeListener stateChangeListener;

    private Context mContext;
    private static final String CHAR_SET = "EUC-KR";

    public Woosim(final Context context) {

        mContext = context;
        mState = ConnectionState.NONE;

        Handler handler = new WoosimHandler();

        mPrinter = new BluetoothPrintService(context, handler);
        mPrinter.start();
    }


    @Override
    public ConnectionState getState() {

        switch (mPrinter.getState()) {
            case BluetoothPrintService.STATE_CONNECTED:
                mState = ConnectionState.CONNECTED;
                break;

            case BluetoothPrintService.STATE_CONNECTING:
                mState = ConnectionState.CONNECTING;
                break;

            case BluetoothPrintService.STATE_LISTEN:
            case BluetoothPrintService.STATE_NONE:
                mState = ConnectionState.NONE;
                break;
        }

        return mState;

    }

    @Override
    public boolean isConnected() {
        return mState == ConnectionState.CONNECTED;
    }

    @Override
    public Model getModel() {
        return Model.WOOSIM;
    }

    @Override
    public boolean connect(final BluetoothDevice device) {

        if ( mState != ConnectionState.NONE)
            return true;


        mPrinter.connect(device, false);

        /*new Handler().post(new Runnable() {
            @Override
            public void run() {


            }
        });
*/
        mState = ConnectionState.CONNECTING;
        if ( stateChangeListener != null ) stateChangeListener.onConnecting();

        return true;
    }

    @Override
    public void disconnect() {

        mPrinter.start();

    }

    @Override
    public void close() {
        mPrinter.stop();
    }

    @Override
    public boolean printText(String text, Alignment alignment, boolean underline, TextSize textSize) {

        if ( mState != ConnectionState.CONNECTED) return false;

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try {

            byteStream.write(WoosimCmd.setTextAlign(alignment.ordinal()));
            byteStream.write(WoosimCmd.setTextStyle(false, underline, false, textSize.ordinal()+1, textSize.ordinal()+1));
            byteStream.write(text.getBytes(CHAR_SET));

            mPrinter.write(byteStream.toByteArray());

            return true;

        } catch (IOException e) {
            e.printStackTrace();


        }

        //set attribute

        return false;


    }

    @Override
    public boolean printImage(Bitmap bitmap) {

        if ( mState != ConnectionState.CONNECTED) return false;

        byte[] data = WoosimImage.fastPrintBitmap(0, 0, 384, 384, bitmap);

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try {

            byteStream.write(WoosimCmd.setPageMode());
            byteStream.write(data);
            byteStream.write(WoosimCmd.PM_setStdMode());

            mPrinter.write(byteStream.toByteArray());

            return true;

        } catch (IOException e) {
            e.printStackTrace();


        }

        return false;
    }

    @Override
    public void setStateChangeListener(StateChangeListener listener) {


        stateChangeListener = listener;

    }

    private class WoosimHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothPrintService.MESSAGE_DEVICE_NAME:
                    //연결됐을 때 알려줌.
                    mState = ConnectionState.CONNECTED;

                    mPrinter.write( WoosimCmd.initPrinter());
                    mPrinter.write(WoosimCmd.setCodeTable(WoosimCmd.MCU_RX, WoosimCmd.CT_DBCS, WoosimCmd.FONT_LARGE));


                    if ( stateChangeListener != null ) stateChangeListener.onConnected();

                    //msg.getData().getString(DEVICE_NAME);

                    break;
                case BluetoothPrintService.MESSAGE_TOAST:

                   // String failMsg = mContext.getString(R.string.connect_fail);

                    int resId =  msg.getData().getInt(BluetoothPrintService.TOAST);


                    if ( R.string.connect_fail == resId ||  R.string.connect_lost == resId ) {

                        mState = ConnectionState.NONE;
                        if ( stateChangeListener != null ) stateChangeListener.onDisconnected(R.string.connect_fail == resId );

                    }


                    //Toast.makeText(getApplicationContext(), msg.getData().getInt(TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothPrintService.MESSAGE_READ:
                    Log.i(TAG, "msg arg1 :" + msg.arg1);
                    Log.i(TAG, "msg arg obj :" + msg.obj);

                    //mWoosim.processRcvData((byte[])msg.obj, msg.arg1);
                    break;
                case WoosimService.MESSAGE_PRINTER:

                    Log.i(TAG, "msg arg1 :" + msg.arg1);
                    Log.i(TAG, "msg arg obj :" + msg.obj);

                    switch (msg.arg1) {

                        case WoosimService.MSR:

                            break;
                    }
                    break;
            }
        }

    }


    @Override
    public void lineFeed(int lines) {

        mPrinter.write(WoosimCmd.printLineFeed(lines));

    }
}


package com.samilcts.printer.android;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bixolon.printer.BixolonPrinter;

/**
 * Created by mskim on 2015-11-30.
 */
public class Bixolon implements Printer {

    private final static String TAG = "Bixolon";

    private final BixolonPrinter mPrinter;

    ConnectionState mState = ConnectionState.NONE;
    private StateChangeListener stateChangeListener;


    public Bixolon(final Context context) {

        mState = ConnectionState.NONE;

        Handler handler = new BixolonHandler();

        mPrinter = new BixolonPrinter(context, handler, null);


    }


    @Override
    public ConnectionState getState() {

        return mState;
    }

    @Override
    public boolean isConnected() {
        return mState == ConnectionState.CONNECTED;
    }

    @Override
    public Model getModel() {
        return Model.BIXOLON;
    }

    @Override
    public boolean connect(BluetoothDevice device) {

        if ( mState != ConnectionState.NONE)
            return true;

        mPrinter.connect(device.getAddress());

        return true;
    }

    @Override
    public void disconnect() {


        mPrinter.disconnect();

    }

    @Override
    public boolean printText(String text, Alignment alignment, boolean underline, TextSize textSize) {

        if ( mState != ConnectionState.CONNECTED) return false;

        //set attribute
        int attribute = 0;

        if (underline) {
            attribute |= BixolonPrinter.TEXT_ATTRIBUTE_UNDERLINE1;
        }


        //set text size
        int size = (textSize.ordinal() * 16); //horizontal
        size |= (textSize.ordinal()); //vertical

        mPrinter.printText(text, alignment.ordinal(), attribute, size, false);

        return true;


    }


    @Override
    public boolean printImage(Bitmap bitmap) {


        if ( mState != ConnectionState.CONNECTED) return false;

        mPrinter.printBitmap(bitmap, BixolonPrinter.ALIGNMENT_LEFT, BixolonPrinter.BITMAP_WIDTH_NONE , 60, true, true, false);

        return true;

    }

    @Override
    public void setStateChangeListener(StateChangeListener listener) {


        stateChangeListener = listener;

    }

    private class BixolonHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {


            switch (msg.what) {
                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:

                            mState = ConnectionState.CONNECTED;

                            if ( stateChangeListener != null ) stateChangeListener.onConnected();

                            break;

                        case BixolonPrinter.STATE_CONNECTING:

                            mState = ConnectionState.CONNECTING;

                            if ( stateChangeListener != null ) stateChangeListener.onConnecting();
                            break;

                        case BixolonPrinter.STATE_NONE:

                            boolean isFailConnect = false;

                            if ( mState == ConnectionState.CONNECTING) isFailConnect = true;

                            mState = ConnectionState.NONE;

                            if ( stateChangeListener != null ) stateChangeListener.onDisconnected(isFailConnect);
                            break;
                    }
            }
        }
    }


    @Override
    public void lineFeed(int lines) {

        mPrinter.lineFeed(lines, false);

    }

    @Override
    public void close() {

        mPrinter.disconnect();

    }
}


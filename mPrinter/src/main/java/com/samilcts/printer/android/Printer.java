package com.samilcts.printer.android;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;

/**
 * Created by mskim on 2015-11-30.
 * mskim@31cts.com
 */
public interface Printer {


    enum ConnectionState {

        NONE,
        CONNECTING,
        State, CONNECTED

    }


    enum Model {

        BIXOLON,
        WOOSIM;


        /**
         *
         * @return 일치하는 모델, 없으면 BIXOLON
         */
        public static Model fromOrdinal(int ordinal){

            for (Model model :
                    Model.values()) {

                if ( model.ordinal() == ordinal)
                    return model;
            }

            return BIXOLON;
        }
    }

    enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    enum TextSize {

        SIZE_1,
        SIZE_2,
        SIZE_3,
        SIZE_4,
        SIZE_5,
        SIZE_6,
        SIZE_7,
        SIZE_8

    }

    ConnectionState getState();

    boolean isConnected();

    Model getModel();

    boolean connect(BluetoothDevice device);

    void disconnect();

    void close();


    boolean printText(String text, Alignment alignment, boolean underline, TextSize textSize);

    boolean printImage(Bitmap bitmap);

    void lineFeed(int lines);

    void setStateChangeListener(StateChangeListener listener);




}

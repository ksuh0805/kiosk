package com.samilcts.sdk.mpaio.print;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;

/**
 * Created by mskim on 2017-12-20.
 * mskim@31cts.com
 */
public interface Printer {

    enum Model {

        ESC_POS;

        /**
         *
         * @return 일치하는 모델, 없으면 ESC_POS
         */
        public static Model fromOrdinal(int ordinal){

            for (Model model :
                    Model.values()) {

                if ( model.ordinal() == ordinal)
                    return model;
            }

            return ESC_POS;
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

    Model getModel();

    boolean printText(String text, Alignment alignment, boolean underline, TextSize textSize);

    boolean printImage(Bitmap bitmap);

    void lineFeed(int lines);


}

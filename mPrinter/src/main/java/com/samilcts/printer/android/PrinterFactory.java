package com.samilcts.printer.android;

import android.content.Context;


/**
 * Created by mskim on 2015-11-30.
 */
public class PrinterFactory {


    private PrinterFactory() {


    }


    public static Printer createPrinter(Context context, Printer.Model model) {

        switch (model) {

            case BIXOLON:

                return new Bixolon(context);

            case WOOSIM:
                return new Woosim(context);

        }

        return new Bixolon(context);

    }


}

package com.samilcts.app.mpaio.demo2.util;

import android.content.Context;
import android.content.ServiceConnection;

import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.printer.android.Printer;
import com.samilcts.printer.android.PrinterFactory;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.util.android.Preference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Created by mskim on 2015-11-25.
 * mskim@31cts.com
 */
public class SharedInstance {


    public static final String PREF_TRANSACTION_NUMBER = "mpaio2.pref.transaction.number";
    public static final String PREF_LAST_CONNECTED_PRINTER_ADDRESS = "mpaio2.pref.last.connected.printer.companyAddress";
    public static final String PREF_LAST_CONNECTED_PRINTER_MODEL = "mpaio2.pref.last.connected.printer.model";
    private static final String TAG = "SharedInstance";
    private static Context mContext;

    public  static String deviceModelName = "";

    public static MpaioManager mpaioManager;

    private static LinkedHashMap<Product, Integer> cartItems = new LinkedHashMap<>();

    public static Map<Printer.Model, Printer> mPrinterMap = new HashMap<>();

    public static Locale locale = Locale.KOREA;

    public static void init(Context context) {

        mContext = context;

    }

    /**
     * 설정에 따라 프린터를 가져온다.
     * 프린터 객체는 한 번만 생성된다.
     *
     * @return
     */
    synchronized public static Printer getPrinter(){

        int model = Preference.getInstance(mContext).get(SharedInstance.PREF_LAST_CONNECTED_PRINTER_MODEL, Printer.Model.WOOSIM.ordinal());
        return getPrinter(Printer.Model.fromOrdinal(model));

    }


    synchronized private static Printer getPrinter(Printer.Model model){

        Printer printer = mPrinterMap.get(model);

        if ( printer == null && mContext != null ) {

            printer = PrinterFactory.createPrinter(mContext, model);
            mPrinterMap.put(model, printer);
        }

        return printer;
    }


    public static void releasePrinters() {

        for (Printer printer : mPrinterMap.values()) {

            printer.close();
        }

    }


    public static LinkedHashMap<Product, Integer> getCartItems(){

        return cartItems;
    }

    public static void clearCartItem(){

        cartItems.clear();

    }

    public static void release() {

        clearCartItem();
        releasePrinters();

        if ( mpaioManager != null)
        {
            mpaioManager.close();
            mpaioManager = null;
        }
    }


    public static ArrayList<String> getInternalPrinterModels() {
        ArrayList<String> embededPrinterModels = new ArrayList<>();

        embededPrinterModels.add("PAYMGATE1");

        return embededPrinterModels;
    }
}

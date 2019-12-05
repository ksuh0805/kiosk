package com.samilcts.mpaio.testtool.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.samilcts.mpaio.testtool.LogItem;
import com.samilcts.mpaio.testtool.LogViewAdapter;
import com.samilcts.mpaio.testtool.data.CardInfo;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.ext.nice.error.NiceResponseError;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by mskim on 2016-07-13.
 * mskim@31cts.com
 */
public class Output {


    private final static Logger logger = AppTool.getLogger();

    public static void printError(RecyclerView view, String tag, Throwable throwable) {

        throwable.printStackTrace();

        String msg = throwable.getMessage();

        msg = null == msg ? throwable.toString() : msg;

        printText(view, "Error-"+tag , null, msg);

    }


    public static void printMessage(RecyclerView view, MpaioMessage message, boolean isOut) {

        String aid = Converter.toHexString(message.getAID());
        String cmd = Converter.toHexString(message.getCommandCode());

        byte[] temp = message.getData();

        byte[] data= new byte[0];

        if ( !isOut && temp.length > 1)
            data = Arrays.copyOfRange(temp, 1, temp.length);
        else if ( isOut )
            data = temp;

        String direction= isOut ? "SEND" : "RECV";
        String responseName = "";
        if ( !isOut && temp.length > 0) {
            NiceResponseError error = NiceResponseError.fromCode(temp[0]);
            responseName = error != null ? " Response : " + error.getName() : "";
        }

        printText(view, direction+"-message(aid:"+aid+", cmd:" + cmd + responseName + ")", message.getData(),  parseData(message, data, isOut));



    }

    private static String parseData(MpaioMessage mpaioMessage, byte[] data, boolean isOut) {

        DefaultParser parser = new DefaultParser();
        String text = "";

        MpaioNiceCommand command = new MpaioNiceCommand(mpaioMessage.getCommandCode());

        if ( isOut ) {

             if ( command.equals(MpaioNiceCommand.SET_DATE_TIME)) {

                text = "Date : " + parser.parseDate(data);

            }

        } else if ( command.equals(MpaioNiceCommand.NOTIFY_READ_MS_CARD)) {

            CardInfo info = parser.parseMSCardData(data);
            text = info.toString();

        } else if ( command.equals(MpaioNiceCommand.NOTIFY_READ_EMV_CARD)) {

            CardInfo info = parser.parseEMVCardData(data);
            text = info.toString();

        } else if ( command.equals(MpaioNiceCommand.NOTIFY_READ_RFID_CARD)) {

            CardInfo info = parser.parseRFIDCardData(data);
            text = "Data : " + info.number; //temp

        } else if ( command.equals(MpaioNiceCommand.NOTIFY_READ_PIN_PAD)) {

            text = parser.parsePinCode(data);

        }  else if ( command.equals(MpaioNiceCommand.NOTIFY_READ_BARCODE)) {

            text = "Barcode : " +parser.parseBarcodeData(data);

        } else if (command.equals(MpaioNiceCommand.GET_BLUETOOTH_ADDRESS)) {

            text = "Address : " +parser.parseBleAddress(data);

        } else if (command.equals(MpaioNiceCommand.GET_DATE_TIME)) {

            text = "Date : " + parser.parseDate(data);

        } else if (command.equals(MpaioNiceCommand.GET_FIRMWARE_VERSION)) {

            text = "F/W ver : " + parser.parseFirmwareVersion(data);

        }else if (command.equals(MpaioNiceCommand.GET_BATTERY_LEVEL)) {

            text = "Battery : " +parser.parseBatteryData(data);

        } else if (command.equals(MpaioNiceCommand.GET_DEVICE_NAME)) {

            text = "Device Name : " + new String(data);

        } else if (command.equals(MpaioNiceCommand.GET_HARDWARE_REVISION)) {

            text = "H/W rev : " + new String(data);

        } else if (command.equals(MpaioNiceCommand.GET_MODEL_NAME)) {

            text = "Model Name : " + new String(data);

        } else if (command.equals(MpaioNiceCommand.GET_SERIAL_NUMBER)) {

            text = "Serial Number : " + new String(data);
        }

        text = (isOut ? "Send " : "Receive ") + "["+command.getName().replaceAll("_"," ") +"]" + (text.isEmpty() ? text : "\n"+text);

        return text;
    }

    public static void printText(final RecyclerView view, final String msg, final byte[] rawData, String detail) {

        if (view == null ) {
            logger.e("addText", "view null");
            return;
        }


        final String header = "[" + AppTool.getTimeString() + "] " + msg;//+ ":\n";

        final LogViewAdapter adapter = (LogViewAdapter) view.getAdapter();
        adapter.addItem(new LogItem(header, Converter.toHexString(rawData),
                (rawData != null ? new String(rawData) : "").replaceAll("[^\\x20-\\x7E]", "ㆍ"), detail));

        Map<RecyclerView, RecyclerView.Adapter> map = new HashMap<>();
        map.put(view, adapter);
        updateSubject.onNext(map);


    }

    private final static PublishSubject<Map<RecyclerView, RecyclerView.Adapter>> updateSubject = PublishSubject.create();

    static {

        updateSubject.onBackpressureBuffer()
                .subscribeOn(Schedulers.computation())
                .throttleLast(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<RecyclerView, RecyclerView.Adapter>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Map<RecyclerView, RecyclerView.Adapter> recyclerViewAdapterMap) {

                        final RecyclerView.Adapter adapter = recyclerViewAdapterMap.values().iterator().next();
                        adapter.notifyDataSetChanged();

                        final RecyclerView view = recyclerViewAdapterMap.keySet().iterator().next();

                       view.post(new Runnable() {
                           @Override
                           public void run() {
                               view.smoothScrollToPosition(adapter.getItemCount());
                           }
                       });



                    }
                });
    }


    public static boolean isEnableLogType(Context context, String type) {

        Preference preference = Preference.getInstance(context);
        Set<String> set = new HashSet<>();

        set = preference.get("logType", set);

        for (String val : set) {

            if (val.equals(type)) {

                return true;
            }
        }

        return  false;
    }

}

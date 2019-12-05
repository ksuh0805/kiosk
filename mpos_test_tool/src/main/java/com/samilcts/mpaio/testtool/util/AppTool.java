package com.samilcts.mpaio.testtool.util;


import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.ext.nice.MpaioNiceManager;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by mskim on 2016-07-05.
 * mskim@31cts.com
 */
public class AppTool {

    public static final String KEY_REVOKE = "key.revoke.receipt.nice";
    public static final String KEY_REVOKE_READ_TYPE = "key.revoke.read.type";



    /**
     *
     * @param mpaioNiceManager mpaioNiceManager
     * @param commands request command to wait notify
     * @return observable
     */
    public static Observable<MpaioMessage> waitNotify(final MpaioNiceManager mpaioNiceManager, final Command... commands){

        return Observable.create(new Observable.OnSubscribe<MpaioMessage>() {
            @Override
            public void call(final Subscriber<? super MpaioMessage> subscriber) {

                Observable<MpaioMessage> observable = Observable.empty();

                for ( Command command : commands) {

                    if (  command.equals(MpaioNiceCommand.READ_BARCODE) ||  command.equals(MpaioNiceCommand.OPEN_BARCODE) || command.equals(MpaioNiceCommand.NOTIFY_READ_BARCODE) ) {
                        observable = observable.mergeWith(mpaioNiceManager.onBarcodeRead());
                    } else if (  command.equals(MpaioNiceCommand.READ_MS_CARD) || command.equals(MpaioNiceCommand.NOTIFY_READ_MS_CARD) ) {
                        observable = observable.mergeWith(mpaioNiceManager.onReadMsCard());
                    } else if (  command.equals(MpaioNiceCommand.READ_EMV_CARD) || command.equals(MpaioNiceCommand.NOTIFY_READ_EMV_CARD) ) {
                        observable = observable.mergeWith(mpaioNiceManager.onReadEmvCard());
                    } else if (  command.equals(MpaioNiceCommand.READ_RFID_CARD) || command.equals(MpaioNiceCommand.NOTIFY_READ_RFID_CARD) ) {
                        observable = observable.mergeWith(mpaioNiceManager.onReadRfidCard());
                    } else if (  command.equals(MpaioNiceCommand.READ_PIN_PAD) || command.equals(MpaioNiceCommand.NOTIFY_READ_PIN_PAD) ) {
                        observable = observable.mergeWith(mpaioNiceManager.onPressPinPad());
                    } else if ( command.equals( MpaioNiceCommand.NOTIFY_PAYMENT_STATE) ) {
                        observable = observable.mergeWith(mpaioNiceManager.onPaymentStateNotify());
                    }

                }

                Subscription subscription = observable.subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {

                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {


                        Command command =  new MpaioCommand(mpaioMessage.getCommandCode());

                        if ( command.equals(MpaioCommand.NOTIFY_READ_PIN_PAD)) {

                            if ( new String(mpaioMessage.getData()).equals("\0E"))
                                subscriber.onCompleted();

                        } else {
                            subscriber.onCompleted();
                        }


                    }
                });

                subscriber.add(subscription);

            }
        });


    }


    /**
     * get current time string
     * @return formatted time string
     */
    public static String getTimeString() {

        int hour, minute, second, milliSecond;

        Calendar cal = new GregorianCalendar();

        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        second = cal.get(Calendar.SECOND);
        milliSecond = cal.get(Calendar.MILLISECOND);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%03d", hour, minute, second, milliSecond);
    }

    public static String getString(ByteBuffer buffer, int length) {

        byte[] temp = getBytes(buffer, length);
        String aa = new String(temp, Charset.forName("ksc5601"));
        BytesBuilder.clear(temp);
        return aa;
    }

    public static byte[] getBytes(ByteBuffer buffer, int length) {

        byte[] temp = new byte[length];
        buffer.get(temp);
        return temp;
    }

    private final static Logger logger;

    public static Logger getLogger(){
        return logger;
    }

    public static void setDebug(int level) {
        logger.setLevel(level);
    }

    static {
        logger = new Logger();

    }

    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }


}

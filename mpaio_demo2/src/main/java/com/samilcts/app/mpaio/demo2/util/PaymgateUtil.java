package com.samilcts.app.mpaio.demo2.util;

import android.os.RemoteException;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Converter;


import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by mskim on 2016-08-31.
 */
public class PaymgateUtil {


    public static Subscriber<byte[]> getEmptySubscriber() {

        return new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {

            }
        };
    }

    public static Subscriber<byte[]> getPrinterSubscriber(final View coordinatorLayout, final Action0 action) {

        return new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {
                Log.i("PRINT", "print... complete");
                action.call();
            }

            @Override
            public void onError(Throwable e) {
                Log.i("PRINT", "print..." + "error");
                if (e instanceof TimeoutException) {

                    Snackbar.make(coordinatorLayout, R.string.error_response_timeout, Snackbar.LENGTH_LONG)
                            .show();
                } else {

                    Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG)
                            .show();
                }

            }

            @Override
            public void onNext(byte[] data) {

                Log.i("PRINT", "print...");
            }
        };
    }


    public static void justSendStop(final MpaioManager service) {

        justSend(service, MpaioCommand.STOP, null);
    }

    public static void justSend(final MpaioManager service, final int commandCode, final byte[] param) {

        if (null == service) return;


        service.rxSendMessage(service.getNextAid(), new MpaioCommand(commandCode).getCode(), param).subscribe(new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {

            }
        });


    }

    public static Observable<byte[]> requestOk(final MpaioManager service, final int commandCode, final byte[] param) {

        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(final Subscriber<? super byte[]> subscriber) {
                //Log.i("TEST write", "code : "+commandCode);
                    Subscription subscription = service.rxSyncRequest(service.getNextAid(), new MpaioCommand(commandCode).getCode(), param).subscribe(new Subscriber<MpaioMessage>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onNext(MpaioMessage mpaioMessage) {

                            byte[] data = mpaioMessage.getData();
                           // Log.i("TEST res", Converter.toHexString(data));
                            //error retry
                            if (data != null && data.length > 0 && data[0] == ResponseError.NO_ERROR.getCode()) {

                                subscriber.onNext(data);
                                subscriber.onCompleted();
                            } else {

                                String msg = "response error";


                                if ( data != null && data.length > 0) {

                                    ResponseError error =  ResponseError.fromCode(data[0]);
                                    msg = error != null ? error.name() : "UNKNOWN RESPONSE ERROR";
                                }

                                subscriber.onError(new Exception(msg));
                            }

                        }
                    });

                subscriber.add(subscription);

            }
        }).subscribeOn(Schedulers.io());
    }


    public static boolean isAck(byte[] data) {
        return data != null && data.length > 0 && data[0] == ResponseError.NO_ERROR.getCode();
    }


    public static void disconnect(MpaioManager service) {

        if (null == service) return;

        service.disconnect();

    }

    public static boolean isConnected(MpaioManager service) {

        return null != service && service.isConnected();

    }
}

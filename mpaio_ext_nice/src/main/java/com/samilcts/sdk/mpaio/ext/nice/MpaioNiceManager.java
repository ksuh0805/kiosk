package com.samilcts.sdk.mpaio.ext.nice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bitmap.monochrome.BitmapConverter;
import com.samilcts.media.exception.MediaException;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.exception.ManagerException;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.ext.nice.error.NiceResponseError;
import com.samilcts.sdk.mpaio.ext.nice.log.LogTool;
import com.samilcts.sdk.mpaio.ext.nice.message.MpaioNiceMessage;
import com.samilcts.sdk.mpaio.ext.nice.message.MpaioNiceMessageAssembler;
import com.samilcts.sdk.mpaio.ext.nice.payment.PaymentError;
import com.samilcts.sdk.mpaio.ext.nice.payment.PaymentListener;
import com.samilcts.sdk.mpaio.ext.nice.payment.ReadType;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.sdk.mpaio.packet.Packet;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kr.co.nicevan.pos.PosClient;
import kr.co.nicevan.signenc.SignEnc;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.util.SubscriptionList;


/**
 * Created by mskim on 2016-07-13.
 * mskim@31cts.com
 */
public final class MpaioNiceManager extends MpaioManager {

    private static final String TAG = "MpaioNiceManager";
    private final int TIMEOUT;

    private final Context mContext;

    private PaymentListener mPaymentListener;

    private byte[] previousCommandCode = new byte[0];
    private byte[] previousAid = new byte[0];
    private byte[] previousResponse = new byte[] {NiceResponseError.NO_ERROR.getCode()};

    private final int retryCount = 2;
    private final PosClient mPosClient;

    private boolean useRealVanServer = false;
    private static final String testIp = "211.33.136.19";
    private static final String realIp = "211.33.136.2";
    private static final int port = 9701;

    private final SubscriptionList mSubscriptionList = new SubscriptionList();
    private final Logger logger = LogTool.getLogger();


    private Subscription timerSubscription; // next step timer subscription.

    public int timeoutCardRead = 4 * 60;

    public MpaioNiceManager(Context context) {
        super(context);

        TIMEOUT = BuildConfig.DEBUG ? 5000 : 1000;

        mContext = context;
        mPosClient = new PosClient();


        mSubscriptionList.add(listenTelegramRequest());
        mSubscriptionList.add(listenPaymentNotify());
        mSubscriptionList.add(listenSignRequest());
        mSubscriptionList.add(listenPinRequest());
        mSubscriptionList.add(listenCompletePayment());
        mSubscriptionList.add(listenCompleteRevokePayment());
        mSubscriptionList.add(listenPaymentReady());

        //setAuthenticationManager(new DefaultAuthenticationManager(this, mDataCrypto, mHashCrypto, new byte[16], 0));

        clearSession();
/*
        byte[] outData = new byte[16];
        new SignEnc().MakePinBlock("6235511115550130045".getBytes(), "891127".getBytes(),outData);

        logger.setLevel(Logger.DEBUG);
        logger.i(TAG, "PIN ENC : " + Converter.toHexString(outData) );*/

    }

    /**
     * receive MpaioNiceCommand.ANNOUNCE_PAYMENT_READY
     * @return observable
     */
    public Observable<MpaioMessage> onPaymentReady() {

        return listenCommand(getCommandCode(MpaioNiceCommand.ANNOUNCE_PAYMENT_READY));
    }

    /**
     * receive MpaioNiceCommand.NOTIFY_CASH_RECEIVED
     * @return observable
     */
    public Observable<MpaioMessage> onCashReceived() {

        return listenCommand(getCommandCode(MpaioNiceCommand.NOTIFY_CASH_RECEIVED));
    }


    /**
     * receive MpaioNiceCommand.ANNOUNCE_PAYMENT_READY
     * Ack is write automatically
     * @return subscription
     */
    private Subscription listenPaymentReady() {

        return listenCommand(getCommandCode(MpaioNiceCommand.ANNOUNCE_PAYMENT_READY))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        logger.i(TAG, "PaymentReady received");
                        sendAck(mpaioMessage);
                    }
                });
    }

    private Subscription listenTelegramRequest() {

        return listenCommand(getCommandCode(MpaioNiceCommand.RELAY_REQUEST_TELEGRAM))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }

                    @Override
                    public void onNext(final MpaioMessage mpaioMessage) {

                        removeTransactionTimer();


                        logger.i(TAG, "Telegram received from device");
                        if ( checkDuplicate(mpaioMessage)) return;

                        byte[] data = mpaioMessage.getData();

                        short replyAid = (short) Converter.toInt(mpaioMessage.getAID());
                        //중계
                        NiceResponseError error = NiceResponseError.NO_ERROR;

                        byte[] receive = new byte[1024];
                        BytesBuilder.clear(receive);
                        final int[] errorCode = new int[1];
                        try {

                            byte[] requestData = mPacker.unpack(data);
                            BytesBuilder.clear(data);

                            rxSendMessage(replyAid, getCommandCode(MpaioNiceCommand.RELAY_REQUEST_TELEGRAM), new byte[]{error.getCode()})
                                    .subscribe(new Subscriber<byte[]>() {
                                        @Override
                                        public void onCompleted() {

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            handleError(e);
                                        }

                                        @Override
                                        public void onNext(byte[] bytes) {

                                        }
                                    });

                            String requestTelegram = new String(requestData, Charset.forName("ksc5601"));

                            if (LogTool.needKtcLog()) {
                                logger.d(TAG, "ksc5601  : [" + requestTelegram + "]");

                                logger.d(TAG, "hex   : [" + Converter.toHexString(requestTelegram.getBytes()) + "]");
                            }

                            logger.i(TAG, "Will write telegram to NICE");
                            errorCode[0] = sendToNice(requestData, receive);
                            logger.i(TAG, "Telegram received from NICE");


                            BytesBuilder.clear(requestData);

                            if (LogTool.needKtcLog()){
                                 String responseTelegram = new String(receive, Charset.forName("ksc5601"));
                                 logger.d(TAG, "ksc5601  : ["  + responseTelegram+"]");
                                logger.d(TAG, "hex   : [" + Converter.toHexString(requestTelegram.getBytes()) + "]");
                            }

                            if ( errorCode[0] != 1)
                            {
                                    logger.i(TAG, "nice module return errorCode : " + errorCode);
                                    PaymentError paymentError;

                                    switch (errorCode[0]) {

                                        case -1:
                                            paymentError = PaymentError.TELEGRAM_PORT_OPEN_ERROR;
                                            break;
                                        case -2:
                                            paymentError = PaymentError.TELEGRAM_SEND_ERROR;
                                            break;
                                        case -3:
                                            paymentError = PaymentError.TELEGRAM_SEND_TIMEOUT_ERROR;
                                            break;
                                        case -4:
                                            paymentError = PaymentError.TELEGRAM_RSA_KEY_RECEIVE_TIMEOUT_ERROR;
                                            break;
                                        case -5:
                                            paymentError = PaymentError.TELEGRAM_ENCRYPTION_ERROR;
                                            break;
                                        case -6:
                                            paymentError = PaymentError.TELEGRAM_HASH_ERROR;
                                            break;
                                        default:
                                            paymentError = PaymentError.UNKNOWN_ERROR;
                                            break;
                                    }

                                handleError(paymentError);

                                receive = (errorCode[0]+"").getBytes();
                            }

                        } catch (SecurityException e) {

                            logger.w(TAG, "Invalid hash ");
                            //hash error

                            error = NiceResponseError.INVALID_HASH_ERROR;
                            rxSendMessage(replyAid, getCommandCode(MpaioNiceCommand.RELAY_REQUEST_TELEGRAM), new byte[]{error.getCode()} )

                                    .subscribe();//RELAY ACK

                            handleError(error);

                            return;
                        }

                        final byte[] packed = mPacker.pack(receive);
                        BytesBuilder.clear(receive);


                        final short aid = getNextAid();

                        Observable.create(new Observable.OnSubscribe<MpaioMessage>() {
                            @Override
                            public void call(final Subscriber<? super MpaioMessage> subscriber) {

                                rxSyncRequest(aid, getCommandCode(MpaioNiceCommand.RELAY_RESPONSE_TELEGRAM), packed )
                                        .subscribe(new Subscriber<MpaioMessage>() {
                                            @Override
                                            public void onCompleted() {
                                                subscriber.onCompleted();
                                            }

                                            @Override
                                            public void onError(Throwable e) {

                                                subscriber.onError(e);
                                            }

                                            @Override
                                            public void onNext(MpaioMessage message) {
                                                byte[] data = message.getData();
                                                if( null != data && data[0] != ResponseError.NO_ERROR.getCode()) {
                                                    subscriber.onError(new ManagerException("response not ok"));
                                                } else {
                                                    subscriber.onNext(message);
                                                }
                                            }
                                        });
                            }
                        })
                            .retry(retryCount)
                            .subscribe(new Subscriber<MpaioMessage>() {

                            @Override
                            public void onCompleted() {
                                BytesBuilder.clear(packed);


                                if ( 1 == errorCode[0] ) {
                                    timerSubscription = Observable.timer(10000, TimeUnit.MILLISECONDS)

                                            .doOnCompleted(new Action0() {
                                                @Override
                                                public void call() {
                                                    if (mPaymentListener != null)
                                                        mPaymentListener.onError(PaymentError.RECEIPT_TIMEOUT_ERROR);
                                                }
                                            }).subscribe();
                                }


                            }

                            @Override
                            public void onError(Throwable e) {
                                BytesBuilder.clear(packed);
                                handleError(e);
                            }

                            @Override
                            public void onNext(MpaioMessage message) {

                                if( !isAckOk(message.getData())) {
                                    logger.i(TAG, "relay response telegram not ok");
                                }
                                BytesBuilder.clear(mpaioMessage.getData());
                            }
                        });



                        }

                });
    }


    /**
     * check whether request is duplicated.
     * if true, write previous response
     * @param mpaioMessage requested message
     * @return true, if duplicated
     */
    private boolean checkDuplicate(MpaioMessage mpaioMessage) {

        boolean result = Arrays.equals(mpaioMessage.getAID(), previousAid) && Arrays.equals(mpaioMessage.getCommandCode(), previousCommandCode);
        result = result && (previousResponse != null && previousResponse.length != 0);

        previousAid = mpaioMessage.getAID().clone();
        previousCommandCode = mpaioMessage.getCommandCode().clone();

        if (result ) {

            logger.w(TAG, "receive duplicated commandCode & AID request. send previous result and do nothing");
            sendResponse(mpaioMessage, previousResponse);
        }

        return result;
    }

    /**
     * must use after authentication
     * start payment
     * @param type type of payment {@link ReadType}
     * @param tradeType card("10") or cash receipt ("21")
     * @param installment installment ("00"~"12")
     * @param serviceCharge serviceCharge serviceCharge
     * @param tax tax
     * @param total total
     * @param paymentListener listener
     */
    synchronized public void startPayment(final ReadType type, String tradeType , String installment, String serviceCharge, String tax, String total, final PaymentListener paymentListener) {


        removeTransactionTimer();
        mPaymentListener = paymentListener;

        int priceLen = 12;

        if ( total == null || tax == null || serviceCharge == null || total.length() > priceLen || tax.length() > priceLen
                || serviceCharge.length() > priceLen || tradeType == null || tradeType.length() != 2
                || installment == null || installment.length() != 2) {
            paymentListener.onError(PaymentError.INVALID_REQUEST_DATA_ERROR);
            return;
        }

        byte[] serviceChargeBytes = addZeroPrefix(serviceCharge.getBytes(), priceLen);
        byte[] taxBytes = addZeroPrefix(tax.getBytes(), priceLen);
        byte[] totalBytes = addZeroPrefix(total.getBytes(), priceLen);


        byte[] data = BytesBuilder.merge( new byte[] {type.getValue()}, tradeType.getBytes(), installment.getBytes(), serviceChargeBytes, taxBytes, totalBytes);

        rxSyncRequest(getNextAid(), getCommandCode(MpaioNiceCommand.START_PAYMENT), data)
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .retry(retryCount)
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {


                    }

                    @Override
                    public void onError(Throwable e) {

                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        byte[] data = mpaioMessage.getData();

                        if (isAckOk(data)) {

                            paymentListener.onPayStarted();


                            if ( timeoutCardRead > 0) {

                                timerSubscription = Observable.timer(timeoutCardRead, TimeUnit.SECONDS)
                                        .doOnCompleted(new Action0() {
                                            @Override
                                            public void call() {
                                                if ( mPaymentListener != null )
                                                    mPaymentListener.onError(PaymentError.CARD_READ_TIMEOUT_ERROR);
                                            }
                                        })
                                        .subscribe();
                            }



                        }

                    }

                });
    }


    /**
     * must use after authentication
     * set payment info
     * @param vanId VAN ID
     * @param catId CAT ID
     * @param groupId Device model code
     * @param deviceId Device serial
     * @param dik DIK
     * @param pmk pmk
     * @param placeId place ID installed
     * @param machineId machine ID installed
     * @param signThreshold sign threshold
     * @return observable
     */
    public Observable<Boolean> injectKey(final String vanId, final String catId, final String groupId, final String deviceId, final String dik, final String pmk, final String placeId, final String machineId, final String signThreshold) {

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                byte[] data = BytesBuilder.merge( vanId.getBytes(), catId.getBytes(), groupId.getBytes()
                        ,deviceId.getBytes(),  dik.getBytes(), pmk.getBytes(), addSpaceSurfix(placeId.getBytes(),20), addSpaceSurfix(machineId.getBytes(),20), signThreshold.getBytes());

                rxSyncRequest(MpaioNiceCommand.INJECT_KEY, mPacker.pack(data)).subscribe(subscriber);

            }
        });

    }

    public Observable<Boolean> setCatId(final String catId, final String placeId, final String machineId) {

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

               rxSyncRequest(MpaioNiceCommand.SET_CAT_ID, mPacker.pack(BytesBuilder.merge(catId.getBytes(),
                     addSpaceSurfix(placeId.getBytes(), 20), addSpaceSurfix(machineId.getBytes(), 20) )))
                       .subscribe(subscriber);;
            }
        });
    }


    /**
     * set ip address
     * onNext called when response received
     * @param pgpIp nice pgp server ip
     * @param pgpPort nice pgp server port
     * @param sslIp nice ssl server ip
     * @param sslPort nice ssl server port
     * @return observable
     */
    public Observable<Boolean> setPaymentServerInfo(final String pgpIp, final String pgpPort, final String sslIp, final String sslPort) {

        byte[] data = BytesBuilder.merge( pgpIp.getBytes(),new byte[]{0x0d},pgpPort.getBytes()
                ,new byte[]{0x0d}, sslIp.getBytes(),new byte[]{0x0d}, sslPort.getBytes()
        );

        return rxSyncRequest(MpaioNiceCommand.SET_PAYMENT_SERVER_INFO, data);

    }


    /**
     * check response has no error
     * @param data packet payload
     * @return true, if no error
     */
    private boolean isAckOk (byte[] data) {

        if ( data != null && data.length > 0 ) {
            NiceResponseError error = NiceResponseError.fromCode(data[0]);
            BytesBuilder.clear(data);

            if ( !NiceResponseError.NO_ERROR.equals(error)) {

                handleError(PaymentError.RESPONSE_NOT_OK_ERROR);

                return false;
            }

            return true;

        } else handleError(PaymentError.RESPONSE_NULL_ERROR);

        logger.w(TAG, "response not ok ");

        return false;

    }

    private byte[] addZeroPrefix(byte[] data, int len) {

        byte[] adder = new byte[len - data.length];
        Arrays.fill(adder, (byte)0x30);

        return BytesBuilder.merge(adder, data);
    }

    private byte[] addSpacePrefix(byte[] data, int len) {

        byte[] adder = new byte[len - data.length];
        Arrays.fill(adder, (byte)0x20);

        return BytesBuilder.merge(adder, data);
    }

    private byte[] addSpaceSurfix(byte[] data, int len)
    {
        byte[] adder = new byte[len - data.length];
        Arrays.fill(adder, (byte)0x20);

        return BytesBuilder.merge(data, adder);
    }


    /**
     * must use after authentication
     * revoke payment
     * @param readType type of payment {@link ReadType}
     * @param tradeType card("10") or cash receipt ("21")
     * @param installment installment ("00"~"12")
     * @param serviceCharge serviceCharge serviceCharge
     * @param tax tax
     * @param total total
     * @param approvalNumber approvalNumber
     * @param approvalDate approvalDate
     * @param tradeUniqueNumber tradeUniqueNumber
     * @param listener listener
     */
    synchronized public void revokePayment(ReadType readType, String tradeType, String installment, String serviceCharge,  String tax, String total, String approvalNumber, String approvalDate, String tradeUniqueNumber, final PaymentListener listener) {

        removeTransactionTimer();
        mPaymentListener = listener;

        int maxLen = 12;

        if (tradeType == null || installment == null || serviceCharge == null || total == null || total.length() > maxLen || tax.length() > maxLen || serviceCharge.length() > maxLen
                || installment.length() != 2 || approvalNumber.length() < 8 || approvalDate.length() != 6 || tradeUniqueNumber.length() != 12 || tradeType.length() != 2) {

            if ( listener != null) {
                listener.onError(PaymentError.INVALID_REQUEST_DATA_ERROR);
            }

            return;
        }

        byte[] serviceChargeBytes = addZeroPrefix(serviceCharge.getBytes(), maxLen);
        byte[] taxBytes = addZeroPrefix(tax.getBytes(), maxLen);
        byte[] totalBytes = addZeroPrefix(total.getBytes(), maxLen);


        byte[] data = BytesBuilder.merge(new byte[]{readType.getValue()}, tradeType.getBytes() ,installment.getBytes(), serviceChargeBytes, taxBytes, totalBytes
                , (approvalNumber.substring(approvalNumber.length()-8)+approvalDate+tradeUniqueNumber).getBytes() ) ;


        rxSyncRequest(getNextAid(), getCommandCode(MpaioNiceCommand.REVOKE_PAYMENT), data)
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .retry(retryCount)
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {


                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        byte[] data = mpaioMessage.getData();

                        if ( isAckOk(data)) {
                            listener.onPayStarted();

                            if ( timeoutCardRead > 0) {

                                timerSubscription = Observable.timer(timeoutCardRead, TimeUnit.SECONDS)
                                        .doOnCompleted(new Action0() {
                                            @Override
                                            public void call() {
                                                if ( mPaymentListener != null )
                                                    mPaymentListener.onError(PaymentError.CARD_READ_TIMEOUT_ERROR);
                                            }
                                        })
                                        .subscribe();
                            }
                        }

                    }

                });
    }

    /**
     * convert throwable to payment error
     * @param e throwable
     */
    private void handleError(Throwable e) {

        e.printStackTrace();

        if( mPaymentListener == null)
            return;

        if (e instanceof TimeoutException) {
            mPaymentListener.onError(PaymentError.RESPONSE_TIMEOUT_ERROR);
        } else if ( e instanceof MediaException || e instanceof ManagerException) {
            mPaymentListener.onError(PaymentError.COMMUNICATION_ERROR);
        }
        else {
            mPaymentListener.onError(PaymentError.UNKNOWN_ERROR);
        }

        mPaymentListener = null;
    }

    /**
     * convert nice error to payment error then call listener
     * @param error NiceResponseError
     */
    private void handleError(NiceResponseError error) {
        if( mPaymentListener == null)
            return;

        if ( NiceResponseError.INVALID_HASH_ERROR.equals(error))
             mPaymentListener.onError(PaymentError.INVALID_RESPONSE_DATA_ERROR);

        mPaymentListener = null;
    }


    @Override
    protected MpaioMessage makeMessage(byte[] aid, byte[] commandCode, byte[] data) {

        MpaioNiceMessage message = new MpaioNiceMessage(aid, commandCode, data);

        if ( message.needDataIndexing())
            message.setMaxPacketLength(maximumPacketLength);

        return message;

    }

    private Subscription listenCompleteRevokePayment() {

        return listenCommand(getCommandCode(MpaioNiceCommand.COMPLETE_REVOKE_PAYMENT))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        logger.i(TAG, "complete revoke payment command received");

                        handleReceipt(mpaioMessage);

                    }
                });
    }

    /**
     * unpack receipt data and send ack
     * @param mpaioMessage received message
     */
    private void handleReceipt(MpaioMessage mpaioMessage) {

        removeTransactionTimer();

        if ( checkDuplicate(mpaioMessage)) return;

        byte[] data = mpaioMessage.getData();
        NiceResponseError error = NiceResponseError.NO_ERROR;
        short replyAid = (short) Converter.toInt(mpaioMessage.getAID());

        try {

            byte[] unpackedData = mPacker.unpack(data);

            if (mPaymentListener != null)
                mPaymentListener.onComplete(unpackedData);

            BytesBuilder.clear(unpackedData);

        } catch (SecurityException e) {

            error = NiceResponseError.INVALID_HASH_ERROR;
            handleError(error);
        } finally {
            BytesBuilder.clear(data);
            rxSendMessage(replyAid, mpaioMessage.getCommandCode(), new byte[]{error.getCode()}).subscribe(new Subscriber<byte[]>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    handleError(e);
                }

                @Override
                public void onNext(byte[] bytes) {

                }
            });
            mPaymentListener = null;
        }
    }

    /**
     * onNext called when mpaio notify payment state
     * @return observable
     */
    public Observable<MpaioMessage>  onPaymentStateNotify() {
        return listenCommand(getCommandCode(MpaioNiceCommand.NOTIFY_PAYMENT_STATE));
    }

    private Subscription listenPaymentNotify() {

        return listenCommand(getCommandCode(MpaioNiceCommand.NOTIFY_PAYMENT_STATE))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        byte[] data = mpaioMessage.getData();

                        if (data != null && data.length == 2) {

                            if (mPaymentListener != null)
                            {
                                mPaymentListener.onStateNotification(mpaioMessage.getData());
                            }

                            if (data[0] >= ReadType.MSR && data[0] <= ReadType.PIN && data[1] == 0x03) {

                                removeTransactionTimer();

                                timerSubscription = Observable.timer(60, TimeUnit.SECONDS)
                                        .doOnCompleted(new Action0() {
                                            @Override
                                            public void call() {

                                                handleError(PaymentError.TELEGRAM_TIMEOUT_ERROR);
                                            }
                                        })
                                        .subscribe();
                            }

                        }
                    }
                });
    }

    private void handleError(PaymentError paymentError) {

        if (null != mPaymentListener)
            mPaymentListener.onError(paymentError);

        removeTransactionTimer();
    }


    private Subscription listenSignRequest() {

        return listenCommand(getCommandCode(MpaioNiceCommand.READ_SIGNATURE))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        logger.i(TAG, "read signature command received");

                        if ( checkDuplicate(mpaioMessage)) return;

                        sendAck(mpaioMessage);

                        if (mPaymentListener != null)
                            mPaymentListener.onSignatureRequested();

                        removeTransactionTimer();

                    }

                });

    }

    /**
     * remove transaction timer ( card timeout timer, telegram timer, etc.., )
     */
    private void removeTransactionTimer() {

        if ( timerSubscription != null && !timerSubscription.isUnsubscribed())
            timerSubscription.unsubscribe();
    }

    private Subscription listenPinRequest() {

        return listenCommand(getCommandCode(MpaioNiceCommand.READ_PIN))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        logger.i(TAG, "read pin command received");

                        if ( checkDuplicate(mpaioMessage)) return;

                        sendAck(mpaioMessage);

                        if (mPaymentListener != null)
                            mPaymentListener.onPinRequested();

                        removeTransactionTimer();
                    }

                });

    }


    private void sendResponse(MpaioMessage mpaioMessage, byte[] data) {

        previousResponse = data != null ? data.clone() : new byte[0];
        short replyAid = (short) Converter.toInt(mpaioMessage.getAID());
        rxSendMessage(replyAid, mpaioMessage.getCommandCode(), data)
                .subscribe(new Subscriber<byte[]>() {
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

    private void sendResponse(MpaioMessage mpaioMessage, byte error, byte[] data) {

        sendResponse(mpaioMessage, BytesBuilder.merge(new byte[]{error}, data));
    }

    private void sendResponse(MpaioMessage mpaioMessage, byte error) {

        sendResponse(mpaioMessage, BytesBuilder.merge(new byte[]{error}, null));
    }

    private void sendAck(MpaioMessage mpaioMessage) {

        sendResponse(mpaioMessage, NiceResponseError.NO_ERROR.getCode(), null);
    }

    /**
     * write sign bitmap to mpaio
     * @param sign sign bitmap
     */
    public void completeSignature(Bitmap sign) {

        logger.i(TAG, "prepare for deliver sign ");

        byte[] signData;
        byte[] encData = new byte[1048];
        try {

            signData = getNiceMonoChromeBitmap(sign);

            SignEnc signEnc = new SignEnc();

            if ( 1 != signEnc.GetEncData(signData, encData) ) {

                if ( mPaymentListener != null)
                    mPaymentListener.onError(PaymentError.CONVERT_SIGN_ERROR);
                return;
            }

        } catch (Throwable e) {

            e.printStackTrace();

            if ( mPaymentListener != null)
                mPaymentListener.onError(PaymentError.CONVERT_SIGN_ERROR);
            return;
        } finally {
            sign.recycle();
        }
        logger.i(TAG, "encData  " + Converter.toHexString(encData));
        signData = mPacker.pack(encData);
        BytesBuilder.clear(encData);

        short aid = getNextAid();
        rxSyncRequest(aid, getCommandCode(MpaioNiceCommand.COMPLETE_SIGNATURE_READING), signData)
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .retry(retryCount)
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        handleError(e);

                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        byte[] data = mpaioMessage.getData();

                        if ( isAckOk(data)) {

                            if ( mPaymentListener != null)
                                mPaymentListener.onSignatureDelivered();
                        }
                    }

                });



    }



    /**
     * send pin to mpaio (encrypt)
     * @param pin pin
     */
    public void completePin(String pin) {

        byte[] plainData = pin.getBytes();
        byte[] encData = mPacker.pack(plainData);

        BytesBuilder.clear(plainData);

        short aid = getNextAid();

        rxSyncRequest(aid, getCommandCode(MpaioNiceCommand.COMPLETE_PIN_READING), encData)
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .retry(retryCount)
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                        handleError(e);

                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        byte[] data = mpaioMessage.getData();

                        if ( isAckOk(data)) {

                            if ( mPaymentListener != null)
                                mPaymentListener.onPinDelivered();
                        }
                    }

                });



    }


    private Subscription listenCompletePayment() {

        return listenCommand(getCommandCode(MpaioNiceCommand.COMPLETE_PAYMENT))
                .retry()
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        handleError(e);
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {



                        logger.i(TAG, "complete payment command received");
                        handleReceipt(mpaioMessage);

                    }
                });

    }


    /**
     * Default is false
     * if true, connect with real server.
     * @param b enabled
     */
    public void useRealVanServer(boolean b) {

        useRealVanServer = b;
    }

    /**
     * whether van server is set to real server or not.
     * @return set or not
     */
    public boolean isSetRealVanServer() {

        return useRealVanServer;
    }

    private int sendToNice(byte[] data, byte[] recv) {

        //write tcp segment with encryption and receive with decryption.
        return mPosClient.service(useRealVanServer ? realIp : testIp, port, data, recv);
    }

    /**
     * Change sign bitmap to  128 *64 mono bitmap (1bit per pixel)
     * @param sign sign bitmap
     * @return 1086 bytes
     * @throws Exception
     */
    private byte[] getNiceMonoChromeBitmap(Bitmap sign) throws Exception {

        int width = sign.getWidth();
        int height = sign.getHeight();
        double wRatio = width / 128.0;
        double hRatio = height / 64.0;

        double ratio = wRatio > hRatio ? wRatio : hRatio;

        int destW = (int)(width / ratio);
        int destH = (int)(height / ratio);

        Bitmap scaledSign = Bitmap.createScaledBitmap(sign, destW, destH, false);

        Bitmap niceSign = Bitmap.createBitmap(128, 64, Bitmap.Config.ARGB_8888);

        niceSign.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(niceSign);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, 128, 64, paint);
        canvas.drawBitmap(scaledSign,new Matrix(), null);
        scaledSign.recycle();
        BitmapConverter converter = new BitmapConverter(mContext);
        return converter.getMonochromeBitmap(niceSign);
    }


    @Override
    protected void clearSession() {

        super.clearSession();

        //mPacker = new StubPacker();
        mMessageAssembler = new MpaioNiceMessageAssembler();
        previousCommandCode = new byte[0];
        previousAid = new byte[0];
        previousResponse = new byte[] {NiceResponseError.NO_ERROR.getCode()};

        removeTransactionTimer();
    }

    @Override
    public void close() {

        mSubscriptionList.unsubscribe();
        removeTransactionTimer();

        super.close();
    }

    /**
     * convert code to value
     * @param code code
     * @return command value
     */
    private byte[] getCommandCode(int code) {

        return new MpaioNiceCommand(code).getCode();
    }

    @Override
    final protected void onPacketReceived(Packet packet) {

        if ( logger.getLevel() < Logger.NONE) {

            MpaioNiceCommand command =  new MpaioNiceCommand(packet.getCommandCode());
            logger.d(TAG, "Received packet("+command.getName()+") : [" + Converter.toHexString(packet.getBytes())+"]"  );
        }
    }

    @Override
    final protected void onPacketSend(Packet packet) {

        if ( logger.getLevel() < Logger.NONE) {

            MpaioNiceCommand command = new MpaioNiceCommand(packet.getCommandCode());
            logger.d(TAG, "Packet write("+command.getName()+") : [" + Converter.toHexString(packet.getBytes())+"]"  );
        }
    }


    /**
     *
     * wait announce_charge command
     * @return observable
     */
    public Observable<MpaioMessage> onChargeAnnounced() {

        return listenCommand(getCommandCode(MpaioNiceCommand.ANNOUNCE_CHARGE));
    }
}

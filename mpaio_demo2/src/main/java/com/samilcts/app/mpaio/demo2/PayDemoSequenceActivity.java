package com.samilcts.app.mpaio.demo2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.base.Strings;
import com.samilcts.app.mpaio.demo2.data.CardInfo;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.DefaultParser;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.receipt.nice.ReceiptParser;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.ui.dialogs.PayDialog;
import com.samilcts.ui.dialogs.PinReceiveDialog;
import com.samilcts.ui.dialogs.SignDialog;
import com.samilcts.util.android.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.internal.util.SubscriptionList;

public abstract class PayDemoSequenceActivity extends MpaioBaseActivity {

    protected Context mContext;

    private PayDialog mPayDialog;
    private SignDialog mSignDialog;
    private MaterialDialog mProgressDialog;

    protected SubscriptionList mSubscriptionList;
    protected Logger logger = AppTool.getLogger();
    protected CoordinatorLayout coordinatorLayout;
    private CardInfo cardInfo = new CardInfo();;
    private PinReceiveDialog mPinDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
    }

    @Override
    protected void onStart() {
        super.onStart();

        coordinatorLayout  = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }


    private void showPinDialog() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mPinDialog = new PinReceiveDialog(PayDemoSequenceActivity.this, R.style.MyDialogTheme);
                mPinDialog.setCanceledOnTouchOutside(false);

                mPinDialog.setOnClickListener(new PinReceiveDialog.OnClickListener() {
                    @Override
                    public void onClick(String pin, DialogInterface dialogInterface) {

                        checkPin(pin, dialogInterface);

                    }
                });

                mPinDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        PaymgateUtil.justSendStop(mpaioManager);
                    }
                });

                mPinDialog.show();

                Subscription subscription = mpaioManager.onPressPinPad()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<MpaioMessage>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(MpaioMessage mpaioMessage) {


                                byte[] data = mpaioMessage.getData();

                                if( data == null || data[0] != ResponseError.NO_ERROR.getCode()) {

                                    Snackbar.make(mPinDialog.getWindow().getDecorView(), R.string.error_response_fail, Snackbar.LENGTH_LONG).show();

                                    return;
                                }

                                String pin = ""+(char)data[1];

                                logger.i(TAG, "PIN : " + pin);
                                switch (pin) {

                                    case "C": //cancel

                                        mPinDialog.cancel();
                                        //ca

                                        break;
                                    case "E":   //enter

                                        checkPin(mPinDialog.getPinCode(), mPinDialog);
                                        //onClickListener.onEnter(getPinCode(), PinDialog.this);

                                        break;
                                    case "<":   //backspace

                                        mPinDialog.backSpacePin();

                                        break;

                                    case "*": //F1
                                    case "#": //F2
                                        break;

                                    default: //numbers

                                        mPinDialog.addPinCode(pin);
                                        break;
                                }
                            }
                        });

                mSubscriptionList.add(subscription);
                //  dismissProgressDialog();

            }
        });
    }

    private void checkPin(String pin, DialogInterface dialogInterface) {

        if (pin.length() == 4) {

            dialogInterface.dismiss();

            ReceiptInfo info = AppTool.getDemoReceipt(PayDemoSequenceActivity.this);
            new ReceiptParser(mContext).setDemoReceipt(cardInfo, info);
            onComplete(info);

        } else {
            Snackbar.make(mPinDialog.getWindow().getDecorView(), R.string.enter_pin, Snackbar.LENGTH_LONG).show();

            PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_PIN_PAD, null)
                    .retry(2).subscribe(PaymgateUtil.getEmptySubscriber());
        }
    }

    private void showSignDialog() {

        logger.i(TAG,"showSignDialog");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logger.i(TAG,"showSignDialog run");
                mSignDialog = new SignDialog(PayDemoSequenceActivity.this, R.style.MyDialogTheme);
                mSignDialog.setCanceledOnTouchOutside(false);

                mSignDialog.setOnClickListener(new SignDialog.OnClickListener() {
                    @Override
                    public void onClick(Bitmap sign, boolean draw, final DialogInterface dialogInterface) {

                        if (!draw) {
                            Snackbar.make(mSignDialog.getWindow().getDecorView().getRootView(), R.string.input_signature, Snackbar.LENGTH_LONG).show();

                        } else {
                            dialogInterface.dismiss();

                            ReceiptInfo info =AppTool.getDemoReceipt(PayDemoSequenceActivity.this);
                           new ReceiptParser(mContext).setDemoReceipt(cardInfo, info);

                            onComplete(info);
                        }
                    }
                });


                //



                //


                mSignDialog.show();

                dismissProgressDialog();



            }
        });


    }




    /**
     * show progress dialog.
     * if dialog already exists, just change message.
     * @param message message to display
     * @param cancelable cancelable or not
     */
    private void showProgress(final String message, final boolean cancelable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String msg = message;
                if ( cancelable)
                    msg += "\n\n"+ getString(R.string.info_cancel);

                if (mProgressDialog == null) {

                    mProgressDialog = new MaterialDialog.Builder(PayDemoSequenceActivity.this)
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    PaymgateUtil.justSend(mpaioManager, MpaioCommand.STOP, null);
                                }
                            })
                            .progress(true, 0)
                            .cancelable(cancelable)
                            .canceledOnTouchOutside(false)
                            .content(msg)
                            .show();
                } else {

                    mProgressDialog.setContent(msg);
                    mProgressDialog.setCancelable(cancelable);
                    mProgressDialog.setCanceledOnTouchOutside(false);

                    if (!mProgressDialog.isShowing())
                        mProgressDialog.show();
                }

            }
        });


    }

    /**
     * this method called when receive telegram from van
     * @param info information for receipt
     */
    abstract void onComplete(ReceiptInfo info);


    /**
     * start payment
     * @param price total price
     */
    protected void startPaySequence(long price) {


        if ( mSubscriptionList != null)
            mSubscriptionList.unsubscribe();

        mSubscriptionList = new SubscriptionList();


        //showSignDialog();


        showPayDialog(getString(R.string.insert_card), true);


        PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_RFID_CARD, new byte[]{0x00})
                .retry(100)
                .subscribe(PaymgateUtil.getEmptySubscriber());

        PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_EMV_CARD, new byte[]{0x00})
                .retry(100)
                .subscribe(PaymgateUtil.getEmptySubscriber());

        PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_MS_CARD, new byte[]{0x00})
                .retry(100)
                .subscribe(PaymgateUtil.getEmptySubscriber());

        Subscription subscription = mpaioManager.onReadMsCard()
                .mergeWith(mpaioManager.onReadEmvCard())
                .mergeWith(mpaioManager.onReadRfidCard())
               // .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        mPayDialog.useImage(true);

                        MpaioCommand command = new MpaioCommand(mpaioMessage.getCommandCode());

                        byte[] data = mpaioMessage.getData();

                        boolean isReadCard = false;
                        int errorMsg = R.string.fail_to_read_card_data;

                        if ( data != null && data[0] == 0x00 ) {

                            isReadCard = true;



                            mPayDialog.setImage(ContextCompat.getDrawable(getBaseContext(), R.drawable.dialog_card_ic01_apro));

                            //sendStop();
                            DefaultParser parser = new DefaultParser();

                             cardInfo = new CardInfo();
                            data = Arrays.copyOfRange(data, 1, data.length);

                            if ( command.equals(MpaioCommand.NOTIFY_READ_MS_CARD)) {
                                logger.i(TAG,"NOTIFY_READ_MS_CARD");
                                cardInfo = parser.parseMSCardData(data);

                                if (Strings.isNullOrEmpty(cardInfo.number)) {
                                    isReadCard = false;
                                    errorMsg = R.string.unknown_data_format;

                                } else {
                                    PaymgateUtil.justSendStop(mpaioManager);
                                    rx.Observable.timer(500, TimeUnit.MILLISECONDS)
                                            .doOnCompleted(new Action0() {
                                                @Override
                                                public void call() {
                                                    dismissAll();
                                                    showSignDialog();
                                                }
                                            })
                                            .subscribe();
                                }



                            } else if ( command.equals(MpaioCommand.NOTIFY_READ_EMV_CARD)) {
                                logger.i(TAG,"NOTIFY_READ_EMV_CARD");
                                cardInfo = parser.parseEMVCardData(data);
                                PaymgateUtil.justSendStop(mpaioManager);
                                PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_PIN_PAD, null)
                                        .retry(3)
                                        .delaySubscription(100, TimeUnit.MILLISECONDS)
                                        .subscribe(PaymgateUtil.getEmptySubscriber());

                                rx.Observable.timer(500, TimeUnit.MILLISECONDS)
                                        .doOnCompleted(new Action0() {
                                            @Override
                                            public void call() {
                                                dismissAll();
                                                logger.i(TAG,"READ_PIN_PAD req");
                                                showPinDialog();
                                            }
                                        })
                                        .subscribe();




                            } else if ( command.equals(MpaioCommand.NOTIFY_READ_RFID_CARD)) {
                                PaymgateUtil.justSendStop(mpaioManager);
                                cardInfo = parser.parseRFIDCardData(data);
                            }

                            if(isReadCard){

                                if ( mSubscriptionList != null)
                                    mSubscriptionList.unsubscribe();

                                mSubscriptionList= new SubscriptionList();
                            }

                        }


                        if ( !isReadCard ){
                            cardInfo = new CardInfo();
                            mPayDialog.useImage(true);
                            mPayDialog.setImage(ContextCompat.getDrawable(getBaseContext(), R.drawable.dialog_card_ic01_fail));

                            Snackbar.make(mPayDialog.getWindow().getDecorView(), errorMsg, Snackbar.LENGTH_LONG)
                                    .show();

                            if ( command.equals(MpaioCommand.NOTIFY_READ_MS_CARD)) {

                                PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_MS_CARD, new byte[]{0x00})
                                        .retry(3)
                                        .delaySubscription(100, TimeUnit.MILLISECONDS)
                                        .subscribe(PaymgateUtil.getEmptySubscriber());

                            } else if ( command.equals(MpaioCommand.NOTIFY_READ_EMV_CARD)) {

                                PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_EMV_CARD, new byte[]{0x00})
                                        .retry(3)
                                        .delaySubscription(100, TimeUnit.MILLISECONDS)
                                        .subscribe(PaymgateUtil.getEmptySubscriber());

                            } else if ( command.equals(MpaioCommand.NOTIFY_READ_RFID_CARD)) {

                                PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_RFID_CARD, new byte[]{0x00})
                                        .retry(3)
                                        .delaySubscription(100, TimeUnit.MILLISECONDS)
                                        .subscribe(PaymgateUtil.getEmptySubscriber());
                            }


                        }

                    }
                });

        mSubscriptionList.add(subscription);

    }

    protected boolean isNetworkNotAvailable() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null) {
            showNetworkNotAvailable();
            return true;
        }

        sendStop();
        dismissAll();
        return false;
    }

    protected void showNetworkNotAvailable() {
        new MaterialDialog.Builder(PayDemoSequenceActivity.this)
                .content(R.string.network_not_available)
                .positiveText(android.R.string.ok)
                .show();
    }

    protected void showPayDialog(final String message, final boolean cancelable) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mPayDialog = new PayDialog(PayDemoSequenceActivity.this, R.style.MyDialogTheme);
                mPayDialog.setCancelable(cancelable);
                mPayDialog.setCanceledOnTouchOutside(false);

                mPayDialog.setMessage(message);
                mPayDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        dismissProgressDialog();

                        mPayDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                                sendStop();
                               // PaymgateUtil.justSendStop(mpaioManager);
                            }
                        });
                    }
                });

                mPayDialog.show();
            }
        });

    }

    private void sendStop() {

        PaymgateUtil.requestOk(mpaioManager, MpaioCommand.STOP, new byte[0])
                //.timeout(1000, TimeUnit.MILLISECONDS)
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .retry(5)
                .toBlocking()
                .subscribe(PaymgateUtil.getEmptySubscriber());

    }

    protected void dismissAll() {

        dismissPayDialog();
        dismissSignDialog();
        dismissProgressDialog();
    }

    private void dismissProgressDialog() {

        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    private void dismissPayDialog() {

        if (mPayDialog != null && mPayDialog.isShowing())
            mPayDialog.dismiss();

    }

    private void dismissSignDialog() {
        if (mSignDialog != null && mSignDialog.isShowing())
            mSignDialog.dismiss();
    }


    @Override
    protected void onMpaioDisconnected() {
        super.onMpaioDisconnected();
        mSubscriptionList.unsubscribe();
        Snackbar.make(coordinatorLayout, R.string.disconnected, Snackbar.LENGTH_LONG)
                .show();
        dismissAll();


    }

    protected void showPopup(final String title, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialDialog.Builder(PayDemoSequenceActivity.this)
                        .title(title)
                        .content(message)
                        .positiveText(android.R.string.ok)
                        .show();
            }
        });


    }


}

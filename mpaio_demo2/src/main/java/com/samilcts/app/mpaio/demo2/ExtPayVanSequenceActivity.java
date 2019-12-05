package com.samilcts.app.mpaio.demo2;

import android.support.design.widget.Snackbar;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.receipt.nice.data.PrepaidReceipt;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Converter;

import rx.Subscriber;
import rx.Subscription;
import rx.internal.util.SubscriptionList;

public abstract class ExtPayVanSequenceActivity extends PayDemoSequenceActivity {

    private static final int PREPAID_TYPE_BALANCE = 0;
    private static final int PREPAID_TYPE_RECHARGE = 1;
    private static final int PREPAID_TYPE_REFUND = 2;

    private int prepaidType = PREPAID_TYPE_BALANCE;

    int prepareCardParam = 0;

    private Subscriber<MpaioMessage> getPrepaidSubscriber() {
        return new Subscriber<MpaioMessage>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onNext(MpaioMessage mpaioMessage) {

                dismissAll();

                String[] params;

                byte[] bytes = mpaioMessage.getData();

                ReceiptInfo info = new ReceiptInfo();
                PrepaidReceipt prepaidReceipt =  info.prepaidReceipt;

                switch (prepaidType) {

                    case PREPAID_TYPE_RECHARGE:
                    case PREPAID_TYPE_REFUND:

                        params = checkPrepareData(bytes, 9);
                        if ( null == params) return;

                        prepaidReceipt.csn = params[0];
                        prepaidReceipt.placeId = params[1];
                        prepaidReceipt.issueDate = params[2];
                        prepaidReceipt.tradeType = params[3];
                        prepaidReceipt.tradeNumber = params[7];
                        prepaidReceipt.afterBalance = params[8];
                        onComplete(info);
                        break;


                }

            }
        };
    }

    /**
     * check prepare on read data is ok or not
     * @param bytes data
     * @param count required parameters count
     * @return params, or null if not ok.
     */

    protected String[] checkPrepareData(byte[] bytes, int count) {

        if ( !PaymgateUtil.isAck(bytes)) {

            Snackbar.make(coordinatorLayout, getString(R.string.error_response_fail), Snackbar.LENGTH_LONG).show();
            return null;
        }

        String data = new String(bytes).substring(1);
/*
        if ( data.charAt(data.length()-1) == '\r') {
            data += "\r";
            logger.i(TAG, "checkPrepareData : " + data);
        }*/

        logger.i(TAG, "checkPrepareData : " + Converter.toHexString(data.getBytes()));
        String[] params = Splitter.on("\r").splitToList(data).toArray(new String[0]); ///data.split("\\r");
        logger.i(TAG, "params length : " + params.length);
        logger.i(TAG, Joiner.on("/").join(params));

        if ( count != params.length ) {
            Snackbar.make(coordinatorLayout, getString(R.string.error_response_fail), Snackbar.LENGTH_LONG).show();
            return null;
        }

        return params;
    }


    /**
     * recharge
     * @param price total price
     */
    protected void startRechargeSequence(int price) {

        mSubscriptionList = new SubscriptionList();

            prepareCardParam = price;
            prepaidType = PREPAID_TYPE_RECHARGE;

        Subscription subscription = PaymgateUtil.requestOk(mpaioManager, MpaioCommand.RECHARGE_PREPAID_CARD, (price+"").getBytes())
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                        showPayDialog(getString(R.string.tag_card), true);
                        mpaioManager.onNotifyPrepaidTransaction()
                                .take(1)
                                .subscribe(getPrepaidSubscriber());

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(byte[] bytes) {

                    }
                });

        mSubscriptionList.add(subscription);

            //mpaioManager.rechargePrepaidCard(price ,prepaidCallback);

    }


    /**
     * refund
     * @param price total price
     */
    protected void startRefundSequence(int price) {

        mSubscriptionList = new SubscriptionList();



            prepareCardParam = price;
            prepaidType = PREPAID_TYPE_REFUND;

       Subscription subscription = PaymgateUtil.requestOk(mpaioManager, MpaioCommand.REFUND_PREPAID_BALANCE, (price+"").getBytes())
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                        showPayDialog(getString(R.string.tag_card), true);
                        mpaioManager.onNotifyPrepaidTransaction()
                                .take(1)
                                .subscribe(getPrepaidSubscriber());

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(byte[] bytes) {

                    }
                });
        mSubscriptionList.add(subscription);

    }


}

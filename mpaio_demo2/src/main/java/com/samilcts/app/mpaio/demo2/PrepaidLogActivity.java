package com.samilcts.app.mpaio.demo2;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;


public class PrepaidLogActivity extends ExtPayVanSequenceActivity {

    public static final String KEY_EXTRA_INDEX_PURCHASE_LOG = "extra.index.purchase.log";
    public static final String KEY_EXTRA_INDEX_RECHARGE_LOG = "extra.index.recharge.log";
    public static final String KEY_EXTRA_INDEX_REFUND_LOG = "extra.index.refund.log";

    private String indexPurchaseLog = "0";
    private String indexRechargeLog = "0";
    private String indexRefundLog = "0";

    private String clickIndex = "0";

    private int type = 0;

    private LogType logType = LogType.Purchase;
    private enum LogType{

        Purchase,
        Recharge,
        Refund,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepaid_log);

        ButterKnife.bind(this);

        init();
    }

    @Override
    void onComplete(ReceiptInfo info) {






    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppTool.setTitleFont(this);

        indexPurchaseLog = getIntent().getStringExtra(KEY_EXTRA_INDEX_PURCHASE_LOG );
        indexRechargeLog =getIntent().getStringExtra(KEY_EXTRA_INDEX_RECHARGE_LOG );
        indexRefundLog = getIntent().getStringExtra(KEY_EXTRA_INDEX_REFUND_LOG );

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        setContentView(R.layout.activity_prepaid_card);
        ButterKnife.bind(this);
        init();
        //
    }

    private void showOptionPopup(String title) {

        logger.i(TAG, indexPurchaseLog+"/"+indexRechargeLog+"/"+indexRefundLog);

        new MaterialDialog.Builder(this)
                .title(title)
                .items(R.array.logOption)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        logger.i(TAG, "which : " + which);
                        int index = 4 - which + Integer.parseInt(clickIndex);

                        logger.i(TAG, "i1 : " + index);
                        index %= 4;
                        logger.i(TAG, "i2 : " + index);
                        switch (logType) {
                            case Purchase:

                                break;
                            case Recharge:
                                index += 4;
                                break;
                            case Refund:
                                index += 8;
                                break;
                        }
                        logger.i(TAG, "i3 : " + index);

                        startReadLogSequence(index);

                        return false;
                    }
                })
                .positiveText(android.R.string.ok)
                .show();

    }

    @OnClick(R.id.btnPurchaseLog)
    void purchaseLog() {
        logType = LogType.Purchase;
        clickIndex = indexPurchaseLog;
        showOptionPopup(getString(R.string.title_pay_log));

    }



    @OnClick(R.id.btnRechargeLog)
    void rechargeLog() {

        logType = LogType.Recharge;
        clickIndex = indexRechargeLog;
        showOptionPopup(getString(R.string.title_charge_log));
    }

    @OnClick(R.id.btnRefundLog)
    void refundLog() {

        logType = LogType.Refund;
        clickIndex = indexRefundLog;
        showOptionPopup(getString(R.string.title_refund_log));
    }


    /**
     * read log
     * @param index total price
     */
    protected void startReadLogSequence(int index) {

        PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_PREPAID_TRANSACTION_LOG, (""+index).getBytes())
        .subscribe(new Subscriber<byte[]>() {
            @Override
            public void onCompleted() {
                showPayDialog(getString(R.string.tag_card), false);
                mpaioManager.onReadPrepaidTransactionLog()
                        .take(1)
                        .subscribe(new Subscriber<MpaioMessage>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {
                                Snackbar.make(coordinatorLayout, ""+e.getMessage(), Snackbar.LENGTH_LONG).show();
                                dismissAll();
                            }

                            @Override
                            public void onNext(MpaioMessage mpaioMessage) {
                                dismissAll();

                                byte[] bytes = mpaioMessage.getData();
                                String[] params = checkPrepareData(bytes, 5);

                                if ( null == params)  {
                                    return;
                                }

                                String msg = getString(R.string.no_record);
                                if ( !params[4].equals(""))
                                    msg = String.format(getString(R.string.transaction_number)+" %s\n"
                                            +getString(R.string.balance_after_transaction)+" %s\n"
                                            + getString(R.string.transaction_amount)+" %s\n"
                                            +getString(R.string.title_terminal_id)+" %s", params[1], params[2], params[3], params[4] );
                                //msg = String.format("거래번호 : %s\n거래 후 잔액 : %s\n거래 금액 : %s\n처리 단말기 ID : %s", params[1], params[2], params[3], params[4] );

                                logger.i(TAG, "msg : " + msg);
                                showPopup(getString(R.string.title_lookup_result), msg);
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(byte[] bytes) {

            }
        });
     //   mpaioManager.readPrepaidTransactionLog(index, callback);


    }

   /* IPrepaidCallback.Stub callback = new IPrepaidCallback.Stub() {
        @Override
        public void onResponse(byte[] bytes) throws RemoteException {

            if ( !PaymgateUtil.isAck(bytes)) {
                Snackbar.make(coordinatorLayout, getString(R.string.error_response_fail), Snackbar.LENGTH_LONG).show();
                return;
            }

            showPayDialog("put your card.", false);
        }

        @Override
        public void onError(final int b) throws RemoteException {
            Snackbar.make(coordinatorLayout, CommonError.fromCode(b).name(), Snackbar.LENGTH_LONG).show();
            dismissAll();
        }

        @Override
        public void onRead(byte[] bytes) throws RemoteException {

            dismissAll();
            String[] params = checkPrepareData(bytes, 5);



            if ( null == params)  {
                return;
            }

            String msg = getString(R.string.no_record);
            if ( !params[4].equals(""))
                msg = String.format(getString(R.string.transaction_number)+" %s\n"
                        +getString(R.string.balance_after_transaction)+" %s\n"
                        + getString(R.string.transaction_amount)+" %s\n"
                        +getString(R.string.title_terminal_id)+" %s", params[1], params[2], params[3], params[4] );
                //msg = String.format("거래번호 : %s\n거래 후 잔액 : %s\n거래 금액 : %s\n처리 단말기 ID : %s", params[1], params[2], params[3], params[4] );

            logger.i(TAG, "msg : " + msg);
            showPopup(getString(R.string.title_lookup_result), msg);

        }

    };*/

}

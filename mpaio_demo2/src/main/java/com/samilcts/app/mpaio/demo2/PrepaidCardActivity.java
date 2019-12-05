package com.samilcts.app.mpaio.demo2;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.text.FormattingTextWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class PrepaidCardActivity extends ExtPayVanSequenceActivity {


    @BindView(R.id.price)
    EditText price;
    @BindView(R.id.key_del)
    View del;

    @BindView(R.id.btnBalance)
    Button btnBalance;

    @BindView(R.id.btnRefund)
    Button btnRefund;

    @BindView(R.id.btnRecharge)
    Button btnRecharge;

    @BindView(R.id.btnLog)
    Button btnLog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepaid_card);


        ButterKnife.bind(this);

        init();
    }

    @Override
    void onComplete(ReceiptInfo info) {

        //not called.

    }


    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AppTool.buildNavigationDrawer(this, toolbar);
        AppTool.setTitleFont(this);


        FormattingTextWatcher formattingTextWatcher = new FormattingTextWatcher(price);
        formattingTextWatcher.enableSymbol(SharedInstance.locale);

        price.addTextChangedListener(formattingTextWatcher);

        price.setFocusable(false);
        price.setCursorVisible(false);

        price.setText("");

        del.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);


        setContentView(R.layout.activity_prepaid_card);
        ButterKnife.bind(this);
        init();
        //
    }

    @Override
    public void onBackPressed() {

        AppTool.showExitConfirmDialog(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        setBalanceMode(true);
        startBalanceSequence();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @OnClick(R.id.btnBalance)
    void balance() {
        startBalanceSequence();

    }

    private void setBalanceMode(boolean enable) {

        btnBalance.setVisibility(enable ? View.VISIBLE : View.GONE);
        btnRecharge.setVisibility(enable ? View.GONE : View.VISIBLE);
        btnRefund.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
        btnLog.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);


    }

    @OnClick(R.id.btnRecharge)
    void recharge() {

        Intent i = new Intent(getApplicationContext(), KeyPadActivity.class);
        i.putExtra(KeyPadActivity.KEY_EXTRA_TYPE, KeyPadActivity.EXTRA_TYPE_RECHARGE);
       // i.putExtra(KeyPadActivity.KEY_EXTRA_BALANCE, balance.replaceAll("[^0-9.]+",""));

        startActivity(i);
    }

    @OnClick(R.id.btnRefund)
    void refund() {

        Intent i = new Intent(getApplicationContext(), KeyPadActivity.class);
       // i.putExtra(KeyPadActivity.KEY_EXTRA_BALANCE, balance.replaceAll("[^0-9.]+",""));
        i.putExtra(KeyPadActivity.KEY_EXTRA_TYPE, KeyPadActivity.EXTRA_TYPE_REFUND);
        startActivity(i);
    }



    @OnClick(R.id.btnLog)
    void log() {

        Intent i = new Intent(getApplicationContext(), PrepaidLogActivity.class);
        i.putExtra(PrepaidLogActivity.KEY_EXTRA_INDEX_PURCHASE_LOG, indexPurchaseLog);
        i.putExtra(PrepaidLogActivity.KEY_EXTRA_INDEX_RECHARGE_LOG, indexRechargeLog);
        i.putExtra(PrepaidLogActivity.KEY_EXTRA_INDEX_REFUND_LOG, indexRefundLog);

        startActivity(i);
        //  startActivity(new Intent(getApplicationContext(), ManualRevokeActivity.class));
    }


    /**
     * start balance
     */
    protected void startBalanceSequence() {

        PaymgateUtil.requestOk(mpaioManager, MpaioCommand.STOP, null)
                .concatWith(   PaymgateUtil.requestOk(mpaioManager, MpaioCommand.READ_PREPAID_BALANCE, null))
                .subscribe(new Subscriber<byte[]>() {
                    @Override
                    public void onCompleted() {

                        showPayDialog(getString(R.string.tag_card), true);

                        mpaioManager.onNotifyPrepaidTransaction()
                                .subscribeOn(Schedulers.io())
                                .take(1)
                                .subscribe(new Subscriber<MpaioMessage>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {


                                    }

                                    @Override
                                    public void onNext(MpaioMessage mpaioMessage) {

                                        dismissAll();

                                        byte[] bytes = mpaioMessage.getData();

                                        String[] params = checkPrepareData(bytes, 9);

                                        if ( null == params)  {

                                            return;
                                        }

                                        indexPurchaseLog = params[4];
                                        indexRechargeLog = params[5];
                                        indexRefundLog = params[6];
                                        balance = params[8];

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setBalanceMode(false);
                                                price.setText(balance);
                                                dismissAll();
                                            }
                                        });


                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(coordinatorLayout, ""+e.getMessage(), Snackbar.LENGTH_LONG).show();
                        dismissAll();
                    }

                    @Override
                    public void onNext(byte[] bytes) {

                    }
                });



    }


    private String indexPurchaseLog = "";
    private String indexRechargeLog = "";
    private String indexRefundLog = "";
    private String balance = "";
}

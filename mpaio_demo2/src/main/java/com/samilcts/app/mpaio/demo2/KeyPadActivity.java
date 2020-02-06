package com.samilcts.app.mpaio.demo2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.receipt.nice.data.PrepaidReceipt;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.receipt.nice.data.TmoneyReceipt;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.util.android.text.FormattingTextWatcher;

import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeyPadActivity extends ExtPayVanSequenceActivity {

    public static final String KEY_EXTRA_INPUT = "extra.input";
    public static final String KEY_EXTRA_TYPE = "extra.type";
    //public static final String KEY_EXTRA_BALANCE = "extra.balance";


    public static final int EXTRA_TYPE_MAIN = 1;
    public static final int EXTRA_TYPE_RECHARGE = 3;
    public static final int EXTRA_TYPE_REFUND = 4;

    public static final int EXTRA_TYPE_SUB = 2;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.price)
    EditText price;

    String tradePrice = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keypad);

        ButterKnife.bind(this);

        logger.i(TAG, "onCreate ");
        init();

        AppTool.connectLastPrinter(this);

        if ( PaymgateUtil.isConnected(mpaioManager) )
            PaymgateUtil.justSend(mpaioManager, MpaioCommand.STOP, new byte[0] );
    }

    private void init() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int type = getIntent().getIntExtra(KEY_EXTRA_TYPE, 0);
        if ( EXTRA_TYPE_MAIN == type)
            AppTool.buildNavigationDrawer(this, toolbar);
        else if ( EXTRA_TYPE_RECHARGE == type)
            setTitle("RECHARGE");
        else if ( EXTRA_TYPE_REFUND == type)
            setTitle("REFUND");
        else if ( EXTRA_TYPE_SUB == type) {
            setTitle("요금 입력");
            fab.setImageResource(R.drawable.check_fab_ic);
        }


        FormattingTextWatcher formattingTextWatcher = new FormattingTextWatcher(price);
        formattingTextWatcher.enableSymbol(SharedInstance.locale);

        price.addTextChangedListener(formattingTextWatcher);

        price.setFocusable(false);
        price.setCursorVisible(false);
        price.setText(tradePrice);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        tradePrice = price.getText().toString();

        setContentView(R.layout.activity_keypad);
        ButterKnife.bind(this);
        init();
    }

    @Override
    void onComplete(ReceiptInfo info) {

        Intent i;

        LinkedHashMap<Product, Integer> cartItems = SharedInstance.getCartItems();
        cartItems.clear();
        double _price = Double.parseDouble(tradePrice.replaceAll("[^0-9.]+", ""));

        PrepaidReceipt prepaidReceipt = info.prepaidReceipt;

        if ( info.type == ReceiptInfo.TYPE_TMONEY_PAY || info.type == ReceiptInfo.TYPE_CASHBEE_PAY) {


            final TmoneyReceipt tmoneyReceipt = info.tmoneyReceipt;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new MaterialDialog.Builder(mContext)
                            .content(getString(R.string.balance_before_transaction) + tmoneyReceipt.preBalance+"\n"
                                    + getString(R.string.transaction_amount)+ tmoneyReceipt.tradeAmount+"\n"+
                                    getString(R.string.balance_after_transaction) + tmoneyReceipt.afterBalance)
                            .positiveText(android.R.string.ok)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    PaymgateUtil.justSendStop(mpaioManager);
                                }
                            })
                            .show();

                }
            });
            return;
        } else if ( !prepaidReceipt.csn.isEmpty()) {

            i = new Intent(getBaseContext(), PrepaidReceiptActivity.class);

            //String bal = getIntent().getStringExtra(KEY_EXTRA_BALANCE);

            info.prepaidReceipt.tradeAmount = tradePrice.replaceAll("[^0-9.]+", "");

            int tradeType = Integer.parseInt(prepaidReceipt.tradeType);

            logger.i(TAG, "tradeType : " + tradeType);

            int afterBalance = Integer.parseInt(prepaidReceipt.afterBalance);
            int amount = Integer.parseInt(info.prepaidReceipt.tradeAmount);

            if ( 0 == tradeType ) {
                info.type = ReceiptInfo.TYPE_PREPAID_PURCHASE;
                info.prepaidReceipt.preBalance = (afterBalance-amount)+"";
            } else if ( tradeType == 1) {
                info.type = ReceiptInfo.TYPE_PREPAID_RECHARGE;
                info.prepaidReceipt.preBalance = (afterBalance-amount)+"";
            } else {
                info.type = ReceiptInfo.TYPE_PREPAID_REFUND;
                info.prepaidReceipt.preBalance =(afterBalance+amount)+"";
            }
            String product = "Service";

            if(  ReceiptInfo.TYPE_PREPAID_RECHARGE == info.type) product = getString(R.string.balance_charge);
            else if(  ReceiptInfo.TYPE_PREPAID_REFUND == info.type) product = getString(R.string.balance_refund);
            else if(  ReceiptInfo.TYPE_PREPAID_PURCHASE == info.type) product = getString(R.string.balance_pay);

            cartItems.put(new Product(product, "btn_close", _price, ""), 1);

        } else {
            i = new Intent(getBaseContext(), MsIcReceiptActivity.class);
            cartItems.put(new Product("Service", "btn_close", _price, ""), 1);
        }

        i.putExtra(CartActivity.EXTRA_RECEIPT_INFO, info);

        startActivity(i);

    }


    @OnClick({R.id.key_0, R.id.key_1, R.id.key_2
            ,R.id.key_3, R.id.key_4, R.id.key_5
            ,R.id.key_6,R.id.key_7,R.id.key_8,R.id.key_9
            ,R.id.key_dot,R.id.key_00 ,R.id.key_del})
    void onKey(View v) {

        logger.i(TAG, "click");

        switch (v.getId()) {

            case R.id.key_dot:
                break;

            case R.id.key_del:
                logger.i(TAG, "DEL");
                //price.setFocusable(true);
                price.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));

                /*if ( price.getText().length() > 0)
                    price.getEditableText().delete()*/

                break;

            default:
                price.append(v.getTag() + "");
                break;

        }


    }


    @OnClick(R.id.fab)
    void payment(View v){

        tradePrice = price.getText().toString();
        long amount = Long.parseLong(tradePrice.replaceAll("[^0-9.]+", ""));

        int type = getIntent().getIntExtra(KEY_EXTRA_TYPE, 0);

        if ( EXTRA_TYPE_MAIN == type)
            startPaySequence(amount);
        else if ( EXTRA_TYPE_RECHARGE == type)
            startRechargeSequence((int)amount);
        else if ( EXTRA_TYPE_REFUND == type)
            startRefundSequence((int)amount);
        else {
                Intent i = new Intent();
                i.putExtra(KEY_EXTRA_INPUT, amount);
                setResult(RESULT_OK, i);
                finish();
        }


    }


    @Override
    public void onBackPressed() {

        if (getIntent().getIntExtra(KEY_EXTRA_TYPE, 0) == EXTRA_TYPE_MAIN)
            AppTool.showExitConfirmDialog(this);
        else {
            //unbind();
            super.onBackPressed();
        }


    }


}

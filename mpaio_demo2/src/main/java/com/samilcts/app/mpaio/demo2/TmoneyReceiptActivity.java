package com.samilcts.app.mpaio.demo2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.samilcts.app.mpaio.demo2.adapter.ReceiptViewAdapter;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.ReceiptViewItem;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.app.mpaio.demo2.util.PrintTool;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.receipt.nice.ReceiptParser;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.receipt.nice.data.TmoneyReceipt;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action0;

public class TmoneyReceiptActivity extends MpaioBaseActivity {

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.rvSales)
    RecyclerView rvSales;

    @BindView(R.id.tvMembershipNumber)
    TextView tvMembershipNumber;

    @BindView(R.id.tvIssueDate)
    TextView tvIssueDate;

    @BindView(R.id.tvDate)
    TextView tvDate;

    @BindView(R.id.tvCardNumber)
    TextView tvCardNumber;

    private ReceiptInfo mInfo;

    @BindView(R.id.tvIssuer)
    TextView tvIssuer;


    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.tvPreBalance)
    TextView tvPreBalance;

    @BindView(R.id.tvTradeAmount)
    TextView tvTradeAmount;

    @BindView(R.id.tvType)
    TextView tvType;

    @BindView(R.id.tvAfterBalance)
    TextView tvAfterBalance;

    @BindView(R.id.tvAffiliateName)
    TextView tvAffiliateName;

    @BindView(R.id.tvCorporateNumber)
    TextView tvCorporateNumber;

    @BindView(R.id.tvAffiliatePhoneNumber)
    TextView tvAffiliatePhoneNumber;

    @BindView(R.id.tvRepresentativeName)
    TextView tvRepresentativeName;


    @BindView(R.id.tvTradeNumber)
    TextView tvTradeNumber;

    @BindView(R.id.tvReceiptType)
    TextView tvReceiptType;

    LinkedHashMap<Product, Integer> paidItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepaid_receipt);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppTool.setTitleFont(this);

        paidItems = SharedInstance.getCartItems(); //.clone();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    ArrayList<ReceiptViewItem> itemArrayList = new ArrayList<>();

        double totalCharge = 0;

        for (Product product :
                paidItems.keySet()) {

            int amount =  paidItems.get(product);

            itemArrayList.add(new ReceiptViewItem(product.getName(), amount, product.getPrice()));


            totalCharge +=  product.getPrice() * amount;
        }


        RecyclerView.Adapter adapter = new ReceiptViewAdapter(itemArrayList);

        rvSales.setAdapter(adapter);

        rvSales.setLayoutManager(new LinearLayoutManager(getBaseContext()));


        NumberFormat numberFormat =  NumberFormat.getCurrencyInstance(SharedInstance.locale);
        numberFormat.setMinimumFractionDigits(0);
        String total =numberFormat.format(totalCharge);
       // tvTotal.setText(total);

        mInfo = (ReceiptInfo) getIntent().getSerializableExtra(CartActivity.EXTRA_RECEIPT_INFO);

        setReceiptData();

    }

    private void setReceiptData() {




        tvReceiptType.setText(ReceiptParser.getReceiptTitle(getBaseContext(), mInfo));

        TmoneyReceipt tmoneyReceipt = mInfo.tmoneyReceipt;

          try {
              DateFormat format = new SimpleDateFormat("yyMMddHHmmss");
              Date date = format.parse(tmoneyReceipt.approvalDatetime);
              format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
              tmoneyReceipt.approvalDatetime = format.format(date);
              tvIssueDate.setText(tmoneyReceipt.approvalDatetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tvDate.setText(format.format(new Date()));
        tvCardNumber.setText(tmoneyReceipt.cardId);

        tvIssuer.setText("Tmoney");
        tvAddress.setText(getString(R.string.company_address));

        tvMembershipNumber.setText(tmoneyReceipt.cardId);

        tvAffiliateName.setText(getString(R.string.affiliate_name));
        tvCorporateNumber.setText(getString(R.string.company_registration_number));
        tvAffiliatePhoneNumber.setText(getString(R.string.affiliate_phone));
        tvRepresentativeName.setText(getString(R.string.company_representative));


        switch (mInfo.type) {
            case ReceiptInfo.TYPE_TMONEY_PAY:
                tvType.setText(getString(R.string.receipt_pay));
                break;
            case ReceiptInfo.TYPE_TMONEY_CANCEL:
                tvType.setText(getString(R.string.cash_trade_cancel));
                break;
        }


        //tvTradeNumber.setText(tmoneyReceipt.ㅅㄱ.trim());

        NumberFormat numberFormat =  NumberFormat.getCurrencyInstance(SharedInstance.locale);
        numberFormat.setMinimumFractionDigits(0);


        String preBalance =numberFormat.format(Integer.parseInt(tmoneyReceipt.preBalance));

        String tradeAmount = numberFormat.format(Integer.parseInt(tmoneyReceipt.tradeAmount));
        String afterBalance =numberFormat.format(Integer.parseInt(tmoneyReceipt.afterBalance));

        tvPreBalance.setText(preBalance);
        tvTradeAmount.setText(tradeAmount);
        tvAfterBalance.setText(afterBalance);

    }

    @OnClick(R.id.print)
    void print(){

            mInfo.cartItems = paidItems;

        if ( !SharedInstance.getPrinter().isConnected()) {


            if ( SharedInstance.getInternalPrinterModels().contains(SharedInstance.deviceModelName)){
                //내장 프린터
                PrintTool.printInternalPrinter(this, mInfo).subscribe(PaymgateUtil.getPrinterSubscriber(coordinatorLayout, new Action0() {
                    @Override
                    public void call() {
                        SharedInstance.clearCartItem();
                        SharedInstance.getPrinter().setStateChangeListener(null);
                        finish();
                    }
                }));


            } else if (AppTool.getLastConnectedPrinterDevice(this) != null) {

                AppTool.showPrinterReconnectDialog(this);

            } else {

                Intent i = getIntent();
                i.setClass(getBaseContext(), SettingsActivity.class);
                i.putExtra(SettingsActivity.EXTRA_TYPE_SUB, true);

                finish();
                startActivity(i);
            }


        } else if ( PrintTool.printReceipt(this, mInfo) ) {

            SharedInstance.clearCartItem();
            SharedInstance.getPrinter().setStateChangeListener(null);
            finish();
        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedInstance.clearCartItem();
        SharedInstance.getPrinter().setStateChangeListener(null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {

            SharedInstance.clearCartItem();
            SharedInstance.getPrinter().setStateChangeListener(null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

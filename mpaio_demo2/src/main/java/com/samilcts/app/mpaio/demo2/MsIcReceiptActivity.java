package com.samilcts.app.mpaio.demo2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.samilcts.app.mpaio.demo2.adapter.ReceiptViewAdapter;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.receipt.nice.ReceiptParser;
import com.samilcts.receipt.nice.data.NiceReceipt;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.app.mpaio.demo2.data.ReceiptViewItem;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PrintTool;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action0;

public class MsIcReceiptActivity extends MpaioBaseActivity {

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.rvSales)
    RecyclerView rvSales;

    @BindView(R.id.tvTotal)
    TextView tvTotal;

    @BindView(R.id.tvCardNumber)
    TextView tvCardNumber;

    @BindView(R.id.tvDate)
    TextView tvDate;
    private ReceiptInfo mInfo;


    @BindView(R.id.tvMessage)
    TextView tvMessage;

    @BindView(R.id.tvIssuer)
    TextView tvIssuer;

    @BindView(R.id.tvAddress)
    TextView tvAddress;

    @BindView(R.id.tvCatId)
    TextView tvCatId;

    @BindView(R.id.tvMembershipNumber)
    TextView tvMembershipNumber;

    @BindView(R.id.tvApprovalNumber)
    TextView tvApprovalNumber;


    @BindView(R.id.tvApprovalDate)
    TextView tvApprovalDate;

    @BindView(R.id.tvRawPrice)
    TextView tvRawPrice;

    @BindView(R.id.tvTax)
    TextView tvTax;

    @BindView(R.id.tvType)
    TextView tvType;

    @BindView(R.id.tvTotalPrice)
    TextView tvTotalPrice;

    @BindView(R.id.tvAcquirer)
    TextView tvAcquirer;

    @BindView(R.id.tvAffiliateNumber)
    TextView tvAffiliateNumber;

    @BindView(R.id.tvAffiliateName)
    TextView tvAffiliateName;

    @BindView(R.id.tvCorporateNumber)
    TextView tvCorporateNumber;

    @BindView(R.id.tvAffiliatePhoneNumber)
    TextView tvAffiliatePhoneNumber;

    @BindView(R.id.tvRepresentativeName)
    TextView tvRepresentativeName;



    @BindView(R.id.tvRemainPoint)
    TextView tvRemainPoint;


    @BindView(R.id.tvTradeUniqueNumber)
    TextView tvTradeUniqueNumber;

    @BindView(R.id.tvReceiptType)
    TextView tvReceiptType;

    LinkedHashMap<Product, Integer> paidItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppTool.setTitleFont(this);

        paidItems = SharedInstance.getCartItems(); //.clone();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Context context = this;
        super.onPostCreate(savedInstanceState);

    ArrayList<ReceiptViewItem> itemArrayList = new ArrayList<>();

        double totalCharge = 0;

        for (Product product :
                paidItems.keySet()) {

            int amount =  paidItems.get(product);

            itemArrayList.add(new ReceiptViewItem(product.getName(), amount, product.getPrice()));


            totalCharge +=  product.getPrice() * amount;
            Thread thread = new Thread(new Runnable() {
                String urlStr = String.format(context.getString(R.string.server),
                        product.getName(), amount, product.getPrice(),
                        context.getString(R.string.ID), context.getString(R.string.PWD));

                @Override
                public void run() {
                    Log.d("ccheck3", urlStr);
                    request(urlStr);
                    Log.d("pserver123", String.valueOf(urlStr));
                }
            });
            thread.start();

        }


        RecyclerView.Adapter adapter = new ReceiptViewAdapter(itemArrayList);

        rvSales.setAdapter(adapter);

        rvSales.setLayoutManager(new LinearLayoutManager(getBaseContext()));




        NumberFormat numberFormat =  NumberFormat.getCurrencyInstance(SharedInstance.locale);
        numberFormat.setMinimumFractionDigits(0);

        String total =numberFormat.format(totalCharge);
        tvTotal.setText(total);

        mInfo = (ReceiptInfo) getIntent().getSerializableExtra(CartActivity.EXTRA_RECEIPT_INFO);


        setReceiptData();

    }

    private void setReceiptData() {

        if ( paidItems.isEmpty()) {
            mInfo.type = ReceiptInfo.TYPE_NICE_CANCEL;
         } else {
            mInfo.type = ReceiptInfo.TYPE_NICE_PAY;
        }


        tvReceiptType.setText(ReceiptParser.getReceiptTitle(getBaseContext(),mInfo));

        NiceReceipt niceReceipt = mInfo.niceReceipt;

          try {
              DateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.getDefault());
              Date date = format.parse(niceReceipt.approvalDate);
              format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
              tvDate.setText(format.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvCardNumber.setText(niceReceipt.membershipNumber);

        String message = String.format(Locale.getDefault(), "%s %s %s %s", niceReceipt.message, niceReceipt.message2,niceReceipt.message3,niceReceipt.message4);

        tvMessage.setText(message.trim());
        tvIssuer.setText(niceReceipt.issuer);
        tvAddress.setText(niceReceipt.companyAddress);

        tvCatId.setText(niceReceipt.catId);
        tvMembershipNumber.setText(niceReceipt.membershipNumber);

      //  testText = new char[] { 'a','b','c','c','t','s'};
       // tvMembershipNumber.setText(testText, 0, testText.length);


        tvApprovalDate.setText(niceReceipt.approvalDate);

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(SharedInstance.locale);
        long rawPrice = niceReceipt.totalPrice -niceReceipt.tax;

        tvRawPrice.setText(numberFormat.format(rawPrice));
        tvTax.setText(numberFormat.format(niceReceipt.tax));
        tvTotalPrice.setText(numberFormat.format(niceReceipt.totalPrice));
        tvApprovalNumber.setText(niceReceipt.approvalNumber.trim());

        tvAcquirer.setText(niceReceipt.acquirerName);
        tvAffiliateNumber.setText(niceReceipt.affiliateNumber);
        tvAffiliateName.setText(niceReceipt.affiliateName);
        tvCorporateNumber.setText(niceReceipt.corporateRegistrationNumber);
        tvAffiliatePhoneNumber.setText(niceReceipt.affiliatePhoneNumber);
        tvRepresentativeName.setText(niceReceipt.representativeName);



        tvType.setText(ReceiptParser.getTradeType(getBaseContext(), mInfo));
        tvTradeUniqueNumber.setText(niceReceipt.tradeUniqueNumber.trim());
        tvRemainPoint.setText(niceReceipt.remainPoint+"");

        /*tvDdc.setText("DDC여부 :" + mInfo.ddc);
        tvGeneratedPoint.setText("발생포인트(할인금액) : " + mInfo.generatedPoint);
        tvAvailablePoint.setText("가용포인트(지불금액) : " +mInfo.availablePoint);
        tvRemainPoint.setText("누적포인트(잔액한도) : " +mInfo.remainPoint);
        tvCashbagAffiliate.setText("캐쉬백가맹점 : " + mInfo.cashbagAffiliate);
        tvCashbagApprovalNumber.setText("캐쉬백승인번호 : " +mInfo.cashbagApprovalNumber);
        tvRealApprovalPrice.setText("실승인금액 : " +mInfo.realApprovalCharge);
        tvUniqueNumber.setText("고유번호 : " + mInfo.uniqueNumber);

        */



    }

    public void request(String urlStr) {

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d("view", String.valueOf(url));

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);       //컨텍션타임아웃 10초
            conn.setReadTimeout(5000);           //컨텐츠조회 타임아웃 5총
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("조회결과 : " + response.toString());

        }catch (Exception ex) {
            Log.d("Error", "예외 발생 : " + ex.toString());
        }
    }

    @OnClick(R.id.print)
    void print(){
        SharedInstance.clearCartItem();
        finish();
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

        PaymgateUtil.justSendStop(mpaioManager);
    }
}

package com.samilcts.app.mpaio.demo2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bartoszlipinski.recyclerviewheader.RecyclerViewHeader;

import com.samilcts.app.mpaio.demo2.adapter.ProductViewAdapter;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.ProductViewItem;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.receipt.nice.data.NiceReceipt;
import com.samilcts.receipt.nice.data.ReceiptInfo;


import com.samilcts.receipt.nice.data.TmoneyReceipt;
import com.samilcts.sdk.mpaio.command.MpaioCommand;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CartActivity extends PayDemoSequenceActivity {

    public static final String EXTRA_RECEIPT_INFO = "extra.receipt.info";

    @BindView(R.id.total) TextView total;
    @BindView(R.id.productListView) RecyclerView productListView;
   /* @Bind(R.id.header_product_list)
    LinearLayout header;*/

    LinkedHashMap<Product, Integer> mCartItems;

    private ArrayList<ProductViewItem> productArrayList;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    private LinearLayoutManager linearLayoutManager;
    private ProductViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ButterKnife.bind(this);

        mCartItems = SharedInstance.getCartItems();

        productArrayList = new ArrayList<>();

        init();

        if ( PaymgateUtil.isConnected(mpaioManager) )
            PaymgateUtil.justSend(mpaioManager, MpaioCommand.STOP, new byte[0] );

    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppTool.setTitleFont(this);

        adapter = new ProductViewAdapter(productArrayList,  R.layout.content_list_product);

        adapter.setButtonClickListener(new ProductViewAdapter.ButtonClickListener() {

            @Override
            public void onClick(View v, int position) {

                if (position < 0) return;


                Log.i(TAG, "pos : " + position);

                ProductViewItem item = productArrayList.get(position);

                mCartItems.remove(item.getProduct());
                productArrayList.remove(position);

                // adapter.notifyItemRemoved(position);

                adapter.notifyDataSetChanged();

                calculateTotal();
            }
        });


        adapter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ProductViewItem item = productArrayList.get((int) parent.getTag());

                item.setAmount(position + 1);

                mCartItems.put(item.getProduct(), position + 1);
                calculateTotal();

                Log.i(TAG, "setOnItemSelectedListener: " + position);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        productListView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(getBaseContext());
        productListView.setLayoutManager(linearLayoutManager);
        productListView.setItemViewCacheSize(10);
        productListView.setHasFixedSize(true);

        //header.attachTo(productListView);

        RecyclerViewHeader header = RecyclerViewHeader.fromXml(getApplicationContext(), R.layout.header_product_list);
        header.attachTo(productListView);

        calculateTotal();
    }

    private void calculateTotal() {

        productArrayList.clear();

        int totalPrice = 0;

        for (Product product:
             mCartItems.keySet()) {

            Integer amount = mCartItems.get(product);

            ProductViewItem item = new ProductViewItem(product, amount);

            productArrayList.add(item);

            totalPrice += (product.getPrice() * amount);
        }


        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(SharedInstance.locale);
        numberFormat.setMinimumFractionDigits(0);
        total.setText(numberFormat.format(totalPrice));

    }


    private void saveCart(){

        mCartItems.clear();

        for (ProductViewItem item:
               productArrayList) {

            mCartItems.put(item.getProduct(), item.getAmount());

        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_cart);
        ButterKnife.bind(this);
        init();
        //
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {

            saveCart();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        saveCart();
    }


    @Override
    void onComplete(ReceiptInfo info) {

        Intent i = new Intent(getBaseContext(), MsIcReceiptActivity.class);

        if ( info.type == ReceiptInfo.TYPE_TMONEY_PAY || info.type == ReceiptInfo.TYPE_CASHBEE_PAY  ) {

            final TmoneyReceipt tmoneyReceipt = info.tmoneyReceipt;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new MaterialDialog.Builder(mContext)
                            .content(getString(R.string.balance_before_transaction) + tmoneyReceipt.preBalance+"\n"
                                    +getString(R.string.transaction_amount) + tmoneyReceipt.tradeAmount+"\n"+
                                    getString(R.string.balance_after_transaction)+ tmoneyReceipt.afterBalance)
                            .positiveText(android.R.string.ok)
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    PaymgateUtil.justSendStop(mpaioManager);
                                }
                            })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    finish();
                                }
                            })
                            .show();
                }
            });

            return;

        } else if (info.type == ReceiptInfo.TYPE_DEMO) {

            NiceReceipt niceReceipt = info.niceReceipt;



            //niceReceipt.approvalDate = "15/12/21 14:32:24";

            niceReceipt.approvalNumber = "14322089";
            niceReceipt.tradeUniqueNumber = "962387015505";


        }else {

            info.type = ReceiptInfo.TYPE_NICE_PAY;

        }

        i.putExtra(CartActivity.EXTRA_RECEIPT_INFO, info);
        startActivity(i);
        finish();
    }


    @OnClick(R.id.fab)
    void payment(View v){

        if ( mCartItems.size() > 0 ) {

            long price = Long.parseLong(total.getText().toString().replaceAll("[^0-9.]+", ""));
            //startPaySequence(price);
            SeqPayWithNiceVan(price);

        } else {

            Snackbar.make(coordinatorLayout, "no product in the cart.", Snackbar.LENGTH_LONG).show();
        }


    }

}

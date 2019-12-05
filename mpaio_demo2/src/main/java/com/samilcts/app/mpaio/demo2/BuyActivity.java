package com.samilcts.app.mpaio.demo2;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.samilcts.app.mpaio.demo2.adapter.ProductViewAdapter;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.ProductViewItem;
import com.samilcts.app.mpaio.demo2.data.SampleProduct;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.DefaultParser;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;

import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;
import com.samilcts.util.android.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

public class BuyActivity extends MpaioBaseActivity {

    public static final String EXTRA_CART_ITEMS = "extra.cart.items";
    private static final int MAX_CART = 10;
    private static final String KEY_FIRST_LAUNCH = "mpaio2.pref.first.launch";
    @BindView(R.id.productListView) RecyclerView productListView;
    @BindView(R.id.tvBadge)
    TextView tvBadge;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindInt(R.integer.product_column) int productColumn;

    ArrayList<ProductViewItem> productViewItems;

    LinkedHashMap<Product, Integer> cartItems;

    ProductViewAdapter adapter;
    private Subscription subscription;
    private Logger logger = AppTool.getLogger();

    private boolean isBarcodeOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_buy);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        AppTool.localPath = getFilesDir().getPath();


        Preference preference = Preference.getInstance(getApplicationContext());

        if ( preference.get(KEY_FIRST_LAUNCH, false) ) {

            preference.set(KEY_FIRST_LAUNCH, true);
            preference.set(SharedInstance.PREF_TRANSACTION_NUMBER, 43235123L);
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent i = new Intent(getBaseContext(), CartActivity.class);

                startActivity(i);

            }
        });

       AppTool.buildNavigationDrawer(this, toolbar);
      //  toolbar.setTitleTextColor(Color.RED);

     //   toolbar.getNavigationIcon().setAlpha(89);

        productViewItems = new ArrayList<>();



      //  Log.i(TAG, "c size : " + (cartItems != null ? cartItems.size() : null) +"");

        //cartItems = new LinkedHashMap<>();
        cartItems = SharedInstance.getCartItems();

    //    Log.i(TAG, "c  : " +cartItems.toString());

      //  Log.i(TAG, "c size : " + cartItems.size());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), productColumn);



        adapter = new ProductViewAdapter(productViewItems, R.layout.content_grid_product);


                adapter.setButtonClickListener(new ProductViewAdapter.ButtonClickListener() {
                    @Override
                    public void onClick(View v, int position) {


                        ProductViewItem item = productViewItems.get(position);


                        Integer amount = cartItems.get(item.getProduct());

                        int newAmount = item.getAmount() + (amount == null ? 0 : amount);

                        if (newAmount > MAX_CART) {

                            Snackbar.make(coordinatorLayout, getString(R.string.can_not_add_to_cart).replace("$0", MAX_CART + ""), Snackbar.LENGTH_LONG).show();

                            return;
                        }

                        cartItems.put(item.getProduct(), newAmount);

                        tvBadge.setText("" + cartItemCount());

                        Snackbar.make(coordinatorLayout, getString(R.string.added_to_cart).replace("$0", item.getProduct().getName()), Snackbar.LENGTH_LONG).show();

                    }
                });

        productListView.setAdapter(adapter);
        productListView.setLayoutManager(gridLayoutManager);




       // final int margin_product_grid_h = 0;
        final int margin_product_grid_v = getResources().getDimensionPixelSize(R.dimen.margin_product_grid_v);
        final int margin_product_grid_item = getResources().getDimensionPixelSize(R.dimen.margin_product_grid_item);


        productListView.setItemViewCacheSize(20);

        productListView.setHasFixedSize(true);




        productListView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                GridLayoutManager.LayoutParams layoutParams
                        = (GridLayoutManager.LayoutParams) view.getLayoutParams();

                int position = layoutParams.getViewLayoutPosition();
                if (position == RecyclerView.NO_POSITION) {
                    outRect.set(0, 0, 0, 0);
                    return;
                }


                //  Log.i(TAG, "itemSpanSize : " +itemSpanMax + " itemSpanIndex : " + itemSpanIndex + " position : " + position );

                // add edge margin only if item edge is not the grid edge
                int itemSpanIndex = layoutParams.getSpanIndex();

                outRect.set(margin_product_grid_item, margin_product_grid_item, margin_product_grid_item, margin_product_grid_item);

                int itemSpanMax = productColumn - 1;
                // is left grid edge?
                // outRect.left = itemSpanIndex == 0 ? margin_product_grid_h : margin_product_grid_item;
                // is top grid edge?
                outRect.top = itemSpanIndex == position ? margin_product_grid_v : margin_product_grid_item;

                //is right grid edge?
                //  outRect.right = itemSpanIndex == itemSpanMax ? margin_product_grid_h : margin_product_grid_item;

                //is bottom grid edge?
                outRect.bottom = adapter.getItemCount() - 1 <= position + itemSpanMax ? margin_product_grid_v : margin_product_grid_item;


            }
        });




        AppTool.connectLastPrinter(this);

        /*getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE, View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
/*


*/

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        subscription = Observable.create(new rx.Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {

                productViewItems.addAll(SampleProduct.getWatchList());
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.newThread())
            .delay(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        adapter.notifyDataSetChanged();
                    }
                })

                .subscribe();

    }




    @Override
    protected void onResume() {
        super.onResume();

        tvBadge.setText("" + cartItemCount());

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        tvBadge.setText("" + cartItemCount());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.buy, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_barcode);

       if ( isBarcodeOn)
            d.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.md_red_600), PorterDuff.Mode.SRC_ATOP);


        menu.findItem(R.id.action_barcode).setIcon(d);

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_barcode) {

            if (isBarcodeOn) {

                PaymgateUtil.requestOk(mpaioManager, MpaioCommand.STOP, new byte[0])
                        .observeOn(Schedulers.io())
                        .timeout(1, TimeUnit.SECONDS)
                        .retry(2)
                        .subscribe(new Subscriber<byte[]>() {
                            @Override
                            public void onCompleted() {
                                invalidateOptionsMenu();
                            }

                            @Override
                            public void onError(Throwable e) {

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

                                if ( !(data != null && data.length > 0 && data[0] == ResponseError.NO_ERROR.getCode()) ) {
                                    Snackbar.make(coordinatorLayout, R.string.error_response_fail, Snackbar.LENGTH_LONG)
                                            .show();
                                } else {
                                    isBarcodeOn = false;
                                }

                            }
                        });

            } else {

                PaymgateUtil.requestOk(mpaioManager, MpaioCommand.OPEN_BARCODE, new byte[0])
                        .observeOn(Schedulers.io())
                        .timeout(1, TimeUnit.SECONDS)
                        .retry(2)
                        .subscribe(new Subscriber<byte[]>() {
                            @Override
                            public void onCompleted() {
                                invalidateOptionsMenu();
                            }

                            @Override
                            public void onError(Throwable e) {

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

                                if ( !(data != null && data.length > 0 && data[0] == ResponseError.NO_ERROR.getCode()) ) {
                                    Snackbar.make(coordinatorLayout, R.string.error_response_fail, Snackbar.LENGTH_LONG)
                                            .show();
                                } else {

                                    isBarcodeOn = true;
                                    invalidateOptionsMenu();

                                    mpaioManager.onBarcodeRead()
                                            .subscribe(new Subscriber<MpaioMessage>() {
                                                @Override
                                                public void onCompleted() {

                                                }

                                                @Override
                                                public void onError(Throwable e) {

                                                    Snackbar.make(coordinatorLayout, ""+e.getMessage(), Snackbar.LENGTH_LONG)
                                                            .show();
                                                }

                                                @Override
                                                public void onNext(MpaioMessage mpaioMessage) {

                                                    byte[] bytes = mpaioMessage.getData();

                                                    //    Log.i(TAG, "NOTIFY_READ_BARCODE");
                                                    if ( !(bytes != null && bytes.length > 0 && bytes[0] == ResponseError.NO_ERROR.getCode()) ) {
                                                        Snackbar.make(coordinatorLayout, R.string.error_response_fail, Snackbar.LENGTH_LONG)
                                                                .show();
                                                    } else {

                                                        DefaultParser parser = new DefaultParser();
                                                        byte[] barcodeData = Arrays.copyOfRange(bytes, 1, bytes.length);
                                                        final String barcode = parser.parseBarcodeData(barcodeData);

                                                        ToastUtil.show(getApplicationContext(), barcode);

                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                                addItemToCart(barcode.substring(3, barcode.length()));
                                                            }
                                                        });


                                                    }
                                                }
                                            });


                                }

                            }
                        });
            }

            //item.setChecked(!item.isChecked());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        logger.i(getLocalClassName(), " onDestroy1");
        if (subscription !=null)
          subscription.unsubscribe();

        super.onDestroy();
    }

    private void addItemToCart(String barcode) {

        LinkedHashMap<Product, Integer> cartItems = SharedInstance.getCartItems();

       // Log.i(TAG, "barcode : " + barcode );
        for (ProductViewItem item :
                productViewItems) {

          //  Log.i(TAG, "" + item.getProduct().getBarcode());

            Product product = item.getProduct();

            if ( barcode.equals(product.getBarcode())) {

                Integer amount = cartItems.get(product);

                if (amount == null)
                    amount = 0;

                if ( amount+1 > MAX_CART) {

                    Snackbar.make(coordinatorLayout,  getString(R.string.can_not_add_to_cart).replace("$0",MAX_CART+""), Snackbar.LENGTH_LONG).show();
                    //ToastUtil.show(getBaseContext(), MAX_CART+"개 이상 담을 수 없습니다.");
                    return;
                }
                cartItems.put(item.getProduct(), amount + 1);

                //ToastUtil.show(getBaseContext(), product.getName() + " is added to cart");

                Snackbar.make(productListView, getString(R.string.added_to_cart).replace("$0",product.getName()), Snackbar.LENGTH_LONG)
                .show();


                tvBadge.setText(""+cartItemCount());

                return;
            }
        }

        //ToastUtil.show(getBaseContext(), "can't find product");
        Snackbar.make(coordinatorLayout, R.string.can_not_find_product, Snackbar.LENGTH_LONG)
                .show();


    }



    private int cartItemCount() {

        //int count = cartItems.size();

        int count = 0;

        for (Integer number :
                cartItems.values()) {


            count += number;
        }

       tvBadge.setVisibility(count != 0 ? View.VISIBLE : View.INVISIBLE);

       return count;
    }


    @Override
    public void onBackPressed() {

        AppTool.showExitConfirmDialog(this);

    }

    @Override
    protected void onMpaioDisconnected() {
        super.onMpaioDisconnected();

        isBarcodeOn = false;

      //  Log.i(TAG, "onMpaioDisconnected");

    }
}

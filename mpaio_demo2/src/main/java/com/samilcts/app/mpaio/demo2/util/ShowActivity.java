package com.samilcts.app.mpaio.demo2.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.samilcts.app.mpaio.demo2.BuyActivity;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.adapter.UltraPagerAdapter;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.SampleProduct;
import com.samilcts.app.mpaio.demo2.data.SampleScenario;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.ultraviewpager.transformer.UltraDepthScaleTransformer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ShowActivity extends AppCompatActivity {

    Context context = this;
    UltraViewPager ultraViewPager;
    private SampleScenario sampleScenario = new SampleScenario();
    private SampleProduct sampleProduct = new SampleProduct();
    private ArrayList<Product> ProductArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_show);
        } else {
            setContentView(R.layout.activity_show_landscape);
        }

        ProductArray = sampleProduct.getDemoProduct(sampleScenario, context);
        Log.d("watchhhh", ProductArray.get(0).getName());

        ultraViewPager = (UltraViewPager)findViewById(R.id.ultra_viewpager);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
//initialize UltraPagerAdapter，and add child view to UltraViewPager
        PagerAdapter adapter = new UltraPagerAdapter(true, ProductArray);
        ultraViewPager.setAdapter(adapter);
        ultraViewPager.setMultiScreen(0.6f);
        ultraViewPager.setItemRatio(1.0f);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            ultraViewPager.setRatio(1.6f);
        } else {
            ultraViewPager.setRatio(3.5f);
        }
        //ultraViewPager.setMaxHeight(800);
        //ultraViewPager.setAutoMeasureHeight(true);

//initialize built-in indicator
        ultraViewPager.initIndicator();
//set style of indicators
        ultraViewPager.getIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setFocusColor(Color.parseColor("#009688"))
                .setNormalColor(Color.parseColor("#9E9E9E"))
                .setIndicatorPadding(10)
                .setMargin(0,0,0,10)
                .setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()));
//set the alignment
        ultraViewPager.getIndicator().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
//construct built-in indicator, and add it to  UltraViewPager
        ultraViewPager.getIndicator().build();
        ultraViewPager.setPageTransformer(false, new UltraDepthScaleTransformer());

//set an infinite loop
        ultraViewPager.setInfiniteLoop(true);
//enable auto-scroll mode
        ultraViewPager.setAutoScroll(5000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }
        }, 1800000, 1800000);
    }
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {

            Intent intent = new Intent(getApplicationContext(), BuyActivity.class);
            intent.putExtra("productlist", ProductArray);

            startActivity(intent);
            //finish();
        }

        return super.onTouchEvent(event);
    }
    public void refresh(View v){

        ProductArray = sampleProduct.getDemoProduct(sampleScenario, context);
        Log.d("wwwwatchhhh", ProductArray.get(0).getName());
        PagerAdapter adapter = new UltraPagerAdapter(true, ProductArray);
        ultraViewPager.setAdapter(adapter);
    }
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            ProductArray = sampleProduct.getDemoProduct(sampleScenario, context);
            Log.d("wwwwatchhhh", ProductArray.get(0).getName());
            PagerAdapter adapter = new UltraPagerAdapter(true, ProductArray);
            ultraViewPager.setAdapter(adapter);
        }
    };
}
package com.samilcts.app.mpaio.demo2.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.data.Product;

import java.util.ArrayList;

/**
 * Created by mikeafc on 15/11/26.
 */
public class UltraPagerAdapter extends PagerAdapter {
    private boolean isMultiScr;
    private ArrayList products;

    public UltraPagerAdapter(boolean isMultiScr, ArrayList proudcts) {
        this.isMultiScr = isMultiScr;
        this.products = proudcts;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.layout_child, null);
        //new LinearLayout(container.getContext());
        TextView textView = (TextView) linearLayout.findViewById(R.id.pager_textview);
        ImageView imageView = (ImageView) linearLayout.findViewById(R.id.pager_image);

        String pname = ((Product)products.get(position)).getName();
        int price = (int) Math.round(((Product)products.get(position)).getPrice());
        textView.setText(pname + "\n"+ price + "원");
        String img = imageView.getContext().getString(R.string.image_server) + ((Product)products.get(position)).getImg();
        Glide.with(imageView.getContext()).load(img).into(imageView);
        container.addView(linearLayout);

        return linearLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        LinearLayout view = (LinearLayout) object;
        container.removeView(view);
    }
}
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
 * 메인화면 viewpager 어댑터
 */
public class UltraPagerAdapter extends PagerAdapter {
    /**
     * 멀티스크린 여부
     */
    private boolean isMultiScr;
    /**
     * 상품리스트
     */
    private ArrayList products;

    /**
     * 생성자
     * @param isMultiScr
     * @param proudcts
     */
    public UltraPagerAdapter(boolean isMultiScr, ArrayList proudcts) {
        this.isMultiScr = isMultiScr;
        this.products = proudcts;
    }

    /**
     * 상품 갯수
     * @return 상품 갯수
     */
    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * viewpager 레이아웃 구성
     * @param container 레이아웃
     * @param position 순서
     * @return 레이아웃
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(container.getContext()).inflate(R.layout.layout_child, null); //viewpager 내부 레이아웃
        //new LinearLayout(container.getContext());
        TextView textView = (TextView) linearLayout.findViewById(R.id.pager_textview);
        ImageView imageView = (ImageView) linearLayout.findViewById(R.id.pager_image);

        /**
         * 상품 이름
         */
        String pname = ((Product)products.get(position)).getName();
        /**
         * 가격 정보
         */
        int price = (int) Math.round(((Product)products.get(position)).getPrice());
        textView.setText(pname + "\n"+ price + "원");
        String img = imageView.getContext().getString(R.string.image_server) + ((Product)products.get(position)).getImg();
        /**
         * 웹서버로부터 이미지 받아옴
         */
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
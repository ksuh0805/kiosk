package com.samilcts.app.mpaio.demo2.data;

import android.content.Context;
import android.util.Log;

import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.util.android.Converter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * 웹서버로부터 상품 정보 가져오기
 */

public class SampleProduct {
    /**
     * 상품정보리스트
     */
    ArrayList<Product> sampleProductArray = new ArrayList<>();

    public void request(String urlStr, Context context){
        /**
         * 웹서버에서 상품 정보 json 읽어와 상품 정보 업데이트
         * @param urlStr 웹서버 접속 url
         * @param context
         */
        String output = "";
        try{
            URL url = new URL(urlStr);
            Log.d("view", String.valueOf(url));

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            if(conn != null){
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                int resCode = conn.getResponseCode();
                Log.d("product rescode", String.valueOf(resCode));
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line = null;

                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }

                    output += line;
                }
                reader.close();
                conn.disconnect();
            }

            JSONArray jsonObject = new JSONArray(output);

            JSONObject Object = jsonObject.getJSONObject(0);

            Product Products = new Product();

            Products.setProductNum(Object.getInt(context.getString(R.string.product_number)));
            Products.setProductName(Object.getString(context.getString(R.string.product_name)));
            Products.setPrice(Object.getInt(context.getString(R.string.product_price)));
            Products.setImg(Object.getString(context.getString(R.string.image_res)));
            Products.setBarcode(Object.getString(context.getString(R.string.barcode)));

            sampleProductArray.add(Products); // product 추가
        } catch (Exception ex) {
            Log.d("SampleProductError", "예외 발생 : " + ex.toString());
        }
    }

    /**
     * 웹서버로부터 상품정보 가져오기
     * @param scenario 시나리오
     * @param context
     * @return
     */
    public ArrayList<Product> getDemoProduct(SampleScenario scenario, Context context){

        sampleProductArray.clear();
        scenario.getDemoScenario(context);
        ArrayList pnum = scenario.Matching();
        for(int i=0; i<pnum.size(); i++) {

            /**
             * 상품 정보 웹서버 url
             */
            final String urlStr = String.format(context.getString(R.string.product_server), pnum.get(i),
                    context.getString(R.string.ID), context.getString(R.string.PWD));

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    request(urlStr, context);
                }
            });
            thread.start();

            try {
                thread.join();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return sampleProductArray;
    }

    /**
     * 화면에 추가될 상품리스트
     */
    private static ArrayList<ProductViewItem> watchList;

    /**
     * 화면에 추가될 상품리스트로 변환
     * @param ProductArray 상품리스트
     * @return 변환된 상품리스트
     */
    public static ArrayList<ProductViewItem> getWatchList(ArrayList ProductArray) {

        if ( watchList != null)
            return watchList;

        watchList = new ArrayList<>();

        /**
         * 0 아닌 숫자로 세팅된 product number 리스트
         */
        for(int i=0 ; i<ProductArray.size(); i++){
            Product demop = (Product) (ProductArray.get(i));

            String pname = demop.getName();
            double price = demop.getPrice();
            String img = demop.getImg();
            String barcode = demop.getBarcode();

            watchList.add(makeProductViewItem(pname, img, price, barcode));
        }

       // watchList.add(makeProductViewItem("this product name for test of long long name print, display 12335145bsdfbsdf DSf xc SDf zdsf FsdvfdsffEND ", R.drawable.w_lg_watch_r, 225));
        //watchList.add(makeProductViewItem("Tvlet Touch & Bag", R.drawable.tvlet_touch_bag, 1045000, "https://smartstore.naver.com/tvlet/products/4536735153"));
        //watchList.add(makeProductViewItem("Tvlet Touch", R.drawable.tvlet_touch, 959200, "https://smartstore.naver.com/tvlet/products/4045704695"));
        //watchList.add(makeProductViewItem("Tvlet Bag", R.drawable.tvlet_bag, 104500, "https://smartstore.naver.com/tvlet/products/2823156102"));
        //watchList.add(makeProductViewItem("Tvlet", R.drawable.tvlet, 639200, "https://smartstore.naver.com/tvlet/products/2810837747"));

        //watchList.add(makeProductViewItem("LG Watch R", R.drawable.w_lg_watch_r, 225, null));
        //watchList.add(makeProductViewItem("Huawei Watch", R.drawable.w_huawei_watch, 289, null));
        //watchList.add(makeProductViewItem("Motorola Moto 360 2", R.drawable.w_motorola_moto_360_2, 229, null));
        //watchList.add(makeProductViewItem("Motorola Moto 360", R.drawable.w_motorola_moto_360, 199, null));
        //watchList.add(makeProductViewItem("Sony SmartWatch 3", R.drawable.w_sony_smartwatch_3, 189, null));

        //watchList.add(makeProductViewItem("LG Watch Urbane", R.drawable.w_lg_watch_urbane, 259, null));
        //watchList.add(makeProductViewItem("Microsoft Band 2", R.drawable.w_microsoft_band_2, 199, null));
        //watchList.add(makeProductViewItem("Asus ZenWatch", R.drawable.w_asus_zenwatch, 199, null));
        //watchList.add(makeProductViewItem("Apple Watch", R.drawable.w_apple_watch, 299, null));
        //watchList.add(makeProductViewItem("Pebble Steel", R.drawable.w_pebble_steel, 179, null));

        //watchList.add(makeProductViewItem("LG G Watch", R.drawable.w_lg_g_watch, 159, null));
        //watchList.add(makeProductViewItem("Sony SmartWatch 2", R.drawable.w_sony_smartwatch_2, 149, null));
        //watchList.add(makeProductViewItem("Samsung Gear 2 Neo", R.drawable.w_samsung_gear_2_neo, 169, null));
        //watchList.add(makeProductViewItem("Martian Notifier", R.drawable.w_martian_notifier, 129, null));
        //watchList.add(makeProductViewItem("Vector Watch Luna", R.drawable.w_vector_watch_luna, 299, null));

        //watchList.add(makeProductViewItem("Samsung Gear 2", R.drawable.w_samsung_gear_2, 299, null));
        //watchList.add(makeProductViewItem("Samsung Galaxy Gear", R.drawable.w_samsung_galaxy_gear, 299, null));
        //watchList.add(makeProductViewItem("Microsoft Band", R.drawable.w_microsoft_band, 169, null));
        //watchList.add(makeProductViewItem("Sony SmartWatch review", R.drawable.w_sony_smartwatch_review, 1, null));
        //watchList.add(makeProductViewItem("Cookoo smart watch", R.drawable.w_cookoo_smart_watch, 10, null));

        return watchList;
    }

    private static ProductViewItem makeProductViewItem(String name, String imageRes ,double price, String code ){

        if(code==null) {
            code = "DEFAULT_BARCODE_DATA_###########";
            try {
                byte[] data = MessageDigest.getInstance("MD5").digest(name.getBytes());
                code = Converter.toHexString(data).replaceAll(" ", "");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return new ProductViewItem(new Product(name, imageRes ,price, code), 1);
    }
}


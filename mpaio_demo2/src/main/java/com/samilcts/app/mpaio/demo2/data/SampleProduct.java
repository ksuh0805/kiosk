package com.samilcts.app.mpaio.demo2.data;

import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.util.android.Converter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class SampleProduct {


    private static ArrayList<ProductViewItem> watchList;

    public static ArrayList<ProductViewItem> getWatchList() {

        if ( watchList != null)
            return watchList;

        watchList = new ArrayList<>();

       // watchList.add(makeProductViewItem("this product name for test of long long name print, display 12335145bsdfbsdf DSf xc SDf zdsf FsdvfdsffEND ", R.drawable.w_lg_watch_r, 225));

        watchList.add(makeProductViewItem("LG Watch R", R.drawable.w_lg_watch_r, 225));
        watchList.add(makeProductViewItem("Huawei Watch", R.drawable.w_huawei_watch, 289));
        watchList.add(makeProductViewItem("Motorola Moto 360 2", R.drawable.w_motorola_moto_360_2, 229));
        watchList.add(makeProductViewItem("Motorola Moto 360", R.drawable.w_motorola_moto_360, 199));
        watchList.add(makeProductViewItem("Sony SmartWatch 3", R.drawable.w_sony_smartwatch_3, 189));

        watchList.add(makeProductViewItem("LG Watch Urbane", R.drawable.w_lg_watch_urbane, 259));
        watchList.add(makeProductViewItem("Microsoft Band 2", R.drawable.w_microsoft_band_2, 199));
        watchList.add(makeProductViewItem("Asus ZenWatch", R.drawable.w_asus_zenwatch, 199));
        watchList.add(makeProductViewItem("Apple Watch", R.drawable.w_apple_watch, 299));
        watchList.add(makeProductViewItem("Pebble Steel", R.drawable.w_pebble_steel, 179));

        watchList.add(makeProductViewItem("LG G Watch", R.drawable.w_lg_g_watch, 159));
        watchList.add(makeProductViewItem("Sony SmartWatch 2", R.drawable.w_sony_smartwatch_2, 149));
        watchList.add(makeProductViewItem("Samsung Gear 2 Neo", R.drawable.w_samsung_gear_2_neo, 169));
        watchList.add(makeProductViewItem("Martian Notifier", R.drawable.w_martian_notifier, 129));
        watchList.add(makeProductViewItem("Vector Watch Luna", R.drawable.w_vector_watch_luna, 299));

        watchList.add(makeProductViewItem("Samsung Gear 2", R.drawable.w_samsung_gear_2, 299));
        watchList.add(makeProductViewItem("Samsung Galaxy Gear", R.drawable.w_samsung_galaxy_gear, 299));
        watchList.add(makeProductViewItem("Microsoft Band", R.drawable.w_microsoft_band, 169));
        watchList.add(makeProductViewItem("Sony SmartWatch review", R.drawable.w_sony_smartwatch_review, 1));
        watchList.add(makeProductViewItem("Cookoo smart watch", R.drawable.w_cookoo_smart_watch, 10));

        return watchList;
    }

    private static ProductViewItem makeProductViewItem(String name, int imageRes ,double price ){

        String code = "DEFAULT_BARCODE_DATA_###########";
        try {
            byte[] data = MessageDigest.getInstance("MD5").digest(name.getBytes());
            code = Converter.toHexString(data).replaceAll(" ","");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return new ProductViewItem(new Product(name, imageRes ,price, code), 1);
    }
}


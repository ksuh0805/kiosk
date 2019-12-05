/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.samilcts.app.mpaio.demo2.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.datamatrix.encoder.SymbolShapeHint;
import com.google.zxing.qrcode.QRCodeWriter;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.ProductViewItem;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Provide views to RecyclerView with data from logitems.
 */
public class ProductViewAdapter extends RecyclerView.Adapter<ProductViewAdapter.ViewHolder> {
    private static final String TAG = "ProductViewAdapter";
    private final NumberFormat mNumberFormat;
    private boolean needTotal = false;

    private ArrayList<ProductViewItem> productViewItems;

    private int contentLayoutRes;
    private String mSymbol;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */

    private ButtonClickListener mOnButtonClickListener;



    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            productViewItems.get((int) parent.getTag()).setAmount(position + 1);

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };



    public interface ButtonClickListener {

        void onClick(View v, int position);
    }


    public static final class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.name) TextView tvName;
        @BindView(R.id.price) TextView tvPrice;
        @BindView(R.id.image) ImageView image;
        @BindView(R.id.spinner) Spinner spinner;
        @BindView(R.id.imageButton) ImageButton imageButton;
        @BindView(R.id.ivBarcode) ImageView ivBarcode;
        private AdapterView.OnItemSelectedListener onItemSelectedListener = null;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

        }

        public void setTvName(CharSequence text){
            tvName.setText(text);
        }

        public void setTvPrice(CharSequence text){
            tvPrice.setText(text);
        }

        public void setImage(final int imageRes) {

            image.setImageResource(imageRes);



        }

        private static final int QRVersion = 4;

        public void setBarcode(String barcode) {

            //ivBarcode.setImageBitmap(createBarcode(barcode));

        //    ivBarcode.setImageBitmap(createQrCode(barcode));

                Drawable img = Drawable.createFromPath(AppTool.localPath + "/" + QRVersion + "_" + barcode + ".png");

            if (img != null)
                ivBarcode.setImageDrawable(img);
            else
                ivBarcode.setImageBitmap(createQrCode(barcode));


        }

        public void setAmount(int amount) {

            amount = amount < 1 ? 1 : amount;

            spinner.setOnItemSelectedListener(null);
            spinner.setSelection(amount - 1 ,false);
            spinner.setOnItemSelectedListener(onItemSelectedListener);

        }

        public void setClickListener(final ButtonClickListener listener) {

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClick(v, getAdapterPosition());

                }
            });

        }

        private Bitmap createQrCode(String code) {

            QRCodeWriter writer = new QRCodeWriter();
            Bitmap bitmap = null;
            FileOutputStream out = null;

            Map<EncodeHintType,Object> option = new HashMap<>();
            option.put(EncodeHintType.MARGIN, 0);
            option.put(EncodeHintType.DATA_MATRIX_SHAPE, SymbolShapeHint.FORCE_RECTANGLE);
            try {

                final int WIDTH = 50;
                final int HEIGHT = 50;

                BitMatrix bytemap = writer.encode(code, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, option);


                bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);

                for (int i = 0 ; i < WIDTH ; ++i){
                    for (int j = 0 ; j < HEIGHT ; ++j) {
                        bitmap.setPixel(i, j, bytemap.get(i,j) ? Color.BLACK : Color.WHITE);
                    }
                }

                File file = new File(AppTool.localPath+"/"+QRVersion+"_"+code+".png");
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return bitmap;
        }

        public void setTag(int tag) {
            imageButton.setTag(tag);
            spinner.setTag(tag);
        }

        public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener){

            onItemSelectedListener = listener;

            spinner.setOnItemSelectedListener(onItemSelectedListener);
        }


    }
    // END_INCLUDE(recyclerViewSampleViewHolder)


    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {

        mOnItemSelectedListener = listener;
    }


    public void setButtonClickListener(ButtonClickListener listener) {

        mOnButtonClickListener = listener;
    }

    public void addItem(ProductViewItem product) {
        productViewItems.add(product);
    }

    public void clear() { productViewItems.clear();}


    public ProductViewAdapter(ArrayList<ProductViewItem> productViewItems, int contentLayoutRes) {

        this.productViewItems = productViewItems;
        this.contentLayoutRes = contentLayoutRes;


        Locale locale = SharedInstance.locale;

        mNumberFormat = NumberFormat.getCurrencyInstance(locale);
        mNumberFormat.setMinimumFractionDigits(0);


        if ( contentLayoutRes == R.layout.content_list_product) {

            needTotal = true;
        }

    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(contentLayoutRes, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Log.i(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element


        ProductViewItem productViewItem = productViewItems.get(position);

        Product product = productViewItem.getProduct();

        viewHolder.setTvName(product.getName());

        if ( needTotal ) {
            viewHolder.setTvPrice(mNumberFormat.format(product.getPrice() * productViewItem.getAmount()));

        } else {
            viewHolder.setTvPrice(mNumberFormat.format(product.getPrice()));
            viewHolder.setBarcode(product.getBarcode());
        }


        viewHolder.setImage(product.getImageRes());
        viewHolder.setAmount(productViewItem.getAmount());
        viewHolder.setTag(position);
        viewHolder.setClickListener(mOnButtonClickListener);
        viewHolder.setOnItemSelectedListener(mOnItemSelectedListener);

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return productViewItems.size();
    }

}

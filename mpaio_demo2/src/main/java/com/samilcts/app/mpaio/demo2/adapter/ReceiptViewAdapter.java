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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.samilcts.app.mpaio.demo2.data.ReceiptViewItem;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.util.android.text.FormattingTextWatcher;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Provide views to RecyclerView with data from logitems.
 */
public class ReceiptViewAdapter extends RecyclerView.Adapter<ReceiptViewAdapter.ViewHolder> {
    private static final String TAG = "ReceiptViewAdapter";
    private final NumberFormat mNumberFormat;

    private ArrayList<ReceiptViewItem> receiptViewItems;

  //  private String mSymbol;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */


    public static final class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvAmount) TextView tvAmount;
        @BindView(R.id.tvTagPrice) TextView tvTagPrice;
        @BindView(R.id.tvPrice) TextView tvPrice;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            FormattingTextWatcher textWatcher = new FormattingTextWatcher(tvPrice,"###,###");
            textWatcher.enableSymbol(SharedInstance.locale);
            tvPrice.addTextChangedListener(textWatcher);

        }

        public void setName(CharSequence text){
            tvName.setText(text);
        }

        public void setPrice(CharSequence text){
            tvPrice.setText(text);
        }

        public void setAmount(CharSequence text){
            tvAmount.setText(text);
        }

        public void setTagPrice(CharSequence text){
            tvTagPrice.setText(text);
        }

    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    public void addItem(ReceiptViewItem product) {
        receiptViewItems.add(product);
    }

    public void clear() { receiptViewItems.clear();}

    public ReceiptViewAdapter(ArrayList<ReceiptViewItem> receiptViewItems) {
        this.receiptViewItems = receiptViewItems;

        Locale locale = SharedInstance.locale;

        //mSymbol =  Currency.getInstance(SharedInstance.locale).getSymbol(SharedInstance.locale);

        mNumberFormat = NumberFormat.getCurrencyInstance(locale);
        mNumberFormat.setMinimumFractionDigits(0);

    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {


      

        //Log.d(TAG, "onCreateViewHolder");
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_list_receipt, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
       // Log.d(TAG, "Element " + position + " set.");

        // Get element from your dataset at this position and replace the contents of the view
        // with that element


        ReceiptViewItem receiptViewItem = receiptViewItems.get(position);

        viewHolder.setName(receiptViewItem.getName());
        viewHolder.setAmount(receiptViewItem.getAmount()+"");
        viewHolder.setTagPrice(mNumberFormat.format(receiptViewItem.getPrice()));

        AppTool.getLogger().i(TAG, "format : " +receiptViewItem.getAmount() * receiptViewItem.getPrice());

        String total = mNumberFormat.format(receiptViewItem.getAmount() * receiptViewItem.getPrice());
        AppTool.getLogger().i(TAG, "format : " + total );
        viewHolder.setPrice(total);
        

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return receiptViewItems.size();
    }


}

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.data.PaymentHistory;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Provide views to RecyclerView with data from logitems.
 */
public class HistoryViewAdapter extends RecyclerView.Adapter<HistoryViewAdapter.ViewHolder> {
    private static final String TAG = "HistoryViewAdapter";
    private final NumberFormat mNumberFormat;

    private ArrayList<PaymentHistory> paymentHistories;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */

    private ButtonClickListener mOnButtonClickListener;

    public interface ButtonClickListener {

        void onClick(View v, int position);
    }


    public static final class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.installment) TextView tvInstallment;
        @BindView(R.id.price) TextView tvPrice;
        @BindView(R.id.approvalNumber) TextView tvApprovalNumber;
        @BindView(R.id.approvalDate) TextView tvApprovalDate;
        @BindView(R.id.tradeNumber) TextView tvTradeNumber;
        @BindView(R.id.imageButton) ImageButton imageButton;
        private AdapterView.OnItemSelectedListener onItemSelectedListener = null;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

        }

        public void setApprovalNumber(CharSequence text){
            tvApprovalNumber.setText(text);
        }

        public void setApprovalDate(CharSequence text){
            tvApprovalDate.setText(text);
        }

        public void setTradeNumber(CharSequence text){
            tvTradeNumber.setText(text);
        }

        public void setInstallment(CharSequence text){
            tvInstallment.setText(text);
        }

        public void setPrice(CharSequence text){
            tvPrice.setText(text);
        }


        public void setClickListener(final ButtonClickListener listener) {

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onClick(v, getAdapterPosition());

                }
            });

        }

        public void setTag(int tag) {
            imageButton.setTag(tag);

        }

    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

/*

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {

        mOnItemSelectedListener = listener;
    }
*/


    public void setButtonClickListener(ButtonClickListener listener) {

        mOnButtonClickListener = listener;
    }

    public void addItem(PaymentHistory paymentHistory) {
        paymentHistories.add(paymentHistory);
    }

    public void clear() { paymentHistories.clear();}


    public HistoryViewAdapter(ArrayList<PaymentHistory> paymentHistories) {

        this.paymentHistories = paymentHistories;
        Locale locale = SharedInstance.locale;

        mNumberFormat = NumberFormat.getCurrencyInstance(locale);
        mNumberFormat.setMaximumFractionDigits(0);
        //mNumberFormat.setMinimumFractionDigits(0);

/*
        if ( contentLayoutRes == R.layout.content_list_product) {

            needTotal = true;
        }*/

    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_list_payment_history, viewGroup, false);

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


        PaymentHistory paymentHistory = paymentHistories.get(position);

        viewHolder.setInstallment(paymentHistory.getInstallment());

        Log.i(TAG, "total : " + paymentHistory.getTotal());
       // viewHolder.setPrice(paymentHistory.getTotal());
       viewHolder.setPrice(mNumberFormat.format(Long.parseLong(paymentHistory.getTotal())));
        viewHolder.setApprovalNumber(paymentHistory.getApprovalNumber());
        viewHolder.setApprovalDate(paymentHistory.getApprovalDate());
        viewHolder.setTradeNumber(paymentHistory.getTradeNumber());

        viewHolder.setTag(position);
        viewHolder.setClickListener(mOnButtonClickListener);


    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return paymentHistories.size();
    }

}

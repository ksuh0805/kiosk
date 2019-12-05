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

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;


import com.samilcts.app.mpaio.demo2.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Provide views to RecyclerView with data from logitems.
 */
public class PrinterViewAdapter extends RecyclerView.Adapter<PrinterViewAdapter.ViewHolder>  {
    private static final String TAG = "PrinterViewAdapter";

    private ArrayList<BluetoothDevice> printerList;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */

    private static OnPrinterClickListener mOnPrinterClickListener;


    public interface OnPrinterClickListener {

        void onClick(BluetoothDevice device);
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.tvAddress)
        TextView tvAddress;

        @BindView(R.id.tvState)
        TextView tvState;

        BluetoothDevice device;

        @BindView(R.id.progressBar)
        ProgressBar progressBar;

        @BindView(R.id.radioButton)
        RadioButton radioButton;

        private OnPrinterClickListener onPrinterClickListener;

        public ViewHolder(View v) {
            super(v);
            // Log.d(TAG, "ViewHolder c");
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);
            radioButton.setOnClickListener(this);
           // this.onPrinterClickListener = onPrinterClickListener;
        }

        public void setName(CharSequence text) {
            tvName.setText(text);
        }

        public void setAddress(CharSequence text) {
            tvAddress.setText(text);
        }

        public void setState(CharSequence text) {
            tvState.setText(text);
        }

        public void setDevice(BluetoothDevice device) {
            this.device = device;
        }

        public void setProgressBar(boolean enable) {
            this.progressBar.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
        }

        public void setSelected(boolean enable) {

            radioButton.setChecked(enable);

        }

        @Override
        public void onClick(View v) {

            if ( mOnPrinterClickListener != null) {

                mOnPrinterClickListener.onClick(device);
                setProgressBar(true);
                setSelected(true);
            }

            mOnPrinterClickListener = null;

        }
    }

    public void setOnPrinterClickListener(OnPrinterClickListener listener) {

        mOnPrinterClickListener = listener;
    }

    public void clear() { printerList.clear();}

    public PrinterViewAdapter(ArrayList<BluetoothDevice> printerList) {

        this.printerList = printerList;


    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.content_list_printer, viewGroup, false);

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


        BluetoothDevice device = printerList.get(position);

        viewHolder.setName(device.getName());
        viewHolder.setAddress(device.getAddress());
        viewHolder.setState(device.getBondState() == BluetoothDevice.BOND_BONDED ? "paired" : "");

        viewHolder.setDevice(device);
        viewHolder.setProgressBar(false);
        viewHolder.setSelected(false);

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return printerList.size();


    }





}

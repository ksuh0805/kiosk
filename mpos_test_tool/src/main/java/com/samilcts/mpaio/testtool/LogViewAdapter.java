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

package com.samilcts.mpaio.testtool;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Provide views to RecyclerView with data from log items.
 */
public class LogViewAdapter extends RecyclerView.Adapter<LogViewAdapter.ViewHolder> {
    private static final String TAG = "LogViewAdapter";

    final private Object sync = new Object();
    private final ArrayList<LogItem> logItems;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvDetail;
        private final TextView tvSubContent;

        public ViewHolder(View v) {
            super(v);

            tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvContent = (TextView) v.findViewById(R.id.tvContent);
            tvSubContent = (TextView) v.findViewById(R.id.tvSubContent);
            tvDetail = (TextView) v.findViewById(R.id.tvDetail);
        }

        public TextView getTvTitle() {
            return tvTitle;
        }
        public TextView getTvContent() {
            return tvContent;
        }
        public TextView getTvSubContent() {
            return tvSubContent;
        }

        public TextView getTvDetail() {
            return tvDetail;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)



     public void addItem(LogItem logItem) {

        synchronized (sync) {

            logItems.add(logItem);

            if (logItems.size() > 500)
                logItems.remove(0);
        }

    }

    public void clear() { logItems.clear();}

    public LogViewAdapter(ArrayList<LogItem> logItems) {
        this.logItems = logItems;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.log_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    synchronized public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your data set at this position and replace the contents of the view
        // with that element

        synchronized (sync) {
            LogItem logItem = logItems.get(position);


        viewHolder.getTvTitle().setText(logItem.getTitle());

        String content = logItem.getContent();

        if (content == null || content.equals("")) viewHolder.getTvContent().setVisibility(View.GONE);
        else {
            viewHolder.getTvContent().setVisibility(View.VISIBLE);
            viewHolder.getTvContent().setText(content);
        }
        String subContent = logItem.getSubContent();

        if (subContent == null || subContent.equals("")) viewHolder.getTvSubContent().setVisibility(View.GONE);
        else {
            viewHolder.getTvSubContent().setVisibility(View.VISIBLE);
            viewHolder.getTvSubContent().setText(subContent);
        }

        String detail = logItem.getDetail();

        if (detail == null || detail.equals("")) viewHolder.getTvDetail().setVisibility(View.GONE);
        else {
            viewHolder.getTvDetail().setVisibility(View.VISIBLE);
            viewHolder.getTvDetail().setText(detail);
        }

        }

    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {

        synchronized (sync) {
            return logItems.size();
        }
    }







}

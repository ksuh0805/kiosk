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

package com.samilcts.mpaio.testtool.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samilcts.mpaio.testtool.LogItem;
import com.samilcts.mpaio.testtool.LogViewAdapter;
import com.samilcts.mpaio.testtool.R;

import java.util.ArrayList;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class LogViewFragment extends Fragment {

    private static final String TAG = "RecyclerViewFragment";

    protected RecyclerView mRecyclerView;
    protected final LogViewAdapter mAdapter;
   // protected RecyclerView.LayoutManager mLayoutManager;
    protected final ArrayList<LogItem> logItems;


    public LogViewFragment() {

        logItems = new ArrayList<>();
        mAdapter = new LogViewAdapter(logItems);
    }

    public RecyclerView getRecyclerView() {

        return mRecyclerView;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragement_log, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rvLog);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
       // linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);

     mRecyclerView.setLayoutManager(linearLayoutManager);

        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        // END_INCLUDE(initializeRecyclerView)


        return rootView;
    }


    public void clear() {

        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

    }


}

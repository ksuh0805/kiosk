package com.samilcts.mpaio.testtool.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.samilcts.mpaio.testtool.R;
import com.samilcts.mpaio.testtool.ScenarioTestActivity;
import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.util.android.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mskim on 2015-09-07.
 * mskim@31cts.com
 */
public class ScenarioListFragment extends Fragment implements AbsListView.OnItemClickListener {

    public static final String EXTRA_SCENARIO = "extra.scenario";
    private static final String ARG_SCENARIO_LIST = "scenarioList";


    private List<String> scenarioList;
    private ScenarioAdapter scenarioAdapter;
    private final Logger logger = AppTool.getLogger();


    public static ScenarioListFragment newInstance(ArrayList<String> scenarioList) {
        ScenarioListFragment fragment = new ScenarioListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SCENARIO_LIST, scenarioList);


        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            scenarioList = (List<String>) getArguments().getSerializable(ARG_SCENARIO_LIST);
        }

        scenarioAdapter = new ScenarioAdapter(getActivity(), scenarioList);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_command_list, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


        ListView lvCommand = (ListView)getView().findViewById(R.id.lvCommand);
        lvCommand.setAdapter(scenarioAdapter);
        lvCommand.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



        String scenario = scenarioAdapter.getItem(position);

        logger.i("LIST", "position : " + position + " name : " + scenario);


        Intent i = new Intent(getActivity(), ScenarioTestActivity.class);

        i.putExtra(EXTRA_SCENARIO, scenario);

        startActivity(i);


    }

    /**
     * @author mskim
     * 검색 목록 어댑터 클래스
     */

    public class ScenarioAdapter extends BaseAdapter {
        private static final String TAG = "ScenarioAdapter";

        final List<String> scenarioList;

        final LayoutInflater inflater;

        public ScenarioAdapter(Context context, List<String> scenarioList) {
            inflater = LayoutInflater.from(context);

            this.scenarioList = scenarioList;

        }

        @Override
        public int getCount() {
            return scenarioList.size();
        }

        @Override
        public String getItem(int position) {

            return scenarioList.get(position);

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if ( convertView == null) {
                convertView = inflater.inflate(R.layout.command_list_element, null);
            }

            String scenario = scenarioList.get(position);

            final TextView tvCommandName = (TextView) convertView.findViewById(R.id.tvCommandName);
            final TextView tvCommandCode = (TextView) convertView.findViewById(R.id.tvCommandCode);

            tvCommandName.setText(scenario);
            tvCommandCode.setText("");

            return convertView;
        }


    }


}

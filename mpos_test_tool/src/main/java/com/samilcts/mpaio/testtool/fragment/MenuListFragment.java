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

import com.samilcts.mpaio.testtool.CommandTestActivity;
import com.samilcts.mpaio.testtool.R;
import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.mpaio.testtool.util.TestMenu;
import com.samilcts.util.android.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mskim on 2015-09-07.
 * mskim@31cts.com
 */
public class MenuListFragment extends Fragment implements AbsListView.OnItemClickListener {

    public static final String EXTRA_COMMAND_INT = "extra.command.parcelable";


    private static final String ARG_COMMAND_LIST = "scenarioList";


    private List<TestMenu> menuList;
    private MenuAdapter menuAdapter;
    private final Logger logger = AppTool.getLogger();


    public static MenuListFragment newInstance(ArrayList<TestMenu> commandList) {
        MenuListFragment fragment = new MenuListFragment();
        Bundle args = new Bundle();
        //args.putParcelableArrayList(ARG_COMMAND_LIST, menuList);

        args.putSerializable(ARG_COMMAND_LIST, commandList);

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            menuList = (ArrayList<TestMenu>)getArguments().getSerializable(ARG_COMMAND_LIST);
        }

        menuAdapter = new MenuAdapter(getActivity(), menuList);
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
        lvCommand.setAdapter(menuAdapter);
        lvCommand.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



        TestMenu command = menuAdapter.getItem(position);

        logger.i("LIST", "position : " + position + " name : " + command.name());


        Intent i = new Intent(getActivity(), CommandTestActivity.class);

        i.putExtra(EXTRA_COMMAND_INT, command.getCode());

        startActivity(i);


    }

    /**
     * @author mskim
     * 검색 목록 어댑터 클래스
     */

    public class MenuAdapter extends BaseAdapter {
        private static final String TAG = "DeviceAdapter";

        final List<TestMenu> menuList;

        final LayoutInflater inflater;

        public MenuAdapter(Context context, List<TestMenu> menuList) {
            inflater = LayoutInflater.from(context);

            this.menuList = menuList;

        }

        @Override
        public int getCount() {
            return menuList.size();
        }

        @Override
        public TestMenu getItem(int position) {

            return menuList.get(position);

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

            TestMenu testMenu = menuList.get(position);

            final TextView tvCommandName = (TextView) convertView.findViewById(R.id.tvCommandName);
            final TextView tvCommandCode = (TextView) convertView.findViewById(R.id.tvCommandCode);

            tvCommandName.setText(testMenu.name().replace("_", " "));
            tvCommandCode.setText(String.format("0x%04X",testMenu.getCode()));

            return convertView;
        }


    }


}

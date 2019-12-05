package com.samilcts.mpaio.testtool;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.samilcts.media.usb.RxUsb;
import com.samilcts.mpaio.testtool.fragment.ManualCommandFragment;
import com.samilcts.mpaio.testtool.fragment.MenuListFragment;
import com.samilcts.mpaio.testtool.fragment.ScenarioListFragment;
import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.mpaio.testtool.util.StateHandler;
import com.samilcts.mpaio.testtool.util.TestMenu;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.crypto.Crypto;
import com.samilcts.sdk.mpaio.crypto.DefaultAES128;
import com.samilcts.sdk.mpaio.ext.dialog.RxConnectionDialog;
import com.samilcts.sdk.mpaio.ext.nice.log.LogTool;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;
import com.samilcts.util.android.ToastUtil;

import net.yslibrary.licenseadapter.LicenseAdapter;
import net.yslibrary.licenseadapter.LicenseEntry;
import net.yslibrary.licenseadapter.Licenses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;


public class MainActivity extends BaseMpaioServiceActivity {

    private static final String TAG = "MainActivity";


    final private String[] fragmentNames = new String[]{"Manual", "Scenario", "All", "Prepaid card", "Devices", "Information", "Control" };

    private static int instanceCount = 0;

    private TextView tvConnectionState;

    private ManualCommandFragment manualCommandFragment;
    private RxConnectionDialog connectionDialog;
    private StateHandler mStateHandler;
    private final Logger logger  = AppTool.getLogger();
    private int prePosition = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (++instanceCount > 1) {

            finish();
        }

        super.onCreate(savedInstanceState);

        logger.setLevel(BuildConfig.DEBUG ? Logger.DEBUG : Logger.NONE);

      /*  if (BuildConfig.FLAVOR.equals("product")) {
            Fabric.with(this, new Crashlytics());
        }*/

        setContentView(R.layout.activity_main);

        //first launch setting.
        if ( !Preference.getInstance(this).get("firstLaunch2", false)) {

            Preference.getInstance(this).set("firstLaunch2", true);
            Preference.getInstance(this).set("useAutoStopCommand", true);
            Set<String> set = new HashSet<>();

            set.add("Message");

            Preference.getInstance(this).set("logType",set);

        }


        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);


        ViewPager commandViewpager = findViewById(R.id.commandViewpager);
        commandViewpager.setAdapter(new FragAdaptor(getSupportFragmentManager()));




        // Bind the tabs to the ViewPager
        TabLayout CommandTabs = findViewById(R.id.CommandTabs);

        CommandTabs.setupWithViewPager(commandViewpager);

        CommandTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (0 == prePosition && 1 != tab.getPosition()  ) {

                    prePosition = tab.getPosition();


                    if (manualCommandFragment != null) {

                        manualCommandFragment.stop();
                        manualCommandFragment.clear();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

     /*   CommandTabs.setAllCaps(false);
        CommandTabs.setShouldExpand(true);

        CommandTabs.setViewPager(commandViewpager);

        CommandTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (0 == prePosition && 1 != position  ) {

                    prePosition = position;


                    if (manualCommandFragment != null) {

                        manualCommandFragment.stop();
                        manualCommandFragment.clear();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
/*

        Crypto test = new DefaultAES128();

        test.initKey(new byte[]{(byte)0xDB, (byte)0xD4, (byte)0xE2,(byte)0xF3,0x2D,0x59
                ,(byte)0x82,(byte)0xE0,0x1C,0x3C,(byte)0xDE,0x50,0x2D,0x31,0x50,0x6E});


        byte[] dec = test.decrypt(new byte[]{(byte)0x22,(byte)0xA3,(byte)0xAA,(byte)0x4E,(byte)0x7D,(byte)0x72,(byte)0xB0,(byte)0x86
                ,(byte)0x5B,(byte)0xD3,(byte)0x50,(byte)0x03,(byte)0xF9,(byte)0x1F,(byte)0x5F,(byte)0x4E} );

        logger.i(TAG, Converter.toHexString(dec));*/




    }


    @Override
    protected void onDestroy() {

        instanceCount--;

        Intent deviceServiceIntent = new Intent(this, MpaioService.class);
        stopService(deviceServiceIntent);

        super.onDestroy();
    }

    //fragment adapter class
    class FragAdaptor extends FragmentPagerAdapter {


        public FragAdaptor(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            ArrayList<TestMenu> list = new ArrayList<>();

            switch (i) {

                case 1:

                    ArrayList<String> scenarioList = new ArrayList<>();

                    scenarioList.add(ScenarioTestActivity.SCENARIO_MUTUAL_AUTHENTICATION);
                    scenarioList.add(ScenarioTestActivity.SCENARIO_PAY_WITH_NICE_VAN);
                    scenarioList.add(ScenarioTestActivity.SCENARIO_REVOKE_WITH_NICE_VAN);

                    if (BuildConfig.FLAVOR.equals("dev")) {

                        scenarioList.add(ScenarioTestActivity.SCENARIO_INJECT_KEY);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_NICE_SERVER_INFO);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_CRN);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_MANAGING_SERVER_INFO);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_INJECT_PREPAID_CARD_KEY);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_NETWORK_INFO);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_RUN_MODE);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_HARDWARE_REVISION);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_SERIAL_NUMBER);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_UPDATE_SERVER_INFO);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_CAT_ID);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_CHARGE_FACTORS);
                        scenarioList.add(ScenarioTestActivity.SCENARIO_SET_CASH_IN_PULSE_CONFIG);

                    }

                    return ScenarioListFragment.newInstance(scenarioList);

                case 2:

                    Collections.addAll(list, TestMenu.values());

                    return MenuListFragment.newInstance(list);

                case 3:
                    list = new ArrayList<>(TestMenu.getCommandList(MpaioCommand.READ_PREPAID_BALANCE >>> 8));
                    
                    return MenuListFragment.newInstance(list);

                case 4:
                    list = new ArrayList<>(TestMenu.getCommandList(MpaioCommand.READ_BARCODE >>> 8));
                    list.addAll(TestMenu.getCommandList(MpaioCommand.READ_MS_CARD >>> 8));
                    list.addAll(TestMenu.getCommandList(MpaioCommand.READ_EMV_CARD >>> 8));
                    list.addAll(TestMenu.getCommandList(MpaioCommand.READ_RFID_CARD >>> 8));
                    list.addAll(TestMenu.getCommandList(MpaioCommand.READ_PIN_PAD >>> 8));

                    return MenuListFragment.newInstance(list);

                case 5:
                    list = new ArrayList<>(TestMenu.getCommandList(MpaioCommand.GET_DATE_TIME >>> 8));
                    return MenuListFragment.newInstance(list);

                case 6:
                    list = new ArrayList<>(TestMenu.getCommandList(MpaioCommand.STOP));
                    return MenuListFragment.newInstance(list);

                default:

                    manualCommandFragment = new ManualCommandFragment();
                    return manualCommandFragment;
            }


        }

        @Override
        public int getCount() {
            return fragmentNames != null ? fragmentNames.length : 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (fragmentNames == null || fragmentNames.length <= position) {
                return "unknown";
            }

            return fragmentNames[position];


        }


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        if ( mpaioService != null) {
            boolean isConnected = mpaioService.isConnected();
            MenuItem item = menu.findItem(R.id.action_stop);
            item.setVisible(isConnected);
            item = menu.findItem(R.id.action_connect);
            item.setTitle(isConnected ? R.string.action_disconnect : R.string.action_connect);
            return true;
        }

        return super.onPrepareOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected (MenuItem item) {



        switch (item.getItemId()) {

            case R.id.action_connect:

                if (manualCommandFragment != null) manualCommandFragment.onOptionsItemSelected(item);

                if ( mpaioService == null) {
                    ToastUtil.show(this, R.string.not_ready);
                }

                if ( mpaioService.isConnected()) {

                    mpaioService.disconnect();
                    return true;

                } else {
                    mStateHandler.getConnectionDialog(getApplicationContext()).show();
                }

                return true;

            case R.id.action_clear:

                //removeKeys log

                if (manualCommandFragment != null) {

                    manualCommandFragment.clear();
                }

                return true;

            case R.id.action_stop:

                if (manualCommandFragment != null) manualCommandFragment.onOptionsItemSelected(item);
                mpaioService.stop();

                return true;

            case R.id.action_setting:

                startActivity(new Intent(this, PreferenceActivity.class));

                return true;

            case R.id.action_license:

                final List<LicenseEntry> dataSet = new ArrayList<>();

                dataSet.add(Licenses.noContent("Android SDK", "Google Inc.", "https://developer.android.com/sdk/terms.html"));
                dataSet.add(Licenses.fromGitHubApacheV2("ReactiveX/RxJava"));
                dataSet.add(Licenses.fromGitHubApacheV2("ReactiveX/RxAndroid"));
                dataSet.add(Licenses.fromGitHubMIT("afollestad/material-dialogs"));
                dataSet.add(Licenses.fromGitHubApacheV2("pilgr/Paper"));
                dataSet.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"));
                dataSet.add(Licenses.fromGitHubApacheV2("artem-zinnatullin/RxJavaProGuardRules"));
                dataSet.add(Licenses.fromGitHubApacheV2("grantland/android-autofittextview"));
                dataSet.add(Licenses.fromGitHubApacheV2("DreaminginCodeZH/MaterialProgressBar"));

                final LicenseAdapter adapter = new LicenseAdapter(dataSet);

                final MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .adapter(adapter, new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false))
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                Licenses.load(dataSet);
                                adapter.notifyDataSetChanged();
                            }
                        })
                .build();

                dialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }



    }


    /**
     * initialize version, connection info.
     */
    private void init() {
        try {
            ((TextView) findViewById(R.id.tvAppVersion)).setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        tvConnectionState = (TextView) findViewById(R.id.connection_state);
        tvConnectionState.setText(R.string.connection_state_none);

        invalidateOptionsMenu();

        logger.setLevel( true ? Logger.DEBUG : Logger.NONE);
        LogTool.getLogger().setLevel(Logger.DEBUG);
        com.samilcts.sdk.mpaio.log.LogTool.getLogger().setLevel(Logger.DEBUG);
        com.samilcts.media.LogTool.getLogger().setLevel(Logger.DEBUG);



        mpaioService.getDeviceManager().onCashReceived()
                .subscribeOn(Schedulers.trampoline())
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {

                        logger.d(TAG, "cash received");
                    }
                });


    }



    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();

    }

    //mpaio service connected.
    @Override
    protected void onServiceConnected(MpaioService service) {

        init();

        logger.i(TAG, "onServiceConnected");

        mStateHandler= new StateHandler(this, service.getDeviceManager(), tvConnectionState);
        connectionDialog = mStateHandler.getConnectionDialog(this);
        mStateHandler.startHandle();


    }


    @Override
    protected void onStop() {

        if ( null != mStateHandler)
            mStateHandler.stopHandle();

        super.onStop();
    }

    //for android api23 (marshmallow)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        logger.i(TAG, "onRequestPermissionResult ");

        if ( null != connectionDialog  && connectionDialog.isShowing() ) {

            logger.i(TAG, "onRequestPermissionResult2 ");
            connectionDialog.onRequestPermissionResult(requestCode, permissions, grantResults);
        }
    }


    public RxConnectionDialog getConnectionDialog() {

        return mStateHandler.getConnectionDialog(this);
    }
}

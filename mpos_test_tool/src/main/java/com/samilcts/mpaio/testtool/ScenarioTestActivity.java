package com.samilcts.mpaio.testtool;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samilcts.mpaio.testtool.fragment.LogViewFragment;
import com.samilcts.mpaio.testtool.fragment.ScenarioListFragment;
import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.mpaio.testtool.util.CommunicationHandler;
import com.samilcts.mpaio.testtool.util.Output;
import com.samilcts.mpaio.testtool.util.StateHandler;
import com.samilcts.receipt.nice.ReceiptParser;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.sdk.mpaio.callback.ResultCallback;
import com.samilcts.sdk.mpaio.ext.dialog.RxConnectionDialog;
import com.samilcts.sdk.mpaio.ext.nice.MpaioNiceManager;
import com.samilcts.sdk.mpaio.ext.nice.payment.PaymentError;
import com.samilcts.sdk.mpaio.ext.nice.payment.PaymentListener;
import com.samilcts.sdk.mpaio.ext.nice.payment.ReadType;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.ui.dialogs.PinDialog;
import com.samilcts.ui.dialogs.SignDialog;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;
import com.samilcts.util.android.ToastUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

public class ScenarioTestActivity extends BaseMpaioServiceActivity {


    public static final String SCENARIO_MUTUAL_AUTHENTICATION = "MUTUAL AUTHENTICATION";
    public static final String SCENARIO_PAY_WITH_NICE_VAN = "PAY WITH NICE VAN";
    public static final String SCENARIO_REVOKE_WITH_NICE_VAN = "PAY REVOKE WITH NICE VAN";
    public static final String SCENARIO_INJECT_KEY = "INJECT KEY";
    public static final String SCENARIO_SET_NICE_SERVER_INFO ="SET NICE SERVER INFO";
    public static final String SCENARIO_SET_CRN = "SET CRN";
    public static final String SCENARIO_SET_MANAGING_SERVER_INFO = "SET MANAGING SERVER INFO";
    public static final String SCENARIO_INJECT_PREPAID_CARD_KEY = "INJECT PREPAID CARD KEY";
    public static final String SCENARIO_SET_NETWORK_INFO = "SET NETWORK INFO";
    public static final String SCENARIO_SET_RUN_MODE = "SET RUN MODE";
    public static final String SCENARIO_SET_HARDWARE_REVISION = "SET HARDWARE REVISION";
    public static final String SCENARIO_SET_SERIAL_NUMBER = "SET SERIAL NUMBER";

    public static final String SCENARIO_SET_CHARGE_FACTORS = "SET CHARGE FACTORS";
    public static final String SCENARIO_SET_CASH_IN_PULSE_CONFIG = "SET CASH IN PULSE";
    public static final String SCENARIO_SET_UPDATE_SERVER_INFO = "SET UPDATE SERVER INFO";

    public static final String SCENARIO_SET_CAT_ID = "SET CAT ID";


    private boolean stopResend = false;

    private String mScenario;
    private Activity mContext;

    private TextView tvConnectionState;

    private EditText mEtParam;
    private Button btnSendCommand;

    private RecyclerView mRvLog;

    private LogViewFragment fragment;

    private final String TAG = "ScenarioTestActivity";
    private MpaioNiceManager mNiceDeviceManager;
    private StateHandler mStateHandler;
    private CommunicationHandler mCommunicationHandler;

    private final Logger logger = AppTool.getLogger();
    private LinearLayout mllParam;
    private CheckBox mCbHex;

    private SignDialog mSignDialog;
    private PinDialog mPinDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenario_test);

        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        fragment = new LogViewFragment();
        transaction.replace(R.id.flHex, fragment);
        transaction.commit();

        tvConnectionState = (TextView)findViewById(R.id.tvConnectionState);

        try {
            ((TextView) findViewById(R.id.tvAppVersion)).setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mCbHex = (CheckBox) findViewById(R.id.cbHex);

       // mTvHex = (TextView)findViewById(R.id.tvHex);




       // mTvASCII = (TextView)findViewById(R.id.tvASCII);


        mEtParam = (EditText)findViewById(R.id.etParam);
        btnSendCommand = (Button)findViewById(R.id.btnSendCommand);
        btnSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!mpaioService.isConnected()) {

                    RxConnectionDialog dialog = mStateHandler.getConnectionDialog(mContext);
                    if (!dialog.isShowing())
                      dialog.show();
                    return;

                }

                String paramStr =  mEtParam.getText().toString().trim();

                try {
                    stopResend = false;

                    if (SCENARIO_MUTUAL_AUTHENTICATION.equals(mScenario) ){
                        mpaioService.setKey(1);
                        btnSendCommand.setEnabled(false);

                        final Subscription subscription = mNiceDeviceManager.onPaymentReady()
                                .take(1)
                                .subscribe(new Subscriber<MpaioMessage>() {
                                    @Override
                                    public void onCompleted() {
                                        onScenarioEnd();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Output.printError(mRvLog,"PayReady", e);
                                    }

                                    @Override
                                    public void onNext(MpaioMessage mpaioMessage) {
                                    }
                                });


                        mNiceDeviceManager.authenticate(new ResultCallback() {
                            @Override
                            public void onCompleted(final boolean isSuccess) {

                                if ( !isSuccess){

                                    subscription.unsubscribe();
                                    logger.i(TAG, "auth fail");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            btnSendCommand.setEnabled(true);
                                        }
                                    });

                                    ToastUtil.show(getApplicationContext(), "Auth Fail");
                                } else {

                                    logger.i(TAG, "auth ok");
                                    ToastUtil.show(getApplicationContext(), "Auth Success");
                                }
                            }
                        });

                    } else if ( SCENARIO_PAY_WITH_NICE_VAN.equals(mScenario)  ){
                        mpaioService.setKey(1);
                        final String[] params = paramStr.split(" ");

                        if ( params.length != 6) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        final ReadType readType = new ReadType(Integer.parseInt(params[0]));


                        mNiceDeviceManager.useRealVanServer(false);

                        runAfterPayReady(new Runnable() {
                            @Override
                            public void run() {

                                Paper.init(mContext);
                                Paper.book().write(AppTool.KEY_REVOKE_READ_TYPE, (readType.getValue() & 0xFF));

                                mNiceDeviceManager.startPayment(readType, params[1], params[2], params[3], params[4], params[5], paymentListener);

                            }
                        });



                    } else if( SCENARIO_REVOKE_WITH_NICE_VAN.equals(mScenario)) {
                        mpaioService.setKey(1);
                        final String[] params = paramStr.split(" ");

                        if (params.length != 9) {

                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        if ((params[1].equals("25") || params[1].equals("21")) && params[8].length() < 12) {
                            params[8] += "            ".substring(params[8].length());
                        }

                        final ReadType readType = new ReadType(Integer.parseInt(params[0]));

                        mNiceDeviceManager.useRealVanServer(false);
                        runAfterPayReady(new Runnable() {
                            @Override
                            public void run() {
                                mNiceDeviceManager.revokePayment(readType, params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], paymentListener);
                            }
                        });

                    } else if( SCENARIO_INJECT_KEY.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 9 ) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.injectKey(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8]), "Inject NICE Key", 2);

                    } else if( SCENARIO_SET_NICE_SERVER_INFO.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 4) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.setPaymentServerInfo(params[0], params[1], params[2], params[3]), "Set NICE server info", 2);

                    } else if( SCENARIO_SET_CRN.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 1) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth( mNiceDeviceManager.setCrn(params[0]), "Set CRN", 2);
                    } else if(   SCENARIO_SET_MANAGING_SERVER_INFO.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 2) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth( mNiceDeviceManager.setManagingServerInfo(params[0], params[1])
                                , "SET MANAGING SERVER INFO", 2);
                    } else if( SCENARIO_INJECT_PREPAID_CARD_KEY.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 5) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.injectPrepaidCardKey(params[0], params[1], params[2], params[3], params[4])
                                , "Inject Prepaid card Key", 3);

                    } else if( SCENARIO_SET_NETWORK_INFO.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 9) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.setNetworkInfo(params[0], params[1], params[2], params[3], params[4], params[5], params[6], Byte.parseByte(params[7]), params[8])
                                , "Set network info", 2);

                    } else if( SCENARIO_SET_RUN_MODE.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 1) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        byte val = Byte.parseByte(params[0]);
                        runWithAuth(mNiceDeviceManager.setRunMode(val)
                                , "Set run mode", 2);

                    }

                    else if( SCENARIO_SET_HARDWARE_REVISION.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 4) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.setHardwareRevision(params[0], params[1], params[2], params[3])
                                , "Set hardware revision", 2);

                    }
                    else if( SCENARIO_SET_SERIAL_NUMBER.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 1) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.setSerialNumber(params[0])
                                , "Set serial number", 2);

                    }
                    else if( SCENARIO_SET_CHARGE_FACTORS.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 6) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.setChargeFactors(params[0],params[1], params[2], params[3], params[4], params[5].split(","))
                                , "Set Charge Factors", 2);

                    }
                    else if( SCENARIO_SET_CASH_IN_PULSE_CONFIG.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 5) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth(mNiceDeviceManager.setCashInPulse(params[0],params[1], params[2], params[3], params[4].split(","))
                                , "Set Cash In Pulse", 2);

                    } else if(   SCENARIO_SET_UPDATE_SERVER_INFO.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 2) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth( mNiceDeviceManager.setUpdateServerInfo(params[0], params[1])
                                , "SET UPDATE SERVER INFO", 2);

                    } else if( SCENARIO_SET_CAT_ID.equals(mScenario)) {

                        final String[] params = paramStr.split(" ");

                        if (params.length != 3) {
                            ToastUtil.show(mContext, getString(R.string.hint_scenario_parameter));
                            return;
                        }

                        runWithAuth( mNiceDeviceManager.setCatId(params[0], params[1], params[2] )
                                , "SET CAT ID", 2);
                    }

                } catch (NumberFormatException e) {
                    ToastUtil.show(mContext, "Input valid number");
                    e.printStackTrace();

                }


                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                logger.i(TAG, "param : " + Converter.toHexString(mEtParam.getText().toString().trim().getBytes()));

            }
        });



        mScenario = getIntent().getStringExtra(ScenarioListFragment.EXTRA_SCENARIO);

       // setTitle(mCommand.name().replace("_", " "));
       // toolbar.setSubtitle(mCommand.name().replace("_", " "));


        mllParam = (LinearLayout) findViewById(R.id.llParam);
        mllParam.setVisibility(View.GONE);

        logger.i("TAG", mScenario);

        mEtParam.setHint(R.string.hint_scenario_parameter);

        ((TextView)findViewById(R.id.tvCommandName)).setText( mScenario);
    }


    @Override
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();
    }

    /**
     *
     * @param runnable run is called after auth, payment ready.
     */
    private void runAfterPayReady(final Runnable runnable) {
        btnSendCommand.setEnabled(false);

        final Subscription subscription = mNiceDeviceManager.onPaymentReady()
                .take(1)
                .delay(200, TimeUnit.MILLISECONDS)
                .subscribe(new Subscriber<MpaioMessage>() {
                    @Override
                    public void onCompleted() {

                        logger.i(TAG, "onPaymentReady");

                        runnable.run();

                    }

                    @Override
                    public void onError(Throwable e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnSendCommand.setEnabled(true);
                            }
                        });
                        Output.printError(mRvLog,"PayReady", e);
                        //addText("MSG",new byte[0], "ready error : " + e.getMessage());
                    }

                    @Override
                    public void onNext(MpaioMessage mpaioMessage) {
                        printText("MSG",new byte[0], "onPaymentReady");
                    }
                });

        mNiceDeviceManager.authenticate(new ResultCallback() {
            @Override
            public void onCompleted(boolean isSuccess) {

                if ( isSuccess) {

                    printText("MSG",new byte[0], "authentication success");

                } else {

                    subscription.unsubscribe();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSendCommand.setEnabled(true);
                        }
                    });
                    printText("MSG",new byte[0], "authentication fail");
                }

            }
        });
    }

    /**
     *
     * @param method
     */
    private void runWithAuth(final Observable<Boolean> method, final String tag, int keyNumber) {

        btnSendCommand.setEnabled(false);

        mpaioService.setKey(keyNumber);
        mNiceDeviceManager.authenticate(new ResultCallback() {
            @Override
            public void onCompleted(boolean isSuccess) {

                if (isSuccess) {

                    printText("MSG", new byte[0], "authentication success");

                    method
                            .doOnTerminate(new Action0() {
                                @Override
                                public void call() {
                                    mpaioService.setKey(1);
                                }
                            })
                            .timeout(1000, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<Boolean>() {
                                @Override
                                public void onCompleted() {

                                }

                                @Override
                                public void onError(Throwable e) {
                                    btnSendCommand.setEnabled(true);
                                    Output.printError(mRvLog,tag, e);
                                }

                                @Override
                                public void onNext(Boolean b) {

                                    if ( b)  onScenarioEnd();
                                    else  btnSendCommand.setEnabled(true);
                                }
                            });

                } else {

                    mpaioService.setKey(1);
                    //subscription.unsubscribe();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnSendCommand.setEnabled(true);
                        }
                    });
                    printText("MSG", new byte[0], "authentication fail");
                }
            }
        });
    }


    private void onScenarioEnd() {

        final boolean isAutoResend = Preference.getInstance(mContext).get("useAutoReSendCommand", false);
        int time = isAutoResend ? 1000 : 0;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                if ( !isAutoResend) btnSendCommand.setEnabled(true);
                else if( !stopResend) btnSendCommand.performClick();
                else btnSendCommand.setEnabled(true);
            }
        }, time);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_command, menu);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                if (  Preference.getInstance(this).get("useAutoStopCommand", false) ){
                    mpaioService.stop();
                }

                return false;

            case R.id.action_connect:

                if ( mpaioService == null) {
                    ToastUtil.show(this, R.string.not_ready);
                }

                if ( mpaioService.isConnected()) {

                    mpaioService.disconnect();
                    btnSendCommand.setEnabled(true);
                    return true;

                } else {

                    mStateHandler.getConnectionDialog(this).show();
                }

                return true;

            case R.id.action_clear:


                fragment.clear();

                return true;

            case R.id.action_stop:

                stopResend = true;
                mpaioService.stop();
                btnSendCommand.setEnabled(true);
                return true;

            case R.id.action_setting:

                //LogSettingDialog.show(this);
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onStop() {

        stopResend = true;


        if (null !=  mStateHandler){
            mStateHandler.stopHandle();
        }
        if (null !=  mCommunicationHandler){
            mCommunicationHandler.stopHandle();
        }

        super.onStop();
    }


    @Override
    protected void onServiceConnected(MpaioService service) {
        super.onServiceConnected(service);

        mRvLog = fragment.getRecyclerView();
        mNiceDeviceManager = service.getDeviceManager();

        mStateHandler = new StateHandler(this, service.getDeviceManager(), tvConnectionState);
        mStateHandler.startHandle();


        mCommunicationHandler= new CommunicationHandler(getApplicationContext(), service.getDeviceManager(), mRvLog);
        mCommunicationHandler.startHandle();

        customize();

    }

    private void customize() {

        switch (mScenario) {
            case SCENARIO_PAY_WITH_NICE_VAN:
                mllParam.setVisibility(View.VISIBLE);
                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("31 10 00 0 27 300");
                mCbHex.setEnabled(false);
                printText("Information", new byte[0], getString(R.string.info_scenario_pay));
                break;
            case SCENARIO_REVOKE_WITH_NICE_VAN:

                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                Paper.init(mContext);

                if ( mEtParam.getText().length() == 0)
                  mEtParam.setText((CharSequence) Paper.book().read(AppTool.KEY_REVOKE));
                //mEtParam.setText(String.format("31 30 00 0 27 300 000000000000 %s 000000000000", date));
                printText("Information", new byte[0], getString(R.string.info_scenario_revoke) );
                break;

            case SCENARIO_INJECT_KEY :
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("02 2393300001 00 00001 000001841508180098D96776A4A8AE43E6AB75CBF22923F2 00000000000000000000000000000000000000000000 SAMILCTS-PB GAME_MACHINE_TEST 500");
                printText("Information", new byte[0], getString(R.string.info_scenario_inject_key) );
                break;
            case SCENARIO_SET_NICE_SERVER_INFO:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("211.33.136.19 47520 211.33.136.19 65004");
                printText("Information", new byte[0], getString(R.string.info_scenario_set_ip) );
                break;
            case SCENARIO_SET_CRN:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);
                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("2208115770");
                printText("Information", new byte[0], getString(R.string.info_scenario_set_crn) );
                break;
            case SCENARIO_SET_MANAGING_SERVER_INFO:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);
                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("0 0");
                printText("Information", new byte[0], getString(R.string.info_scenario_set_managing_server_info) );
                break;
            case SCENARIO_INJECT_PREPAID_CARD_KEY :
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("PayMGate_G-money FFFFFFFFFFFF SamilCts20161011 PayMGate20000001 500000");
                printText("Information", new byte[0], getString(R.string.info_scenario_inject_prepaid_card_key) );
                break;
            case SCENARIO_SET_NETWORK_INFO :
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("SAMILCTS-PB SAMILCTS! 0 0 0 0 1 0 1");
                printText("Information", new byte[0], getString(R.string.info_scenario_set_network_info) );
                break;
            case SCENARIO_SET_RUN_MODE :
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("1");
                printText("Information", new byte[0], getString(R.string.info_scenario_set_run_mode) );
                break;
            case SCENARIO_SET_HARDWARE_REVISION :
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("1.0 1.0 1.0 1.0");

                printText("Information", new byte[0], getString(R.string.info_scenario_set_hardware_revision) );
                break;
            case SCENARIO_SET_SERIAL_NUMBER :
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("GPN1704SU0100000");

                printText("Information", new byte[0], getString(R.string.info_scenario_set_serial_number) );
                break;

            case SCENARIO_SET_CAT_ID:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("2393300001 SAMILCTS-PB GAME_MACHINE_TEST");

                printText("Information", new byte[0], getString(R.string.info_scenario_set_cat_id) );
                break;

            case SCENARIO_SET_CASH_IN_PULSE_CONFIG:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("200 100 1000 0 1,5,10");

                printText("Information", new byte[0], getString(R.string.info_scenario_set_cash_in_pulse_config) );
                break;

            case SCENARIO_SET_UPDATE_SERVER_INFO:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("192.168.0.78 23013");

                printText("Information", new byte[0], getString(R.string.info_scenario_set_update_server_info) );
                break;

            case SCENARIO_SET_CHARGE_FACTORS:
                mllParam.setVisibility(View.VISIBLE);
                mCbHex.setEnabled(false);

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("1 0 1000 6 1 1,2,3,5,7,10");

                printText("Information", new byte[0], getString(R.string.info_scenario_set_charge_factors) );
                break;
        }
    }

    private final PaymentListener paymentListener = new PaymentListener() {

        @Override
        public void onPayStarted() {

            printText("MSG",new byte[0], "payment started");
        }

        @Override
        public void onSignatureRequested() {

            printText("MSG",new byte[0], "signature requested");


            showSignDialog();
/*
            try {
                // get input stream
                InputStream is = mContext.getAssets().open("test_sign2.png");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ToastUtil.show(mContext, "write test test_sign2.png");
                mNiceDeviceManager.completeSignature(bitmap);
                printText("MSG",new byte[0], "write test signature");
            } catch (IOException ex) {
                ToastUtil.show(mContext, "fail to read sample sing bmp");
            }*/
        }


        @Override
        public void onPinRequested() {

            showPinDialog();

        }

        @Override
        public void onPinDelivered() {

        }

        @Override
        public void onStateNotification(byte[] data) {
            printText("MSG",new byte[0], "state notification");
            //ToastUtil.show(mContext, "Complete Card Reading ");
        }

        @Override
        public void onComplete(byte[] data) {


            onScenarioEnd();

            try {
                ReceiptInfo info = new ReceiptParser(getApplicationContext()).parse(data);


                if ( checkSuccess(info)) {

                    if (ReceiptInfo.TYPE_NICE_PAY == info.type) {
                        Paper.init(getApplicationContext());
                        Paper.init(mContext);

                        int readType = Paper.book().read(AppTool.KEY_REVOKE_READ_TYPE);

                        String tradeType = info.niceReceipt.issuerCode.equals("70") ? "21" : "30";
                        String tradeNumber = info.niceReceipt.issuerCode.equals("70") ? "CASH1" : info.niceReceipt.tradeUniqueNumber;
                        String approvalDate = info.niceReceipt.approvalDate.replaceAll("[/: ]", "").substring(0, 6);

                        String revokeParam = String.format(Locale.getDefault(), "%d %s %s %s %s %s %s %s %s",
                                readType, tradeType, info.niceReceipt.installmentMonth,
                                info.niceReceipt.serviceCharge, info.niceReceipt.tax, info.niceReceipt.totalPrice
                                , info.niceReceipt.approvalNumber.trim(), approvalDate, tradeNumber);
                        Paper.book().write(AppTool.KEY_REVOKE, revokeParam);
                    }
                }

                String text = "";

                if (  info.type <= ReceiptInfo.TYPE_NICE_CANCEL) {
                    text = "[Complete Payment]"
                            //   + raw
                            + "\nResponse code : " + info.niceReceipt.responseCode
                            +"\nApproval number : " + info.niceReceipt.approvalNumber
                            +"\nApproval approvalDate : " + info.niceReceipt.approvalDate
                            +"\nApproval tradeUniqueNumber : " + info.niceReceipt.tradeUniqueNumber
                    ;
                }

                printText("MSG",new byte[0], text);

            } catch (Exception e) {

                Output.printError(mRvLog, "parse", e);
            }

            ToastUtil.show(mContext, "payment completed");
        }


        @Override
        public void onSignatureDelivered() {

            printText("MSG",new byte[0], "sign delivered");
        }

        @Override
        public void onError(PaymentError error) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnSendCommand.setEnabled(true);
                }
            });

            Output.printText(mRvLog, "error", null, error.name());
            ToastUtil.show(mContext, "error : " + error.name());
        }
    };


    private void printText(final String msg, final byte[] rawData, String detail) {

        Output.printText(mRvLog, msg, rawData, detail);
    }

    @Override
    protected void onDestroy() {
        if (  Preference.getInstance(this).get("useAutoStopCommand", false) ){
            mpaioService.stop();
        }
        super.onDestroy();
    }

    private boolean checkSuccess(final ReceiptInfo info) {

        if ( (ReceiptInfo.TYPE_TMONEY_PAY == info.type || ReceiptInfo.TYPE_TMONEY_CANCEL == info.type  || ReceiptInfo.TYPE_PREPAID_RECHARGE == info.type) ) {

            return info.tmoneyReceipt.responseCode.equals("0000");

        } /*else if ( (ReceiptInfo.TYPE_CASHBEE_PAY == info.type || ReceiptInfo.TYPE_CASHBEE_CANCEL == info.type )
                && !info.cashbeeReceipt.responseCode.equals("0000") ){
            message = "캐시비 실패";

        } */ else if ( info.niceReceipt.responseCode.equals("0000") ) {

            return true;
        }

        return false;
    }

    private void showPinDialog() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mPinDialog = new PinDialog(ScenarioTestActivity.this, R.style.MyDialogTheme);
                mPinDialog.setCanceledOnTouchOutside(false);

                mPinDialog.setOnClickListener(new PinDialog.OnClickListener() {
                    @Override
                    public void onClick(String pin, DialogInterface dialogInterface) {


                        mpaioService.getDeviceManager().completePin(pin);

                        dialogInterface.dismiss();
                    }
                });
                mPinDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                        mpaioService.stop();
                        dialogInterface.dismiss();
                        onScenarioEnd();
                    }
                });

                mPinDialog.show();
                //  dismissProgressDialog();

            }
        });
    }


    private void showSignDialog() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mSignDialog = new SignDialog(ScenarioTestActivity.this, R.style.MyDialogTheme);
                mSignDialog.setCanceledOnTouchOutside(false);

                mSignDialog.setOnClickListener(new SignDialog.OnClickListener() {
                    @Override
                    public void onClick(Bitmap sign, boolean draw, final DialogInterface dialogInterface) {

                        if (!draw) {
                            ToastUtil.show(getApplicationContext(), R.string.input_signature);
                        } else {

                            mpaioService.getDeviceManager().completeSignature(sign);
                            dialogInterface.dismiss();
                        }
                    }
                });

                mSignDialog.show();
                //  dismissProgressDialog();

            }
        });
    }
}

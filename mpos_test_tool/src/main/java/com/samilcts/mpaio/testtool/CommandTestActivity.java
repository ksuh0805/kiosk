package com.samilcts.mpaio.testtool;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.samilcts.mpaio.testtool.fragment.MenuListFragment;
import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.mpaio.testtool.util.CommunicationHandler;
import com.samilcts.mpaio.testtool.util.Output;
import com.samilcts.mpaio.testtool.util.StateHandler;
import com.samilcts.mpaio.testtool.util.TestMenu;
import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.error.ResponseError;
import com.samilcts.sdk.mpaio.ext.dialog.RxConnectionDialog;
import com.samilcts.sdk.mpaio.ext.nice.MpaioNiceManager;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;
import com.samilcts.util.android.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class CommandTestActivity extends BaseMpaioServiceActivity {


    private Command mCommand;
    private Activity mContext;

    private TextView tvConnectionState;

    private EditText mEtParam;
    private Button btnSendCommand;

    private LogViewFragment fragment;

    private CheckBox cbHex;
    private final String TAG = "CommandTestActivity";
    private RecyclerView mRvLog;
    private StateHandler mStateHandler;

    private CommunicationHandler mCommunicationHandler;
    private Subscription mSubscription;
    private MpaioNiceManager mConnectionManager;
    private final Logger logger = AppTool.getLogger();
    private int mCommandCode;
    private LinearLayout mLlParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command_test);

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

        cbHex = (CheckBox)findViewById(R.id.cbHex);

        mEtParam = (EditText)findViewById(R.id.etParam);
        btnSendCommand = (Button)findViewById(R.id.btnSendCommand);
        btnSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!mpaioService.isConnected()) {

                    RxConnectionDialog dialog = mStateHandler.getConnectionDialog(CommandTestActivity.this);
                    if (!dialog.isShowing())
                      dialog.show();
                    return;

                }

                String paramStr =  mEtParam.getText().toString().trim();
                byte[] param;

                try {

                    if (cbHex.isChecked() && !paramStr.isEmpty()) {

                        param = AppTool.hexToByteArray(paramStr);

                    } else {
                        paramStr = paramStr.replace("\\r", new String(new byte[]{(byte)0x0D}));
                        paramStr = paramStr.replace("\\n", new String(new byte[]{(byte)0x0A}));
                        param = paramStr.getBytes();
                    }



                    final boolean isAutoResend = Preference.getInstance(mContext).get("useAutoReSendCommand", false);
                    if ( isAutoResend) btnSendCommand.setEnabled(false);


                   Observable<MpaioMessage> observable = mConnectionManager.rxSyncRequest(mConnectionManager.getNextAid(), mCommand.getCode(), param)
                            .timeout(1000, TimeUnit.MILLISECONDS);

                    observable = observable.concatWith(AppTool.waitNotify(mConnectionManager, mCommand));

                    if ( isAutoResend)
                        observable = observable.delay(1,TimeUnit.SECONDS).repeat();

                    mSubscription = observable.observeOn(AndroidSchedulers.mainThread())
                           .subscribe(new Subscriber<MpaioMessage>() {
                                @Override
                                public void onCompleted() {

                                    if ( !isAutoResend )
                                       btnSendCommand.setEnabled(true);

                                }

                                @Override
                                public void onError(Throwable e) {

                                    btnSendCommand.setEnabled(true);
                                    Output.printError(mRvLog, "request", e);
                                }

                                @Override
                                public void onNext(MpaioMessage mpaioMessage) {

                                    if (ResponseError.NO_ERROR.equals(mpaioMessage.getData()[0])){
                                        //ok...
                                        int i = 1;
                                    }

                                    logger.d(TAG, "rx receive : " + Converter.toHexString(mpaioMessage.getAID()));
                                }
                            });

                } catch (NumberFormatException e) {
                    ToastUtil.show(mContext, "Input valid number");
                    e.printStackTrace();

                }

                InputMethodManager imm = (InputMethodManager)getSystemService(
                        INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        });


        //mCommand = getIntent().getParcelableExtra(MenuListFragment.EXTRA_COMMAND_INT);

        mCommandCode = getIntent().getIntExtra(MenuListFragment.EXTRA_COMMAND_INT, 0);
        mCommand = new MpaioCommand(mCommandCode);

        mLlParam = (LinearLayout) findViewById(R.id.llParam);
        mLlParam.setVisibility(View.GONE);



        ((TextView)findViewById(R.id.tvCommandName)).setText(TestMenu.fromCode(mCommandCode).name().replace("_", " "));



    }


    private void customize() {
        int hCode = mCommand.getCode()[1];

        if ( (hCode == 0x01 && (mCommand.getCode()[0] & 0xFF) % 2 == 1 )|| mCommand.equals(MpaioCommand.JUMP_TO_BOOT) ){
            mLlParam.setVisibility(View.VISIBLE);
        } else if (mCommand.equals(MpaioNiceCommand.READ_RFID_CARD)) {
            mLlParam.setVisibility(View.VISIBLE);
        } else if (mCommand.equals(MpaioNiceCommand.RECHARGE_PREPAID_CARD)
                || mCommand.equals(MpaioNiceCommand.REFUND_PREPAID_BALANCE)
                || mCommand.equals(MpaioNiceCommand.PURCHASE_BY_PREPAID_CARD)
                || mCommand.equals(MpaioNiceCommand.READ_PREPAID_TRANSACTION_LOG)) {
            mLlParam.setVisibility(View.VISIBLE);
        }

        String title = "Information";

        switch (mCommandCode) {
            case MpaioCommand.SET_DATE_TIME:

                if ( mEtParam.getText().length() == 0)
                  mEtParam.setText(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));
                printText(title, new byte[0], getString(R.string.info_command_datetime));
                break;
            case MpaioCommand.RECHARGE_PREPAID_CARD:
            case MpaioCommand.REFUND_PREPAID_BALANCE:
            case MpaioCommand.PURCHASE_BY_PREPAID_CARD:
                printText(title, new byte[0], getString(R.string.info_command_prepaid_change));
                break;
            case MpaioCommand.READ_PREPAID_TRANSACTION_LOG:

                if ( mEtParam.getText().length() == 0)
                    mEtParam.setText("12");
                printText(title, new byte[0], getString(R.string.info_command_prepaid_log));
                break;
        }
    }

    private void printText(final String msg, final byte[] rawData, String detail) {

        Output.printText(mRvLog, msg, rawData, detail);
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

                mpaioService.stop();
                btnSendCommand.setEnabled(true);

                if (null != mSubscription)
                mSubscription.unsubscribe();
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



        if (null !=  mStateHandler){
            mStateHandler.stopHandle();
        }

        if (null !=  mCommunicationHandler){
            mCommunicationHandler.stopHandle();
        }

        if (null != mSubscription)
            mSubscription.unsubscribe();


        super.onStop();
    }
    @Override
    protected void onServiceConnected(MpaioService service) {
        super.onServiceConnected(service);
        mConnectionManager = mpaioService.getDeviceManager();
        mRvLog = fragment.getRecyclerView();
        mStateHandler = new StateHandler(this, service.getDeviceManager(), tvConnectionState);
        mStateHandler.startHandle();

        mCommunicationHandler= new CommunicationHandler(getApplicationContext(), service.getDeviceManager(), mRvLog);
        mCommunicationHandler.startHandle();
        customize();
    }


    @Override
    protected void onDestroy() {

        if (  Preference.getInstance(this).get("useAutoStopCommand", false) ){
            mpaioService.stop();
        }
        super.onDestroy();
    }
}

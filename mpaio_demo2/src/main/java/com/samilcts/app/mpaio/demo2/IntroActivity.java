package com.samilcts.app.mpaio.demo2;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;

import android.view.View;
import android.widget.ProgressBar;



import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PaymgateUtil;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.app.mpaio.demo2.util.ShowActivity;
import com.samilcts.media.State;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.ext.dialog.RxConnectionDialog;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Logger;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.internal.util.SubscriptionList;
import rx.schedulers.Schedulers;


public class IntroActivity extends MpaioBaseActivity {


    private RxConnectionDialog connectionDialog;

    private boolean isStarted = false;

    private Logger logger = AppTool.getLogger();

    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.prog) ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        ButterKnife.bind(this);

        if ( mpaioManager != null) {
            mpaioManager.setMaximumPacketLength(256);
            mpaioManager.setBleWriteInterval(0);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.d(TAG, "onResume");
        mSubscriptionList = new SubscriptionList();

        if ( mpaioManager.isConnected())
            onConnected();
        else
            requestConnection();
    }

    private void onConnected() {

        logger.i(TAG, "PAYMGATE Connected");

        Subscription s1 = stop()
                .delay(2000, TimeUnit.MILLISECONDS)
                .concatWith(getModelName())
                .concatWith(setDate())
           .subscribe(new Subscriber<byte[]>() {
               @Override
               public void onCompleted() {

                   //goBuy();
                   goShow();

               }

               @Override
               public void onError(Throwable e) {

                  /* Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG)
                           .show();*/

                   logger.i(TAG, "Throwable : " + e.toString());
                   PaymgateUtil.disconnect(mpaioManager);
               }

               @Override
               public void onNext(byte[] data) {
                   //logger.i(TAG, "mposMessage : " + Converter.toHexString((byte[])mposMessage.getData()));
               }
           });

        mSubscriptionList.add(s1);
    }

    private void goBuy() {
        if ( !isStarted ) {
            Intent i = new Intent(getApplicationContext(), BuyActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();

            isStarted = true;
        }
    }

    private void goShow() {
        if (!isStarted) {
            Intent i = new Intent(getApplicationContext(), ShowActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();

            isStarted = true;
        }
    }

    @Override
    protected void onStop() {

        if ( null != mSubscriptionList )
            mSubscriptionList.unsubscribe();

        super.onStop();

    }


    @Override
    protected void onStateChanged(int state) {
        super.onStateChanged( state);

        logger.i(TAG, "intro state : " + state);

        switch (state) {

            case State.CONNECTING:

                progressBar.setVisibility(View.VISIBLE);

                break;
            case State.CONNECTED:

                onConnected();

                break;

            case State.DISCONNECTED:

                onConnected();
                /*Snackbar.make(coordinatorLayout,mState == State.CONNECTED
                        ? R.string.disconnected : R.string.fail_to_connect, Snackbar.LENGTH_LONG)
                        .show();

                mState = state;

                if (!mSubscriptionList.isUnsubscribed())
                    requestConnection();

                progressBar.setVisibility(View.INVISIBLE);

                break;*/
                //goBuy();
                goShow();

        }

        mState = state;
    }

    /**
     * MPAIO 연결 팝업 출력
     */
    private void requestConnection(){

        mpaioManager.connect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                        //dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        //tvUsbMsg.setText(e.getMessage());
                        //tvUsbMsg.setVisibility(View.VISIBLE);
                        logger.w(TAG, "onError : " + e.getMessage());
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
      /*if ( connectionDialog == null ) {

            connectionDialog = new RxConnectionDialog(IntroActivity.this, mpaioManager);
            connectionDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                    finish();
                }
            });

            connectionDialog.setCanceledOnTouchOutside(false);
        }

        if ( mState != State.CONNECTING && mState != State.CONNECTED)
            connectionDialog.show();*/
    }

    private int mState = State.DISCONNECTED;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

      if ( connectionDialog != null && connectionDialog.isShowing()) {

            connectionDialog.onRequestPermissionResult(requestCode, permissions, grantResults);
        }

    }

    /**
     * set paymgate device date and time to app local date and time
     * @return onNext returns response payload
     */
    private Observable<byte[]> setDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());

        String date = dateFormat.format(new Date());

        return PaymgateUtil.requestOk(mpaioManager, MpaioCommand.SET_DATE_TIME, date.getBytes())//rxSyncRequestOk(mMpaioManager.getNextAID(), new DefaultCommand(DefaultCommand.SET_DATE_TIME).getCode(), date.getBytes())
                .subscribeOn(Schedulers.io())
                .timeout(1, TimeUnit.SECONDS)
                .retry(2);
    }

    /**
     * set paymgate device date and time to app local date and time
     * @return onNext returns response payload
     */
    private Observable<byte[]> getModelName() {

        return PaymgateUtil.requestOk(mpaioManager, MpaioCommand.GET_MODEL_NAME, null)
                .doOnNext(new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {

                        if ( bytes.length > 1)
                            SharedInstance.deviceModelName = new String(Arrays.copyOfRange(bytes, 1, bytes.length));

                        logger.i(TAG, "deviceModelName : " +  SharedInstance.deviceModelName);
                    }
                })
                .subscribeOn(Schedulers.io())

                .timeout(1, TimeUnit.SECONDS)
                .retry(2);
    }


    /**
     * stop paymgate device
     * @return onNext returns response payload
     */
    private Observable<byte[]> stop() {

        return PaymgateUtil.requestOk(mpaioManager, MpaioCommand.STOP, null) //rxSyncRequestOk(mMpaioManager.getNextAID(),  new DefaultCommand(DefaultCommand.STOP).getCode() , new byte[0])
                .subscribeOn(Schedulers.io())
                .timeout(1, TimeUnit.SECONDS)
                .retry(2)
                ;
    }

}

package com.samilcts.mpaio.testtool.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.samilcts.media.State;
import com.samilcts.media.ble.BleState;
import com.samilcts.media.usb.UsbState;
import com.samilcts.mpaio.testtool.R;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.ext.dialog.RxConnectionDialog;
import com.samilcts.util.android.Logger;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by mskim on 2016-07-13.
 * mskim@31cts.com
 */
public class StateHandler {


    private static final String TAG = "StateHandler";
    private final TextView mLabel;
    private MaterialDialog mProgressDialog;
    private RxConnectionDialog mConnectionDialog;
    private final MpaioManager mConnectionManager;
    private Activity mActivity;
    private Subscription mSubscription;
    private final Logger logger = AppTool.getLogger();

    public StateHandler(Activity activity, MpaioManager connectionManager, TextView label) {

        mActivity = activity;
        mConnectionManager = connectionManager;
        mLabel = label;
    }


    public void stopHandle() {

        if(null != mSubscription ) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }

    }

    public void startHandle() {

        mSubscription = mConnectionManager
                .onStateChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<State>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        logger.i(TAG, "onError : " + e.getMessage());
                    }

                    @Override
                    public void onNext(State state) {

                        boolean isBleState = state instanceof BleState;
                        String type = isBleState ? "BLE" : (state instanceof UsbState ? "USB" : "UART");

                        logger.i(TAG, "type : " + type);

                        switch (state.getValue()) {

                            case State.CONNECTING:

                                logger.i(TAG, "STATE_CONNECTING");

                                mProgressDialog = new MaterialDialog.Builder(mActivity)
                                        .content(setConnectionStateText(type, R.string.connection_state_connecting))
                                        .progress(true, 0)
                                        .cancelable(false)
                                        .show();


                                break;

                            case State.CONNECTED:
                                logger.i(TAG, "STATE_CONNECTED!");

                                setConnectionStateText(type, R.string.connection_state_connected);

                                mActivity.invalidateOptionsMenu();


                                setPacketSetting();
                                if (mProgressDialog != null) mProgressDialog.dismiss();
                                getConnectionDialog(mActivity).dismiss();

                                break;

                            case State.DISCONNECTED:
                                logger.i(TAG, "STATE_DISCONNECTED");

                                setConnectionStateText(type, R.string.connection_state_disconnected);

                                //ToastUtil.show(mActivity, "disconnected");

                                 mActivity.invalidateOptionsMenu();

                                if (mProgressDialog != null)
                                    mProgressDialog.dismiss();

                                if (!mConnectionManager.isConnected() && !mActivity.isFinishing()) {

                                    if (!getConnectionDialog(mActivity).isShowing())
                                        getConnectionDialog(mActivity).show();
                                }

                                break;


                        }

                    }
                });



        if ( mConnectionManager.isBleConnected()) {
            setConnectionStateText("BLE", R.string.connection_state_connected);

        } else if (mConnectionManager.isUsbConnected()) {
            setConnectionStateText("USB", R.string.connection_state_connected);
        } else if (mConnectionManager.isSerialConnected()) {
            setConnectionStateText("UART", R.string.connection_state_connected);
        } else {
            setConnectionStateText("", R.string.connection_state_none);
        }

    }

    synchronized public RxConnectionDialog getConnectionDialog(Context context) {

        if ( null == mConnectionDialog )
            mConnectionDialog =  new RxConnectionDialog(context, mConnectionManager);

        return mConnectionDialog;
    }

    private String setConnectionStateText(String type, final int resId) {

        String msg = mActivity.getString(resId).replace("#1", type);

        if (mLabel != null)
            mLabel.setText(msg);

        return msg;
    }

    private void setPacketSetting() {

        int length = 256;
        int interval = 0;

       if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //length = 100;
            interval = 50;
        }
        mConnectionManager.setMaximumPacketLength(length);
        mConnectionManager.setBleWriteInterval(interval);
    }
}

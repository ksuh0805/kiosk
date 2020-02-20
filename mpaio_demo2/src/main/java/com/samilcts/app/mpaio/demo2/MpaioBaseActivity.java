package com.samilcts.app.mpaio.demo2;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.media.State;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.ext.nice.MpaioNiceManager;
import com.samilcts.util.android.ToastUtil;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;


public abstract class MpaioBaseActivity extends BaseActivity {

    protected SubscriptionList mSubscriptionList = new SubscriptionList();

    private static MpaioBaseActivity mActivity;

    private static boolean isConnected = false;

    protected static MpaioNiceManager mpaioManager;
    protected MpaioService mpaioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        if (null == SharedInstance.mpaioManager) {
            logger.i(TAG, "new manager : ");

            mpaioManager = new MpaioNiceManager(getApplicationContext());
            SharedInstance.mpaioManager = mpaioManager;

            mpaioManager.onStateChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<State>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(State state) {
                            logger.i(TAG, "state : " + state.getValue());
                            onStateChanged(state.getValue());
                        }
                    });
        }

        Intent deviceServiceIntent = new Intent(this, MpaioService.class);
        startService(deviceServiceIntent);
        bindService(deviceServiceIntent, mpaioServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mpaioServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mpaioService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            logger.i(TAG, "mDeviceServiceConnection");
            mpaioService = ((MpaioService.LocalBinder) service).getService();
            mpaioService.setmDeviceManager(mpaioManager);
            //mpaioManager = mpaioService.getDeviceManager();

            //SeqMutualAuthentication();
            //BaseMpaioServiceActivity.this.onServiceConnected(mpaioService);
        }
    };

    protected void onStateChanged(int state) {

        switch (state) {

            case State.CONNECTING:

                break;

            case State.CONNECTED:

                mActivity.onMpaioConnected();
                break;

            case State.DISCONNECTED:

                mActivity.onMpaioDisconnected();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnected();
    }

    /**
     * check mpaio is connected. if not connected, show disconnection dialog.
     */
    public void checkConnected() {
/*
        if ( !mpaioManager.isConnected() && this.getClass() != IntroActivity.class ) {
            showDisconnectionConfirmDialog();
        }*/

    }

    protected void onMpaioConnected() {
        isConnected  = true;
    }

    protected void onMpaioDisconnected() {


        if ( isConnected && !mpaioManager.isConnected() && this.getClass() != IntroActivity.class ) {
            //temp test
            //showDisconnectionConfirmDialog();
        }


        isConnected = false;
    }

    @Override
    protected void onDestroy() {

        if ( null != mSubscriptionList  )
            mSubscriptionList.unsubscribe();

        super.onDestroy();
    }
}

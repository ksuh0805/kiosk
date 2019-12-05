package com.samilcts.app.mpaio.demo2;
import android.os.Bundle;

import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.media.State;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.util.android.ToastUtil;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.internal.util.SubscriptionList;


public abstract class MpaioBaseActivity extends BaseActivity {

    protected SubscriptionList mSubscriptionList = new SubscriptionList();

    private static MpaioBaseActivity mActivity;

    private static boolean isConnected = false;

    protected static MpaioManager mpaioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        if ( null ==  SharedInstance.mpaioManager) {
            logger.i(TAG, "new manager : ");

            mpaioManager = new MpaioManager(getApplicationContext());
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



    }


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

        if ( !mpaioManager.isConnected() && this.getClass() != IntroActivity.class ) {
            showDisconnectionConfirmDialog();
        }

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

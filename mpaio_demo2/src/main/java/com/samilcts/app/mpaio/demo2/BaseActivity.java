package com.samilcts.app.mpaio.demo2;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialize.MaterializeBuilder;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.util.android.Logger;

import java.util.ArrayList;



/**
 * Created by mskim on 2016-07-18.
 * mskim@31cts.com
 */
public class BaseActivity extends AppCompatActivity {

    private static final Object obj = new Object();

    private static int activityCount = 0;
    private static ArrayList<Activity> activities = new ArrayList<>();

    private boolean stateSaved = false;
    protected String TAG = "";

    private BaseActivity mActivity;

    protected Logger logger = AppTool.getLogger();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppTool.setDebug(BuildConfig.DEBUG ? Logger.VERBOSE : Logger.NONE);
        SharedInstance.init(getApplicationContext());

        TAG = getLocalClassName();

        mActivity = this;

        synchronized (obj) {
            // boolean isReconnectCall = getIntent().getBooleanExtra(MainActivity.EXTRA_CALL_RECONNECT,false);
            //  if ( this instanceof MainActivity && activityCount > 1  && !isReconnectCall ) finish();

            activityCount++;
            activities.add(this);

            //   logger.i(getLocalClassName(), " activityCount : " + activityCount);
        }


        boolean lock_portrait = getResources().getBoolean(R.bool.lock_portrait);

        //if (lock_portrait) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        stateSaved = false;




    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new MaterializeBuilder()
                .withActivity(this)
                .withFullscreen(true)
                .withTranslucentNavigationBar(false)
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        stateSaved = false;
    }


    @Override
    protected void onDestroy() {
        AppTool.getLogger().i(getLocalClassName(), " onDestroy3");
        synchronized (obj) {

            activityCount--;

            AppTool.getLogger().i(getLocalClassName(), " activityCount : " + activityCount);

            if (activityCount <= 0 && !stateSaved) {
                SharedInstance.release();
                activityCount = 0;


            }

        }


        super.onDestroy();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Icepick.saveInstanceState(this, outState);

        stateSaved = true;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
       // Icepick.restoreInstanceState(this, savedInstanceState);
        stateSaved = false;
    }

    /**
     * show disconnection confirm dialog
     */
    public void showDisconnectionConfirmDialog() {

        if (mActivity != null) {

            if (mActivity.isFinishing() || mActivity.isDestroyed()) return;

            try {

                new MaterialDialog.Builder(mActivity)
                        .content(R.string.app_exited)
                        .cancelable(false)
                        .positiveText(android.R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                exitApp();


                            }
                        })
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * finish all activity
     */
    protected void exitApp() {

        for (Activity act :
                activities) {
            act.finish();
        }
        activities.clear();
        SharedInstance.release();

    }






}

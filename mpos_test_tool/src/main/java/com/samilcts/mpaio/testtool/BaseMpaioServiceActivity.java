package com.samilcts.mpaio.testtool;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.util.android.Logger;


/**
 * Created by mskim on 2015-07-14.
 * mskim@31cts.com
 */
public class BaseMpaioServiceActivity extends AppCompatActivity {


    private final String TAG = this.getClass().getSimpleName();
    MpaioService mpaioService;
    private final Logger logger = AppTool.getLogger();
    private final ServiceConnection mpaioServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mpaioService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            logger.i(TAG, "mDeviceServiceConnection");
            mpaioService = ((MpaioService.LocalBinder) service).getService();

            BaseMpaioServiceActivity.this.onServiceConnected(mpaioService);

        }
    };


    @Override
    protected void onStart() {
        super.onStart();


        Intent deviceServiceIntent = new Intent(this, MpaioService.class);
        startService(deviceServiceIntent);
        bindService(deviceServiceIntent, mpaioServiceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {

        unbindService(mpaioServiceConnection);
        super.onStop();
    }

    void onServiceConnected(MpaioService service) {

    }

}

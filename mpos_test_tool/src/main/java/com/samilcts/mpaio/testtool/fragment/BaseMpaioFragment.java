package com.samilcts.mpaio.testtool.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;

import com.samilcts.mpaio.testtool.MpaioService;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseMpaioFragment extends Fragment {


    MpaioService mpaioService;

    public BaseMpaioFragment() {
        // Required empty public constructor
    }


    @Override
    public void onResume() {
        super.onResume();

        Intent deviceServiceIntent = new Intent(getActivity(), MpaioService.class);
        getActivity().bindService(deviceServiceIntent, mpaioServiceConnection, Context.BIND_AUTO_CREATE);


    }


    @Override
    public void onStop() {

        try {
            getActivity().unbindService(mpaioServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        super.onStop();
    }

    private final ServiceConnection mpaioServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mpaioService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mpaioService = ((MpaioService.LocalBinder) service).getService();

            BaseMpaioFragment.this.onServiceConnected(mpaioService);

        }
    };




    void onServiceConnected(MpaioService service) {


      /*  MessageDigest hash;
        try {
             hash = MessageDigest.getInstance("SHA-256");

            NiceDeviceManager nice = new NiceDeviceManager(getContext(), service.getDeviceManager(), new PaymentListener() {
                @Override
                public void onSignatureRequested() {

                }

                @Override
                public void onComplete(byte[] data) {

                }
            }, new DefaultPacker(new DefaultAES128(), new DefaultAES128(), hash));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }*/
    }
}

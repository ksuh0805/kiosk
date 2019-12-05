package com.samilcts.mpaio.testtool.fragment;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.samilcts.mpaio.testtool.MainActivity;
import com.samilcts.mpaio.testtool.MpaioService;
import com.samilcts.mpaio.testtool.R;
import com.samilcts.mpaio.testtool.util.AppTool;
import com.samilcts.mpaio.testtool.util.CommunicationHandler;
import com.samilcts.mpaio.testtool.util.Output;
import com.samilcts.sdk.mpaio.ext.dialog.RxConnectionDialog;
import com.samilcts.sdk.mpaio.ext.nice.MpaioNiceManager;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;
import com.samilcts.util.android.ToastUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManualCommandFragment extends BaseMpaioFragment {

    private EditText etCmd;
    private EditText etParam;

    private Button btnSendCommand;
    private LogViewFragment fragment;
    private CheckBox cbHex;

    private CommunicationHandler mCommunicationHandler;
    private Subscription mSubscription;
    private final Logger logger  = AppTool.getLogger();

    public ManualCommandFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_command, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);




        etCmd = (EditText)getView().findViewById(R.id.etCmd);
        etParam = (EditText)getView().findViewById(R.id.etParam);
        btnSendCommand =  (Button)getView().findViewById(R.id.btnSend);
        cbHex =  (CheckBox)getView().findViewById(R.id.cbHex);


        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        fragment = new LogViewFragment();
        transaction.replace(R.id.flHex, fragment);

        transaction.commit();
    }

    @Override
    public void onStop() {

     /*   btnSendCommand.setEnabled(true);

        if (  Preference.getInstance(getContext()).get("useAutoStopCommand", false) ){

            if ( mpaioService != null)
                mpaioService.stop();
        }*/

        if ( mCommunicationHandler != null )
            mCommunicationHandler.stopHandle();

        if ( null != mSubscription)
            mSubscription.unsubscribe();
        super.onStop();
    }


    public void stop() {

        btnSendCommand.setEnabled(true);
        mpaioService.stop();

        if ( mCommunicationHandler != null ) {
            mCommunicationHandler.stopHandle();
            mCommunicationHandler.startHandle();
        }

        if ( null != mSubscription)
            mSubscription.unsubscribe();
    }


    @Override
    void onServiceConnected(MpaioService service) {
        super.onServiceConnected(service);


        mCommunicationHandler= new CommunicationHandler(getContext(), service.getDeviceManager(), fragment.getRecyclerView());
        mCommunicationHandler.startHandle();


        btnSendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( !mpaioService.isConnected() ) {


                    RxConnectionDialog dialog = ((MainActivity)getActivity()).getConnectionDialog();
                    if ( !dialog.isShowing())
                        dialog.show();
                    return;

                }

/*
                final MpaioNiceManager manager2 = mpaioService.getDeviceManager();

                manager2.setSerialConfig(115200, UsbSerialInterface.DATA_BITS_8, UsbSerialInterface.STOP_BITS_1,
                        UsbSerialInterface.PARITY_NONE, UsbSerialInterface.FLOW_CONTROL_OFF);

                final EscposBuilder pos = new EscposBuilder();

                final Action1<byte[]> action1 = new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {

                        logger.i("PRINT", Converter.toHexString(bytes));
                    }
                };*/

               /* manager2.rxSendBytes(pos.addSetAlign(0)).subscribe(action1);
                manager2.rxSendBytes(pos.addText("this is test1")).subscribe(action1);
                manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);

                manager2.rxSendBytes(pos.addText("this is test2\r\n")).subscribe(action1);


                manager2.rxSendBytes(pos.addSetAlign(0)).subscribe(action1);
                manager2.rxSendBytes(pos.addText("this is left align\r\n")).subscribe(action1);

                manager2.rxSendBytes(pos.addSetAlign(1)).subscribe(action1);
                manager2.rxSendBytes(pos.addText("this is center align\r\n")).subscribe(action1);

                manager2.rxSendBytes(pos.addSetAlign(2)).subscribe(action1);
                manager2.rxSendBytes(pos.addText("this is right align\r\n")).subscribe(action1);


                manager2.rxSendBytes(pos.addText("this is test1")).subscribe(action1);
                manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);

                manager2.rxSendBytes(pos.addText("this is test2\r\n")).subscribe(action1);

                manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);
                manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);
                manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);*/

            /*    manager2.rxSendBytes(pos.addSetAlign(0)).subscribe(action1);
                byte[] data = BytesBuilder.merge(pos.addText("this is test1"), pos.addNewLine(), pos.addText("this is test2\r\n"),
                        pos.addSetAlign(0), pos.addText("this is left align\r\n"), pos.addSetAlign(1), pos.addText("this is center align\r\n"), pos.addSetAlign(2), pos.addText("this is right align\r\n"));

                manager2.rxSendBytes(data).subscribe(action1);

                manager2.rxSendBytes(pos.addLineFeed(3)).subscribe(action1);
*/

            /*    pos.addSetAlign(0)
                        .addNewLine(1)
                        .addText("this is test2")
                        .addNewLine(1)
                        .addSetAlign(0)
                        .addText("this is left align")
                        .addNewLine(1)
                        .addSetAlign(1)
                        .addText("this is center align")
                        .addNewLine(1)
                        .addSetAlign(2)
                        .addText("this is right align")
                        .addNewLine(4);




                logger.i("PRINT", "start print image");

               // manager2.rxSendBytes(pos.build()).subscribe(action1);

                try {
                    // get input stream
                    *//*InputStream is = getContext().getAssets().open("testSign.bmp");
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
*//*
                    Bitmap bitmap =  BitmapFactory.decodeResource(getContext().getResources(),
                            R.drawable.samil_logo);

                    Bitmap resizeBitmap = Bitmap.createScaledBitmap(bitmap, 384, 132, true);

                    bitmap.recycle();

                    pos.addImage(resizeBitmap)
                    .addNewLine(3);
                    logger.i("PRINT", "all data size : " + pos.build(false).length);
                    manager2.rxSendBytes(pos.build()).subscribe(action1);
*/
                /*    manager2.rxSendBytes(builder.pop(4)).subscribe(action1);


                   Observable<byte[]> obs = Observable.create(new Observable.OnSubscribe<byte[]>() {
                        @Override
                        public void call(Subscriber<? super byte[]> subscriber) {

                            while(builder.getSize() > 0) {
                                subscriber.onNext(builder.pop(384));
                              *//*  try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }*//*
                            }

                            subscriber.onCompleted();

                        }
                    });

                    obs.subscribeOn(Schedulers.trampoline())
                            .doOnCompleted(new Action0() {
                                @Override
                                public void call() {
                                    manager2.rxSendBytes(pos.addLineFeed(3)).subscribe(action1);
                                }
                            })
                            .subscribe(new Action1<byte[]>() {
                        @Override
                        public void call(byte[] bytes) {

                            manager2.rxSendBytes(bytes).subscribe(new Action1<byte[]>() {
                                @Override
                                public void call(byte[] bytes) {

                                    logger.i("PRINT", "send bitmap data " +  Converter.toHexString(bytes));
                                }
                            });
                        }
                    });*/



/*

                } catch (Exception ex) {
                    ex.printStackTrace();
                    ToastUtil.show(getContext(), "fail to read sample sing bmp");
                }


             */
/*   manager2.rxSendBytes(pos.addSetAlign(0)).subscribe(action1);
                byte[] data = BytesBuilder.merge(pos.addText("this is test1"), pos.addNewLine(), pos.addText("this is test2\r\n"),
                        pos.addSetAlign(0), pos.addText("this is left align\r\n"));

                byte[] data2 = BytesBuilder.merge(pos.addSetAlign(1), pos.addText("||this is center align\r\n"), pos.addSetAlign(2), pos.addText("this is right align\r\n"));

                manager2.rxSendBytes(data).subscribe(action1);
                manager2.rxSendBytes(data2).subscribe(action1);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);
               // manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);
               // manager2.rxSendBytes(pos.addNewLine()).subscribe(action1);*//*



                if (true) return;
*/


                String cmdString = etCmd.getText().toString().trim();



                    try {
                        int cmdInt = Integer.parseInt(cmdString, 16);
                        byte[] cmd = Converter.toBigEndianBytes((short) cmdInt);

                        String paramStr = etParam.getText().toString().trim();

                        byte[] param;

                        if ( cbHex.isChecked() && !paramStr.isEmpty()) {

                            param = AppTool.hexToByteArray(paramStr);

                        } else {


                            paramStr = paramStr.replace("\\r", new String(new byte[]{(byte)0x0D}));
                            paramStr = paramStr.replace("\\n", new String(new byte[]{(byte)0x0A}));
                            param = paramStr.getBytes();
                        }

                        logger.i("param", " param : " + Converter.toHexString(param));


                        final boolean isAutoResend = Preference.getInstance(getContext()).get("useAutoReSendCommand", false);
                        if ( isAutoResend) btnSendCommand.setEnabled(false);

                        MpaioNiceManager manager = mpaioService.getDeviceManager();

                        Observable<MpaioMessage> observable = manager.rxSyncRequest(manager.getNextAid(), cmd, param)
                                .timeout(1000, TimeUnit.MILLISECONDS);

                        observable = observable.concatWith(AppTool.waitNotify(mpaioService.getDeviceManager(), new MpaioNiceCommand(cmd)));

                        if ( isAutoResend)
                            observable = observable.delay(1, TimeUnit.SECONDS).repeat();

                        mSubscription = observable.subscribe(new Subscriber<MpaioMessage>() {
                                    @Override
                                    public void onCompleted() {

                                        if (!isAutoResend) btnSendCommand.setEnabled(true);
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                        btnSendCommand.setEnabled(true);
                                        Output.printError(fragment.getRecyclerView(), "write", e);
                                    }

                                    @Override
                                    public void onNext(MpaioMessage mpaioMessage) {

                                    }
                                });

                    } catch (NumberFormatException e) {

                        ToastUtil.show(getActivity(), "Input valid number");
                        e.printStackTrace();
                    }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        });




        //////




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_stop:

                btnSendCommand.setEnabled(true);

                if ( null != mSubscription)
                    mSubscription.unsubscribe();

                break;

            case R.id.action_connect:

                if ( mpaioService == null) {
                    ToastUtil.show(getContext(), R.string.not_ready);
                }
                if ( mpaioService.isConnected()) {
                    btnSendCommand.setEnabled(true);
                }

                break;


        }

        return super.onOptionsItemSelected(item);
    }

    public void clear() {

        fragment.clear();

    }

}

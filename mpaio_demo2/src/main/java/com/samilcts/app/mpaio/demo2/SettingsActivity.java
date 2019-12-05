package com.samilcts.app.mpaio.demo2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.samilcts.app.mpaio.demo2.adapter.PrinterViewAdapter;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.PrintTool;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.printer.android.Printer;
import com.samilcts.printer.android.StateChangeListener;
import com.samilcts.util.android.BluetoothUtil;
import com.samilcts.util.android.Preference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends BaseActivity {

    private BluetoothUtil btUtil;


    @BindView(R.id.rvPrinterList)
    RecyclerView rvPrinterList;

    @BindView(R.id.tvPrint) TextView tvPrint;

    @BindView(R.id.btnScan)
    Button btnScan;

    @BindView(R.id.cbState)
    CheckBox cbState;

    @BindView(R.id.tvConnectedPrinter)
    TextView tvConnectedPrinter;

    @BindView(R.id.print) Button print;

    private ArrayList<BluetoothDevice> printerList = new ArrayList<>();
    private PrinterViewAdapter adapter;
    public static final String EXTRA_TYPE_SUB = "extra.type.isSubActivity";
    private boolean isSubActivity;
    private ReceiptInfo mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppTool.setTitleFont(this);

        btUtil = new BluetoothUtil(getApplicationContext());





        isSubActivity = getIntent().getBooleanExtra(EXTRA_TYPE_SUB, false);

        if ( !isSubActivity) {
            AppTool.buildNavigationDrawer(this, toolbar);
            mInfo = new ReceiptInfo();
        } else {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mInfo = (ReceiptInfo)getIntent().getSerializableExtra(CartActivity.EXTRA_RECEIPT_INFO);

            tvPrint.setText(R.string.print_receipt);
            print.setText(R.string.print);
        }

        adapter = new PrinterViewAdapter(printerList);

        adapter.setOnPrinterClickListener(printerClickListener());

        rvPrinterList.setAdapter(adapter);

        rvPrinterList.setLayoutManager(new LinearLayoutManager(getBaseContext()));

      /*  rvPrinterList.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                final BluetoothDevice device = printerList.get(position);



            }
        }));*/

       cbState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!isChecked) {

                    SharedInstance.getPrinter().disconnect();
                    setPrinterOff();
                    printerList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });

/*
        MpaioNiceManager deviceManager =  SharedInstance.getDeviceManager();
        if ( deviceManager != null && deviceManager.isConnected())
            deviceManager.rxSyncRequest(SharedInstance.getNextAID(), new NiceCommand(NiceCommand.STOP).getCode(), new byte[0]).subscribe();
*/



    }



    @NonNull
    private PrinterViewAdapter.OnPrinterClickListener printerClickListener() {
        return new PrinterViewAdapter.OnPrinterClickListener() {
            @Override
            public void onClick(final BluetoothDevice device) {

                Printer printer = SharedInstance.getPrinter();

                if (printer.isConnected()) {

                    printer.setStateChangeListener(null);
                    printer.disconnect();
                }

                printer = SharedInstance.getPrinter();

                final Printer finalPrinter = printer;

                if (printer != null) {
                    printer.setStateChangeListener(new StateChangeListener() {
                        @Override
                        public void onConnected() {

                            setPrinterOn(finalPrinter, device.getName(), finalPrinter.getModel(), device.getAddress());

                            printerList.clear();
                            adapter.notifyDataSetChanged();


                            Preference.getInstance(getApplicationContext()).set(SharedInstance.PREF_LAST_CONNECTED_PRINTER_ADDRESS, device.getAddress());
                            Preference.getInstance(getApplicationContext()).set(SharedInstance.PREF_LAST_CONNECTED_PRINTER_MODEL, finalPrinter.getModel().ordinal());

                            Snackbar.make(rvPrinterList, "connected with " + device.getName(), Snackbar.LENGTH_LONG).show();

                            // ToastUtil.show(getBaseContext(), "connect to " + device.getName());
                        }

                        @Override
                        public void onConnecting() {

                            Snackbar.make(rvPrinterList, "connecting to " + device.getName(), Snackbar.LENGTH_LONG).show();

                            // ToastUtil.show(getBaseContext(), );
                           // Log.i(TAG, "device. companyAddress  : " + device.getAddress());
                        }

                        @Override
                        public void onDisconnected(boolean isFailConnect) {

                            setPrinterOff();

                            if (isFailConnect) {
                                tvConnectedPrinter.setText(" connection failed");
                                Snackbar.make(rvPrinterList, "connection failed", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(rvPrinterList, "disconnected with " + device.getName(), Snackbar.LENGTH_LONG).show();
                            }

                            adapter.notifyDataSetChanged();
                            adapter.setOnPrinterClickListener(printerClickListener());
                        }
                    });
                }
                printer.connect(device);
            }
        };
    }

    private void setPrinterOff() {

        cbState.setChecked(false);
        cbState.setEnabled(false);
        tvConnectedPrinter.setText(" please connect");
    }

    private void setPrinterOn(Printer finalPrinter, String name, Printer.Model model, String address) {

        cbState.setEnabled(true);
        cbState.setChecked(true);

        tvConnectedPrinter.setText("");



        tvConnectedPrinter.setText(String.format(" [%s] %s - %s", model.name(), name, address));



    }


    @Override
    protected void onStart() {
        super.onStart();


        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

       // BluetoothAdapter.ACTION

        registerReceiver(bluetoothReceiver, intentFilter);



        final Printer printer = SharedInstance.getPrinter();
        final BluetoothDevice btPrinter = AppTool.getLastConnectedPrinterDevice(this);

        printer.setStateChangeListener(new StateChangeListener() {
            @Override
            public void onConnected() {

                setPrinterOn(printer, btPrinter.getName(), printer.getModel(), btPrinter.getAddress());
            }

            @Override
            public void onConnecting() {

            }

            @Override
            public void onDisconnected(boolean isFailConnect) {
                setPrinterOff();
            }
        });

        if (printer.isConnected()) {
            setPrinterOn(printer, btPrinter.getName(), printer.getModel(), btPrinter.getAddress());
        } else {
            tvConnectedPrinter.setText(" please connect");
            cbState.setEnabled(false);
        }


    }


    @Override
    protected void onStop() {
        super.onStop();


        unregisterReceiver(bluetoothReceiver);

        SharedInstance.getPrinter().setStateChangeListener(null);


        Printer printer = SharedInstance.getPrinter();

        if ( printer != null) {

            printer.setStateChangeListener(null);
        }

    }

    @OnClick(R.id.print)
    void testPrint() {


        if ( !SharedInstance.getPrinter().isConnected()) {

            if ( SharedInstance.getInternalPrinterModels().contains(SharedInstance.deviceModelName) && !isSubActivity) {
                PrintTool.printInternalPrinterSample(this);
                return;
            }

            Snackbar.make(rvPrinterList, R.string.printer_not_connected, Snackbar.LENGTH_LONG)
            .show();

            return;

        }

        mInfo.cartItems = SharedInstance.getCartItems();

        if ( !isSubActivity) {
            PrintTool.printSample(this);

        } else if ( PrintTool.printReceipt(this, mInfo) ) {

            finish();
        }



    }


    @OnClick(R.id.btnScan)
    void onScan(){

        adapter.setOnPrinterClickListener(printerClickListener());

        if ( btnScan.isSelected()) {
            btUtil.stopScan();

        } else {

            btUtil.startScan();

        }

    }


    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //do something
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name = device.getName();

                if ( name != null/* && (name.contains("WOOSIM") || name.contains("SPP-R200II"))*/ && !printerList.contains(device) ) {

                    printerList.add(device);
                    adapter.notifyDataSetChanged();
                }

            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                btnScan.setText(R.string.start);
                btnScan.setSelected(false);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {


                printerList.clear();

                printerList.addAll(AppTool.getPairedPrinters(context, Printer.Model.WOOSIM));
                printerList.addAll(AppTool.getPairedPrinters(context, Printer.Model.BIXOLON));

                Printer printer = SharedInstance.getPrinter();
                if ( printer!= null && printer.isConnected() ) {

                    printerList.remove( AppTool.getLastConnectedPrinterDevice(context));


                }



                adapter.notifyDataSetChanged();

                btnScan.setText(R.string.stop);
                btnScan.setSelected(true);
            }




        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {

            SharedInstance.clearCartItem();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        boolean sub = getIntent().getBooleanExtra(EXTRA_TYPE_SUB, false);

        if(!sub) AppTool.showExitConfirmDialog(this);
        else super.onBackPressed();
    }


}

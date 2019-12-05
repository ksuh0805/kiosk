package com.samilcts.app.mpaio.demo2.util;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.robototextview.RobotoTypefaces;
import com.devspark.robototextview.style.RobotoTypefaceSpan;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import com.samilcts.app.mpaio.demo2.BuyActivity;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.KeyPadActivity;
import com.samilcts.app.mpaio.demo2.PrepaidCardActivity;

import com.samilcts.app.mpaio.demo2.SettingsActivity;
import com.samilcts.printer.android.Printer;
import com.samilcts.printer.android.StateChangeListener;
import com.samilcts.receipt.nice.data.NiceReceipt;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.util.android.BluetoothUtil;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Logger;
import com.samilcts.util.android.Preference;
import com.samilcts.util.android.ToastUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by mskim on 2015-12-04.
 * mskim@31cts.com
 */
public class AppTool {




    //public static final String KEY_REVOKE_READ_TYPE = "key.revoke.read.type";
    public static final String KEY_REVOKE_APPROVAL_NUMBER = "key.revoke.approval.number";
    public static final String KEY_REVOKE_APPROVAL_DATE = "key.revoke.approval.date";
    public static final String KEY_REVOKE_TRADE_NUMBER = "key.revoke.trade.number";
    public static final String KEY_REVOKE_TOTAL = "key.revoke.total";
    public static final String KEY_REVOKE_INSTALMENT_MONTH = "key.revoke.installment.month";
    public static final String KEY_REVOKE_TYPE = "key.revoke.type";



    public static List<BluetoothDevice> getPairedPrinters(Context context, Printer.Model model){

        BluetoothUtil btUtil = new BluetoothUtil(context);

        String name = model == Printer.Model.BIXOLON ? "SPP-R200II" : "WOOSIM";

        Set<BluetoothDevice> deviceSet = btUtil.getBondedDevices();

        ArrayList<BluetoothDevice> list = new ArrayList<>();

        for (BluetoothDevice device :
                deviceSet) {

            if ( device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING
                    && device.getName().contains(name)) {
                list.add(device);
            }
        }

        return list;
    }


    public static List<BluetoothDevice> getPairedPrinters(Context context){

        int model = Preference.getInstance(context).get(SharedInstance.PREF_LAST_CONNECTED_PRINTER_MODEL, 0);


        return getPairedPrinters(context, Printer.Model.fromOrdinal(model));

    }


    public static String localPath="";


    public static void connectLastPrinter(Context context){

        BluetoothDevice btPrinter = getLastConnectedPrinterDevice(context);

        Printer printer = SharedInstance.getPrinter();

        if (printer != null && btPrinter != null && !printer.isConnected())
            printer.connect(btPrinter);
    }

    public static BluetoothDevice getLastConnectedPrinterDevice(Context context) {

        String address = Preference.getInstance(context).get(SharedInstance.PREF_LAST_CONNECTED_PRINTER_ADDRESS, "");
        int model = Preference.getInstance(context).get(SharedInstance.PREF_LAST_CONNECTED_PRINTER_MODEL, 0);

        return AppTool.getPairedPrinter(context, Printer.Model.fromOrdinal(model), address);
    }

    public static BluetoothDevice getPairedPrinter(Context context, Printer.Model model, String address) {

        List<BluetoothDevice> list = AppTool.getPairedPrinters(context, model);

        for (BluetoothDevice device: list ) {

            if ( device.getAddress().equals(address)) {

                return device;
            }

        }

        return null;
    }

    public static Printer.Model stringToModel(String a) {

        switch (a) {
            case "0":
                return Printer.Model.BIXOLON;
            case "1":
                return Printer.Model.WOOSIM;
            default:
                return null;
        }

    }


    public static void openBluetoothSetting(Context context){

        final Intent intent = new Intent( Settings.ACTION_BLUETOOTH_SETTINGS);
       /* intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings",
                "com.android.settings.bluetooth.BluetoothSettings");
        intent.setComponent(cn);*/
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

/*
    public static void disconnectNoUsePrinter(Context context){


        final String KEY_PRINTER_SETTING = context.getString(R.string.key_printer_setting);
        final String NONE = context.getString(R.string.value_printer_setting_none);

        String setting = Preference.getInstance(context).get(KEY_PRINTER_SETTING, NONE);

        Printer.Model model = (stringToModel(setting));

        for (Printer.Model _model :
                Printer.Model.values()) {

            if ( model != _model && SharedInstace.getPrinter(_model).isConnected() ) {

                SharedInstace.getPrinter(model).disconnect();
            }

        }

    }*/


    public static void setTitleFont(AppCompatActivity activity){

        RobotoTypefaceSpan span = new RobotoTypefaceSpan(
                activity,
                RobotoTypefaces.TYPEFACE_ROBOTO_MEDIUM);
        Spannable spannable = new SpannableString( activity.getSupportActionBar().getTitle());
        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        activity.getSupportActionBar().setTitle(spannable);
    }

    public static void buildNavigationDrawer(final AppCompatActivity activity, Toolbar toolbar){

        final int selectedPosition;

        if ( activity instanceof BuyActivity) {
            selectedPosition = 0;
        } else if (activity instanceof KeyPadActivity  ) {
            selectedPosition = 1;
        } else if (activity instanceof PrepaidCardActivity) {
            selectedPosition = 2;
        } else {
            selectedPosition = 3;
        }

        toolbar.setNavigationIcon(R.drawable.buy_bar_ic01);


        setTitleFont(activity);


        Drawer drawer = new DrawerBuilder()


                .withActivity(activity)
                .withRootView(R.id.frameLayout)
                .withToolbar(toolbar)

                .withSliderBackgroundColor(ContextCompat.getColor(activity, R.color.colorDrawerBackground))



                //.withTranslucentStatusBar(true)
                .withActionBarDrawerToggleAnimated(false)


                .addDrawerItems(new PrimaryDrawerItem().withIcon(R.drawable.ic_menu_buy).withName(R.string.title_activity_buy)

                                .withTextColorRes(R.color.colorWhite70).withSelectedTextColorRes(R.color.colorWhite)
                                .withSelectedColorRes(R.color.colorDark20)
                )
                .addDrawerItems(new PrimaryDrawerItem().withIcon(R.drawable.ic_menu_calculator).withName(R.string.title_activity_keypad)
                        .withTextColorRes(R.color.colorWhite70).withSelectedTextColorRes(R.color.colorWhite)
                        .withSelectedColorRes(R.color.colorDark20))


                .addDrawerItems(new PrimaryDrawerItem().withIcon(R.drawable.ic_menu_prepaid).withName(R.string.title_activity_prepaid_card)
                        .withTextColorRes(R.color.colorWhite70).withSelectedTextColorRes(R.color.colorWhite)
                        .withSelectedColorRes(R.color.colorDark20))

                .addDrawerItems(new PrimaryDrawerItem().withIcon(R.drawable.ic_menu_printer).withName(R.string.title_activity_settings)
                        .withTextColorRes(R.color.colorWhite70).withSelectedTextColorRes(R.color.colorWhite)
                        .withSelectedColorRes(R.color.colorDark20))

                        //아이콘 색 변경 처리 필요.

                //.withActionBarDrawerToggle(new ActionBarDrawerToggle(activity, ))

                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        Intent intent = new Intent();

                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        switch (position) {

                            case 1:

                                intent.setClass(activity.getApplicationContext(), KeyPadActivity.class);
                                intent.putExtra(KeyPadActivity.KEY_EXTRA_TYPE, KeyPadActivity.EXTRA_TYPE_MAIN);
                                break;

                            case 2:
                                intent.setClass(activity.getApplicationContext(), PrepaidCardActivity.class);
                                break;
                            case 3:
                                intent.setClass(activity.getApplicationContext(), SettingsActivity.class);
                                break;

                            default:
                                intent.setClass(activity.getApplicationContext(), BuyActivity.class);
                                break;
                        }

                        if (position != selectedPosition) {
                            SharedInstance.clearCartItem();
                            activity.finish();
                        }


                        activity.startActivity(intent);

                        return false;
                    }
                })

                .withSelectedItemByPosition(selectedPosition)

                .build();



      drawer.setActionBarDrawerToggle(new ActionBarDrawerToggle(activity, drawer.getDrawerLayout(), toolbar, R.string.material_drawer_open, R.string.material_drawer_close));



    }


    public static void showExitConfirmDialog(final Activity activity){

        if ( activity != null) {

            new MaterialDialog.Builder(activity)
                    .title(R.string.title_exit)
                    .cancelable(false)
                    .content(R.string.are_you_sure)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            activity.finish();
                        }
                    })
                    .show();
        }

    }


    public static String getString(ByteBuffer buffer, int length) {

        byte[] temp = getBytes(buffer, length);
        String aa = new String(temp, Charset.forName("ksc5601"));
        BytesBuilder.clear(temp);
        return aa;
    }

    public static byte[] getBytes(ByteBuffer buffer, int length) {

        byte[] temp = new byte[length];
        buffer.get(temp);
        return temp;
    }

    private final static Logger logger;

    public static Logger getLogger(){
        return logger;
    }

    public static void setDebug(int level) {
        logger.setLevel(level);
    }

    static {
        logger = new Logger();
    }


    public static ReceiptInfo getDemoReceipt(Context context){

        ReceiptInfo receiptInfo = new ReceiptInfo();
        receiptInfo.type = ReceiptInfo.TYPE_DEMO;

        NiceReceipt niceReceipt = new NiceReceipt();

        niceReceipt.representativeName = context.getString(R.string.company_representative);
        niceReceipt.companyAddress = context.getString(R.string.company_address);
        niceReceipt.affiliatePhoneNumber = context.getString(R.string.affiliate_phone);
        niceReceipt.affiliateName = context.getString(R.string.affiliate_name);
        niceReceipt.corporateRegistrationNumber = "215-81-73822";

        niceReceipt.issuer = context.getString(R.string.sample_card);
        //niceReceipt.membershipNumber = "5433-****-****-****";
        niceReceipt.catId = "2393300001";
        niceReceipt.approvalDate = "15/12/21 14:32:24";


        niceReceipt.installmentMonth = "00";
       /* niceReceipt.serviceCharge = 913;
        niceReceipt.tax = 91;
        niceReceipt.totalPrice = 1004;*/
        niceReceipt.approvalNumber = "14322089";

        niceReceipt.affiliateNumber = "100063423";
        niceReceipt.acquirerName  = context.getString(R.string.sample_card);

        niceReceipt.remainPoint = 0;
        //niceReceipt.tradeUniqueNumber = "962387015505";
        receiptInfo.niceReceipt = niceReceipt;

        return receiptInfo;
    }

    public static void showPrinterReconnectDialog(final Context context) {

        SharedInstance.getPrinter()
                .setStateChangeListener(new StateChangeListener() {
                    @Override
                    public void onConnected() {

                        ToastUtil.show(context, context.getString(R.string.printer_connected));
                    }

                    @Override
                    public void onConnecting() {
                        ToastUtil.show(context, context.getString(R.string.printer_connecting));
                    }

                    @Override
                    public void onDisconnected(boolean isFailConnect) {

                        if ( isFailConnect)
                            ToastUtil.show(context, context.getString(R.string.printer_fail_connect));
                    }
                });

        new MaterialDialog.Builder(context)
                .content(R.string.want_reconnect_printer)
                .negativeText(android.R.string.no)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        AppTool.connectLastPrinter(context);
                    }
                })
                .show();
    }

}

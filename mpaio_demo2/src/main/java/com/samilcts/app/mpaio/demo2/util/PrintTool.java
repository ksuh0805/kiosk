package com.samilcts.app.mpaio.demo2.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.felhr.usbserial.UsbSerialInterface;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.SampleProduct;
import com.samilcts.printer.android.Printer;
import com.samilcts.receipt.nice.ReceiptParser;
import com.samilcts.receipt.nice.data.NiceReceipt;
import com.samilcts.receipt.nice.data.PrepaidReceipt;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.sdk.mpaio.MpaioManager;
import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.sdk.mpaio.print.EscposBuilder;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;

import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by mskim on 2015-12-18.
 * mskim@31cts.com
 */
public class PrintTool {

    private static final String TAG = "PrintTool";

    public static void printSample(Context context){

        Printer printer = SharedInstance.getPrinter();

        if ( printer.isConnected() ) {

            HashMap<Product, Integer> cartItems = new HashMap<>();

            cartItems.put(SampleProduct.getWatchList(context).get(0).getProduct(), 1);
            cartItems.put(SampleProduct.getWatchList(context).get(1).getProduct(), 1);
            cartItems.put(SampleProduct.getWatchList(context).get(2).getProduct(), 1);

            ReceiptInfo receiptInfo = new ReceiptInfo();
            receiptInfo.type = ReceiptInfo.TYPE_SAMPLE;

            NiceReceipt niceReceipt = new NiceReceipt();

            niceReceipt.representativeName = context.getString(R.string.company_representative);
            niceReceipt.companyAddress = context.getString(R.string.company_address);
            niceReceipt.affiliatePhoneNumber = context.getString(R.string.affiliate_phone);
            niceReceipt.affiliateName = context.getString(R.string.affiliate_name);
            niceReceipt.corporateRegistrationNumber = "215-81-73822";

            niceReceipt.issuer = context.getString(R.string.sample_card);
            niceReceipt.membershipNumber = "5433-****-****-****";
            niceReceipt.catId = "2393300001";
            niceReceipt.approvalDate = "15/12/21 14:32:24";
            niceReceipt.installmentMonth = "00";
            niceReceipt.serviceCharge = 0;
            niceReceipt.tax = 91;
            niceReceipt.totalPrice = 1004;
            niceReceipt.approvalNumber = "14322089";

            niceReceipt.affiliateNumber = "100063423";
            niceReceipt.acquirerName = context.getString(R.string.sample_card);

            niceReceipt.remainPoint = 0;
            niceReceipt.tradeUniqueNumber = "962387015505";
            receiptInfo.niceReceipt = niceReceipt;
            printReceipt(context,receiptInfo, context.getString(R.string.sign_path_test) );

        }
    }

    public static void printInternalPrinterSample(Context context){

        HashMap<Product, Integer> cartItems = new HashMap<>();

        cartItems.put(SampleProduct.getWatchList(context).get(0).getProduct(), 1);
        cartItems.put(SampleProduct.getWatchList(context).get(1).getProduct(), 1);
        cartItems.put(SampleProduct.getWatchList(context).get(2).getProduct(), 1);

        ReceiptInfo receiptInfo = new ReceiptInfo();
        receiptInfo.type = ReceiptInfo.TYPE_SAMPLE;

        NiceReceipt niceReceipt = new NiceReceipt();

        niceReceipt.representativeName = context.getString(R.string.company_representative);
        niceReceipt.companyAddress = context.getString(R.string.company_address);
        niceReceipt.affiliatePhoneNumber = context.getString(R.string.affiliate_phone);
        niceReceipt.affiliateName = context.getString(R.string.affiliate_name);
        niceReceipt.corporateRegistrationNumber = "215-81-73822";

        niceReceipt.issuer = context.getString(R.string.sample_card);
        niceReceipt.membershipNumber = "5433-****-****-****";
        niceReceipt.catId = "2393300001";
        niceReceipt.approvalDate = "15/12/21 14:32:24";
        niceReceipt.installmentMonth = "00";
        niceReceipt.serviceCharge = 0;
        niceReceipt.tax = 91;
        niceReceipt.totalPrice = 1004;
        niceReceipt.approvalNumber = "14322089";

        niceReceipt.affiliateNumber = "100063423";
        niceReceipt.acquirerName = context.getString(R.string.sample_card);

        niceReceipt.remainPoint = 0;
        niceReceipt.tradeUniqueNumber = "962387015505";
        receiptInfo.niceReceipt = niceReceipt;
        printInternalPrinter(context, receiptInfo);


    }
    private static void showPrinterList(Context context) {


        final List<BluetoothDevice> list = AppTool.getPairedPrinters(context);


        ArrayList<String> arrayList = new ArrayList<>();


        for (BluetoothDevice device :
                list) {

            arrayList.add(device.getName() + " (" + device.getAddress() + ")");
        }


        String[] items = new String[arrayList.size()];

        items = arrayList.toArray(items);


        new MaterialDialog.Builder(context)
                .items(items)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {

                                SharedInstance.getPrinter().connect(list.get(i));

                            }
                        }).show();

    }



    public static boolean printReceipt(Context context, ReceiptInfo receiptInfo) {

        Printer printer = SharedInstance.getPrinter();

        if ( printer.isConnected() ) {

            printReceipt(context, receiptInfo, context.getString(R.string.sign_path));


            return true;

        } /*else if (AppTool.getPairedPrinters(context).size() > 0 ) {

            showPrinterList(context);

        } *//*else {

            AppTool.openBluetoothSetting(context);

        }*/

        return false;
    }


    public static Observable<byte[]> printInternalPrinter(Context context, ReceiptInfo receiptInfo) {



        //escpos

        final MpaioManager manager = SharedInstance.mpaioManager;


        manager.disconnect();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        manager.connect().toBlocking().subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                manager.setSerialConfig(115200, UsbSerialInterface.DATA_BITS_8, UsbSerialInterface.STOP_BITS_1,
                        UsbSerialInterface.PARITY_NONE, UsbSerialInterface.FLOW_CONTROL_OFF);

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Void aVoid) {

            }
        });


        final EscposBuilder escpos = new EscposBuilder();

        final Action1<byte[]> action1 = new Action1<byte[]>() {
            @Override
            public void call(byte[] bytes) {

                Log.i("PRINT", Converter.toHexString(bytes));
            }
        };

        //print logo
        Bitmap bmp =  BitmapFactory.decodeResource(context.getResources(),
                R.drawable.samil_logo);
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        float scale = (float) (width > height ? width : height) / (float) 384;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, (int) (width / scale), (int) (height / scale), false);

        bmp.recycle();

        try {
            escpos.addLineFeed(1)
                    .addImage(scaledBitmap)
                    .addLineFeed(2);

        } catch (Exception ex){};
        scaledBitmap.recycle();


        manager.rxSendBytes(escpos.build(false)).subscribe();

        BytesBuilder builder = new BytesBuilder();
        builder.add(escpos.build());
        while(builder.getSize() > 0 ){

            Log.i(TAG, "nice print image : " + Converter.toHexString(builder.pop(1024)));
        }



        if ( true)
        return Observable.empty();
        //11 test






        Observable<byte[]> observable =  PaymgateUtil.requestOk(manager, MpaioCommand.RELAY_PRINTING_COMMAND, escpos.build());


        AppTool.getLogger().i(TAG, "receiptInfo.type " + receiptInfo.type );

        if (receiptInfo.type <= ReceiptInfo.TYPE_NICE_CANCEL) {

            observable.concatWith(printNiceEscpos(context, receiptInfo, manager, receiptInfo.niceReceipt));

        }  else if ( receiptInfo.type <= ReceiptInfo.TYPE_TMONEY_CANCEL){

            //printPrepaid(context, receiptInfo, printer, receiptInfo.prepaidReceipt);

        } else if ( receiptInfo.type <= ReceiptInfo.TYPE_CASHBEE_CANCEL){


            //printPrepaid(context, receiptInfo, printer, receiptInfo.prepaidReceipt);
        } else if ( receiptInfo.type >= ReceiptInfo.TYPE_PREPAID_RECHARGE){

            observable.concatWith(printPrepaidEscpos(context, receiptInfo, manager, receiptInfo.prepaidReceipt));
        }

        return observable.timeout(1000, TimeUnit.MILLISECONDS);

    }


    private static void printReceipt(Context context, ReceiptInfo receiptInfo, String signPath) {

        final Printer printer = SharedInstance.getPrinter();


        //print logo
        Bitmap bmp =  BitmapFactory.decodeResource(context.getResources(),
                R.drawable.samil_logo);
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        float scale = (float) (width > height ? width : height) / (float) 384;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, (int) (width / scale), (int) (height / scale), false);

        bmp.recycle();

        printer.printImage(scaledBitmap);
        scaledBitmap.recycle();

        printer.lineFeed(1);

        AppTool.getLogger().i(TAG, "receiptInfo.type " + receiptInfo.type );

        if (receiptInfo.type <= ReceiptInfo.TYPE_NICE_CANCEL) {

            printNice(context, receiptInfo, printer, receiptInfo.niceReceipt);

        }  else if ( receiptInfo.type <= ReceiptInfo.TYPE_TMONEY_CANCEL){


            //printPrepaid(context, receiptInfo, printer, receiptInfo.prepaidReceipt);

         } else if ( receiptInfo.type <= ReceiptInfo.TYPE_CASHBEE_CANCEL){


            //printPrepaid(context, receiptInfo, printer, receiptInfo.prepaidReceipt);
        } else if ( receiptInfo.type >= ReceiptInfo.TYPE_PREPAID_RECHARGE){

            printPrepaid(context, receiptInfo, printer, receiptInfo.prepaidReceipt);
        }



    }

    private static void printNice(Context context, ReceiptInfo receiptInfo, Printer printer, NiceReceipt niceReceipt) {

        final Locale locale = SharedInstance.locale;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        final String stripe = "\n================================\n";

        String title = ReceiptParser.getReceiptTitle(context, receiptInfo);

        printer.printText(niceReceipt.issuer +" "+title+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(ReceiptParser.getTradeType(context, receiptInfo), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_for_customer), Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);

        printer.printText(context.getString(R.string.title_membership_number)+"  CATID:"+niceReceipt.catId+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(niceReceipt.membershipNumber+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_validity_period)+ "   "+context.getString(R.string.transaction_date)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_year_month)+ niceReceipt.approvalDate, Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);


        long total =niceReceipt.totalPrice;
        long tax = niceReceipt.tax;
        long remainPoint = niceReceipt.remainPoint;
        String installment = niceReceipt.installmentMonth.equals("00") ? context.getString(R.string.lump_sum)+"\n" : "\n("+niceReceipt.installmentMonth+context.getString(R.string.month_installment)+")\n";
        printer.printText(installment, Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        String won = context.getString(R.string.won);
        for (Product product :
                receiptInfo.cartItems.keySet()) {

            int amount = receiptInfo.cartItems.get(product);

            String name = product.getName();

            if ( name.length() >= 32) {
                name = name.substring(0, 29) + "...";
            }
            printer.printText(name + "\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
            printer.printText(numberFormat.format(product.getPrice()) + won+"*" + amount + " "  + numberFormat.format(product.getPrice() * amount)+won+"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        }
        printer.printText(context.getString(R.string.transaction_amount), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText((total-tax) +won+"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_surtax), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(tax+won+"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_total), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(total+won+"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_balance), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(remainPoint+"", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText("\n"+context.getString(R.string.title_approval_number), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(niceReceipt.approvalNumber.trim(), Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText("\n"+context.getString(R.string.title_transaction_unique_number), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(niceReceipt.tradeUniqueNumber.trim(), Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);

        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);


        printer.printText(context.getString(R.string.title_acquirer_name)+ niceReceipt.acquirerName+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_affiliate_number)+ niceReceipt.affiliateNumber+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_affiliate_name)+ niceReceipt.affiliateName+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_representative)+ niceReceipt.representativeName+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_crn) + niceReceipt.corporateRegistrationNumber+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(niceReceipt.companyAddress, Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);

        String message = String.format(Locale.getDefault(), "%s %s %s %s", niceReceipt.message, niceReceipt.message2,niceReceipt.message3,niceReceipt.message4);
        printer.printText(context.getString(R.string.title_affiliate_tel) + niceReceipt.affiliatePhoneNumber+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(message.trim()+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.thanks)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);

        printer.lineFeed(2);
    }

    private static void printPrepaid(Context context, ReceiptInfo receiptInfo, Printer printer, PrepaidReceipt prepaidReceipt) {

        final Locale locale = SharedInstance.locale;
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        final String stripe = "\n================================\n";

        String title = ReceiptParser.getReceiptTitle(context, receiptInfo);



        printer.printText(ReceiptParser.getTradeType(context, receiptInfo)+ " "+ title+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_for_customer), Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);

        printer.printText(context.getString(R.string.title_membership_number)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(prepaidReceipt.csn+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_issue_date)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(prepaidReceipt.issueDate, Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);
        String won = context.getString(R.string.won);
        for (Product product :
                receiptInfo.cartItems.keySet()) {

            int amount = receiptInfo.cartItems.get(product);

            String name = product.getName();

            if ( name.length() >= 32) {
                name = name.substring(0, 29) + "...";
            }
            printer.printText(name + "\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
            printer.printText(numberFormat.format(product.getPrice()) + won+"*" + amount + " "  + numberFormat.format(product.getPrice() * amount)+won+"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        }
        printer.printText(context.getString(R.string.balance_before_transaction), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);

        printer.printText(prepaidReceipt.preBalance + won +"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.transaction_amount), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(prepaidReceipt.tradeAmount+ won +"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.balance_after_transaction), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(prepaidReceipt.afterBalance+ won +"\n", Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.transaction_amount), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(prepaidReceipt.tradeNumber, Printer.Alignment.RIGHT, false, Printer.TextSize.SIZE_1);

        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);

        printer.printText(context.getString(R.string.title_affiliate_name)+ context.getString(R.string.affiliate_name) +"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_representative)+ context.getString(R.string.company_representative)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.title_crn) + context.getString(R.string.company_registration_number)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.company_address), Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(stripe, Printer.Alignment.CENTER, false, Printer.TextSize.SIZE_1);

        printer.printText(context.getString(R.string.title_affiliate_tel) + context.getString(R.string.affiliate_phone)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);
        printer.printText(context.getString(R.string.thanks)+"\n", Printer.Alignment.LEFT, false, Printer.TextSize.SIZE_1);

        printer.lineFeed(2);
    }


    private static  Observable<byte[]>  printNiceEscpos(Context context, ReceiptInfo receiptInfo, MpaioManager printer, NiceReceipt niceReceipt) {


        Log.i(TAG, "printNiceEscpos");
        final EscposBuilder escpos = new EscposBuilder();



        escpos.addSetLanguage(EscposBuilder.Language.USA)      ;


        final Locale locale = Locale.getDefault();

        String language = locale.getLanguage();
        Log.i(TAG, "lang : " + language);

        if ( "ko".equals(language)) {
            escpos.addSetLanguage(EscposBuilder.Language.KOREA);
            escpos.charset = Charset.forName("euc-kr");
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        final String stripe = "\n================================\n";

        String title = ReceiptParser.getReceiptTitle(context, receiptInfo);


        escpos.addInit()
                .addSetAlign(EscposBuilder.Align.LEFT).addText(niceReceipt.issuer + " " + title + "\n")
                .addText(ReceiptParser.getTradeType(context, receiptInfo))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(context.getString(R.string.title_for_customer))
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe)
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_membership_number)+"  CATID:"+niceReceipt.catId+"\n")
                .addText(niceReceipt.membershipNumber+"\n")
                .addText(context.getString(R.string.title_validity_period)+ "   "+context.getString(R.string.transaction_date)+"\n")
                .addText(context.getString(R.string.title_year_month)+ niceReceipt.approvalDate)
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe);


        long total = niceReceipt.totalPrice;
        long tax = niceReceipt.tax;
        long remainPoint = niceReceipt.remainPoint;
        String installment = niceReceipt.installmentMonth.equals("00") ? context.getString(R.string.lump_sum) + "\n" : "\n(" + niceReceipt.installmentMonth + context.getString(R.string.month_installment) + ")\n";
        escpos.addSetAlign(EscposBuilder.Align.LEFT).addText(installment);
        String won = "$";//context.getString(R.string.won);
        for (Product product :
                receiptInfo.cartItems.keySet()) {

            int amount = receiptInfo.cartItems.get(product);

            String name = product.getName();

            if (name.length() >= 32) {
                name = name.substring(0, 29) + "...";
            }

            escpos.addSetAlign(EscposBuilder.Align.LEFT)
                    .addText(name + "\n")
                    .addSetAlign(EscposBuilder.Align.RIGHT)
                    .addText(numberFormat.format(product.getPrice()) + won + "*" + amount + " " + numberFormat.format(product.getPrice() * amount) + won + "\n");
        }

        escpos
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.transaction_amount)+"\r")
                .addSetAlign(EscposBuilder.Align.RIGHT).addText((total - tax) + won + "\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_surtax))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(tax + won + "\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_total))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(total + won + "\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_balance))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(remainPoint + "")
                .addSetAlign(EscposBuilder.Align.LEFT).addText("\n" + context.getString(R.string.title_approval_number))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(niceReceipt.approvalNumber.trim())
                .addSetAlign(EscposBuilder.Align.LEFT).addText("\n" + context.getString(R.string.title_transaction_unique_number))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(niceReceipt.tradeUniqueNumber.trim())
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe)

                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_acquirer_name) + niceReceipt.acquirerName + "\n")
                .addText(context.getString(R.string.title_affiliate_number) + niceReceipt.affiliateNumber + "\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_affiliate_name) + context.getString(R.string.affiliate_name) + "\n")
                .addText(context.getString(R.string.title_representative) + context.getString(R.string.company_representative) + "\n")
                .addText(context.getString(R.string.title_crn) + context.getString(R.string.company_registration_number) + "\n")
                .addText(context.getString(R.string.company_address))
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe)
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_affiliate_tel) + context.getString(R.string.affiliate_phone) + "\n")
                .addText(context.getString(R.string.thanks) + "\n")
                .addInit()
                .addLineFeed(2);

       /* EscposBuilder escpos2 = new EscposBuilder();
        escpos2.addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.transaction_amount)+"\r")
                .addSetAlign(EscposBuilder.Align.RIGHT).addText((total - tax) + won + "\n");

        Log.i(TAG, Converter.toHexString(escpos2.build()));*/

        Log.i(TAG, "nice print : " + Converter.toHexString(escpos.build()));
        return PaymgateUtil.requestOk(printer, MpaioCommand.RELAY_PRINTING_COMMAND, escpos.build());
    /*    printer.rxSendBytes(escpos.build()).subscribe(new Action1<byte[]>() {
            @Override
            public void call(byte[] bytes) {

                Log.i(TAG, Converter.toHexString(bytes));
            }
        });*/
    }

    private static  Observable<byte[]>  printPrepaidEscpos(Context context, ReceiptInfo receiptInfo, MpaioManager printer, PrepaidReceipt prepaidReceipt) {

        //

        final EscposBuilder escpos = new EscposBuilder();

        final Action1<byte[]> action1 = new Action1<byte[]>() {
            @Override
            public void call(byte[] bytes) {

                Log.i("PRINT", Converter.toHexString(bytes));
            }
        };

        final Locale locale = SharedInstance.locale;

        String language = locale.getLanguage();
        Log.i(TAG, "lang : " + language);

        if ( "ko".equals(language)) {
            escpos.addSetLanguage(EscposBuilder.Language.KOREA);
            escpos.charset = Charset.forName("euc-kr");
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        final String stripe = "\n================================\n";

        String title = ReceiptParser.getReceiptTitle(context, receiptInfo);




        escpos.addSetAlign(EscposBuilder.Align.LEFT).addText(ReceiptParser.getTradeType(context, receiptInfo)+ " "+ title+"\n").addNewLine(1)
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(context.getString(R.string.title_for_customer))
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe)
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_membership_number)+"\n")
                .addText(prepaidReceipt.csn+"\n")
                .addText(context.getString(R.string.title_issue_date)+"\n")
                .addText(prepaidReceipt.issueDate)
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe);


        String won = context.getString(R.string.won);
        for (Product product :
                receiptInfo.cartItems.keySet()) {

            int amount = receiptInfo.cartItems.get(product);

            String name = product.getName();

            if ( name.length() >= 32) {
                name = name.substring(0, 29) + "...";
            }
            escpos.addSetAlign(EscposBuilder.Align.LEFT)
                    .addText(name + "\n")
                    .addSetAlign(EscposBuilder.Align.RIGHT)
                    .addText(numberFormat.format(product.getPrice()) + won+"*" + amount + " "  + numberFormat.format(product.getPrice() * amount)+won+"\n");
        }

        escpos
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.balance_before_transaction))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(prepaidReceipt.preBalance + won +"\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.transaction_amount))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(prepaidReceipt.tradeAmount+ won +"\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.balance_after_transaction))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(prepaidReceipt.afterBalance+ won +"\n")
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.transaction_amount))
                .addSetAlign(EscposBuilder.Align.RIGHT).addText(prepaidReceipt.tradeNumber)
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe)
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_affiliate_name)+ context.getString(R.string.affiliate_name) +"\n")
                .addText(context.getString(R.string.title_representative)+ context.getString(R.string.company_representative)+"\n")
                .addText(context.getString(R.string.title_crn) + context.getString(R.string.company_registration_number)+"\n")
                .addText(context.getString(R.string.company_address))
                .addSetAlign(EscposBuilder.Align.CENTER).addText(stripe)
                .addSetAlign(EscposBuilder.Align.LEFT).addText(context.getString(R.string.title_affiliate_tel) + context.getString(R.string.affiliate_phone)+"\n")
                .addText(context.getString(R.string.thanks)+"\n")
                .addLineFeed(2);

      //  printer.rxSendBytes(escpos.build()).subscribe();

        return PaymgateUtil.requestOk(printer, MpaioCommand.RELAY_PRINTING_COMMAND, escpos.build());
    }





}

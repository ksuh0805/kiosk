package com.samilcts.receipt.nice;

import android.content.Context;
import android.util.Log;

import com.google.common.base.Strings;
import com.samilcts.app.mpaio.demo2.R;
import com.samilcts.app.mpaio.demo2.data.CardInfo;
import com.samilcts.app.mpaio.demo2.data.Product;
import com.samilcts.app.mpaio.demo2.data.ReceiptViewItem;
import com.samilcts.app.mpaio.demo2.util.SharedInstance;
import com.samilcts.receipt.nice.data.NiceReceipt;
import com.samilcts.receipt.nice.data.ReceiptInfo;
import com.samilcts.receipt.nice.data.TmoneyReceipt;
import com.samilcts.app.mpaio.demo2.util.AppTool;
import com.samilcts.app.mpaio.demo2.util.CardName;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by mskim on 2016-10-28.
 * mskim@31cts.com
 */

public class ReceiptParser {

    private static final String TAG = "ReceiptParser";
    private final Context mContext;
    private Logger logger = AppTool.getLogger();


    public ReceiptParser(Context context) {

        mContext = context;
    }

    public ReceiptInfo parse(byte[] data) {

        ReceiptInfo receiptInfo = new ReceiptInfo();
        logger.i(TAG, "Received ReceiptData : " + new String(data, Charset.forName("ksc5601")));

        if ( 434 == data.length ) {

            setNiceMsIcReceipt(data, receiptInfo);

        } else if (45 == data.length) {

            setTmoneyReceipt(data, receiptInfo);

        } /*else if (136 == data.length || 267 == data.length) {

            setCashbeeReceipt(data, receiptInfo);
        }
*/



        return receiptInfo;
    }



    private void setTmoneyReceipt(byte[] data, ReceiptInfo receiptInfo) {

        ByteBuffer buffer = ByteBuffer.wrap(data);
        //buffer.

        TmoneyReceipt tmoneyReceipt = receiptInfo.tmoneyReceipt;

        tmoneyReceipt.catId = AppTool.getString(buffer, 10);
        tmoneyReceipt.responseCode = AppTool.getString(buffer, 4);
        byte[] temp =  AppTool.getBytes(buffer, 1);

        tmoneyReceipt.tradeType = new String(temp);

        if ( (byte)0x90 == temp[0]) {
            receiptInfo.type = ReceiptInfo.TYPE_TMONEY_PAY;
        } else if ( (byte)0x91 == temp[0]) {
            receiptInfo.type = ReceiptInfo.TYPE_TMONEY_CANCEL;
        }  if ( (byte)0xA0 == temp[0]) {
            receiptInfo.type = ReceiptInfo.TYPE_CASHBEE_PAY;
        } else if ( (byte)0xA1 == temp[0]) {
            receiptInfo.type = ReceiptInfo.TYPE_CASHBEE_CANCEL;
        }

        tmoneyReceipt.preBalance = AppTool.getString(buffer, 10);
        tmoneyReceipt.tradeAmount = AppTool.getString(buffer, 10);
        tmoneyReceipt.afterBalance = AppTool.getString(buffer, 10);

      /*  if ( tmoneyReceipt.tradeType.equals("T1")) {
            receiptInfo.type = ReceiptInfo.TYPE_TMONEY_PAY;
        } else if ( tmoneyReceipt.tradeType.equals("T2")) {
            receiptInfo.type = ReceiptInfo.TYPE_TMONEY_PAY;
        }

        tmoneyReceipt.catId = AppTool.getString(buffer, 10);
        tmoneyReceipt.responseCode = AppTool.getString(buffer, 2);
        tmoneyReceipt.approvalNumber = AppTool.getString(buffer, 10);
        tmoneyReceipt.approvalDatetime = AppTool.getString(buffer, 12);
        tmoneyReceipt.approvalDatetime = new StringBuilder(AppTool.getString(buffer, 12))
                .insert(2, "/")
                .insert(5, "/")
                .insert(8, " ")
                .insert(11, ":")
                .insert(14, ":")
                .toString();

        AppTool.getBytes(buffer, 26);
        tmoneyReceipt.cardId = AppTool.getString(buffer, 16);

        AppTool.getBytes(buffer, 12); //not interested

        tmoneyReceipt.preBalance = AppTool.getString(buffer, 10);
        tmoneyReceipt.tradeAmount = AppTool.getString(buffer, 10);
        tmoneyReceipt.afterBalance = AppTool.getString(buffer, 10);*/
    }



    private void setNiceMsIcReceipt(byte[] data, ReceiptInfo receiptInfo) {
        LinkedHashMap<Product, Integer> paidItems;
        paidItems = SharedInstance.getCartItems(); //.clone();

        ArrayList<ReceiptViewItem> itemArrayList = new ArrayList<>();

        double totalCharge = 0;

        for (Product product :
                paidItems.keySet()) {

            int amount =  paidItems.get(product);

            totalCharge +=  product.getPrice() * amount;
            Log.d("ccheck3", String.valueOf(totalCharge));
            Log.d("ccheck3", String.valueOf(product.getName()));
            Log.d("ccheck3", String.valueOf(amount));
            Log.d("ccheck3", String.valueOf(product.getPrice()));

            Thread thread = new Thread(new Runnable() {
                String urlStr = String.format(mContext.getString(R.string.server),
                        product.getName(), amount, product.getPrice(),
                        mContext.getString(R.string.ID), mContext.getString(R.string.PWD));

                @Override
                public void run() {
                    Log.d("ccheck3", urlStr);
                    request(urlStr);
                    Log.d("pserver123", String.valueOf(urlStr));
                }
            });
            thread.start();
        }


        ByteBuffer buffer = ByteBuffer.wrap(data);

        NiceReceipt niceReceipt = receiptInfo.niceReceipt;

        niceReceipt.representativeName = mContext.getString(R.string.company_representative);
        niceReceipt.companyAddress = mContext.getString(R.string.company_address);
        niceReceipt.affiliatePhoneNumber = mContext.getString(R.string.affiliate_phone);
        niceReceipt.affiliateName = mContext.getString(R.string.affiliate_name);


        niceReceipt.catId =  AppTool.getString(buffer, 10);
        logger.i(TAG, "catId : " + niceReceipt.catId);

        niceReceipt.responseCode = AppTool.getString(buffer, 4);
        logger.i(TAG, "responseCode : " + niceReceipt.responseCode);


        niceReceipt.wcc = AppTool.getString(buffer, 1);
        logger.i(TAG, "wcc : " + niceReceipt.wcc);

        byte[] temp =  AppTool.getBytes(buffer, 39);
        niceReceipt.membershipNumber = new String(temp).split("=")[0];

        logger.i(TAG, "membershipNumber : " + niceReceipt.membershipNumber);

        niceReceipt.installmentMonth = AppTool.getString(buffer,2);
        logger.i(TAG, "installmentMonth : " + niceReceipt.installmentMonth);


        niceReceipt.serviceCharge =  getLong(buffer, 12);
        logger.i(TAG, "serviceCharge : " + niceReceipt.serviceCharge);


        niceReceipt.tax =  getLong(buffer, 12);
        logger.i(TAG, "tax : " + niceReceipt.tax);

        niceReceipt.totalPrice =   getLong(buffer, 12);
        logger.i(TAG, "totalPrice : " + niceReceipt.totalPrice);


        niceReceipt.corporateRegistrationNumber = new StringBuilder(AppTool.getString(buffer, 10))
                .insert(3,"-")
                .insert(6,"-").toString();
        logger.i(TAG, "corporateRegistrationNumber : " + niceReceipt.corporateRegistrationNumber);


        AppTool.getBytes(buffer,29);

        niceReceipt.issuerCode = AppTool.getString(buffer, 2);
        logger.i(TAG, "issuer code : " + niceReceipt.issuerCode);
        niceReceipt.issuer = CardName.getCardName(niceReceipt.issuerCode);
        logger.i(TAG, "issuer by code : " + niceReceipt.issuer);

        niceReceipt.issuer = AppTool.getString(buffer, 20).trim();
        logger.i(TAG, "issuer name  : " + niceReceipt.issuer);

        niceReceipt.acquirerCode = AppTool.getString(buffer, 2);
        logger.i(TAG, "acquirerCode code : " + niceReceipt.acquirerCode);

        niceReceipt.acquirerName = CardName.getCardName(niceReceipt.acquirerCode);
        logger.i(TAG, "acquirer by code: " + niceReceipt.acquirerName);

        niceReceipt.acquirerName =AppTool.getString(buffer, 20).trim();
        logger.i(TAG, "acquirer name: " + niceReceipt.acquirerName);

        niceReceipt.affiliateNumber = AppTool.getString(buffer, 15).trim(); //가맹번호
        logger.i(TAG, "affiliateNumber : " + niceReceipt.affiliateNumber);

        // receiptInfo.approvalDate = new String(bytesBuilder.pop(12));

        niceReceipt.approvalDate = new StringBuilder(AppTool.getString(buffer, 12))
                .insert(2, "/")
                .insert(5, "/")
                .insert(8, " ")
                .insert(11, ":")
                .insert(14, ":")
                .toString();

        logger.i(TAG, "approvalDate : " + niceReceipt.approvalDate);

        niceReceipt.approvalNumber = AppTool.getString(buffer, 12);
        logger.i(TAG, "approvalNumber : " + niceReceipt.approvalNumber);

        niceReceipt.tradeUniqueNumber = AppTool.getString(buffer, 12);
        logger.i(TAG, "tradeUniqueNumber : " + niceReceipt.tradeUniqueNumber);

        niceReceipt.ddc = AppTool.getString(buffer, 1);
        logger.i(TAG, "ddc : " + niceReceipt.ddc);

        niceReceipt.message = AppTool.getString(buffer, 40);
        logger.i(TAG, "message1 : " + niceReceipt.message);

        niceReceipt.message2 = AppTool.getString(buffer, 24);
        logger.i(TAG, "message2 : " + niceReceipt.message2);
        niceReceipt.message3 =  AppTool.getString(buffer, 24);
        logger.i(TAG, "message3 : " + niceReceipt.message3);
        niceReceipt.message4 =  AppTool.getString(buffer, 24);
        logger.i(TAG, "message4 : " + niceReceipt.message4);


        niceReceipt.generatedPoint = getLong(buffer, 9);
        logger.i(TAG, "generatedPoint : " + niceReceipt.generatedPoint);

        niceReceipt.availablePoint = getLong(buffer, 9);
        logger.i(TAG, "availablePoint : " + niceReceipt.availablePoint);

        niceReceipt.remainPoint = getLong(buffer, 9);
        logger.i(TAG, "remainPoint : " + niceReceipt.remainPoint);


        niceReceipt.cashbagAffiliate = AppTool.getString(buffer, 15).trim();
        logger.i(TAG, "cashbagAffiliate : " + niceReceipt.cashbagAffiliate);

        niceReceipt.cashbagApprovalNumber = AppTool.getString(buffer, 12).trim();
        logger.i(TAG, "cashbagApprovalNumber : " + niceReceipt.cashbagApprovalNumber);

        niceReceipt.realApprovalCharge = AppTool.getString(buffer, 21).trim();
        logger.i(TAG, "realApprovalCharge : " + niceReceipt.realApprovalCharge);

        niceReceipt.uniqueNumber = AppTool.getString(buffer, 20);
        logger.i(TAG, "uniqueNumber : " + niceReceipt.uniqueNumber);

        if (niceReceipt.issuerCode.equals("70") ) {

             if ( niceReceipt.membershipNumber.trim().length() == 10) {
                niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(0, 3) +"-"
                        + niceReceipt.membershipNumber.substring(3, 5) + "-"
                        + niceReceipt.membershipNumber.substring(5);

            } else if ( niceReceipt.membershipNumber.trim().length() <= 11) {

                niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(0, 3) +"-"
                        + niceReceipt.membershipNumber.substring(3,7) +"-"
                        + niceReceipt.membershipNumber.substring(7);

            } else if ( niceReceipt.membershipNumber.trim().length() == 13) {
                niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(0, 6) +"-"
                        + niceReceipt.membershipNumber.substring(6);

            } else {
                niceReceipt.membershipNumber = niceReceipt.membershipNumber.replaceAll("[0-9*]{4}", "$0-").replaceAll("\\-[0-9*]{0,3}$","@$0").replaceAll("@-","");
            }

        } else {
            niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(2);

            niceReceipt.membershipNumber = niceReceipt.membershipNumber.replaceAll("[0-9*]{4}", "$0-").replaceAll("\\-$","");
        }

        buffer.clear();

        BytesBuilder.clear(temp);
        SharedInstance.clearCartItem();
    }

    public void request(String urlStr) {

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.d("view", String.valueOf(url));

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);       //컨텍션타임아웃 10초
            conn.setReadTimeout(5000);           //컨텐츠조회 타임아웃 5총
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("조회결과 : " + response.toString());

        }catch (Exception ex) {
            Log.d("Error", "예외 발생 : " + ex.toString());
        }
    }


    public void setDemoReceipt(CardInfo cardInfo, ReceiptInfo receiptInfo) {

        NiceReceipt niceReceipt = receiptInfo.niceReceipt;

        DateFormat  format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        niceReceipt.approvalDate = format.format(new Date());
        double total = 0;
        receiptInfo.cartItems = SharedInstance.getCartItems();
        for (Product product :
                receiptInfo.cartItems.keySet()) {

            int amount = receiptInfo.cartItems.get(product);

            total = product.getPrice() * amount;
        }

        niceReceipt.serviceCharge = 0;
        niceReceipt.tax = (long)(total / 11);
        niceReceipt.totalPrice = (long)total;

        niceReceipt.membershipNumber = cardInfo.number;

        if ( niceReceipt.membershipNumber.trim().length() < 10) {

            return;

        } else if ( niceReceipt.membershipNumber.trim().length() == 10) {
            niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(0, 3) +"-"
                    + niceReceipt.membershipNumber.substring(3, 5) + "-"
                    + niceReceipt.membershipNumber.substring(5);

        } else if ( niceReceipt.membershipNumber.trim().length() <= 11) {

            niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(0, 3) +"-"
                    + niceReceipt.membershipNumber.substring(3,7) +"-"
                    + niceReceipt.membershipNumber.substring(7);

        } else if ( niceReceipt.membershipNumber.trim().length() == 13) {
            niceReceipt.membershipNumber = niceReceipt.membershipNumber.replace( niceReceipt.membershipNumber.substring(8,12), "****");
            niceReceipt.membershipNumber = niceReceipt.membershipNumber.substring(0, 6) +"-"
                    + niceReceipt.membershipNumber.substring(6);

        } else {

            niceReceipt.membershipNumber = niceReceipt.membershipNumber.replace( niceReceipt.membershipNumber.substring(8,12), "****");

            niceReceipt.membershipNumber = niceReceipt.membershipNumber.replaceAll("[0-9*]{4}", "$0-").replaceAll("\\-[0-9*]{0,3}$","@$0").replaceAll("@-","");
        }
    }

        private long getLong(ByteBuffer buffer, int length) {

        String value = AppTool.getString(buffer, length).replaceAll("[^0-9]", "");
        value = value.trim().isEmpty() ? "0" : value;
        return Long.parseLong(value);
    }


    public static String getTradeType(Context context, ReceiptInfo receiptInfo) {

        String tradeType = context.getString(R.string.title_credit_approval);
        String wcc = receiptInfo.niceReceipt.wcc;


        if ( receiptInfo.niceReceipt.issuerCode.equals("70")) {
            tradeType = context.getString(R.string.title_cash_receipt);
        }

        switch (wcc) {

            case "@":
                tradeType += context.getString(R.string.title_trade_type_manual);
                break;
            case "A":
                tradeType += "(MS)";
                break;
            case "V":
                tradeType += "(Visa wave)";
                break;
            case "F":
                tradeType += "(IC FallBack)";
                break;
            case "B":
                tradeType += "(후불교통)";
                break;
            case "H":
                tradeType += "(국민후불교통)";
                break;
            case "I":
                tradeType += "(IC)";
                break;
            case "Q":
                tradeType += "(QR)";
                break;
            case "L":
                tradeType += "(Barcode)";
                break;
            case "T":
                tradeType += "(K-MOTION)";
                break;
            case "K":
                tradeType += "(모바일앱키인거래)";
                break;
            case "N":
                tradeType += "(NFCP2P)";
                break;
            default:

                //tradeType ="";
            /*    if (Strings.isNullOrEmpty(receiptInfo.prepaidReceipt.issueDate))
                    tradeType ="("+ context.getString(R.string.name_prepaid_card)+")";
*/
                break;
        }

        return tradeType;
    }

    public static String getReceiptTitle(Context context, ReceiptInfo receiptInfo) {


        String title = "";
        switch (receiptInfo.type) {
            case ReceiptInfo.TYPE_SAMPLE:
                title  = context.getString(R.string.title_receipt_sample);
                break;
            case ReceiptInfo.TYPE_NICE_PAY:
                title  = context.getString(R.string.title_receipt_sales);
                break;
            case ReceiptInfo.TYPE_NICE_CANCEL:
                title  = context.getString(R.string.title_receipt_revoke);
                break;
            case ReceiptInfo.TYPE_PREPAID_PURCHASE:
                title  = context.getString(R.string.title_receipt_pay);
                break;
            case ReceiptInfo.TYPE_PREPAID_RECHARGE:
                title  = context.getString(R.string.title_receipt_charge);
                break;
            case ReceiptInfo.TYPE_PREPAID_REFUND:
                title  = context.getString(R.string.title_receipt_refund);
                break;
            default:
                title  = "??? ";
                break;
        }

        return title;
    }
}

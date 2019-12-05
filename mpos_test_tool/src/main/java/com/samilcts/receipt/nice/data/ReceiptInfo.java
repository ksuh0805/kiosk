package com.samilcts.receipt.nice.data;

import java.io.Serializable;

/**
 * Created by mskim on 2016-06-22.
 * mskim@31cts.com
 */
public class ReceiptInfo implements Serializable {

    public static final int TYPE_SAMPLE = 0;
    public static final int TYPE_NICE_PAY = 1;
    public static final int TYPE_NICE_CANCEL = 2;

    public static final int TYPE_TMONEY_PAY = 3;
    public static final int TYPE_TMONEY_CANCEL = 4;

    public static final int TYPE_CASHBEE_PAY = 5;
    public static final int TYPE_CASHBEE_CANCEL = 6;

    public static final int TYPE_PREPAID_RECHARGE = 100;
    public static final int TYPE_PREPAID_REFUND = 101;
    public static final int TYPE_PREPAID_PURCHASE = 102;


    /* 공용 */
    public int type = 0;

    public CashbeeReceipt cashbeeReceipt = new CashbeeReceipt();
    public TmoneyReceipt tmoneyReceipt = new TmoneyReceipt();
    public NiceReceipt niceReceipt = new NiceReceipt();
    public PrepaidReceipt prepaidReceipt = new PrepaidReceipt();

    //public HashMap<Product, Integer> cartItems = new HashMap<>();

}

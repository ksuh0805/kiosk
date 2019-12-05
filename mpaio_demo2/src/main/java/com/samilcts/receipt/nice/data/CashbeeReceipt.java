package com.samilcts.receipt.nice.data;

import java.io.Serializable;

/**
 * Created by mskim on 2016-06-22.
 * mskim@31cts.com
 */
public class CashbeeReceipt implements Serializable {
    //
    public String catId = "";
    public String responseCode = "";
    public String approvalNumber = "";
    public String approvalDatetime = "";
    public String cardId = "";
    public String tradeDatetime = "";


    //pay cancel only start

    public String preBalance = "";
    public String tradeAmount = "";
    public String afterBalance = "";
    public String tradeType = "";
    public String tradeSerialNumber = "";


}

package com.samilcts.app.mpaio.demo2.data;

import java.io.Serializable;

/**
 * Created by mskim on 2015-09-14.
 * mskim@31cts.com
 */
public class PaymentHistory implements Serializable {


    private final String installment;
    private final String serviceCharge;
    private final String tax;
    private final String total;
    private final String approvalNumber;
    private final String approvalDate;
    private final int index;

    public String getInstallment() {
        return installment;
    }

    public String getServiceCharge() {
        return serviceCharge;
    }

    public String getTax() {
        return tax;
    }

    public String getTotal() {
        return total;
    }

    public String getApprovalNumber() {
        return approvalNumber;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public String getTradeNumber() {
        return tradeNumber;
    }

    private final String tradeNumber;

    public PaymentHistory(String installment, String serviceCharge, String tax, String total, String approvalNumber, String approvalDate, String tradeNumber, int index) {

        this.index = index;
        this.installment = installment;
        this.serviceCharge = serviceCharge;
        this.tax = tax;
        this.total = total;
        this.approvalNumber = approvalNumber;
        this.approvalDate = approvalDate;
        this.tradeNumber = tradeNumber;
    }

}
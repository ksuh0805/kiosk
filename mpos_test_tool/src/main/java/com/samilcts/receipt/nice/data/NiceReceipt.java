package com.samilcts.receipt.nice.data;

import java.io.Serializable;

/**
 * Created by mskim on 2016-06-22.
 * mskim@31cts.com
 */
public class NiceReceipt implements Serializable {

    /* 나이스 */

    public String responseCode = "";
    public String wcc = "";
    public String issuerCode="";
    public String issuer="";
    public String catId="";
    public String membershipNumber=""; //card number with *
    public String installmentMonth="";
    public String tradeUniqueNumber = "";



    public long serviceCharge;
    public long tax;
    public long totalPrice;
    public String approvalNumber="";
    public String approvalDate="";
    public String affiliateNumber="";
    public String acquirerCode ="";
    public String acquirerName ="";
    public String affiliateName="";
    public String representativeName="";
    public String corporateRegistrationNumber="";


    public String companyAddress ="";

    public String affiliatePhoneNumber="";

    public String ddc = "";
    public String message="";
    public String message2="";
    public String message3="";
    public String message4="";

    public long generatedPoint;
    public long availablePoint;
    public long remainPoint;

    public String cashbagAffiliate ="";
    public String cashbagApprovalNumber ="";
    public String realApprovalCharge="";

    public String uniqueNumber="";


}

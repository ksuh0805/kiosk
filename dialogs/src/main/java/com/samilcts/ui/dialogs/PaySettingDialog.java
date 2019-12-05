package com.samilcts.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mskim on 2016-01-07.
 * mskim@31cts.com
 */
public class PaySettingDialog extends Dialog {

    private static final String TAG = "PaySettingDialog";

    private OnClickListener onClickListener;

    @BindView(R2.id.spnInstallment)
    Spinner spnInstallment;

    @BindView(R2.id.spnPayType)
    Spinner spnPayType;

    @BindView(R2.id.spnCashReceiptType)
    Spinner spnCashReceiptType;

    @BindView(R2.id.llCashReceipt)
    LinearLayout llCashReceipt;

    @BindView(R2.id.llInstallment)
    LinearLayout llInstallment;

    @BindString(R2.string.type_pay_card)
    String payCard;
    @BindString(R2.string.type_pay_cash)
    String payCash;

    @BindString(R2.string.cash_type_personal)
    String cashTypePersonal;

    @BindString(R2.string.cash_type_business)
    String cashTypeBusiness;

    public static final int PAY_TYPE_CARD = 1;
    public static final int PAY_TYPE_CASH = 2;

   // private boolean isForRecharge = false;

    public interface OnClickListener {

        void onClick(int type, String installment, DialogInterface dialogInterface);
    }

    public PaySettingDialog(Context context, int themeResId) {
        super(context, themeResId);

    }

    public void setOnClickListener(OnClickListener l) {

        onClickListener = l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_pay_setting);
        ButterKnife.bind(this);


        llCashReceipt.setVisibility(View.GONE);

        spnPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String type = (String)spnPayType.getItemAtPosition(position);

                if ( type.equals(payCard) ) {

                    llCashReceipt.setVisibility(View.GONE);
                    llInstallment.setVisibility(View.VISIBLE);

                } else if ( type.equals(payCash)) {

                    llCashReceipt.setVisibility(View.VISIBLE);
                    llInstallment.setVisibility(View.GONE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick(R2.id.btnOk)
    void clickOk(View v) {


        String installment = (String)spnInstallment.getSelectedItem();
        int payType = PAY_TYPE_CARD;

        if ( llInstallment.getVisibility() == View.GONE)  {
            installment = (String)spnCashReceiptType.getSelectedItem();

            installment = installment.equals(cashTypePersonal) ? "01" : "02";
            payType = PAY_TYPE_CASH;
        }

        onClickListener.onClick(payType ,installment , PaySettingDialog.this);
    }
    @OnClick(R2.id.btnClose)
    void clickClose(){

           cancel();
    }




}

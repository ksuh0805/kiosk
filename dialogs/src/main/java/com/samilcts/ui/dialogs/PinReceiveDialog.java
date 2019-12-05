package com.samilcts.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mskim on 2016-01-07.
 */
public class PinReceiveDialog extends Dialog {

    private static final String TAG = "PinReceiveDialog";

    private OnClickListener onClickListener;

    @BindViews({R2.id.etPin1, R2.id.etPin2, R2.id.etPin3, R2.id.etPin4})
    List<EditText> uiPins;// = new ArrayList<>();
    private int posPin = 0;

    public interface OnClickListener {

        void onClick(String pin, DialogInterface dialogInterface);
    }

    public PinReceiveDialog(Context context, int themeResId) {
        super(context, themeResId);

    }

    public void setOnClickListener(OnClickListener l) {

        onClickListener = l;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_pin_receive);
        ButterKnife.bind(this);

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    @OnClick(R2.id.btnOk)
    void clickOk() {

        onClickListener.onClick(getPinCode(), this);
    }
    @OnClick(R2.id.btnClose)
    void clickClose(){

        dismiss();
    }

    /**
     * add pin code to dialog
     * @param pin pressed code
     */
    public void addPinCode(String pin) {

        if ( posPin < uiPins.size()) {

            uiPins.get(posPin).setText(pin);
            posPin++;

            if ( posPin < uiPins.size())
                uiPins.get(posPin).requestFocus();
        }
    }




    /**
     * delete pin.
     */
    public void backSpacePin() {

        if (posPin > 0 && posPin <= uiPins.size()) {

            posPin--;

            uiPins.get(posPin).setText("");
            uiPins.get(posPin).requestFocus();
        }

    }


    /**
     * get pin code
     * @return pin code
     */
    public String getPinCode() {

        String pinCode = "";

        for (EditText editText : uiPins) {
            pinCode += editText.getText().toString();
        }

        return pinCode;
    }

}

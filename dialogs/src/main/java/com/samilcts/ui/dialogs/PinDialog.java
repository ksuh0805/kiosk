package com.samilcts.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.samilcts.ui.view.SignPad;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mskim on 2016-01-07.
 */
public class PinDialog extends Dialog {

    private static final String TAG = "PinDialog";

    private OnClickListener onClickListener;


    @BindView(R2.id.etPin)
    EditText etPin;

    @BindViews({R2.id.btnKey1, R2.id.btnKey2, R2.id.btnKey3, R2.id.btnKey4, R2.id.btnKey5, R2.id.btnKey6, R2.id.btnKey7, R2.id.btnKey8, R2.id.btnKey9, R2.id.btnKey0})
    Button[] keys;


    public interface OnClickListener {

        void onClick(String pin, DialogInterface dialogInterface);

    }


    public PinDialog(Context context, int themeResId) {
        super(context, themeResId);


    }

    @Override
    public void show() {
        super.show();

        etPin.setText("");

        String numbers[] = {"1","2","3","4","5","6","7","8","9","0"};

        List<String> list = Arrays.asList(numbers);

        Collections.shuffle(list);

        int i = 0;
        for (Button btn :
                keys) {

            btn.setText(list.get(i));
            i++;
        }

    }

    public void setOnClickListener(OnClickListener l) {

        onClickListener = l;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_pin);
        ButterKnife.bind(this);

        //getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

    }

    @OnClick(R2.id.btnOk)
    void clickOk(View v) {

        onClickListener.onClick(etPin.getText().toString(), PinDialog.this);
    }

    @OnClick({R2.id.btnKey1, R2.id.btnKey2, R2.id.btnKey3, R2.id.btnKey4, R2.id.btnKey5, R2.id.btnKey6, R2.id.btnKey7, R2.id.btnKey8, R2.id.btnKey9, R2.id.btnKey0})
    void clickNumber(Button v) {


       etPin.append(v.getText());

    }
    @OnClick(R2.id.btnDel)
    void remove(View v) {

        int len = etPin.getText().length();

        if ( len > 0) {
            etPin.setText(etPin.getText().subSequence(0, len-1));
        }
    }

    @OnClick(R2.id.btnClose)
    void clickClose(){

        cancel();
    }




}

package com.samilcts.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;


import com.samilcts.ui.view.SignPad;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mskim on 2016-01-07.
 */
public class SignDialog extends Dialog {

    private static final String TAG = "SignDialog";

    private OnClickListener onClickListener;

    @BindView(R2.id.signPad)
    SignPad signPad;

    public interface OnClickListener {

        void onClick(Bitmap sign, boolean draw, DialogInterface dialogInterface);

    }


    public SignDialog(Context context, int themeResId) {
        super(context, themeResId);

    }


    public void setOnClickListener(OnClickListener l) {

        onClickListener = l;
    }


    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_sign);
        ButterKnife.bind(this);

    }

    @OnClick(R2.id.btnOk)
    void clickOk(View v) {

        onClickListener.onClick(getBitmapFromView(signPad), signPad.isTouched(),  SignDialog.this);
    }
    @OnClick(R2.id.btnClose)
    void clickClose(){

        cancel();
    }




}

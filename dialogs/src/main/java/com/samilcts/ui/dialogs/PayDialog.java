package com.samilcts.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mskim on 2016-01-07.
 */
public class PayDialog extends Dialog {


    private static final String TAG = "PayDialog";
    private final Context mContext;


    @BindView(R2.id.tvMsg) TextView tvMsg;
    @BindView(R2.id.ivState)ImageView ivState;
    private String mMessage;

    public PayDialog(Context context, int themeResId) {
        super(context, themeResId);

        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_pay);

        ButterKnife.bind(this);

    }


    public void useImage(boolean enable) {

        ivState.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
        tvMsg.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
    }

    public void setImage(Drawable drawable){

        ivState.setImageDrawable(drawable);
    }

    public void setMessage(String message) {

        mMessage = message;
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        tvMsg.setText(mMessage);
        ivState.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();


    }

    @OnClick(R2.id.btnClose)
    void clickClose(){

        cancel();

    }

}

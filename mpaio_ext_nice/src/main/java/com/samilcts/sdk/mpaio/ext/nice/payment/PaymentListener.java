package com.samilcts.sdk.mpaio.ext.nice.payment;

/**
 * Created by mskim on 2016-06-14.
 * mskim@31cts.com
 */
public abstract class PaymentListener {

    public void onPayStarted(){}

    public void onSignatureRequested(){}

    public void onPinRequested(){}

    public void onStateNotification(byte[] data){}

    public abstract void onComplete(byte[] data);

    public void onSignatureDelivered(){}

    public void onPinDelivered(){}

    public void onError(PaymentError error){}
}

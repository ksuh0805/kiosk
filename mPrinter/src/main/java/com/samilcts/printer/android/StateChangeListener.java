package com.samilcts.printer.android;

/**
 * Created by mskim on 2015-12-01.
 */
public interface StateChangeListener {

    public void onConnected();

    public void onConnecting();

    public void onDisconnected(boolean isFailConnect);

}

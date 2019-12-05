package com.samilcts.media;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public interface State {

    int DISCONNECTED = 0;
    int CONNECTING = 1;
    int CONNECTED = 2;

    int getValue();
}

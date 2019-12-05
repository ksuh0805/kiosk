package com.samilcts.media.usb;

import com.samilcts.media.State;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public class UsbState implements State {

    public static final int STATE_ATTACHED = 3;
    public static final int STATE_DETACHED = 4;

    private final int value;

    public UsbState(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

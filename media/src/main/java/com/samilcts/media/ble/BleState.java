package com.samilcts.media.ble;

import com.samilcts.media.State;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public class BleState implements State {

    private final int value;

    public BleState(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

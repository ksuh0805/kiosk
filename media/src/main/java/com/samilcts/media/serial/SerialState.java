package com.samilcts.media.serial;

import com.samilcts.media.State;

/**
 * Created by mskim on 2018-02-08.
 * mskim@31cts.com
 */
public class SerialState implements State {

    private final int value;

    public SerialState(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

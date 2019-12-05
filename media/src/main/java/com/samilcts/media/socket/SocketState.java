package com.samilcts.media.socket;

import com.samilcts.media.State;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public class SocketState implements State {

    private final int value;

    public SocketState(int value) {

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

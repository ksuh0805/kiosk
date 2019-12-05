package com.samilcts.media.exception;

/**
 * Created by mskim on 2016-07-11.
 * mskim@31cts.com
 */
public class BleException extends MediaException {

    public BleException(String detail) {
        super(detail);
    }

    public int status = Integer.MIN_VALUE;
}

package com.samilcts.sdk.mpaio.ext.nice.payment;

/**
 * Created by mskim on 2016-06-22.
 * mskim@31cts.com
 */
public class ReadType {

    public static final int MSR = 0x01;
    public static final int EMV = 0x02;
    public static final int RFID = 0x04;
    public static final int BARCODE = 0x08;
    public static final int PIN = 0x10;
    public static final int ALL = MSR | EMV | RFID | BARCODE | PIN;

    private int type;

    public ReadType(int type) {

        this.type = type;
    }

    public void addType(int type) {

        this.type |= type;
    }

    public void removeType(int type) {

        this.type ^= type;
    }

    public boolean hasType(int type) {

        return (this.type & type) != 0;
    }

    public byte getValue() {

        return (byte)type;
    }
}

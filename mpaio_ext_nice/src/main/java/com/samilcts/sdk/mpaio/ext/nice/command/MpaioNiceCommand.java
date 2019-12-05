package com.samilcts.sdk.mpaio.ext.nice.command;

import com.samilcts.sdk.mpaio.command.MpaioCommand;

/**
 * Created by mskim on 2016-08-04.
 * mskim@31cts.com
 */
public class MpaioNiceCommand extends MpaioCommand {


    // Payment
    public final static int START_PAYMENT = 0x0800;
    public final static int REVOKE_PAYMENT = 0x0801;
    public final static int COMPLETE_SIGNATURE_READING = 0x0802;
    public final static int RELAY_RESPONSE_TELEGRAM = 0x0803;
    public final static int COMPLETE_PIN_READING = 0x0804;

    public final static int INJECT_KEY = 0x0820;
    public final static int SET_PAYMENT_SERVER_INFO = 0x0821;
    public final static int SET_CAT_ID = 0x0822;


    public final static int READ_SIGNATURE = 0x0840;
    public final static int RELAY_REQUEST_TELEGRAM = 0x0841;
    public final static int COMPLETE_PAYMENT = 0x0842;
    public final static int COMPLETE_REVOKE_PAYMENT = 0x0843;
    public final static int ANNOUNCE_PAYMENT_READY = 0x0844;
    public final static int READ_PIN = 0x0845;
    public final static int ANNOUNCE_CHARGE = 0x0846;

    public final static int NOTIFY_PAYMENT_STATE = 0x0880;
    public final static int NOTIFY_CASH_RECEIVED = 0x0881;

    /**
     * create command
     * @param code 0 ~ 65535
     */
    public MpaioNiceCommand(int code) {

        super(code);
    }

    /**
     * create command
     * @param code must 2 byte length
     */
    public MpaioNiceCommand(byte[] code) {

       super(code);
    }

}

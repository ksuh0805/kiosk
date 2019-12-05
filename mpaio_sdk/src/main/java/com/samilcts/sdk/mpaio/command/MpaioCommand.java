package com.samilcts.sdk.mpaio.command;

import com.samilcts.util.android.Converter;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by mskim on 2016-08-04.
 * mskim@31cts.com
 */
public class MpaioCommand implements Command {

    final private byte[] code;

    private final int nCode;

    public final static int STOP = 0x0000;
    public final static int JUMP_TO_BOOT = 0x0001;
    public final static int RESET = 0x0002;
    public final static int CHECK_PAY_AVAILABILITY = 0x0003;

    public final static int GET_DATE_TIME = 0x0100;
    public final static int SET_DATE_TIME = 0x0101;
    public final static int GET_SERIAL_NUMBER = 0x0102;
    public final static int SET_SERIAL_NUMBER = 0x0103;
    public final static int GET_BLUETOOTH_ADDRESS = 0x0104;
    public final static int GET_DEVICE_NAME = 0x0106;
    public final static int GET_MODEL_NAME = 0x0108;
    public final static int GET_HARDWARE_REVISION = 0x010A;
    public final static int SET_HARDWARE_REVISION = 0x010B;
    public final static int GET_FIRMWARE_VERSION = 0x010C;
    public final static int GET_BATTERY_LEVEL = 0x010E;
    public final static int GET_INTEGRITY_CHECK_RESULT = 0x0110;
    public final static int SET_BEEP_VOLUME = 0x0113;
    public final static int GET_BUILD_NUMBER = 0x0114;


    public final static int GET_MANAGING_SERVER_INFO = 0x0120;
    public final static int SET_MANAGING_SERVER_INFO = 0x0121;
    public final static int SET_CRN = 0x0123;
    public final static int GET_NETWORK_INFO = 0x0124;
    public final static int SET_NETWORK_INFO = 0x0125;
    public final static int SET_RUN_MODE = 0x0127;
    public final static int SET_CHARGE_FACTORS = 0x0129;
    public final static int GET_VERIFICATION_DATA = 0x0130;
    public final static int SET_CASH_IN_PULSE_ = 0x0131;
    public final static int GET_UPDATE_SERVER_INFO = 0x0132;
    public final static int SET_UPDATE_SERVER_INFO = 0x0133;
    public final static int GET_MULTI_PACKET_DELAY = 0x0134;
    public final static int SET_MULTI_PACKET_DELAY = 0x0135;

    //barcode
    public final static int READ_BARCODE = 0x0200;
    public final static int OPEN_BARCODE = 0x0201;
    public final static int DISPLAY_QR_CODE = 0x0210;
    public final static int CLEAR_QRCODE = 0x0211;

    public final static int NOTIFY_READ_BARCODE = 0x0280;

    //MSR
    public final static int READ_MS_CARD = 0x0300;
    public final static int NOTIFY_READ_MS_CARD = 0x0380;

    //EMV
    public final static int READ_EMV_CARD = 0x0400;
    public final static int NOTIFY_READ_EMV_CARD = 0x0480;

    //RFID
    public final static int READ_RFID_CARD = 0x0500;
    public final static int NOTIFY_WAIT_RFID_CARD = 0x0580;
    public final static int NOTIFY_READ_RFID_CARD = 0x0581;


    //PIN pad

    public final static int READ_PIN_PAD = 0x0600;
    public final static int NOTIFY_READ_PIN_PAD = 0x0680;


    //MUTUAL_AUTHENTICATION
    public final static int START_MUTUAL_AUTHENTICATION = 0x0700;
    public final static int VERIFY_MUTUAL_AUTHENTICATION = 0x0701;
    public final static int CONFIRM_MUTUAL_AUTHENTICATION = 0x0702;

    //Socket command

    public final static int OPEN_TCP_SOCKET = 0x0940;
    public final static int CLOSE_TCP_SOCKET = 0x0941;
    public final static int WRITE_TCP_SEGMENT = 0x0942;
    public final static int READ_TCP_SEGMENT = 0x0943;



    //Prepaid card command

    public final static int READ_PREPAID_BALANCE = 0x0A00;
    public final static int RECHARGE_PREPAID_CARD = 0x0A01;
    public final static int REFUND_PREPAID_BALANCE = 0x0A02;
    public final static int PURCHASE_BY_PREPAID_CARD = 0x0A03;
    public final static int READ_PREPAID_TRANSACTION_LOG = 0x0A04;
    public final static int INJECT_PREPAID_CARD_KEY = 0x0A20;
    public final static int NOTIFY_PREPAID_TRANSACTION = 0x0A80;
    public final static int NOTIFY_READ_PREPAID_TRANSACTION_LOG = 0x0A81;


    public final static int RELAY_PRINTING_COMMAND = 0x0D00;

    /**
     * create command
     * @param code 0 ~ 65535
     */
    public MpaioCommand(int code) {

        this.code = Converter.toBytes((short)code);
        nCode = code;
    }

    /**
     * create command
     * @param code must 2 byte length
     */
    public MpaioCommand(byte[] code) {

        this.code = code.clone();
        nCode = Converter.toInt(code);
    }

    public byte[] getCode() {

        return code.clone();
    }

    public boolean equals(int code) {
        return code == nCode;
    }

    public boolean equals(Command command) {

        return equals(command.getCode());
    }

    public boolean equals(byte[] code) {

        return Arrays.equals(code, this.code);
    }


    public String getName() {

        Field[] fields = this.getClass().getFields();

        try {

            for (Field filed : fields) {

                int val = filed.getInt(this);

                if ( val == nCode)
                    return filed.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "UNKNOWN";
    }


}

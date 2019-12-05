package com.samilcts.sdk.mpaio.ext.nice.error;

import java.lang.reflect.Field;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public enum NiceResponseError {
    NO_ERROR((byte)0x00)
    ,UNKNOWN_ERROR((byte)0x01)
    ,REQUEST_TIMEOUT_ERROR((byte)0x02)
    ,INVALID_PARAMETER_ERROR((byte)0x03)
    ,NOT_SUPPORTED_ERROR((byte)0x04)
    ,PREPAID_READ_WRITE_ERROR((byte)0xD0)
    ,UNKNOWN_CARD_ERROR((byte)0xD1)
    ,INVALID_PLACE_ID_ERROR((byte)0xD2)
    ,INSUFFICIENT_BALANCE_ERROR((byte)0xD3)
    ,TOO_MANY_BALANCE_ERROR((byte)0xD4)
    ,INVALID_HASH_ERROR((byte)0xE0)
    ,SOCKET_CONNECTION_ERROR((byte)0xF0)
    ,SOCKET_WRITE_ERROR((byte)0xF1)
    ,SOCKET_READ_ERROR((byte)0xF2)
    ,SOCKET_NOT_CONNECTED_ERROR((byte)0xF3);


    private final byte value;

    NiceResponseError(byte code) {

        this.value = code;

    }

    public byte getCode() {

        return value;
    }

    public static NiceResponseError fromCode(byte code) {

        for (NiceResponseError responseError : NiceResponseError.values()) {

            if (responseError.getCode() == code) {

                return responseError;
            }
        }

        return null;
    }


    public String getName() {

        try {

            for (NiceResponseError error : NiceResponseError.values()) {

                if ( error.value == value)
                    return error.name();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "UNKNOWN";
    }

    public boolean equals(byte b) {

        return b == value;
    }
}

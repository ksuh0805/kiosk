package com.samilcts.sdk.mpaio.ext.nice.payment;

/**
 * Created by mskim on 2016-06-29.
 * mskim@31cts.com
 */
public enum PaymentError {

    UNKNOWN_ERROR,
    INVALID_REQUEST_DATA_ERROR,
    INVALID_RESPONSE_DATA_ERROR,
    RESPONSE_TIMEOUT_ERROR,
    RESPONSE_NOT_OK_ERROR,
    RESPONSE_NULL_ERROR,
    CARD_READ_TIMEOUT_ERROR,
    CONVERT_SIGN_ERROR,
    TELEGRAM_PORT_OPEN_ERROR,
    TELEGRAM_SEND_ERROR,
    TELEGRAM_SEND_TIMEOUT_ERROR,
    TELEGRAM_RSA_KEY_RECEIVE_TIMEOUT_ERROR,
    TELEGRAM_ENCRYPTION_ERROR,
    TELEGRAM_HASH_ERROR,
    COMMUNICATION_ERROR,
    RESERVED_1, // FAIL_AUTHENTICATION_ERROR
    RECEIPT_TIMEOUT_ERROR,
    TELEGRAM_TIMEOUT_ERROR,
    RESERVED_2; //USER_CANCEL_ERROR;




  static PaymentError fromOrdinal(int ordinal) {

    for (PaymentError error : PaymentError.values()) {

      if( error.ordinal() == ordinal)
        return error;
    }

    return null;
  }

}

package com.samilcts.mpaio.testtool.util;

import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.util.android.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mskim on 2015-08-20.
 *
 * 커맨드 코드 Enum
 */


public enum TestMenu {

    //control
     STOP(MpaioCommand.STOP)
    ,JUMP_TO_BOOT(MpaioCommand.JUMP_TO_BOOT)
    ,RESET(MpaioCommand.RESET)
    ,CHECK_PAY_AVAILABILITY(MpaioCommand.CHECK_PAY_AVAILABILITY)


    //information
    , GET_DATE_TIME(MpaioCommand.GET_DATE_TIME)
    , SET_DATE_TIME(MpaioCommand.SET_DATE_TIME)
    , GET_SERIAL_NUMBER(MpaioCommand.GET_SERIAL_NUMBER)
    , GET_BLUETOOTH_ADDRESS(MpaioCommand.GET_BLUETOOTH_ADDRESS)
    , GET_DEVICE_NAME(MpaioCommand.GET_DEVICE_NAME)
    , GET_MODEL_NAME(MpaioCommand.GET_MODEL_NAME)

    , GET_HARDWARE_REVISION(MpaioCommand.GET_HARDWARE_REVISION)

    , GET_FIRMWARE_VERSION(MpaioCommand.GET_FIRMWARE_VERSION)
    , GET_BATTERY_LEVEL(MpaioCommand.GET_BATTERY_LEVEL)
    , GET_INTEGRITY_CHECK_RESULT(MpaioCommand.GET_INTEGRITY_CHECK_RESULT)
    , SET_BEEP_VOLUME(MpaioCommand.SET_BEEP_VOLUME)
    , GET_BUILD_NUMBER(MpaioCommand.GET_BUILD_NUMBER)

    , GET_MANAGING_SERVER_INFO(MpaioCommand.GET_MANAGING_SERVER_INFO)
    , GET_NETWORK_INFO(MpaioCommand.GET_NETWORK_INFO)
    , GET_UPDATE_SERVER_INFO(MpaioCommand.GET_UPDATE_SERVER_INFO)

    //barcode
    , READ_BARCODE(MpaioCommand.READ_BARCODE)
    , OPEN_BARCODE(MpaioCommand.OPEN_BARCODE)
 //   , NOTIFY_READ_BARCODE(MpaioCommand.NOTIFY_READ_BARCODE)
    //MSR
    , READ_MS_CARD(MpaioCommand.READ_MS_CARD)
    //,OPEN_MSR((byte)0x03,(byte)0x01)
  //  , NOTIFY_READ_MS_CARD(MpaioCommand.NOTIFY_READ_MS_CARD)
    //EMV
    , READ_EMV_CARD(MpaioCommand.READ_EMV_CARD)
   // , NOTIFY_READ_EMV_CARD(MpaioCommand.NOTIFY_READ_EMV_CARD)

    //RFID
    , READ_RFID_CARD(MpaioCommand.READ_RFID_CARD)
  //  , NOTIFY_WAIT_RFID_CARD(MpaioCommand.NOTIFY_WAIT_RFID_CARD)
  //  , NOTIFY_READ_RFID_CARD(MpaioCommand.NOTIFY_READ_RFID_CARD)

    //PIN pad
    , READ_PIN_PAD(MpaioCommand.READ_PIN_PAD)
    //, NOTIFY_READ_PIN_PAD(MpaioCommand.NOTIFY_READ_PIN_PAD)

   /* //MUTUAL_AUTHENTICATION
    , START_MUTUAL_AUTHENTICATION(MpaioCommand.START_MUTUAL_AUTHENTICATION)
    , VERIFY_MUTUAL_AUTHENTICATION(MpaioCommand.VERIFY_MUTUAL_AUTHENTICATION)
    , CONFIRM_MUTUAL_AUTHENTICATION(MpaioCommand.CONFIRM_MUTUAL_AUTHENTICATION)

    //Payment
    , START_PAYMENT(MpaioNiceCommand.START_PAYMENT)
    , REVOKE_PAYMENT(MpaioNiceCommand.REVOKE_PAYMENT)
    , COMPLETE_SIGNATURE(MpaioNiceCommand.COMPLETE_SIGNATURE_READING)
    , RELAY_RESPONSE_TELEGRAM(MpaioNiceCommand.RELAY_RESPONSE_TELEGRAM)

    , INJECT_KEY(MpaioNiceCommand.INJECT_KEY)
    , SET_PAYMENT_SERVER_INFO (MpaioNiceCommand.SET_PAYMENT_SERVER_INFO)
    , SET_CRN (MpaioNiceCommand.SET_CRN)*/

    , READ_PREPAID_BALANCE(MpaioCommand.READ_PREPAID_BALANCE)
    , RECHARGE_PREPAID_CARD(MpaioCommand.RECHARGE_PREPAID_CARD)
    , REFUND_PREPAID_BALANCE(MpaioCommand.REFUND_PREPAID_BALANCE)
    , PURCHASE_BY_PREPAID_CARD(MpaioCommand.PURCHASE_BY_PREPAID_CARD)
    , READ_PREPAID_TRANSACTION_LOG(MpaioCommand.READ_PREPAID_TRANSACTION_LOG)
    ;

    private final int code;
    //public boolean isAppCommand;

    TestMenu(int code) {

        this.code = code;

    }


    public int getCode() {

        return code;
    }

    /**
     * 커맨드 객체를 가져온다.
     * @param code 커맨드의 코드
     * @return 커맨드
     */

    public static TestMenu fromCode(int code) {

        for (TestMenu command : TestMenu.values()) {

            if (command.getCode() == code) {

                return command;
            }
        }

        return null;
    }


    public static TestMenu fromCode(byte[] code) {

        for (TestMenu command : TestMenu.values()) {

            if (Arrays.equals(Converter.toBytes((short) command.getCode()), code)) {

                return command;
            }
        }

        return null;
    }

    /**
     * type과 high 바이트가 일치하는 모든 커맨드 리스트를 가져온다.
     * @param hCode high code
     * @return 커맨드 리스트
     */

    public static List<TestMenu> getCommandList(int hCode) {

        List<TestMenu> list = new ArrayList<>();

        for (TestMenu command :
                TestMenu.values()) {

            if ( (command.getCode() >>> 8) == hCode)
                list.add(command);
        }

        return list;

    }



}
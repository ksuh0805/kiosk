package com.samilcts.sdk.mpaio.command;

/**
 * Created by mskim on 2015-08-20.
 *
 * command id enumeration. equals with MFi packet's command id
 */
public enum CommandID {

    IN((byte)0x42)
    ,OUT((byte)0x43)
    ;

    private final byte value;

    CommandID(byte value) {


        this.value = value;

    }

    public byte getValue() {

        return value;
    }

    public static CommandID fromValue(int value) {

        for (CommandID commandID : CommandID.values()) {

            if (commandID.value == value) {

                return commandID;
            }
        }

        return null;
    }

    public boolean equals(byte b){

        return value == b;
    }
}

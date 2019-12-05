package com.samilcts.sdk.mpaio.packet;


import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.CommandID;

import java.io.Serializable;

/**
 * Created by mskim on 2015-07-23.
 *
 */
public interface Packet extends Serializable {

    /**
     * convert to byte array
     * @return bytes
     */
    byte[] getBytes();

    /**
     * check checksum of packet
     * @return validation
     */
    boolean validate();


    /**
     * {@link CommandID}
     * @return CommandId
     */
    byte getCommandID();

    /**
     * {@link Command}
     * @return CommandCode
     */

    byte[] getCommandCode();


    /**
     * Get Action ID of packet
     * @return AID
     */
    byte[] getAID();


    /**
     * Get payload of packet
     * @return payload
     */
    byte[] getPayload();


    /**
     * Get checksum of packet
     * @return checksum
     */

    byte getCheckSum();
}

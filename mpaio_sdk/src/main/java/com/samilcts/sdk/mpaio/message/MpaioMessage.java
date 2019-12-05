package com.samilcts.sdk.mpaio.message;

import java.io.Serializable;

/**
 * Created by mskim on 2015-08-26.
 *
 *
 * @author mskim
 *
 */
public interface MpaioMessage extends Serializable{

    /**
     * Get aid
     * @return AID
     */
    byte[] getAID();

    /**
     * Get command code
     * @return Command code
     */
    byte[] getCommandCode();

    /**
     * get data of message
     * @return payload of message
     */
     byte[] getData();

    /**
     * Check if message need data indexing
     * @return true, if needed
     */
    boolean needDataIndexing();

    /**
     * get maximum pure data length of packet.
     * @return maximum pure data length
     */
    int getMaxPacketDataLength();

}

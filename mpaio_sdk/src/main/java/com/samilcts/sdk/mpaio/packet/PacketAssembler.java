package com.samilcts.sdk.mpaio.packet;

/**
 * Created by mskim on 2015-07-23.
 * mskim@31cts.com
 */
public interface PacketAssembler {


    /**
     * add packet to buffer
     * @param bytes bytes
     */
    void add(byte[] bytes);

    /**
     * pop packet from queue. if has not, return null
     * @return packet or null
     */
    Packet pop();


    /**
     * Check has completed packet
     * @return true, if has
     */
    boolean hasPacket();

}

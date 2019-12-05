package com.samilcts.sdk.mpaio.packet;

import com.samilcts.sdk.mpaio.command.CommandID;

/**
 * Created by mskim on 2015-07-27.
 * mskim@31cts.com
 */
public interface PacketFactory {


    /**
     *
     * @param commandID id {@link CommandID}
     * @param AID aid
     * @param commandCode command code
     * @param payload data
     * @return packet
     */
     Packet createPacket(CommandID commandID, byte[] AID, byte[] commandCode, byte[] payload);



    /**
     *
     * @param bytes all packet bytes
     * @return packet
     */
     Packet createPacket(byte[] bytes);
}

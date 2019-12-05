package com.samilcts.sdk.mpaio.packet;

import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.util.android.Converter;

/**
 * Created by mskim on 2015-07-27.
 * mskim@31cts.com
 */
final public class MpaioPacketFactory implements PacketFactory {

    private static short transactionID = 1;

    @Override
    public Packet createPacket(CommandID commandID, byte[] AID, byte[] commandCode, byte[] payload) {

        Packet packet = new MpaioPacket(commandID.getValue(), Converter.toBigEndianBytes(transactionID), AID, commandCode, payload);

        transactionID++;

        return packet;
    }


    @Override
    public Packet createPacket(byte[] bytes) {

        return new MpaioPacket(bytes);
    }
}

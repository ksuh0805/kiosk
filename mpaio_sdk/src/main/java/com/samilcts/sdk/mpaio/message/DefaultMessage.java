package com.samilcts.sdk.mpaio.message;

import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.packet.MpaioPacket;

import java.io.Serializable;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public class DefaultMessage implements MpaioMessage, Serializable {

    private final byte[] AID;
    private final byte[] commandCode;
    private final byte[] data;

    public static final int MAX_PACKET_LENGTH = 255 - (MpaioPacket.LEN_HEADER - MpaioPacket.Start.length - MpaioPacket.LEN_HEADER_FIELD_LENGTH);
    private int maxPacketLength = MAX_PACKET_LENGTH;


    /**
     *
     * @param AID action id
     * @param cmd command code
     * @param data parameter of command
     */

    public DefaultMessage(byte[] AID, byte[] cmd, byte[] data) {

        this.AID = AID;
        this.commandCode = cmd;
        this.data = (data == null ? new byte[0] : data);

    }


    @Override
    public byte[] getAID() {
        return AID;
    }

    @Override
    public byte[] getCommandCode() {
        return commandCode;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public boolean needDataIndexing(){

        Command command = new MpaioCommand(getCommandCode());
        return command.equals(MpaioCommand.READ_TCP_SEGMENT);
    }
    @Override
    public int getMaxPacketDataLength(){

        int max = maxPacketLength;

        if ( needDataIndexing()) max -= 4;

        return max;
    }

    /**
     * Set maximum packet length
     * @param maxLength maximum length ( max : 258, min : 13)
     */
    public void setMaxPacketLength(int maxLength) {


        final int MIN = MpaioPacket.LEN_HEADER + MpaioPacket.LEN_FOOTER;

        if ( maxLength > MAX_PACKET_LENGTH)
            maxLength = MAX_PACKET_LENGTH;
        else if ( maxLength < MIN + 1)
            maxLength = MIN + 1;

        maxPacketLength = maxLength;
    }
}

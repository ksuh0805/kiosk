package com.samilcts.sdk.mpaio.packet;

import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.util.android.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by mskim on 2015-07-23.
 * mskim@31cts.com
 */
final public class MpaioPacket implements Packet {

    public static final int LEN_HEADER = 11;
    public static final int LEN_FOOTER = 1;
    public static final int LEN_HEADER_FIELD_LENGTH = 1;

    public static final byte[] Start = new byte[]{(byte)0xFF, 0x55};
    public static final byte LingoID = 0x00;
    private final Logger logger = LogTool.getLogger();


    private byte[] header;
    private byte[] payload;
    private byte[] footer;


   // private boolean hasIndexedData = false;

    public static final  int IDX_HEADER_LENGTH = 2;
    public static final  int IDX_HEADER_LingoID = 3;
    public static final int IDX_HEADER_COMMAND_ID = 4;
    private static final int IDX_HEADER_AID = 7;
    private static final int IDX_HEADER_COMMAND_CODE = 9;
    private static final int IDX_FOOTER_CHECK_SUM = 0;




    /**
     *
     * @param commandID {@link CommandID}
     * @param transactionID MFI transaction ID
     * @param AID Action id (not 0x00)
     * @param commandCode Command code
     * @param payload payload
     */

    public MpaioPacket(byte commandID, byte[] transactionID, byte[] AID, byte[] commandCode, byte[] payload){

        fillBytes(commandID, transactionID, AID, commandCode, payload);
    }

    private void fillBytes(byte commandID, byte[] transactionID, byte[] AID,  byte[] commandCode, byte[] payload) {

        int payloadSize = payload == null ? 0 : payload.length;

        //set payload
        if (payload == null)
            this.payload = new byte[0];
        else
            this.payload = payload;


        //set header
        ByteBuffer buffer = ByteBuffer.wrap(new byte[LEN_HEADER]);
        buffer.put(Start);
        buffer.put((byte)(LEN_HEADER+payloadSize - Start.length - LEN_HEADER_FIELD_LENGTH));
        buffer.put(LingoID);
        buffer.put(commandID);
        buffer.put(transactionID);
        // buffer.put((byte)(transactionID >> 8 & 0x00FF));
        //buffer.put((byte)(transactionID & 0x00FF));
        buffer.put(AID);
        buffer.put(commandCode);


        header = buffer.array();


        //set footer
        buffer = ByteBuffer.wrap(new byte[LEN_FOOTER]);
        buffer.put(calculateCheckSum());
        footer = buffer.array();
    }


    /**
     * @param bytes all bytes of packet
     * @throws IllegalArgumentException occurred when bytes length < least packet size
     */

    public MpaioPacket(byte[] bytes) throws IllegalArgumentException {

        if ( bytes == null || bytes.length < LEN_HEADER + LEN_FOOTER ) {

            logger.e("DefaultPacker", "invalid packet");
            throw new IllegalArgumentException("bytes must >= " + (LEN_HEADER + LEN_FOOTER));
        }

        header = Arrays.copyOfRange(bytes, 0, LEN_HEADER);
        payload = Arrays.copyOfRange(bytes,LEN_HEADER, bytes.length-LEN_FOOTER);
        footer = Arrays.copyOfRange(bytes,bytes.length-LEN_FOOTER, bytes.length);

    }

    @Override
    public byte getCommandID() {

        return header[IDX_HEADER_COMMAND_ID];
    }

    @Override
    public byte[] getCommandCode() {

        return Arrays.copyOfRange(header, IDX_HEADER_COMMAND_CODE, IDX_HEADER_COMMAND_CODE+2);
    }

    @Override
    public byte[] getAID() {

        return Arrays.copyOfRange(header, IDX_HEADER_AID, IDX_HEADER_AID+2);
    }


    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public byte[] getBytes() {

        ByteBuffer buffer = ByteBuffer.wrap(new byte[header.length+payload.length+footer.length]);
        buffer.put(header);
        buffer.put(payload);
        buffer.put(footer);

        return buffer.array();
    }


    @Override
    public byte getCheckSum() {

        return footer[IDX_FOOTER_CHECK_SUM];
    }

    @Override
    public boolean validate() {

        return getCheckSum() == calculateCheckSum();
    }

    /**
     *  체크섬을 계산하여 반환한다.
     *  @return checksum
     */

    private byte calculateCheckSum(){

        byte checkSum = 0x00;

        for (int i = Start.length; i < header.length; i++) {

            checkSum += header[i];

        }

        for (byte aPayload : payload) {

            checkSum += aPayload;

        }

        checkSum = (byte) (~checkSum + 1);

        return checkSum;

    }

}

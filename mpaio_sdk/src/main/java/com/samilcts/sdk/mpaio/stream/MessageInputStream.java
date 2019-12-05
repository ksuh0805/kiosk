package com.samilcts.sdk.mpaio.stream;

import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.sdk.mpaio.message.MpaioMessage;
import com.samilcts.sdk.mpaio.packet.MpaioPacket;
import com.samilcts.sdk.mpaio.packet.MpaioPacketFactory;
import com.samilcts.sdk.mpaio.packet.Packet;
import com.samilcts.sdk.mpaio.packet.PacketFactory;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by mskim on 2015-09-15.
 *
 * create packet dynamically
 */
public class MessageInputStream implements MpaioInputStream {


    private static final String TAG = "MessageInputStream";


    private BufferedInputStream inputStream;

    private byte[] rawDataReadBuffer;

    private final BytesBuilder packetByteBuffer = new BytesBuilder();
    private final Logger logger = LogTool.getLogger();


    public byte[] getAid() {
        return aid;
    }

    public byte[] getCommandCode() {
        return commandCode;
    }

    private byte[] aid = new byte[0];
    private byte[] commandCode = new byte[0];
    private CommandID commandId;
    private boolean needIndexing = false;


    private int available = 0;

    private final int LEN_PACKET_INDEX = 4;
    private int idxCur = 0;
    private int idxMax = 0;

    private static final int MIN_BUFFER_SIZE = 128;
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final PacketFactory packetFactory = new MpaioPacketFactory();

    /**
     *
     * @param mpaioMessage message
     * @param commandId command id
     */
    public MessageInputStream(MpaioMessage mpaioMessage, CommandID commandId) {

        initialize(mpaioMessage, commandId, DEFAULT_BUFFER_SIZE);
    }

    /**
     *
     * @param mpaioMessage message
     * @param commandId command id
     * @param size min is 128, default is {@link #MIN_BUFFER_SIZE}
     */
    public MessageInputStream(MpaioMessage mpaioMessage, CommandID commandId, int size) {

        initialize(mpaioMessage, commandId, size);
    }


    /**
     * initialize
     * @param mpaioMessage message
     * @param commandId command id
     * @param size min is 128, default is {@link #MIN_BUFFER_SIZE}
     */
    private void initialize(MpaioMessage mpaioMessage, CommandID commandId, int size) {

        if ( size < MIN_BUFFER_SIZE) size = 128;

        aid = mpaioMessage.getAID();
        commandCode = mpaioMessage.getCommandCode();
        needIndexing = mpaioMessage.needDataIndexing();
        this.commandId = commandId;

        rawDataReadBuffer = new byte[mpaioMessage.getMaxPacketDataLength()];

        int dataLen;


        Object data = mpaioMessage.getData();

        if ( data == null)  {

            data = new byte[0];
            dataLen = ((byte[])data).length;

            inputStream =  new BufferedInputStream(new ByteArrayInputStream((byte[])data), size);

        }  else {
            dataLen = ((byte[])data).length;
            inputStream =  new BufferedInputStream(new ByteArrayInputStream((byte[])data), size);
        }


        idxMax = ( (dataLen-1) / mpaioMessage.getMaxPacketDataLength() ) ;

        int packetContainerLen = MpaioPacket.LEN_HEADER + MpaioPacket.LEN_FOOTER  + (mpaioMessage.needDataIndexing() ? LEN_PACKET_INDEX : 0) ;
        available = ((idxMax+1) * packetContainerLen) + dataLen;

    }


    @Override
    synchronized public int read(byte[] buffer) throws IOException {

        if ( buffer == null ) return 0;

        int count=0;




        /* 스트림에서 데이터를 읽고 패킷으로 변환하여 저장한다 */
        while( packetByteBuffer.getSize() < buffer.length  && (count = inputStream.read(rawDataReadBuffer)) > -1)  {

            Packet packet = makePacket(Arrays.copyOf(rawDataReadBuffer, count));
            packetByteBuffer.add(packet.getBytes());

        }

        if ( count < 0 && available > 0 && packetByteBuffer.getSize() <= 0) {
            Packet packet = makePacket(null);
            packetByteBuffer.add(packet.getBytes());
        }


        /*요청 받은 만큼의 데이터를 버퍼에 복사한다.*/


        byte[] readBytes = packetByteBuffer.pop(buffer.length);

        int readCount = readBytes != null ? readBytes.length : -1;

        if ( readCount > 0)
            System.arraycopy(readBytes, 0, buffer, 0, readCount);


        if ( readCount > 0) available -= readCount;

        return readCount;

    }

    /**
     * make packet
     * @param rawData raw data
     * @return packet
     */
    private Packet makePacket(byte[] rawData) {

        byte[] data = null;

        if ( needIndexing ) {
           data = BytesBuilder.merge(Converter.toBytes((short) idxCur++), Converter.toBytes((short) idxMax));
        }

        data = BytesBuilder.merge(data, rawData) ;

        return packetFactory.createPacket(commandId, aid, commandCode, data);

    }

    @Override
    synchronized public void close()  {

        if ( inputStream != null) {

            try {


                inputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                inputStream = null;
            }
        }

    }

    @Override
    synchronized public int available() {
        return available;
    }
}

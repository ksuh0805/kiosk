package com.samilcts.sdk.mpaio.packet;

import android.util.Log;

import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * mpaio packet assembler
 * @author mskim
 *
 */

final public class MpaioPacketAssembler implements PacketAssembler {

    private final BytesBuilder buffer = new BytesBuilder();

    private final Queue<Packet> assembledPacketQueue = new ConcurrentLinkedQueue<>();


    @Override
    synchronized public void add(byte[] bytes) {

        if (bytes == null)
            return;

        buffer.add(bytes);

        while (hasCompletedPacket()) {

            queuePacket();

        }

    }

    /**
     *  queue packet to completion queue
     */

    private void queuePacket() {

        int fieldLength = getLengthParameter(buffer.peek());
        int len = calculatePacketSize(fieldLength);

        try {

            Packet packet = new MpaioPacketFactory().createPacket(buffer.pop(len));

            assembledPacketQueue.add(packet);
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }

    }


    /**
     * Check has completed packet
     * @return true, if has completed packet
     */

    private boolean hasCompletedPacket(){

        if ( buffer.getSize() == 0) return false;

        buffer.add(removeGarbage(buffer.pop()));

        int fieldLength = getLengthParameter(buffer.peek());

        return buffer.getSize() > MpaioPacket.LEN_HEADER && (calculatePacketSize(fieldLength)) <= buffer.getSize();

    }




    @Override
    public boolean hasPacket() {

        return !assembledPacketQueue.isEmpty();
    }

    @Override
    public Packet pop() {

        return assembledPacketQueue.poll();

    }

    /**
     * calculate total packet size
     * @param fieldLength length field value
     * @return size
     */

    private int calculatePacketSize(int fieldLength) {

        return MpaioPacket.Start.length + MpaioPacket.LEN_HEADER_FIELD_LENGTH + fieldLength + MpaioPacket.LEN_FOOTER;
    }

    /**
     *
     * Get packet length parameter if packet total length >= ({@link MpaioPacket#IDX_HEADER_LENGTH}+1)
     * else return 0
     * @param packet packet
     * @return length parameter value
     */
	private int getLengthParameter(byte[] packet) {
		
		if ( packet == null || packet.length < 3) return 0;
		
		return ( packet[MpaioPacket.IDX_HEADER_LENGTH] & 0xFF /* prevent '-' value */);
	}


    /**
     * remove garbage data previous packet header
     * @param bytes bytes
     * @return packet
     */

    private byte[] removeGarbage(byte[] bytes) {

        if ( bytes == null || bytes.length < MpaioPacket.LEN_HEADER)
            return bytes;


        String strBytes = new String(bytes);
        String start = new String(MpaioPacket.Start);

        int headerStart = 0;

        while( (headerStart = strBytes.indexOf(start, headerStart)) >= 0  ) {

            if ( headerStart+ MpaioPacket.IDX_HEADER_COMMAND_ID < bytes.length &&
                    (bytes[headerStart+ MpaioPacket.IDX_HEADER_LENGTH] & 0xFF) >= 0x06 &&
                    bytes[headerStart+ MpaioPacket.IDX_HEADER_LingoID] == MpaioPacket.LingoID &&
                    (bytes[headerStart+ MpaioPacket.IDX_HEADER_COMMAND_ID] == CommandID.OUT.getValue() ||
                            bytes[headerStart+ MpaioPacket.IDX_HEADER_COMMAND_ID] == CommandID.IN.getValue()) ) {

               //find packet start
                break;

            }

            headerStart++;

        }
        bytes = headerStart >= 0 ? Arrays.copyOfRange(bytes, headerStart, bytes.length) :  new byte[0];

        return bytes;

    }
}

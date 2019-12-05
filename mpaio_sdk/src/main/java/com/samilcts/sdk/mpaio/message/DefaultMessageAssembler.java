package com.samilcts.sdk.mpaio.message;

import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.sdk.mpaio.command.MpaioCommand;
import com.samilcts.sdk.mpaio.packet.Packet;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public class DefaultMessageAssembler implements MessageAssembler {

        /**
         * for hashMap
         */

        private class MessageKey {

            private final byte[] AID;
            private final byte[] commandCode;

            public MessageKey(byte[] AID, byte[] commandCode) {

                this.AID = AID;
                this.commandCode = commandCode;
            }

            @Override
            public int hashCode() {

                String text = new String(AID) + new String(commandCode);

                return text.hashCode();
            }

            @Override
            public boolean equals(Object o) {

                return o instanceof MessageKey
                        && o.hashCode() == hashCode();
            }

        }



        private final HashMap<MessageKey, Map<Integer,Packet>>  packetMap = new HashMap<>();


        private final Queue<DefaultMessage> assembledMessageQueue = new ConcurrentLinkedQueue<>();

        /**
         * check packet has data index
         * @param packet packet to check
         * @return true, if packet data has data index
         */
    protected boolean isDataIndexed(Packet packet) {

        byte[] commandCode = packet.getCommandCode();
        byte commandId = packet.getCommandID();

        Command command = new MpaioCommand(commandCode);

        return (command.equals(MpaioCommand.NOTIFY_READ_BARCODE) && CommandID.IN.equals(commandId))
                || (command.equals(MpaioCommand.WRITE_TCP_SEGMENT) && CommandID.IN.equals(commandId))
                || (command.equals(MpaioCommand.READ_TCP_SEGMENT) && CommandID.OUT.equals(commandId))
                || (command.equals(MpaioCommand.RELAY_PRINTING_COMMAND) && CommandID.OUT.equals(commandId))
        ;

    }


    @Override
    public boolean add(Packet packet) {

        if ( packet == null || !packet.validate())
            return false;

        if ( isDataIndexed(packet)) {

            return addMultiPacket(packet);

        } else {

            assembledMessageQueue.add(new DefaultMessage(packet.getAID(), packet.getCommandCode(), packet.getPayload()));
            return true;
        }

    }

    /**
     * add multi packet to multi packet hash map for assemble
     * @param packet multi packet to assemble
     * @return true, if has completed message
     */
    private boolean addMultiPacket(Packet packet) {

        //logger.i("Assemble Message", "addMultiPacket" );

        byte[] payload = packet.getPayload();

        if ( payload == null ) {

            //logger.v("Assemble Message", "payload is null");
            return false;
        }


        if ( payload.length < 4 ) {
            //has no index.

            //logger.v("Assemble Message", "payload length under 4");

            assembledMessageQueue.add(new DefaultMessage(packet.getAID(), packet.getCommandCode(), packet.getPayload()));

            return true;
        }

        // AID 임시로 고정한다.. 펌웨어 발송은 펌웨어의 자체 AID를 쓴다..

        MessageKey key = new MessageKey(new byte[]{0,0}, packet.getCommandCode());
        Map<Integer,Packet> packetList = packetMap.get(key);

        if (packetList == null) {

            packetList = new TreeMap<>();

        }


        packetList.put(getCurrentIndex(payload), packet);
        packetMap.put(key, packetList);


        if (isAllPacketReceived(packetList)) {

            BytesBuilder bytesBuilder = new BytesBuilder();

            final int IDX_DATA_START = 4;

            for (Packet _packet :
                    packetList.values()) {

                byte[] _payload = _packet.getPayload();
                bytesBuilder.add(Arrays.copyOfRange(_payload, IDX_DATA_START, _payload.length));
            }

            assembledMessageQueue.add(new DefaultMessage( packet.getAID(), packet.getCommandCode(), bytesBuilder.pop()));
            packetMap.remove(key);

            return true;
        }

        return false;

    }

    /**
     * check all multi packet is received
     * @param packetList received packet list
     * @return true, if all packet received.
     */
    private boolean isAllPacketReceived(Map<Integer,Packet> packetList) {


        int size =  packetList.size();
        int maxIndex = getMaxIndex(packetList.values().iterator().next().getPayload());

       // logger.i("Assemble Message", "size : " + size + " maxIndex : " + maxIndex );

        int idx = 0;

        if ( size != maxIndex+1)
            return false;


        for (Integer index :
                packetList.keySet()) {

            if ( idx != index)
                break;

            idx++;


        }

        return maxIndex + 1 == idx;
    }



    @Override
    public MpaioMessage pop() {

        return assembledMessageQueue.poll();

    }


    /**
     * get current index of multi packet
     * @param payload packet payload
     * @return current index
     */
    private int getCurrentIndex(byte[] payload) {

        return Converter.toInt(Arrays.copyOf(payload,2));
    }

    /**
     * get last index of multi packet
     * @param payload packet payload
     * @return last index
     */
    private int getMaxIndex(byte[] payload) {

        return Converter.toInt(Arrays.copyOfRange(payload,2,4));

    }


}

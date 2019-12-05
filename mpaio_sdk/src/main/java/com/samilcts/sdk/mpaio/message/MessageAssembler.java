package com.samilcts.sdk.mpaio.message;

import com.samilcts.sdk.mpaio.packet.Packet;

/**
 * Created by mskim on 2015-08-26.
 * message assembler
 * @author mskim
 *
 */
public interface MessageAssembler {


    /**
     * assemble packets to message
     * @param packet packet to assemble
     * @return true, if has completed message
     */
    boolean add(Packet packet);

    /**
     * pop message from queue
     * @return return message if has. or return null
     */
    MpaioMessage pop();

}

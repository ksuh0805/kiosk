package com.samilcts.sdk.mpaio.ext.nice.message;

import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.command.CommandID;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.message.DefaultMessageAssembler;
import com.samilcts.sdk.mpaio.packet.Packet;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public class MpaioNiceMessageAssembler extends DefaultMessageAssembler {

    @Override
    protected boolean isDataIndexed(Packet packet) {

        byte[] commandCode = packet.getCommandCode();
        byte commandId = packet.getCommandID();

        Command command = new MpaioNiceCommand(commandCode);

        if ( super.isDataIndexed(packet) ) {
            return true;
        } else if (
                (command.equals(MpaioNiceCommand.RELAY_REQUEST_TELEGRAM)  && CommandID.IN.equals(commandId))
                || (command.equals(MpaioNiceCommand.COMPLETE_PAYMENT)  && CommandID.IN.equals(commandId))
                || (command.equals(MpaioNiceCommand.COMPLETE_REVOKE_PAYMENT)  && CommandID.IN.equals(commandId))
                || (command.equals(MpaioNiceCommand.COMPLETE_SIGNATURE_READING)  && CommandID.OUT.equals(commandId))
                || (command.equals(MpaioNiceCommand.NOTIFY_READ_BARCODE)  && CommandID.IN.equals(commandId))
                || (command.equals(MpaioNiceCommand.RELAY_RESPONSE_TELEGRAM)  && CommandID.OUT.equals(commandId))

                ) {


            return  true;
        }

        return false;
    }



}

package com.samilcts.sdk.mpaio.ext.nice.message;

import com.samilcts.sdk.mpaio.command.Command;
import com.samilcts.sdk.mpaio.ext.nice.command.MpaioNiceCommand;
import com.samilcts.sdk.mpaio.message.DefaultMessage;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public class MpaioNiceMessage extends DefaultMessage {


    public MpaioNiceMessage(byte[] AID, byte[] cmd, byte[] data) {
        super(AID, cmd, data);
    }

    @Override
    public boolean needDataIndexing() {


        if ( super.needDataIndexing() )
            return true;
        else {

            Command command = new MpaioNiceCommand(getCommandCode());

            return command.equals(MpaioNiceCommand.COMPLETE_SIGNATURE_READING)
                    || command.equals(MpaioNiceCommand.RELAY_RESPONSE_TELEGRAM)
                    || command.equals(MpaioNiceCommand.COMPLETE_PIN_READING)
                    ;
        }


    }

}


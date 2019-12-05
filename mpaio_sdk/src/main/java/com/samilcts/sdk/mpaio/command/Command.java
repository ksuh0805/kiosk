package com.samilcts.sdk.mpaio.command;

/**
 * Created by mskim on 2016-08-04.
 * mskim@31cts.com
 */
public interface Command {

    /**
     * Get value of Command
     * @return command code
     */
    byte[] getCode();

    boolean equals(int code);

    boolean equals(Command command);

    boolean equals(byte[] code);
}

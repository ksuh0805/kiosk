package com.samilcts.sdk.mpaio.stream;

import java.io.IOException;

/**
 * Created by mskim on 2015-09-16.
 * mskim@31cts.com
 */
public interface MpaioInputStream {

    /**
     * Read data to buffer
     * @param buffer buffer
     * @return read length
     * @throws IOException
     */

    int read(byte[] buffer) throws IOException;

    /**
     * close stream
     *
     */
    void close();

    /**
     * get available length
     * @return available length
     */

    int available();


    byte[] getAid();

    byte[] getCommandCode();
}

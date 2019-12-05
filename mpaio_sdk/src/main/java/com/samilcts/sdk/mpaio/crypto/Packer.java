package com.samilcts.sdk.mpaio.crypto;

/**
 * Created by mskim on 2016-06-14.
 * mskim@31cts.com
 */
public interface Packer {

    /**
     * packet data filed
     * @param unpackedData plain data
     * @return packed data
     */
    byte[] pack(byte[] unpackedData);

    /**
     * unpack data field
     * @param packedData packed data
     * @return plain data
     */
    byte[] unpack(byte[] packedData);

}

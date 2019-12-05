package com.samilcts.sdk.mpaio.crypto;

/**
 * Created by mskim on 2016-06-16.
 *
 * do nothing
 */
public class StubPacker implements Packer {

    /**
     * return clone
     * @param unpackedData plain data
     * @return plain data
     */
    @Override
    public byte[] pack(byte[] unpackedData) {
        return unpackedData.clone();
    }

    /**
     * return clone
     * @param packedData packed data
     * @return packed data
     */
    @Override
    public byte[] unpack(byte[] packedData)  {
        return packedData.clone();
    }
}

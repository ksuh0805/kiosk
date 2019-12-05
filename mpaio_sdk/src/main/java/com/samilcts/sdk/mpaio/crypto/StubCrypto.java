package com.samilcts.sdk.mpaio.crypto;

/**
 * Created by mskim on 2016-06-10.
 *
 * this is stub. no nothing
 */
public class StubCrypto implements Crypto {

    @Override
    public byte[] encrypt(byte[] plain) {
        return plain;
    }

    @Override
    public byte[] decrypt(byte[] encrypted) {
        return encrypted;
    }

    @Override
    public void initKey(byte[] key) {

    }
}

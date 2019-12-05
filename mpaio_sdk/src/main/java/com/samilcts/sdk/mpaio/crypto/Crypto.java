package com.samilcts.sdk.mpaio.crypto;

/**
 * Created by mskim on 2016-06-10.
 * mskim@31cts.com
 */
public interface Crypto {

    byte[] encrypt(byte[] plain);
    byte[] decrypt(byte[] encrypted);

    void initKey(byte[] key);
}

package com.samilcts.sdk.mpaio.crypto;

import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by mskim on 2016-06-10.
 * mskim@31cts.com
 */
public class DefaultAES128 implements Crypto {


    private static final String TAG = "DefaultAES128";
    private static final String ALGORITHM = "AES/CBC/NoPadding";
    private SecretKeySpec aesKey;
    private Cipher cipher;
    private IvParameterSpec ivParams;
    private final Logger logger = LogTool.getLogger();

    /**
     * AES/CBC/NoPadding
     * IV 0s
     */

    public byte[] encrypt(byte[] plain) {

        try {
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivParams);

            return cipher.doFinal(plain);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }


    public byte[] decrypt(byte[] encrypted) {

        try {
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivParams);
            return cipher.doFinal(encrypted);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param key 32byte key
     */
    @Override
    public void initKey(byte[] key) {


        
        aesKey = new SecretKeySpec(key, "AES");
        try {
            cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[cipher.getBlockSize()];
            Arrays.fill(iv, (byte)0);
            ivParams = new IvParameterSpec(iv);

            if (LogTool.needKtcLog())
                printLog("[initialize] algorithm : " +ALGORITHM+ ", key [" + Converter.toHexString(key) +"], iv : [" + Converter.toHexString(iv)+"]");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

    }


    private void printLog(String log) {

        int max = log.length();
        int printLength = 2048;

        for ( int i = 0; i <= max; i += printLength )
            logger.d(TAG, log.substring(i, log.length() > i + printLength ? i + printLength : log.length() ));

    }
}

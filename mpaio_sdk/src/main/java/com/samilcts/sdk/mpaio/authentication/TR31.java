package com.samilcts.sdk.mpaio.authentication;

import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.util.android.Logger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by mskim on 2016-06-08.
 *
 * this class is compatible with MPAIO.
 */
public class TR31 {

    private Cipher cipher;
    private final SecretKey secretKey;
    private final String algorithm = "DESede/ECB/NoPadding";
    private final Logger logger = LogTool.getLogger();

    /**
     * clone key block encryption key
     * @return kbek
     */
    public byte[] getKbek() {
        return kbek.clone();
    }

    /**
     * clone kbmk
     * @return kbmk
     */
    public byte[] getKbmk() {
        return kbmk.clone();
    }

    private final byte[] kbek = new byte[16];
    private final byte[] kbmk = new byte[16];


    /**
     *
     * @param kbpk key block protection key
     * @throws InvalidKeyException
     */
    public TR31 ( byte[] kbpk) throws InvalidKeyException {


        byte[] realKey = new byte[24];

        System.arraycopy(kbpk, 0, realKey, 0, 16);
        System.arraycopy(kbpk, 0, realKey, 16, 8);

      //  logger.i(TAG, "kbpk : " + Converter.toHexString(realKey));
        secretKey = new SecretKeySpec(realKey, "DESede");
        clearArray(realKey);


        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

    }


    /**
     * remove all key data from memory.
     */
    public void removeKeys() {

        clearArray(kbek);
        clearArray(kbmk);
        clearArray(cmacK1);
        clearArray(cmacK2);
    }

    private final byte[] cmacK1 = new byte[8];
    private final byte[] cmacK2 = new byte[8];


    private static final String TAG = "TR31";

    /**
     * Key Block Protection Key
     * @return 8 byte key
     */
    public boolean generateKeys() {

        if ( deriveCmacSubkey()) {

            deriveKbek();
            deriveKbmk();
            return true;
        }

        return false;
    }

    /**
     * derive kbmk from kbpk
     */
    private void deriveKbmk() {

        byte[] data = new byte[] {0x01,0x00,0x01,0,0,0,0,(byte) 0x80};

        data = exclusiveOr(data, cmacK1);

        byte[] temp = encrypt(data);

        if ( temp != null) {
            System.arraycopy(temp, 0, kbmk, 0, temp.length);
        }


        clearArray(temp);
        clearArray(data);

        data[0] = 0x02;
        data[2] = 0x01;
        data[7] = (byte)0x80;

        data = exclusiveOr(data, cmacK1);

        temp = encrypt(data);
        clearArray(data);

        if ( temp != null) {
            System.arraycopy(temp, 0, kbmk, 8, temp.length);
        }

        clearArray(temp);
    }

    /**
     * derive kbek from kbpk
     */
    private void deriveKbek() {

        byte[] data = new byte[] {0x01,0,0,0,0,0,0,(byte) 0x80};

        data = exclusiveOr(data, cmacK1);

        byte[] temp = encrypt(data);

        if ( temp != null) {
            System.arraycopy(temp, 0, kbek, 0, temp.length);
        }
        clearArray(temp);
        clearArray(data);
        data[0] = 0x02;
        data[7] = (byte)0x80;

        data = exclusiveOr(data, cmacK1);

        temp = encrypt(data);
        clearArray(data);

        if ( temp != null) {
            System.arraycopy(temp, 0, kbek, 8, temp.length);
        }

        clearArray(temp);
    }

    /**
     * drive sub keys
     * @return success or not
     */
    private boolean deriveCmacSubkey() {

        byte[] cmacS = new byte[8];

        clearArray(cmacS);
        
        cmacS = encrypt(cmacS);

       // logger.i(TAG, "cmacS : " + Converter.toHexString(cmacS));

        if ( cmacS == null) {
            logger.e(TAG, "cmacS Encrypt Fail : ");
            return false;
        }


        //create k1

      //  logger.i(TAG, "cmacS : " + Converter.toHexString(cmacS));
        int carry = (cmacS[0] & 0x80);
        cmacS = shiftLeft1(cmacS);

      // logger.i(TAG, "cmacS : " + Converter.toHexString(cmacS));

        byte[] cmacR64 = new byte[8];
        clearArray(cmacR64);
        cmacR64[7] = 0x1B;


        if (carry != 0) {

            byte[] temp = exclusiveOr(cmacS, cmacR64);
            System.arraycopy(temp, 0, cmacS, 0, temp.length);
            clearArray(temp);
        }

        System.arraycopy(cmacS, 0, cmacK1, 0, cmacS.length);

       // logger.i(TAG, "CMAC K1 : " + Converter.toHexString(cmacK1));


        //create k2

        System.arraycopy(cmacK1, 0, cmacK2, 0, cmacK1.length);

       // logger.i(TAG, "CMAC K2 : " + Converter.toHexString(cmacK2));

        carry = (cmacK2[0] & 0x80);
        System.arraycopy(shiftLeft1(cmacK2), 0, cmacK2, 0, cmacK2.length);

       // logger.i(TAG, "shiftLeft1 CMAC K2 : " + Converter.toHexString(cmacK2));

        if (carry != 0) {

            byte[] temp = exclusiveOr(cmacK2, cmacR64);
            System.arraycopy(temp, 0, cmacK2, 0, temp.length);
            clearArray(temp);
           // logger.i(TAG, "exclusiveOr CMAC K2 : " + Converter.toHexString(cmacK2));
        }

        //logger.i(TAG, "CMAC K2 : " + Converter.toHexString(cmacK2));


        return true;
    }


    /**
     * 1bit shift to left.
     * @param array array to shift
     * @return shifted array
     */
    public static byte[] shiftLeft1( byte[] array )
    {
        boolean carry;
        int i;
        byte temp;

       // logger.i(TAG, "shiftLeft1 before : " + Converter.toHexString(array));

        for( i = 0; i < array.length; i++ )
        {
            temp = array[i];
            temp <<= 1;

           // logger.i(TAG, "shiftLeft1 be : " + Converter.toHexString( array[i]));
            carry = i < array.length-1 && (array[i + 1] & 0x80) > 0;

           // logger.i(TAG, "shiftLeft1 carry : " + carry);

            temp |= carry ? 1 : 0;
           // logger.i(TAG, "shiftLeft1 af : " + Converter.toHexString(temp));
            array[i] = temp;

           // logger.i(TAG, "shiftLeft1 af : " + Converter.toHexString( array[i]));
        }

      //  logger.i(TAG, "shiftLeft1 after : " + Converter.toHexString(array));

        return array;
    }

    /**
     * src1 XOR src2
     * @param src1 source 1
     * @param src2 source 2
     * @param arrays sources.
     * @return result array
     */
    public static byte[] exclusiveOr(byte[] src1, byte[] src2, byte[]... arrays) {



        int length = src1.length < src2.length ? src1.length : src2.length;

        byte[] result = new byte[length];

        for (int i = 0; i < length; i++) {
            result[i] = (byte)((int)src1[i] ^ (int) src2[i]);

            for (byte[] temp :
                    arrays) {

                result[i] ^= temp[i];
            }

        }

        return result;
    }

    /**
     * DESede/ECB/NoPadding encryption
     * @return encrypted data or null when exception occurred
     */
    private byte[] encrypt(byte[] data) {

        try {
            return cipher.doFinal(data);
        } catch ( BadPaddingException  | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * fill FF -> 00
     * @param array array to fill to zero
     */
    public static void clearArray(byte[] array) {

        Arrays.fill(array, (byte)0xFF);
        Arrays.fill(array, (byte)0);
    }

}

package com.samilcts.sdk.mpaio.crypto;

import com.samilcts.sdk.mpaio.log.LogTool;
import com.samilcts.util.android.BytesBuilder;
import com.samilcts.util.android.Converter;
import com.samilcts.util.android.Logger;

import java.security.MessageDigest;
import java.util.Arrays;



/**
 * Created by mskim on 2016-06-14.
 *
 */
public class DefaultPacker implements Packer {


    private static final String TAG = "Default Packet";
    private final Crypto mDataCrypto;
    private final Crypto mHashCrypto;
    private final MessageDigest mHash;
    private final Logger logger = LogTool.getLogger();

    public DefaultPacker(Crypto dataCrypto, Crypto hashCrypto, MessageDigest hash) {

        mDataCrypto = dataCrypto;
        mHashCrypto = hashCrypto;
        mHash = hash;
    }


    @Override
    public byte[] pack(byte[] unpackedData) {

        int tailLen = unpackedData.length % 16;

        byte[] encryptedData;

        if ( tailLen > 0) {

            byte[] padding = new byte[16-tailLen];
            BytesBuilder.clear(padding);
            byte[] padded = BytesBuilder.merge(unpackedData, padding);
            encryptedData = mDataCrypto.encrypt(padded);
            BytesBuilder.clear(padded);

        } else {

            encryptedData = mDataCrypto.encrypt(unpackedData);
        }

        byte[] hash = mHash.digest(encryptedData);

        hash = mHashCrypto.encrypt(hash);

        byte[] packed = new byte[encryptedData.length+hash.length];

        System.arraycopy(encryptedData,0, packed, 0, encryptedData.length);
        System.arraycopy(hash,0, packed, encryptedData.length, hash.length);

        mHash.reset();

        if (LogTool.needKtcLog())
            printLog("[encrypt] plain : [" + Converter.toHexString(unpackedData) +"], encrypted : [" + Converter.toHexString(encryptedData)+"]");

        BytesBuilder.clear(encryptedData);
        BytesBuilder.clear(hash);

        return packed;
    }

    @Override
    public byte[] unpack(byte[] packedData) throws SecurityException {


        int hashLength = mHash.getDigestLength();

        byte[] encryptedData = Arrays.copyOf(packedData, packedData.length-hashLength);

        byte[] decryptedData = mDataCrypto.decrypt(encryptedData);

        byte[] encryptedHash = Arrays.copyOfRange(packedData, packedData.length- hashLength, packedData.length);

        byte[] hash = mHashCrypto.decrypt(encryptedHash);


        mHash.reset();

       byte[] calcHash =  mHash.digest(encryptedData);

        byte[] temp = removeZeroPadding(decryptedData);

        BytesBuilder.clear(decryptedData);

        decryptedData = temp;

        if (LogTool.needKtcLog())
            printLog("[decrypt] encrypted : [" + Converter.toHexString(encryptedData) +"], plain : [" + Converter.toHexString(decryptedData)+"]");

        if ( Arrays.equals(hash, calcHash))
                return decryptedData;
        else
            throw new SecurityException("Invalid hash");
    }

    /**
     * trim zero bytes of tail.
     * @param unpacked data to remove zero padding
     * @return bytes
     */
    private byte[] removeZeroPadding(byte[] unpacked) {

        int i = unpacked.length-1;
        while (  i >= 0 && unpacked[i] == 0x00) {
            i--;
        }

        if ( i < unpacked.length && i >= 0 ) {
            unpacked = Arrays.copyOf(unpacked, i+1);
        }
        return unpacked;
    }

    private void printLog(String log) {

        int max = log.length();
        int printLength = 2048;

        for ( int i = 0; i <= max; i += printLength )
            logger.d(TAG, log.substring(i, log.length() > i + printLength ? i + printLength : log.length() ));

    }
}

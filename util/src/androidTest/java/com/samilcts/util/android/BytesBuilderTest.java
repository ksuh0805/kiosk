package com.samilcts.util.android;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public class BytesBuilderTest extends TestCase {


    public void testAddAll() {


        byte[] bytes1 = new byte[]{0x05,0x01,(byte)0xFF,0x25,0x25,(byte)0xaa};


        byte[] bytes2 = new byte[]{0x52,(byte)0xcf,(byte)0x2F,(byte)0xd5,0x25,(byte)0xaa};
        byte[] bytes3 = new byte[]{0x08,(byte)0xca,(byte)0x3F};

        byte[] bytes4 = new byte[]{0x05,0x01,(byte)0xFF,0x25,0x25,(byte)0xaa, 0x52,(byte)0xcf,(byte)0x2F,(byte)0xd5,0x25,(byte)0xaa, 0x08,(byte)0xca,(byte)0x3F};



        assertTrue(Arrays.equals(bytes4, BytesBuilder.merge(bytes1, bytes2, bytes3)));

        byte[] bytes5 = new byte[]{0x05,0x01,(byte)0xFF,0x25,0x25,(byte)0xaa, 0x52,(byte)0xcf,(byte)0x2F,(byte)0xd5,0x25,(byte)0xaa};


        assertTrue(Arrays.equals(bytes5, BytesBuilder.merge(bytes1, bytes2)));


        assertTrue(Arrays.equals(bytes1, BytesBuilder.merge(bytes1, null)));
        assertTrue(Arrays.equals(bytes2, BytesBuilder.merge(bytes2, null)));

        assertTrue(Arrays.equals(bytes2, BytesBuilder.merge(null, bytes2)));

        assertTrue(Arrays.equals(bytes4,  BytesBuilder.merge(null, null, bytes1, bytes2, bytes3)));


    }


}

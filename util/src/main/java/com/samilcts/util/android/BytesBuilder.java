package com.samilcts.util.android;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by mskim on 2015-08-24.
 * mskim@31cts.com
 */
public class BytesBuilder {

    private byte[] buffer;


    /**
     * save byte to internal buffer
     * @param _byte cast to byte
     * @return BytesBuilder
     */
    public BytesBuilder add(int _byte) {

        return add(new byte[]{(byte)_byte});
    }


    /**
     * save byte to internal buffer
     * @param _byte byte
     * @return BytesBuilder
     */
    public BytesBuilder add(byte _byte) {

       return add(new byte[]{_byte});
    }

    /**
     * save bytes to internal buffer
     * @param bytes bytes
     * @return BytesBuilder
     */
    public BytesBuilder add(byte[] bytes) {

        byte[] temp = merge(buffer, bytes);
        clear(buffer);
        buffer = temp;
        return this;
    }

    /**
     * return all bytes and clear from buffer.
     * @return all saved bytes
     */
    public byte[] pop() {

        byte[] temp = buffer;
        buffer = null;

        return temp;
    }

    /**
     * return all bytes. don't clear from buffer
     * @return all saved bytes
     */

    public byte[] peek() {

        return buffer;
    }

    /**
     * return bytes of length form the header.
     * if length is shorter then saved. return all bytes
     *
     * @param length length
     * @return bytes
     */
    public byte[] pop(int length) {

        if ( buffer == null) return null;
        else if ( length >= buffer.length) return pop();

        byte[] temp = Arrays.copyOf(buffer, length);

        buffer= Arrays.copyOfRange(buffer, length, buffer.length);

        return temp;
    }


    /**
     *
     * @return saved size
     */
    public int getSize() {

        return buffer != null ? buffer.length : 0;
    }

    /**
     * add all bytes
     * @param bytes1 first bytes
     * @param bytes2 second bytes
     * @param byteArrays other bytes
     * @return new array of merged all bytes.
     */

    public static byte[] merge(byte[] bytes1, byte[] bytes2, byte[]... byteArrays) {

        int length = (bytes1 != null ? bytes1.length : 0) +  (bytes2 != null ? bytes2.length : 0);

        for ( byte[] byteArray : byteArrays)
            length += byteArray != null ? byteArray.length : 0;

        ByteBuffer buffer = ByteBuffer.wrap(new byte[length]);

        if ( bytes1 != null)  {
            buffer.put(bytes1);

        }
        if ( bytes2 != null) {
            buffer.put(bytes2);
        }

        for ( byte[] byteArray : byteArrays) {
            buffer.put(byteArray);
        }

        return buffer.array();

    }


    /**
     * clear data to zero
     * @param buffer buffer to clear
     */
    public static void clear(byte[] buffer) {

        if ( null == buffer ) return;

        Arrays.fill(buffer, (byte)0);
        Arrays.fill(buffer, (byte)0xFF);
        Arrays.fill(buffer, (byte)0x00);

    }

}

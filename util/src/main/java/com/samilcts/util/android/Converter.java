package com.samilcts.util.android;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by mskim on 2015-07-24.
 * mskim@31cts.com
 */
public class Converter {


    /**
     * if bytes is null, return ""
     * @param bytes bytes
     * @return hex string. ex)FF 55 4F ... 23
     */

    public static String toHexString(byte[] bytes) {

        if (bytes == null) return "";

        StringBuilder builder = new StringBuilder();

        for (byte b : bytes) {
            builder.append(String.format("%02X ", b));
        }

        if ( builder.length() > 2)
            builder.deleteCharAt(builder.length()-1);

        return builder.toString();
    }

    public static String toHexString(byte b) {

        return String.format("%02X", b);

    }

    /**
     * short to Big Endian bytes
     * @param val short o convert
     * @return byte[2]
     */
    public static byte[] toBigEndianBytes(short val) {

        return new byte[]{(byte)((val >> 8)), (byte)(val & 0xFF)};
    }

    /**
     * int to Big Endian bytes
     * @param val int to convert
     * @return byte[4]
     */
    public static byte[] toBigEndianBytes(int val) {

        return new byte[]{(byte)((val >> 24)),
                (byte)((val >> 16)),
                (byte)((val >> 8)),
                (byte)(val & 0xFF)};
    }

    /**
     * convert val to 4 bytes length array
     * @param val value to convert
     * @return converted bytes
     */
    public static byte[] toBytes(int val) {

        return new byte[]{ (byte)(val & 0xFF), (byte)((val >> 8)),
                (byte)((val >> 16)), (byte)((val >> 24))};
    }

    /**
     *  convert val to 2 bytes length array
     * @param val  value to convert
     * @return converted bytes
     */
    public static byte[] toBytes(short val) {

        return new byte[]{(byte)(val & 0xFF), (byte)((val >> 8))};
    }

    /**
     * Little endian bytes to int
     *
     * @param bytes bytes to convert int
     * @return int
     */
    public static int toInt(byte[] bytes) {


        if (bytes == null)
            return 0;

        int result = 0;

        for (int i = 0, shift = 0; i < bytes.length; i++, shift+=8) {

            result += (bytes[i] & 0xFF ) << shift ;
        }

        return result;

    }

    /**
     * Big endian bytes to int
     *
     * @param bytes bytes to convert
     * @return int
     */
    public static int toBigEndianInt(byte[] bytes) {


        if (bytes == null)
            return 0;

        int result = 0;

        for (int i = 0, shift = (bytes.length-1) * 8; i < bytes.length; i++, shift-=8) {

            result += (bytes[i] & 0xFF ) << shift ;
        }

        return result;

    }

    /**
     *
     * @param data data to split
     * @param delimiter delimiter
     * @return string array
     */
    public static ArrayList<byte[]> split(byte[] data, byte delimiter) {

        ArrayList<byte[]> bytes = new ArrayList<>();


        int start = 0;

        int i;
        for( i = 0; i < data.length; i++){


            if ( data[i] == delimiter ) {

                bytes.add(Arrays.copyOfRange(data, start, i));
                start = i+1;
            }

        }

        if ( start < i)
            bytes.add(Arrays.copyOfRange(data, start, i));


        return bytes;
    }

    /**
     * hex string to byte array
     * @param hex hex string (ex FFAB)
     * @return bytes array (ex {0xFF, 0xAB})
     */
    public static byte[] toByteArray(String hex) {
        if (hex == null || hex.length() == 0) {
            return new byte[0];
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }
}

package kr.co.nicevan.signenc;

public class SignEnc {

    static {
        System.loadLibrary("SignEnc");
    } 
 
    public native int GetEncData(byte[] input, byte[] output);
    public native int MakePinBlock(byte[] Track, byte[] ppPassword, byte[] EPinBlock16);
}

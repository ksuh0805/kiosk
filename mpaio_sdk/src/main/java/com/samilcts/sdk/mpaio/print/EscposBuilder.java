package com.samilcts.sdk.mpaio.print;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.samilcts.util.android.BytesBuilder;

import java.nio.charset.Charset;

/**
 * Created by Minsu Kim on 2017-12-20.
 */

public class EscposBuilder {


    public enum Align {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum Language {
        USA,
        FRANCE,
        GERMANY,
        UK,
        DENMARK_I,
        SWEDEN,
        ITALY,
        SPAIN_I,
        JAPAN,
        NORWAY,
        DENMARK_II,
        SPAIN_II,
        LATIN_AMERICA,
        KOREA

    }



    public Charset charset = Charset.defaultCharset();

    private BytesBuilder builder = new BytesBuilder();

    public EscposBuilder addText(String text) {
        builder.add(text.getBytes(charset));
        return this;
    }

    public EscposBuilder addInit() {

        builder.add(new byte[]{0x1B, 0x40});

        return this;
    }
    public EscposBuilder addSetLanguage(Language language) {

        builder.add(new byte[]{0x1B, 0x51, (byte)language.ordinal()});

        return this;
    }

    public EscposBuilder addNewLine(int n) {

        for (int i = 0 ; i < n; i++)
            builder.add("\n".getBytes());

        return this;
    }

    public EscposBuilder addLineFeed(int n) {

        if ( n < 0) n = 0;
        else if (n > 255) n = 255;

        builder.add(new byte[]{0x1B, 0x64, (byte)n});
        return this;
    }

    public EscposBuilder addSetAlign(int align) {

        if ( align < 0) align = 0;
        else if (align > 2) align = 2;
        builder.add(new byte[]{0x1B, 0x61, (byte)align});
        return this;
    }

    public EscposBuilder addSetAlign(Align align) {

        return addSetAlign(align.ordinal());
    }

    /// <summary>
    /// Prints the image. The image must be 384px wide.
    /// </summary>
    /// <param name='image'>
    /// Image to print.
    /// </param>
    public EscposBuilder addImage(Bitmap image) throws Exception
    {
        int width = image.getWidth();
        int height = image.getHeight();

        byte[][] imgArray = new byte[width][height];

        if (width != 384 || height > 65635) {
            throw new Exception("Image width must be 384px, height cannot exceed 65635px.");
        }

        int B=0,G=0,R=0;

        //Print LSB first bitmap
        builder.add(0x12);
        builder.add(0x76);

        builder.add((height & 0xFF)); 	//height LSB
        builder.add((height >> 8)); 	//height MSB

        //Processing image data
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < (width/8); x++) {

                imgArray[x][y] = 0;
                for (byte n = 0; n < 8; n++) {
                    int pixel = image.getPixel(x*8+n,y);

                    // retrieve color of all channels
                    R = Color.red(pixel);
                    G = Color.green(pixel);
                    B = Color.blue(pixel);
                    // take conversion up to one single value by calculating pixel intensity.
                    R = G = B = (int)(0.299 * R + 0.587 * G + 0.114 * B);

                    if (R  < 200) {
                        imgArray[x][y] += (byte)(1 << n);
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < (width/8); x++) {
                builder.add(imgArray[x][y]);
            }
        }

        return this;
    }

    /**
     * build and clear
     * @return
     */
    public byte[] build() {
        return builder.pop();
    }

    public byte[] build(boolean withClear) {
        return withClear ? builder.pop() : builder.peek();
    }

    public void clear(){
        builder.pop();
    }

}

package com.bitmap.monochrome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;

/**
 * https://github.com/acdevs/1bpp-monochrome-android
 *
 * modify...
 */
public class BitmapConverter {

	private int mDataWidth;
	private byte mRawBitmapData[];
	private byte[] mDataArray;
	private static final String TAG = "BitmapConverter";
	//private ProgressDialog mPd;
	private final Context mContext;
	private int mWidth, mHeight;
	private String mStatus;
	private String mFileName;
	
	
	public BitmapConverter(Context context) {

		mContext = context;
	}

/**
 * Converts the input image to 1bpp-monochrome bitmap
 * @param inputBitmap : Bitmap to be converted
 * @return :  monochome bitmap bytes
 */
    public byte[] getMonochromeBitmap(Bitmap inputBitmap) throws Exception {
		
    	mWidth = inputBitmap.getWidth();
    	mHeight = inputBitmap.getHeight();

    	mDataWidth=((mWidth+31)/32)*4*8;
    	mDataArray = new byte[(mDataWidth * mHeight)];
    	mRawBitmapData = new byte[(mDataWidth * mHeight) / 8];

		convertArgbToGrayscale(inputBitmap, mWidth, mHeight);
		inputBitmap.recycle();
		createRawMonochromeData();

		BMPFile bmpFile = new BMPFile();

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		bmpFile.saveBitmap(out, mRawBitmapData, mWidth, mHeight);



		return out.toByteArray();
    }

	

	private void convertArgbToGrayscale(Bitmap bmpOriginal, int width, int height) throws Exception {
    	int pixel;
    	int k = 0;
    	int B=0,G=0,R=0;

    	for(int x = 0; x < height; x++) {
            for(int y = 0; y < width; y++, k++) {
                // get one pixel color
                pixel = bmpOriginal.getPixel(y, x);
                
                // retrieve color of all channels
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value by calculating pixel intensity.
                R = G = B = (int)(0.299 * R + 0.587 * G + 0.114 * B);
                // set new pixel color to output bitmap
                if (R < 128) {
					mDataArray[k] = 0;
				} else {
					mDataArray[k] = 1;
				}
            }
            if(mDataWidth>width){
				for(int p=width;p<mDataWidth;p++,k++){
					mDataArray[k]=1;
				}
			}
        }

    }
    
    private void createRawMonochromeData(){
    	int length = 0;
    	for (int i = 0; i < mDataArray.length; i = i + 8) {
			byte first = mDataArray[i];
			for (int j = 0; j < 7; j++) {
				byte second = (byte) ((first << 1) | mDataArray[i + j]);
				first = second;
			}
			mRawBitmapData[length] = first;
			length++;
		}
    }

  /*  private String saveImage(String fileName, int width, int height) throws IOException {
    	FileOutputStream fileOutputStream;
    	BMPFile bmpFile = new BMPFile();
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName + ".bmp");

        file.createNewFile();
        fileOutputStream = new FileOutputStream(file);
		bmpFile.saveBitmap(fileOutputStream, mRawBitmapData, width, height);

		return "Success";
    }*/

}

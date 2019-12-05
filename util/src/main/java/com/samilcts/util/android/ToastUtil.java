package com.samilcts.util.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Android Toast wrapper class
 * if toast messages ara equal, do not create new message and just show again.
 * @author mskim
 *
 */

public class ToastUtil {

	private static Toast toast;
	private static String prevText = "";



	private final static Handler handler = new Handler(Looper.getMainLooper());

	public static void show(Context context, int resource) {

		show(context, resource, Toast.LENGTH_SHORT);
	}


	public static void show(Context context, int resource, int length ){
		
		
		String text = context.getString(resource);

		show(context, text, length);
		
	}


	public static void show(Context context, String text) {

		show(context, text, Toast.LENGTH_SHORT);
	}

	public static void show(final Context context, final String text, final int length ){

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (toast != null && prevText.equals(text)) {

                    toast.setText(text);

                } else {

                    toast = Toast.makeText(context, text, length);
                    prevText = text;
                }


                toast.show();
            }
        });


	}
	
}

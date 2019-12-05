package com.samilcts.util.android;


/**
 * android Log wrapper
 */

public class Logger {

    public static int VERBOSE = 0;
    public static int DEBUG = 1;
    public static int INFO = 2;
    public static int WARN = 3;
    public static int ERROR = 4;
    public static int NONE = 5;

    private int logLevel = NONE;

    public void setLevel(int level) {
        logLevel = level;
    }

    public int getLevel() {
        return logLevel;
    }

    public int v(String tag, String msg) {

        if (logLevel <= VERBOSE) {
            return android.util.Log.v(tag, msg);
        }
        return 0;
    }

    public int d(String tag, String msg) {
        if (logLevel <= DEBUG) {
            return android.util.Log.d(tag, msg);
        }
        return 0;
    }

    public int i(String tag, String msg) {

        if (logLevel <= INFO) {
            return android.util.Log.i(tag, msg);
        }
        return 0;
    }

    public int w(String tag, String msg) {
        if (logLevel <= WARN) {
            return android.util.Log.w(tag, msg);
        }
        return 0;
    }

    public int e(String tag, String msg) {
        if (logLevel <= ERROR) {
            return android.util.Log.e(tag, msg);
        }
        return 0;
    }


}

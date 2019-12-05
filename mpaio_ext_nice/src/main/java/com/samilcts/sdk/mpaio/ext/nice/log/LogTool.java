package com.samilcts.sdk.mpaio.ext.nice.log;

import com.samilcts.util.android.Logger;

/**
 * Created by mskim on 2016-08-19.
 * mskim@31cts.com
 */
final public class LogTool {

    private final static Logger logger;

    public static Logger getLogger(){
        return logger;
    }

    static {

        logger = new Logger();
    }

    private final static boolean LOG_KTC = true;

    public static boolean needKtcLog(){
        return LOG_KTC;
    }
}

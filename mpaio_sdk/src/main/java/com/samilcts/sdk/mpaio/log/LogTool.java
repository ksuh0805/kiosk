package com.samilcts.sdk.mpaio.log;

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
        logger.setLevel(Logger.VERBOSE);

    }

    private final static boolean LOG_KTC = false;
    public static boolean needKtcLog(){
        return LOG_KTC;
    }


}

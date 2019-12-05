package com.samilcts.media;

import com.samilcts.util.android.Logger;

/**
 * Created by mskim on 2016-09-02.
 * mskim@31cts.com
 */
public class LogTool {
    private final static Logger logger;

    public static Logger getLogger(){
        return logger;
    }

   static {
       logger = new Logger();
   }
}

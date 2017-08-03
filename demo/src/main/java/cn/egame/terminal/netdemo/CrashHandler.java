package cn.egame.terminal.netdemo;


/*
 * FileName:    CrashHandler.java
 * Copyright:   炫彩互动网络科技有限公司
 * Author:      weilai
 * Description: <文件描述>
 * History:     2/27/17 1.00 初始版本
 */


import android.content.Context;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler sCrashHandler = new CrashHandler();

    private Context mContext;

    public static CrashHandler getInstance() {
        return sCrashHandler;
    }


    public void init(Context context) {
        mContext = context;
//        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(sCrashHandler);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }
}

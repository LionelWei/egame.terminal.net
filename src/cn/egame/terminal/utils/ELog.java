/*
 * FileName:	Logger.java
 * Copyright:	炫彩互动网络科技有限公司
 * Author: 		Hein
 * Description:	日志输出类
 * History:		2013-10-21 1.00 初始版本
 */
package cn.egame.terminal.utils;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Printer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 日志输出类
 * 如果注册Handler可将所有日志内容抛出
 *
 * @author Hein
 */
public class ELog {

    private static final String TAG = "LAZY";
    public static boolean LOG_ON = false;

    static {
        com.orhanobut.logger.Logger.init();
        LOG_ON = getLocalPrinter().getSettings().getLogLevel() == LogLevel.FULL;
    }

    private static Handler sHandler = null;
    private static int OUT_SIGNAL = -1;


    public static Printer getLocalPrinter() {
        return com.orhanobut.logger.Logger.t(TAG, 0);
    }

    /**
     * 注册接收日志的handler
     *
     * @param handler
     * @param outSignal Message的what
     */
    public static void register(Handler handler, int outSignal) {
        sHandler = handler;
        OUT_SIGNAL = outSignal;
    }

    /**
     * 反注册
     *
     * @param handler
     */
    public static void unRegister(Handler handler) {
        if (handler == sHandler) {
            sHandler = null;
            OUT_SIGNAL = -1;
        }
    }

    private static void logOut(int level, String tag, String msg) {
        if (sHandler == null) {
            return;
        }

        int color = Color.WHITE;
        String levelFlog = null;

        switch (level) {
            case Log.VERBOSE:
                color = Color.GRAY;
                levelFlog = "V";
                break;
            case Log.DEBUG:
//            color = 0xFF7FFFFF;
                color = Color.MAGENTA;
                levelFlog = "D";
                break;
            case Log.INFO:
                color = 0xFF007F00;
                levelFlog = "I";
                break;
            case Log.WARN:
                color = 0xFFFF7F00;
                levelFlog = "W";
                break;
            case Log.ERROR:
                color = 0xFFFF0000;
                levelFlog = "E";
                break;
            default:
                levelFlog = "";
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(levelFlog);
        sb.append(":");
        sb.append(tag);
        sb.append("->");
        sb.append(msg);
        sb.append("\n");

        SpannableString spanString = new SpannableString(sb.toString());
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        spanString.setSpan(span, 0, sb.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Message msgObj = new Message();
        msgObj.what = OUT_SIGNAL;
        msgObj.obj = spanString;
        msgObj.arg1 = level;

        sHandler.sendMessage(msgObj);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag
     * @param msg
     */
    public static void v(String tag, String msg) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().v(msg);
        }

        logOut(Log.VERBOSE, tag, msg);
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param tag
     * @param msg
     * @param tr
     */
    public static void v(String tag, String msg, Throwable tr) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().v(msg, tr);
        }

        logOut(Log.VERBOSE, tag, msg);
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag
     * @param msg
     */
    public static void d(String tag, String msg) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().d(msg);
        }

        logOut(Log.DEBUG, tag, msg);
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param tag
     * @param msg
     * @param tr
     */
    public static void d(String tag, String msg, Throwable tr) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().d(msg, tr);
        }

        logOut(Log.DEBUG, tag, msg);
    }

    /**
     * Send an INFO log message.
     *
     * @param tag
     * @param msg
     */
    public static void i(String tag, String msg) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().i(msg);
        }

        logOut(Log.INFO, tag, msg);
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param tag
     * @param msg
     * @param tr
     */
    public static void i(String tag, String msg, Throwable tr) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().i(msg, tr);
        }

        logOut(Log.INFO, tag, msg);
    }

    /**
     * Send a WARN log message.
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().w(msg);
        }

        logOut(Log.WARN, tag, msg);
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag
     * @param msg
     * @param tr
     */
    public static void w(String tag, String msg, Throwable tr) {

        if (tag == null || msg == null) {
            return;
        }

        if (LOG_ON) {
            getLocalPrinter().w(msg, tr);
        }

        logOut(Log.WARN, tag, msg);
    }

    /**
     * 打印Error级别的日志,日志开关对此类日志不屏蔽
     *
     * @param tag
     * @param msg
     */
    public static void e(String tag, String msg) {

        if (tag == null || msg == null) {
            return;
        }

        if (true) {
            getLocalPrinter().e(msg);
        }

        logOut(Log.ERROR, tag, msg);
    }

    /**
     * 打印Error级别的日志,日志开关对此类日志不屏蔽
     *
     * @param tag
     * @param msg
     * @param tr
     */
    public static void e(String tag, String msg, Throwable tr) {

        if (tag == null || msg == null) {
            return;
        }

        if (true) {
            getLocalPrinter().e(msg, tr);
        }

        logOut(Log.ERROR, tag, msg);
    }

    /**
     * 默认标签 TAG=LAZY 输出INFO LOG
     *
     * @param msg
     */
    public static void lazy(String msg) {
        i(TAG, msg);
    }

    public static void json(String tag, String msg) {
        if (tag == null || msg == null) {
            return;
        }

        getLocalPrinter().json(msg);

        logOut(Log.VERBOSE, tag, msg);
    }

    public static String getJSONLogString(String json) {
        try {
            String message;
            if (json.startsWith("{")) {
                JSONObject e1 = new JSONObject(json);
                message = e1.toString(4);
                return message;
            }

            if (json.startsWith("[")) {
                JSONArray e = new JSONArray(json);
                message = e.toString(4);
                return message;
            }
        } catch (JSONException var4) {
            return var4.getCause().getMessage() + "\n" + json;
        }

        return json;
    }
}

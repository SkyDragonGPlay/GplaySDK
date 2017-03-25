package com.skydragon.gplay.host.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogWrapper {
    private static boolean showLog = true;
    private static String LOG_PREFIX = "==> ";

    /**
     * Enables or disables logging.
     *
     * @param enable Set to true if logging should be enabled, false to disable logging.
     */
    public static void enableLogging(boolean enable) {
        showLog = enable;
        i("PBLog", "enableLogging: " + enable);
    }

    /**
     * Set an info log message.
     *
     * @param tag Tag for the log message.
     * @param msg Log message to output to the console.
     */
    public static void i(String tag, String msg) {
        if (showLog) {
            Log.i(tag, LOG_PREFIX + msg);
        }
    }

    /**
     * Set an error log message.
     *
     * @param tag Tag for the log message.
     * @param msg Log message to output to the console.
     */
    public static void e(String tag, String msg) {
        if (showLog) {
            Log.e(tag, LOG_PREFIX + msg);
        }
    }

    /**
     * Set an error log message.
     *
     * @param tag Tag for the log message.
     * @param e   An exception to log
     */
    public static void e(String tag, Exception e) {
        if (showLog) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            Log.e(tag, LOG_PREFIX + sw.toString());
        }
    }

    /**
     * Set a warning log message.
     *
     * @param tag Tag for the log message.
     * @param msg Log message to output to the console.
     */
    public static void w(String tag, String msg) {
        if (showLog) {
            Log.w(tag, LOG_PREFIX + msg);
        }
    }

    /**
     * Set a debug log message.
     *
     * @param tag Tag for the log message.
     * @param msg Log message to output to the console.
     */
    public static void d(String tag, String msg) {
        if (showLog) {
            Log.d(tag, LOG_PREFIX + msg);
        }
    }

    /**
     * Set a verbose log message.
     *
     * @param tag Tag for the log message.
     * @param msg Log message to output to the console.
     */
    public static void v(String tag, String msg) {
        if (showLog) {
            Log.v(tag, LOG_PREFIX + msg);
        }
    }
}

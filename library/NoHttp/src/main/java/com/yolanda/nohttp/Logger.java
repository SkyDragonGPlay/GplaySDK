/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yolanda.nohttp;

import android.util.Log;

/**
 * Created in Jul 28, 2015 7:32:05 PM.
 *
 * @author Yan Zhenjie.
 */
public class Logger {

    /**
     * Library debug tag.
     */
    private static String STag = "NoHttp";
    /**
     * Library debug sign.
     */
    private static boolean SDebug = false;

    /**
     * Set tag of log.
     *
     * @param tag tag.
     */
    public static void setTag(String tag) {
        STag = tag;
    }

    /**
     * Open debug mode of {@code NoHttp}.
     *
     * @param debug true open, false close.
     */
    public static void setDebug(boolean debug) {
        SDebug = debug;
    }

    public static void i(Object msg) {
        if (SDebug) {
            Log.i(STag, toString(msg));
        }
    }

    public static void i(Throwable e) {
        if (SDebug) {
            Log.i(STag, "", e);
        }
    }

    public static void i(Throwable e, Object msg) {
        if (SDebug) {
            Log.i(STag, toString(msg), e);
        }
    }

    public static void d(Object msg) {
        if (SDebug) {
            Log.d(STag, toString(msg));
        }
    }

    public static void d(Throwable e) {
        if (SDebug) {
            Log.d(STag, "", e);
        }
    }

    public static void d(Throwable e, Object msg) {
        if (SDebug) {
            Log.d(STag, toString(msg), e);
        }
    }

    public static void e(Object msg) {
        if (SDebug) {
            Log.e(STag, toString(msg));
        }
    }

    public static void e(Throwable e) {
        if (SDebug) {
            Log.e(STag, "", e);
        }
    }

    public static void e(Throwable e, String msg) {
        if (SDebug) {
            Log.e(STag, toString(msg), e);
        }
    }

    public static void w(String msg) {
        if (SDebug) {
            Log.w(STag, toString(msg));
        }
    }

    public static void w(Throwable e) {
        if (SDebug) {
            Log.w(STag, "", e);
        }
    }

    public static void w(Throwable e, String msg) {
        if (SDebug) {
            Log.w(STag, toString(msg), e);
        }
    }

    private static String toString(Object o) {
        return o == null ? "null" : o.toString();
    }
}

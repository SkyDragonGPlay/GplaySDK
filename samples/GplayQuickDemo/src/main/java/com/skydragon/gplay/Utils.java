package com.skydragon.gplay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.skydragon.gplay.OnFileDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    
    public static final String TAG = "Utils";

    private static String sPreferencesSuffix = "_preferences";
    
    public static SharedPreferences getSharedPreferences(Context ctx) {
        String preferencesName = ctx.getPackageName() + sPreferencesSuffix;
        SharedPreferences sh = ctx.getSharedPreferences(preferencesName, Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
        return sh;
    }

    public static String getChannelCode() {
        return Constants.DEFAULT_CHANNEL_ID;
    }

    public static String getCacheDir(Context ctx) {
        String sCacheDir =  Environment.getExternalStorageDirectory() + "/" + ctx.getPackageName() + "/gplay";
        File f = new File(sCacheDir);
        if(!f.exists()) {
            f.mkdirs();
        }
        return sCacheDir;
    }

    public static void downloadFileInThread(final String downloadUrl, final String savePath, final OnFileDownloadListener listener) {
        new Thread() {
            public void run() {
                Log.d(TAG, "downloadFile... " + downloadUrl);
                HttpURLConnection httpConn = null;
                try {
                    URL url = new URL(downloadUrl);
                    httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setConnectTimeout(5000);
                    httpConn.setRequestMethod("GET");
                } catch (Exception e) {
                    Log.e(TAG, "downloadFile failed(open connection exception)!");
                    listener.onFailure();
                    return;
                }

                File file = new File(savePath);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "downloadFile failed(create file exception)!");
                    listener.onFailure();
                    return;
                }
                int fileLength = 0;
                int downedFileLength = 0;
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    inputStream = httpConn.getInputStream();
                } catch (Exception e) {
                    Log.e(TAG, "downloadFile failed(get input stream exception)!");
                    listener.onFailure();
                    file.delete();
                    return;
                }
                try {
                    byte[] buffer = new byte[1024 * 4];
                    fileLength = httpConn.getContentLength();

                    if (fileLength > 0) {
                        Log.d(TAG, "download file start");
                        listener.onStart();
                    }
                    int size = -1;
                    while ((size = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, size);
                        downedFileLength += size;
                        listener.onProgress(downedFileLength, fileLength);
                    }
                    Log.d(TAG, "download file success");
                    listener.onSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpConn.disconnect();
                    tryClose(outputStream);
                    tryClose(inputStream);
                }
            }
        }.start();
    }

    public static void tryClose(InputStream is) {
        try{
            if(null != is) {
                is.close();
            }
        } catch( Exception e) {
            e.printStackTrace();;
        }
    }

    public static void tryClose(OutputStream os) {
        try{
            if(null != os) {
                os.close();
            }
        } catch( Exception e) {
            e.printStackTrace();;
        }
    }

    public static boolean isEmpty(String s) {
        return null == s || s.trim().equals("");
    }

    public static void ensureFileExist(String path) {
        File f = new File(path);
        if(!f.exists()) {
            f.mkdirs();
        }
    }

    public static void removeFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public static Drawable getDrawable(Context ctx, String resourceName) {
        Resources rs = ctx.getResources();
        int id = rs.getIdentifier(resourceName, "drawable", ctx.getPackageName());
        return rs.getDrawable(id);
    }

    public static int getLayout(Context ctx, String resourceName) {
        Resources rs = ctx.getResources();
        return rs.getIdentifier(resourceName, "layout", ctx.getPackageName());
    }

    public static int getResourceId(Context ctx, String name) {
        Resources rs = ctx.getResources();
        return rs.getIdentifier(name, "id", ctx.getPackageName());
    }

    public static String ensurePathEndsWithSlash(String path) {
        if (TextUtils.isEmpty(path))
            return "";

        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        return path;
    }
}

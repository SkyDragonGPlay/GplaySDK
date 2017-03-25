package com.skydragon.gplay.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Utils {
    
    public static final String TAG = "Utils";

    private static String sPreferencesSuffix = "_preferences";
    
    public static SharedPreferences getSharedPreferences(Context ctx) {
        String preferencesName = ctx.getPackageName() + sPreferencesSuffix;
        SharedPreferences sh = ctx.getSharedPreferences(preferencesName, Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
        return sh;
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

    public static boolean deleteSubFile(String filePath) {
        File fRoot = new File(filePath);
        if(fRoot.isFile()) return false;
        try {
            for (File f : fRoot.listFiles()) {
                deleteFile(f);
            }
        } catch( Exception e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteFile(String filePath) {
        return deleteFile(filePath, null);
    }

    public static boolean deleteFile(String filePath, ArrayList<String> excepts) {
        boolean ret = false;
        File file = new File(filePath);
        try {
            ret = deleteFile(file, excepts);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static boolean deleteFile(File file) throws IOException {
        return deleteFile(file, null);
    }

    public static boolean deleteFile(File file, ArrayList<String> excepts) throws IOException {
        boolean ret = true;
        boolean hasExclude = false;

        if (!file.exists()) {
            LogWrapper.i(TAG, "File (" + file.getPath() + ") doesn't exist!");
            return true;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File child : files) {
                if (isExcluded(child.getAbsolutePath(), excepts)) {
                    hasExclude = true;
                    // 被排除掉了
                    continue;
                }

                if (child.isDirectory()) {
                    if (!deleteFile(child)) {
                        throw new IOException("Delete (" + child.getPath() + ") failed!");
                    }
                } else {
                    if (!safeDeleteFile(child)) {
                        ret = false;
                    }
                }
            }
        } else {
            return safeDeleteFile(file);
        }

        if (!hasExclude) {
            ret = safeDeleteFile(file);
        }

        return ret;
    }

    private static boolean isExcluded(String path, ArrayList<String> excepts) {
        if (excepts == null) {
            return false;
        }

        String absolutePath = new File(path).getAbsolutePath();
        for (String str : excepts) {
            File child = new File(str);
            if (child.getAbsolutePath().equals(absolutePath)) {
                return true;
            }
        }

        return false;
    }

    private static boolean safeDeleteFile(File file) {
        boolean ret;
        String filePath = file.getAbsolutePath();
        File to = new File(filePath + System.currentTimeMillis());
        ret = file.renameTo(to);

        if (ret) {
            ret = to.delete();
        }

        if (ret) {
            LogWrapper.d(TAG, "safeDeleteFile (" + filePath + ") succeed");
        } else {
            LogWrapper.e(TAG, "safeDeleteFile (" + filePath + ") failed");
        }

        return ret;
    }

    public static void removeFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }


    public static void clearAllCaches() {
        Log.d(TAG, "GplayEmulator clearAllCaches called!!!");
        File fMark = new File(Environment.getExternalStorageDirectory(), "gplay_clear_cache_mark");
        if(fMark.exists()) {
            fMark.delete();
        }
        String gameDir = FileConstants.getGplayGameDefaultDir();
        Utils.deleteSubFile(gameDir);

        if(fMark.exists()) return;
        try {
            fMark.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

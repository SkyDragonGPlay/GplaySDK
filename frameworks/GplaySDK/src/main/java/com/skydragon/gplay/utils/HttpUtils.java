package com.skydragon.gplay.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpUtils {
    private static final String TAG = "HttpUtils";

    public interface HttpResponseListener {
        void onFailure();
        void onSuccess(String response);
    }

    public interface OnFileDownloadListener {
        void onStart();
        void onFailure();
        void onProgress(long downloaded, long total);
        void onSuccess();
    }

    private static Thread sDownloadThread;
    private static HttpURLConnection sConnection = null;
    private static boolean sIsCanceled = false;

    public static void requestData(final String serverUrl, final HttpResponseListener listener) {
        Log.d(TAG, "requestData... " + serverUrl);
        Thread requestThread = new Thread() {
            public void run() {
                requestDataInThread(serverUrl, listener);
            }
        };
        requestThread.start();
    }

    private static void requestDataInThread(final String serverUrl, final HttpResponseListener listener) {
        Log.d(TAG, "requestDataInThread... " + serverUrl);
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(serverUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(6000);
            httpConn.setRequestMethod("GET");

            int status = httpConn.getResponseCode();
            if( status != 200 ) {
                Log.d(TAG, "requestDataInThread failed");
                listener.onFailure();
                return;
            }

            StringBuilder sb = new StringBuilder("");
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader( httpConn.getInputStream()));
            while( (line = br.readLine() ) != null ) {
                sb.append(line);
            }
            String json = sb.toString();
            if(json.isEmpty()) {
                Log.d(TAG, "requestDataInThread failed");
                listener.onFailure();
                return;
            }
            Log.d(TAG, "requestDataInThread success json " + json);
            listener.onSuccess(json);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure();
        } finally {
            if(null != httpConn) {
                httpConn.disconnect();
            }
        }
    }

    public static void downloadFile(final String downloadUrl, final String savePath, final OnFileDownloadListener listener) {
        Log.d(TAG, "downloadFile... " + downloadUrl);

        sDownloadThread = new Thread() {
            public void run() {
                downloadFileInThread(downloadUrl, savePath, listener);
            }
        };
        sDownloadThread.start();
    }

    private static void downloadFileInThread(final String downloadUrl, final String savePath, final OnFileDownloadListener listener) {
        Log.d(TAG, "downloadFileInThread... " + downloadUrl);
        try {
            URL url = new URL(downloadUrl);
            sConnection = (HttpURLConnection) url.openConnection();
            sConnection.setConnectTimeout(5000);
            sConnection.setRequestMethod("GET");
        } catch (Exception e) {
            Log.e(TAG, "downloadFileInThread failed(open connection exception)!");
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
            Log.e(TAG, "downloadFileInThread failed(create file exception)!");
            listener.onFailure();
            return;
        }
        int fileLength;
        int downedFileLength = 0;
        boolean downloadEnd = false;
        OutputStream outputStream;
        InputStream inputStream;
        try {
            outputStream = new FileOutputStream(file);
            inputStream = sConnection.getInputStream();
        } catch (Exception e) {
            Log.e(TAG, "downloadFileInThread failed(get input stream exception)!");
            listener.onFailure();
            file.delete();
            return;
        }
        try {
            byte[] buffer = new byte[1024 * 4];
            fileLength = sConnection.getContentLength();

            if (fileLength > 0) {
                Log.d(TAG, "download file start");
                listener.onStart();
            }
            int size;
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
                downedFileLength += size;
                listener.onProgress(downedFileLength, fileLength);
            }

            downloadEnd = true;

            Log.d(TAG, "download file success");
            listener.onSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            if (!sIsCanceled) {
                listener.onFailure();
            }
        } finally {
            sConnection.disconnect();
            sConnection = null;
            FileUtils.tryClose(outputStream);
            FileUtils.tryClose(inputStream);
        }

        if (!downloadEnd) {
            Log.d(TAG, "delete download temp file if download failed!");
            file.delete();
        }
    }

    public static void cancelDownload() {
        Log.d(TAG, "cancelDownload...");
        sIsCanceled = true;
        stopDownload();
    }

    private static void stopDownload() {
        Log.d(TAG, "stopDownload...");
        if (sDownloadThread != null) {
            sDownloadThread.interrupt();

            sDownloadThread = null;
        }

        if (sConnection != null) {
            sConnection.disconnect();
            sConnection = null;
        }
    }

    public static String getCPUABI() {
        String abi = Build.CPU_ABI;
        abi = (abi == null || abi.trim().length() == 0) ? "" : abi;
        // 检视是否有第二类型，1.6没有这个字段
        try {
            String cpuAbi2 = Build.class.getField("CPU_ABI2").get(null).toString();
            cpuAbi2 = (cpuAbi2 == null || cpuAbi2.trim().length() == 0) ? null : cpuAbi2;
            if (cpuAbi2 != null) {
                abi = abi + "," + cpuAbi2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abi;
    }
}

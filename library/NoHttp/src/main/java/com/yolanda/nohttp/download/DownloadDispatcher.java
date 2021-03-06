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
package com.yolanda.nohttp.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.PosterHandler;

import java.util.concurrent.BlockingQueue;

/**
 * <p>
 * Download queue polling thread.
 * </p>
 * Created in Oct 21, 2015 2:46:23 PM.
 *
 * @author Yan Zhenjie.
 */
class DownloadDispatcher extends Thread {

    /**
     * Un finish task queue.
     */
    private final BlockingQueue<DownloadRequest> mUnFinishQueue;
    /**
     * Download task queue.
     */
    private final BlockingQueue<DownloadRequest> mDownloadQueue;
    /**
     * Are you out of this thread.
     */
    private boolean mQuit = false;

    private DownloadHandler mDownloadHandler;

    private class DownloadHandler extends Handler {

        DownloadHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ThreadPoster.COMMAND_PROGRESS: {
                    DownloadRequest downloadRequest = (DownloadRequest) msg.obj;
                    if (downloadRequest != null) {
                        DownloadListener listener = downloadRequest.downloadListener();
                        listener.onProgress(downloadRequest.what(), msg.arg1, msg.arg2);
                    }
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Create a thread that executes the download queue.
     *
     * @param unFinishQueue un finish queue.
     * @param downloadQueue download queue to be polled.
     */
    public DownloadDispatcher(BlockingQueue<DownloadRequest> unFinishQueue, BlockingQueue<DownloadRequest> downloadQueue) {
        mUnFinishQueue = unFinishQueue;
        mDownloadQueue = downloadQueue;
        mDownloadHandler = new DownloadHandler(Looper.getMainLooper());
    }

    /**
     * Quit this thread.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (!mQuit) {
            final DownloadRequest request;
            try {
                request = mDownloadQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    mDownloadHandler = null;
                    return;
                }
                continue;
            }

            if (request.isCanceled()) {
                Logger.d(request.url() + " is canceled.");
                continue;
            }

            request.start();
            SyncDownloadExecutor.INSTANCE.execute(request.what(), request, new DownloadListener() {

                @Override
                public void onStart(String what, boolean isResume, long beforeLength, Headers headers, long allCount) {
                    ThreadPoster threadPoster = new ThreadPoster(request.what(), request.downloadListener());
                    threadPoster.onStart(isResume, beforeLength, headers, allCount);
                    PosterHandler.getInstance().post(threadPoster);
                }

                @Override
                public void onDownloadError(String what, Exception exception) {
                    ThreadPoster threadPoster = new ThreadPoster(request.what(), request.downloadListener());
                    threadPoster.onError(exception);
                    PosterHandler.getInstance().post(threadPoster);
                }

                @Override
                public void onProgress(String what, int fileCount, int fileSize) {
                    if (mDownloadHandler != null) {
                        Message message = Message.obtain();
                        message.what = ThreadPoster.COMMAND_PROGRESS;
                        message.obj = request;
                        message.arg1 = fileCount;
                        message.arg2 = fileSize;
                        mDownloadHandler.sendMessage(message);
                    }
                }

                @Override
                public void onFinish(String what, String filePath) {
                    ThreadPoster threadPoster = new ThreadPoster(request.what(), request.downloadListener());
                    threadPoster.onFinish(filePath);
                    PosterHandler.getInstance().post(threadPoster);
                }

                @Override
                public void onCancel(String what) {
                    ThreadPoster threadPoster = new ThreadPoster(request.what(), request.downloadListener());
                    threadPoster.onCancel();
                    PosterHandler.getInstance().post(threadPoster);
                }
            });
            request.finish();
            mUnFinishQueue.remove(request);
        }

        mDownloadHandler = null;
    }

    private class ThreadPoster implements Runnable {

        public static final int COMMAND_START = 0;
        public static final int COMMAND_PROGRESS = 1;
        public static final int COMMAND_ERROR = 2;
        public static final int COMMAND_FINISH = 3;
        public static final int COMMAND_CANCEL = 4;

        private final String what;
        private final DownloadListener downloadListener;

        // command
        private int command;

        // start
        private Headers responseHeaders;
        private long allCount;
        private boolean isResume;
        private long beforeLength;

        // error
        private Exception exception;

        // finish
        private String filePath;

        public ThreadPoster(String what, DownloadListener downloadListener) {
            this.what = what;
            this.downloadListener = downloadListener;
        }

        public void onStart(boolean isResume, long beforeLength, Headers responseHeaders, long allCount) {
            this.command = COMMAND_START;
            this.isResume = isResume;
            this.beforeLength = beforeLength;
            this.responseHeaders = responseHeaders;
            this.allCount = allCount;
        }

        public void onError(Exception exception) {
            this.command = COMMAND_ERROR;
            this.exception = exception;
        }

        public void onCancel() {
            this.command = COMMAND_CANCEL;
        }

        public void onFinish(String filePath) {
            this.command = COMMAND_FINISH;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            switch (command) {
                case COMMAND_START:
                    downloadListener.onStart(what, isResume, beforeLength, responseHeaders, allCount);
                    break;
                case COMMAND_ERROR:
                    downloadListener.onDownloadError(what, exception);
                    break;
                case COMMAND_FINISH:
                    downloadListener.onFinish(what, filePath);
                    break;
                case COMMAND_CANCEL:
                    downloadListener.onCancel(what);
                    break;
                default:
                    break;
            }
        }
    }
}

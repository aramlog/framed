package com.frames.utils;

import android.os.AsyncTask;

import com.frames.managers.DownloadManager;

public class FileDownloaderTask extends AsyncTask<String, Void, Boolean> {

    private OnFileDownloadListener onDownloadListener;

    public FileDownloaderTask(OnFileDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String url = params[0];
        return DownloadManager.getInstance().downloadFile(url);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (onDownloadListener != null) {
            if (result) {
                onDownloadListener.onDownloadSuccess();
            } else {
                onDownloadListener.onDownloadFailure();
            }
        }
    }
}

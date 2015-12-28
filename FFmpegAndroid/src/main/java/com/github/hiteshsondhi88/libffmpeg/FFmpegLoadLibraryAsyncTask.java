package com.github.hiteshsondhi88.libffmpeg;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

class FFmpegLoadLibraryAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private final FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler;
    private final Context context;

    FFmpegLoadLibraryAsyncTask(Context context, FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler) {
        this.context = context;
        this.ffmpegLoadBinaryResponseHandler = ffmpegLoadBinaryResponseHandler;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File ffmpegFile = new File(FileUtils.getFFmpeg(context));
        if (ffmpegFile.exists() && isDeviceFFmpegVersionOld() && !ffmpegFile.delete()) {
            return false;
        }
        if (!ffmpegFile.exists()) {
            boolean isFileCopied = FileUtils.copyBinaryFromRawToData(context, FileUtils.ffmpegFileName);

            // make file executable
            if (isFileCopied) {
                if(!ffmpegFile.canExecute()) {
                    Log.d("FFmpeg is not executable, trying to make it executable ...");
                    if (ffmpegFile.setExecutable(true)) {
                        return true;
                    }
                } else {
                    Log.d("FFmpeg is executable");
                    return true;
                }
            }
        }
        return ffmpegFile.exists() && ffmpegFile.canExecute();
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (ffmpegLoadBinaryResponseHandler != null) {
            if (isSuccess) {
                ffmpegLoadBinaryResponseHandler.onSuccess();
            } else {
                ffmpegLoadBinaryResponseHandler.onFailure();
            }
            ffmpegLoadBinaryResponseHandler.onFinish();
        }
    }

    private boolean isDeviceFFmpegVersionOld() {
        String latestSha1 = FileUtils.getLatestFfmpegSha1(context);
        String currentSha1 = FileUtils.SHA1(FileUtils.getFFmpeg(context));
        boolean newerFffmpegAvailable = !latestSha1.equals(currentSha1);
        Log.d(String.format("Latest sha1: %s current sha1 : %s ", latestSha1, currentSha1));
        Log.d("Newer ffmpeg availabel : " + newerFffmpegAvailable);
        return newerFffmpegAvailable;
    }
}

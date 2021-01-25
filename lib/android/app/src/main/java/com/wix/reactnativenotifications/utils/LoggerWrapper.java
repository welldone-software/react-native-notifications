package com.wix.reactnativenotifications.utils;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoggerWrapper {

    private static final String TAG = LoggerWrapper.class.getSimpleName();
    private static final String DATE_FORMAT = "MMMM dd yyyy, h:mm:ss a";
    private static final String SUB_FOLDER = "silverfort/logs";
    private static final String FILENAME_1 = "notifications_logs.txt";
    private static final String FILENAME_2 = "notifications_logs_2.txt";
    private static final int LOGS_SIZE_LIMIT = 24 * 1024 * 1000;

    private static LoggerWrapper mLogger;

    private final String mLogsFilesUrl;
    private final String mLogFileUrl;
    private final String mLogFileUrl2;
    private final FileObserver mFileObserver;

    public static LoggerWrapper getInstance(Context context) {
        if (mLogger == null) {
            mLogger = new LoggerWrapper(context);
        }
        return mLogger;
    }

    private LoggerWrapper(Context context) {
        File externalDirectory = context.getExternalFilesDir(null);
        String baseUrl = externalDirectory.getAbsolutePath();
        mLogsFilesUrl = baseUrl + "/" + SUB_FOLDER;
        mLogFileUrl = mLogsFilesUrl + "/" + FILENAME_1;
        mLogFileUrl2 = mLogsFilesUrl + "/" + FILENAME_2;

        mFileObserver = new FileObserver(mLogsFilesUrl) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                Log.i("FileObserver", "event: " + event + ", path: " + path);
            }
        };
        mFileObserver.startWatching();
    }

    private String getTag(LogLevel level, String tag) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String time = simpleDateFormat.format(new Date());
        return "[" + level.mLabel + "] [" + time + "] " + tag;
    }

    private void saveLog(@NonNull LogLevel level, @NonNull String tag, String message) {
        File folder = new File(mLogsFilesUrl);
        folder.mkdirs();

        String fileUrl = getFileUrl();
        File file = new File(fileUrl);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "Could not create file!");
            }
        }
        try {
            OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(file, true));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            String logTag = getTag(level, tag);
            String log = logTag + ": " + message + "\n";
            bufferedWriter.write(log);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileUrl() {
        File file1 = new File(mLogFileUrl);
        File file2 = new File(mLogFileUrl2);

        boolean biggerFile1 = file1.length() > LOGS_SIZE_LIMIT;
        boolean biggerFile2 = file2.length() > LOGS_SIZE_LIMIT;

        if (biggerFile1 && biggerFile2) {
            if (file1.exists()) {
                file1.delete();
            }
            return mLogFileUrl;
        }

        if (biggerFile1) {
            if (file2.exists()) {
                file2.delete();
            }
            return mLogFileUrl2;
        }

        return mLogFileUrl;
    }

    public void d(@NonNull String tag, String message) {
        saveLog(LogLevel.d, tag, message);
        Log.d(tag, message);
    }

    public void d(@NonNull String tag) {
        saveLog(LogLevel.d, tag, "");
        Log.d(tag, "");
    }

    public void e(@NonNull String tag, String message) {
        saveLog(LogLevel.e, tag, message);
        Log.e(tag, message);
    }

    public void e(@NonNull String tag) {
        saveLog(LogLevel.e, tag, "");
        Log.e(tag, "");
    }

    public void i(@NonNull String tag, String message) {
        saveLog(LogLevel.i, tag, message);
        Log.i(tag, message);
    }

    public void i(@NonNull String tag) {
        saveLog(LogLevel.i, tag, "");
        Log.i(tag, "");
    }

    public void v(@NonNull String tag, String message) {
        saveLog(LogLevel.v, tag, message);
        Log.v(tag, message);
    }

    public void v(@NonNull String tag) {
        saveLog(LogLevel.v, tag, "");
        Log.v(tag, "");
    }

    public void w(@NonNull String tag, String message) {
        saveLog(LogLevel.w, tag, message);
        Log.w(tag, message);
    }

    public void w(@NonNull String tag) {
        saveLog(LogLevel.w, tag, "");
        Log.w(tag, "");
    }

    public void wtf(@NonNull String tag, String message) {
        saveLog(LogLevel.wtf, tag, message);
        Log.wtf(tag, message);
    }

    public void wtf(@NonNull String tag) {
        saveLog(LogLevel.wtf, tag, "");
        Log.wtf(tag, "");
    }

    private enum LogLevel {
        d("LOG"),
        e("ERROR"),
        i("LOG"),
        v("LOG"),
        w("WARN"),
        wtf("WTF");

        public final String mLabel;

        LogLevel(String label) {
            mLabel = label;
        }
    }

}

package com.wix.reactnativenotifications.core;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class NotificationActionService extends HeadlessJsTaskService {

    private static final String TASK_NAME = "notificationActionClick";
    private static final int TASK_TIMEOUT = 1000 * 60;
    private static final boolean TASK_IN_FOREGROUND = true;

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            return new HeadlessJsTaskConfig(
                    TASK_NAME,
                    Arguments.fromBundle(extras),
                    TASK_TIMEOUT,
                    TASK_IN_FOREGROUND
            );
        }
        return null;
    }

}

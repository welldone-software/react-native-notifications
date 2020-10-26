package com.wix.reactnativenotifications.core;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.wix.reactnativenotifications.core.notificationdrawer.IPushNotificationsDrawer;
import com.wix.reactnativenotifications.core.notificationdrawer.PushNotificationsDrawer;

public class NotificationActionService extends HeadlessJsTaskService {

    private static final String TASK_NAME = "notificationActionClick";
    private static final String PUSH_NOTIFICATION_EXTRA = "pushNotification";
    private static final String ID_EXTRA = "id";
    private static final int TASK_TIMEOUT = 1000 * 15;
    private static final boolean TASK_IN_FOREGROUND = true;

    private void dismissNotification(Bundle notification) {
        Bundle payload = notification.getBundle(PUSH_NOTIFICATION_EXTRA);
        if (payload != null) {
            int notificationId = payload.getInt(ID_EXTRA);
            IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(getApplicationContext());
            notificationsDrawer.onNotificationClearRequest(notificationId);
        }
    }

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            dismissNotification(extras);
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

package com.wix.reactnativenotifications.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wix.reactnativenotifications.core.notification.PushNotificationProps;

import static com.wix.reactnativenotifications.Defs.NOTIFICATION_ACTION_CLICK;
import static com.wix.reactnativenotifications.Defs.ACTION_EXTRA_NAME;
import static com.wix.reactnativenotifications.Defs.PUSH_NOTIFICATION_EXTRA_NAME;

public class NotificationIntentAdapter {

    public static PendingIntent createPendingNotificationIntent(Context appContext, Intent intent, PushNotificationProps notification) {
        return createPendingNotificationIntent(appContext, intent, notification, null);
    }

    public static PendingIntent createPendingNotificationIntent(Context appContext, Intent intent, PushNotificationProps notification, String action) {
        intent.putExtra(PUSH_NOTIFICATION_EXTRA_NAME, notification.asBundle());
        if (action != null) {
            intent.putExtra(ACTION_EXTRA_NAME, action);
            intent.setAction(NOTIFICATION_ACTION_CLICK);
        }
        return PendingIntent.getService(appContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
    }

    public static Bundle extractPendingNotificationDataFromIntent(Intent intent) {
        return intent.getBundleExtra(PUSH_NOTIFICATION_EXTRA_NAME);
    }

    public static boolean canHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle notificationData = intent.getExtras();
            return notificationData != null && intent.hasExtra(PUSH_NOTIFICATION_EXTRA_NAME);
        }

        return false;
    }
}

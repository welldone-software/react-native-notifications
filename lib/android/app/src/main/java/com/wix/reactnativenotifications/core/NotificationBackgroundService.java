package com.wix.reactnativenotifications.core;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.wix.reactnativenotifications.core.actions.ActionPayloadSaver;
import com.wix.reactnativenotifications.core.actions.UnlockActivity;
import com.wix.reactnativenotifications.core.notificationdrawer.IPushNotificationsDrawer;
import com.wix.reactnativenotifications.core.notificationdrawer.PushNotificationsDrawer;

public class NotificationBackgroundService extends HeadlessJsTaskService {

    private static final String PUSH_NOTIFICATION_EXTRA = "pushNotification";
    private static final String ID_EXTRA = "id";
    private static final int TASK_TIMEOUT = 1000 * 15;
    private static final boolean TASK_IN_FOREGROUND = true;

    public static final String NOTIFICATION_ACTION_CLICK = "notification_action_click";
    public static final String NOTIFICATION_ARRIVED = "notification_arrived";

    private void dismissNotification(Bundle notification) {
        Bundle payload = notification.getBundle(PUSH_NOTIFICATION_EXTRA);
        if (payload != null) {
            int notificationId = payload.getInt(ID_EXTRA);
            IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(getApplicationContext());
            notificationsDrawer.onNotificationClearRequest(notificationId);
        }
    }

    private boolean isLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    private void promptUnlock() {
        Intent intent = new Intent(this, UnlockActivity.class);
        intent.setAction(UnlockActivity.PROMPT_UNLOCK_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras != null && action != null) {
            switch (action) {
                case NOTIFICATION_ACTION_CLICK:
                    if (isLocked()) {
                        ActionPayloadSaver.getInstance(this).saveAwaitingAction(extras);
                        promptUnlock();
                    } else {
                        dismissNotification(extras);
                        return new HeadlessJsTaskConfig(
                                NOTIFICATION_ACTION_CLICK,
                                Arguments.fromBundle(extras),
                                TASK_TIMEOUT,
                                TASK_IN_FOREGROUND
                        );
                    }
                case NOTIFICATION_ARRIVED:
                    return new HeadlessJsTaskConfig(
                            NOTIFICATION_ARRIVED,
                            Arguments.fromBundle(extras),
                            TASK_TIMEOUT,
                            TASK_IN_FOREGROUND
                    );
            }
        }
        return null;
    }

}

package com.wix.reactnativenotifications.core.actions;

import android.app.KeyguardManager;
import android.content.Context;
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

    private boolean isLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    private void promptUnlock() {
        Intent intent = new Intent(this, UnlockActivity.class);
        intent.setAction(UnlockActivity.PROMPT_UNLOCK_ACTION);
        startActivity(intent);
    }

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (isLocked()) {
                ActionPayloadSaver.getInstance(this).saveAwaitingAction(extras);
                promptUnlock();
            } else {
                dismissNotification(extras);
                return new HeadlessJsTaskConfig(
                        TASK_NAME,
                        Arguments.fromBundle(extras),
                        TASK_TIMEOUT,
                        TASK_IN_FOREGROUND
                );
            }
        }
        return null;
    }

}

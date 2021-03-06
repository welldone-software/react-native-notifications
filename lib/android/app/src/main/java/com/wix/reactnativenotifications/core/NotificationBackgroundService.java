package com.wix.reactnativenotifications.core;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.wix.reactnativenotifications.MfaStorage;
import com.wix.reactnativenotifications.core.actions.ActionPayloadSaver;
import com.wix.reactnativenotifications.core.actions.UnlockActivity;
import com.wix.reactnativenotifications.core.notificationdrawer.IPushNotificationsDrawer;
import com.wix.reactnativenotifications.core.notificationdrawer.PushNotificationsDrawer;
import com.wix.reactnativenotifications.utils.LoggerWrapper;

import com.wix.reactnativenotifications.Defs;

public class NotificationBackgroundService extends HeadlessJsTaskService {

    private static final String PUSH_NOTIFICATION_EXTRA = "pushNotification";
    private static final int TASK_TIMEOUT = 1000 * 60;
    private static final boolean TASK_IN_FOREGROUND = true;
    private static final String TAG = NotificationBackgroundService.class.getSimpleName();

    private void dismissNotification(Bundle notification) {
        Bundle payload = notification.getBundle(PUSH_NOTIFICATION_EXTRA);
        if (payload != null) {
            String mfaRequestId = payload.getString(MfaStorage.REQUEST_ID_KEY);
            int notificationId = MfaStorage.getInstance(this).getNotificationId(mfaRequestId);
            IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(this);
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
        LoggerWrapper logger = LoggerWrapper.getInstance(this);
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        logger.i(TAG, "Intent arrived: " + action);
        if (extras != null && Defs.NOTIFICATION_ACTION_CLICK.equals(action)) {
            if (isLocked()) {
                logger.i(TAG, "Device is locked, prompting unlock");
                ActionPayloadSaver.getInstance(this).saveAwaitingAction(extras);
                promptUnlock();
            } else {
                logger.i(TAG, "Device is unlocked, notifying JS");
                dismissNotification(extras);
                return new HeadlessJsTaskConfig(
                        Defs.NOTIFICATION_ACTION_CLICK,
                        Arguments.fromBundle(extras),
                        TASK_TIMEOUT,
                        TASK_IN_FOREGROUND
                );
            }
        }
        return null;
    }

}

package com.wix.reactnativenotifications.core;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.wix.reactnativenotifications.NotificationsStorage;
import com.wix.reactnativenotifications.core.actions.ActionPayloadSaver;
import com.wix.reactnativenotifications.background.BackgroundAuthActivity;
import com.wix.reactnativenotifications.core.notificationdrawer.IPushNotificationsDrawer;
import com.wix.reactnativenotifications.core.notificationdrawer.PushNotificationsDrawer;
import com.wix.reactnativenotifications.utils.LoggerWrapper;

import com.wix.reactnativenotifications.Defs;

public class NotificationBackgroundService extends HeadlessJsTaskService {

    private static final String PUSH_NOTIFICATION_EXTRA = "pushNotification";
    private static final int TASK_TIMEOUT = 1000 * 60;
    private static final boolean TASK_IN_FOREGROUND = true;
    private static final String TAG = NotificationBackgroundService.class.getSimpleName();

    private void dismissNotification(Bundle notification, boolean delete) {
        Bundle payload = notification.getBundle(PUSH_NOTIFICATION_EXTRA);
        if (payload != null) {
            String mfaRequestId = payload.getString(NotificationsStorage.MFA_REQUEST_ID);
            NotificationsStorage storage = NotificationsStorage.getInstance(this);
            int notificationId = delete ? storage.removeNotification(mfaRequestId) : storage.getNotificationId(mfaRequestId);
            PushNotificationsDrawer.get(this).onNotificationClearRequest(notificationId);
        }
    }

    private boolean isLocked() {
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    private void promptUnlock(boolean withBiometrics) {
        Intent intent = new Intent(this, BackgroundAuthActivity.class);
        intent.setAction(withBiometrics ? BackgroundAuthActivity.PROMPT_BIOMETRIC_ACTION : BackgroundAuthActivity.PROMPT_UNLOCK_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(intent);
    }

    private void promptUnlock() {
        promptUnlock(false);
    }

    private HeadlessJsTaskConfig sendJSTask(String event, Bundle extras) {
        return new HeadlessJsTaskConfig(
                event,
                Arguments.fromBundle(extras),
                TASK_TIMEOUT,
                TASK_IN_FOREGROUND
        );
    }

    @Override
    protected @Nullable
    HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        LoggerWrapper logger = LoggerWrapper.getInstance(this);
        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        logger.i(TAG, "Intent arrived: " + action);
        if (extras != null && action != null) {
            switch (action) {
                case Defs.NOTIFICATION_ACTION_CLICK:
                    if (isLocked()) {
                        logger.i(TAG, "Device is locked, prompting unlock");
                        dismissNotification(extras, false);
                        ActionPayloadSaver.getInstance(this).saveAwaitingAction(extras);
                        promptUnlock();
                    } else {
                        logger.i(TAG, "Device is unlocked, notifying JS");
                        Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        sendBroadcast(closeIntent);

                        boolean authRequired = extras.getBoolean(Defs.PUSH_NOTIFICATION_EXTRA_AUTH_REQUIRED, true);
                        boolean authenticated = extras.getBoolean(Defs.PUSH_NOTIFICATION_EXTRA_AUTHENTICATED, false);
                        if (authRequired && !authenticated) {
                            dismissNotification(extras, false);
                            ActionPayloadSaver.getInstance(this).saveAwaitingAction(extras);
                            promptUnlock(true);
                        } else {
                            dismissNotification(extras, true);
                            return sendJSTask(Defs.NOTIFICATION_ACTION_CLICK, extras);
                        }
                    }
                case Defs.NOTIFICATION_ARRIVED:
                    return sendJSTask(Defs.NOTIFICATION_ARRIVED, extras);
            }
        }
        return null;
    }

}

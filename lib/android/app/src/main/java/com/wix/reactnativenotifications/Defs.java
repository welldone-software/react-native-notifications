package com.wix.reactnativenotifications;

public interface Defs {
    String LOGTAG = "ReactNativeNotifications";

    String TOKEN_RECEIVED_EVENT_NAME = "remoteNotificationsRegistered";

    String NOTIFICATION_RECEIVED_EVENT_NAME = "notificationReceived";
    String NOTIFICATION_RECEIVED_BACKGROUND_EVENT_NAME = "notificationReceivedBackground";
    String NOTIFICATION_OPENED_EVENT_NAME = "notificationOpened";

    String ACTION_EXTRA_NAME = "action";
    String NOTIFICATION_ACTION_CLICK = "notification_action_click";
    String PUSH_NOTIFICATION_EXTRA_NAME = "pushNotification";
}

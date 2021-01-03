package com.wix.reactnativenotifications;

public interface Defs {
    String LOGTAG = "ReactNativeNotifs";

    String TOKEN_RECEIVED_EVENT_NAME = "remoteNotificationsRegistered";

    String NOTIFICATION_RECEIVED_EVENT_NAME = "notificationReceived";
    String NOTIFICATION_OPENED_EVENT_NAME = "notificationOpened";

    String ACTION_EXTRA_NAME = "action";
    String NOTIFICATION_ACTION_CLICK = "notification_action_click";
    String NOTIFICATION_ARRIVED = "notification_arrived";
    String PUSH_NOTIFICATION_EXTRA_NAME = "pushNotification";
    String PUSH_NOTIFICATION_EXTRA_AUTH_REQUIRED = "auth_required";
    String PUSH_NOTIFICATION_EXTRA_AUTHENTICATED = "authenticated";
}

package com.wix.reactnativenotifications;

public interface Defs {
    String LOGTAG = "ReactNativeNotifications";

    String TOKEN_RECEIVED_EVENT_NAME = "remoteNotificationsRegistered";

    String NOTIFICATION_RECEIVED_EVENT_NAME = "notificationReceived";
    String NOTIFICATION_OPENED_EVENT_NAME = "notificationOpened";

    String NOTIFICATION_ACTION_CLICK = "notification_action_click";
    String NOTIFICATION_ARRIVED = "notification_arrived";
    String APPROVE_ACTION = "APPROVE";

    String EXTRA_ACTION_NAME = "action";
    String EXTRA_PAYLOAD = "pushNotification";
    String EXTRA_SECURED = "secured";
    String EXTRA_AUTHENTICATED = "authenticated";
}

package com.wix.reactnativenotifications.fcm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wix.reactnativenotifications.BuildConfig;
import com.wix.reactnativenotifications.Defs;
import com.wix.reactnativenotifications.core.NotificationBackgroundService;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotification;
import com.wix.reactnativenotifications.utils.LoggerWrapper;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

/**
 * Instance-ID + token refreshing handling service. Contacts the FCM to fetch the updated token.
 *
 * @author amitd
 */
public class FcmInstanceIdListenerService extends FirebaseMessagingService {

    private static final String TAG = FcmInstanceIdListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage message){
        LoggerWrapper logger = LoggerWrapper.getInstance(this);

        Bundle bundle = message.toIntent().getExtras();
        if(BuildConfig.DEBUG) logger.d(TAG, "New message from FCM: " + bundle);

        if (bundle != null) {
            Intent serviceIntent = new Intent(this, NotificationBackgroundService.class);
            serviceIntent.setAction(Defs.NOTIFICATION_ARRIVED);
            serviceIntent.putExtras(bundle);
            startService(serviceIntent);
        }

        try {
            final IPushNotification notification = PushNotification.get(getApplicationContext(), bundle);
            notification.onReceived();
        } catch (IPushNotification.InvalidNotificationException e) {
            // An FCM message, yes - but not the kind we know how to work with.
            if(BuildConfig.DEBUG) logger.v(TAG, "FCM message handling aborted: " + e.getMessage());
        }
    }
}

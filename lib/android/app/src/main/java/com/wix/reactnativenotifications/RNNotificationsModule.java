package com.wix.reactnativenotifications;

import android.app.Activity;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.wix.reactnativenotifications.core.AppLifecycleFacadeHolder;
import com.wix.reactnativenotifications.core.InitialNotificationHolder;
import com.wix.reactnativenotifications.core.NotificationIntentAdapter;
import com.wix.reactnativenotifications.core.ReactAppLifecycleFacade;
import com.wix.reactnativenotifications.core.notification.INotificationChannel;
import com.wix.reactnativenotifications.core.notification.IPushNotification;
import com.wix.reactnativenotifications.core.notification.NotificationChannel;
import com.wix.reactnativenotifications.core.notification.PushNotification;
import com.wix.reactnativenotifications.core.notification.PushNotificationProps;
import com.wix.reactnativenotifications.core.notificationdrawer.IPushNotificationsDrawer;
import com.wix.reactnativenotifications.core.notificationdrawer.PushNotificationsDrawer;
import com.wix.reactnativenotifications.fcm.FcmInstanceIdRefreshHandlerService;

import static com.wix.reactnativenotifications.Defs.LOGTAG;

public class RNNotificationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    public RNNotificationsModule(Application application, ReactApplicationContext reactContext) {
        super(reactContext);
        if (AppLifecycleFacadeHolder.get() instanceof ReactAppLifecycleFacade) {
            ((ReactAppLifecycleFacade) AppLifecycleFacadeHolder.get()).init(reactContext);
        }

        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "RNBridgeModule";
    }

    @Override
    public void initialize() {
        if (BuildConfig.DEBUG) Log.d(LOGTAG, "Native module init");
        startFcmIntentService(FcmInstanceIdRefreshHandlerService.EXTRA_IS_APP_INIT);

        final IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(getReactApplicationContext().getApplicationContext());
        notificationsDrawer.onAppInit();
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {
        if (NotificationIntentAdapter.canHandleIntent(intent)) {
            Bundle notificationData = intent.getExtras();
            final IPushNotification notification = PushNotification.get(getReactApplicationContext().getApplicationContext(), notificationData);
            if (notification != null) {
                notification.onOpened();
            }
        }
    }

    @ReactMethod
    public void refreshToken() {
        if (BuildConfig.DEBUG) Log.d(LOGTAG, "Native method invocation: refreshToken()");
        startFcmIntentService(FcmInstanceIdRefreshHandlerService.EXTRA_MANUAL_REFRESH);
    }

    @ReactMethod
    public void getInitialNotification(final Promise promise) {
        if (BuildConfig.DEBUG) Log.d(LOGTAG, "Native method invocation: getInitialNotification");
        Object result = null;

        final PushNotificationProps notification = InitialNotificationHolder.getInstance().get();
        try {
            InitialNotificationHolder.getInstance().clear();
            if (notification != null) {
                result = Arguments.fromBundle(notification.asBundle());
            }
            promise.resolve(result);
        } catch (Exception error) {
            promise.reject(error);
        }
    }

    @ReactMethod
    public void getInitialAction(final Promise promise) {
        promise.resolve(null);
    }

    @ReactMethod
    public void postLocalNotification(ReadableMap notificationPropsMap, int notificationId) {
        if (BuildConfig.DEBUG) Log.d(LOGTAG, "Native method invocation: postLocalNotification");
        final Bundle notificationProps = Arguments.toBundle(notificationPropsMap);
        final IPushNotification pushNotification = PushNotification.get(getReactApplicationContext().getApplicationContext(), notificationProps);
        pushNotification.onPostRequest(notificationId);
    }

    @ReactMethod
    public void cancelLocalNotification(int notificationId) {
        IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(getReactApplicationContext().getApplicationContext());
        notificationsDrawer.onNotificationClearRequest(notificationId);
    }

    @ReactMethod
    public void dismissNotification(String mfaRequestId) {
        MFAStorage storage = MFAStorage.getInstance(getReactApplicationContext().getApplicationContext());
        IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(getReactApplicationContext().getApplicationContext());
        notificationsDrawer.onNotificationClearRequest(storage.getNotificationId(mfaRequestId));
    }

    @ReactMethod
    public void setCategories(ReadableArray categories) {

    }

    @ReactMethod
    public void removeDeliveredNotifications(ReadableArray mfaRequestIds) {
        MFAStorage storage = MFAStorage.getInstance(getReactApplicationContext().getApplicationContext());
        for (int i = 0; i < mfaRequestIds.size(); i++) {
            storage.dismissNotification(mfaRequestIds.getString(i));
        }
    }

    @ReactMethod
    public void isRegisteredForRemoteNotifications(Promise promise) {
        boolean hasPermission = NotificationManagerCompatFacade.from(getReactApplicationContext()).areNotificationsEnabled();
        promise.resolve(hasPermission);
    }

    @ReactMethod
    void removeAllDeliveredNotifications() {
        IPushNotificationsDrawer notificationsDrawer = PushNotificationsDrawer.get(getReactApplicationContext().getApplicationContext());
        notificationsDrawer.onAllNotificationsClearRequest();
        MFAStorage storage = MFAStorage.getInstance(getReactApplicationContext().getApplicationContext());
        storage.clearAll();
    }

    @ReactMethod
    void getPendingMFAs(final Promise promise) {
        MFAStorage storage = MFAStorage.getInstance(getReactApplicationContext().getApplicationContext());
        try {
            promise.resolve(storage.getPendingMFAs());
        } catch (Exception error) {
            promise.reject(error);
        }
    }

    @ReactMethod
    void saveFetchedMFAs(final Promise promise, ReadableArray fetchedMFAs) {
        MFAStorage storage = MFAStorage.getInstance(getReactApplicationContext().getApplicationContext());
        try {
            storage.saveMFAs(fetchedMFAs);
            promise.resolve(null);
        } catch (Exception error) {
            promise.reject(error);
        }
    }

    @ReactMethod
    void updateMFA(final Promise promise, String requestId, boolean answer) {
        MFAStorage storage = MFAStorage.getInstance(getReactApplicationContext().getApplicationContext());
        try {
            storage.updateMFA(requestId, answer);
            promise.resolve(null);
        } catch (Exception error) {
            promise.reject(error);
        }
    }

    @ReactMethod
    void setNotificationChannel(ReadableMap notificationChannelPropsMap) {
        final Bundle notificationChannelProps = Arguments.toBundle(notificationChannelPropsMap);
        INotificationChannel notificationsDrawer = NotificationChannel.get(
                getReactApplicationContext().getApplicationContext(),
                notificationChannelProps
        );
        notificationsDrawer.setNotificationChannel();
    }

    protected void startFcmIntentService(int jobId) {
        final Context appContext = getReactApplicationContext().getApplicationContext();
        ComponentName serviceComponent = new ComponentName(appContext, FcmInstanceIdRefreshHandlerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        builder.setOverrideDeadline(3 * 1000);
        JobScheduler jobScheduler = (JobScheduler) appContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }
}

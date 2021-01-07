package com.wix.reactnativenotifications.core.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;

import androidx.core.text.HtmlCompat;

import com.facebook.react.bridge.ReactContext;
import com.wix.reactnativenotifications.R;
import com.wix.reactnativenotifications.core.AppLaunchHelper;
import com.wix.reactnativenotifications.core.AppLifecycleFacade;
import com.wix.reactnativenotifications.core.AppLifecycleFacade.AppVisibilityListener;
import com.wix.reactnativenotifications.core.AppLifecycleFacadeHolder;
import com.wix.reactnativenotifications.core.InitialNotificationHolder;
import com.wix.reactnativenotifications.core.JsIOHelper;
import com.wix.reactnativenotifications.core.NotificationBackgroundService;
import com.wix.reactnativenotifications.core.NotificationIntentAdapter;
import com.wix.reactnativenotifications.core.ProxyService;
import com.wix.reactnativenotifications.utils.LoggerWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.wix.reactnativenotifications.Defs.NOTIFICATION_OPENED_EVENT_NAME;
import static com.wix.reactnativenotifications.Defs.NOTIFICATION_RECEIVED_EVENT_NAME;

public class PushNotification implements IPushNotification {

    private final LoggerWrapper mLogger;

    final protected Context mContext;
    final protected AppLifecycleFacade mAppLifecycleFacade;
    final protected AppLaunchHelper mAppLaunchHelper;
    final protected JsIOHelper mJsIOHelper;
    final protected PushNotificationProps mNotificationProps;
    final protected AppVisibilityListener mAppVisibilityListener = new AppVisibilityListener() {
        @Override
        public void onAppVisible() {
            mAppLifecycleFacade.removeVisibilityListener(this);
            dispatchImmediately();
        }

        @Override
        public void onAppNotVisible() {
        }
    };

    public static IPushNotification get(Context context, Bundle bundle) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof INotificationsApplication) {
            return ((INotificationsApplication) appContext).getPushNotification(context, bundle, AppLifecycleFacadeHolder.get(), new AppLaunchHelper());
        }
        return new PushNotification(context, bundle, AppLifecycleFacadeHolder.get(), new AppLaunchHelper(), new JsIOHelper());
    }

    protected PushNotification(Context context, Bundle bundle, AppLifecycleFacade appLifecycleFacade, AppLaunchHelper appLaunchHelper, JsIOHelper JsIOHelper) {
        mContext = context;
        mAppLifecycleFacade = appLifecycleFacade;
        mAppLaunchHelper = appLaunchHelper;
        mJsIOHelper = JsIOHelper;
        mNotificationProps = createProps(bundle);
        mLogger = LoggerWrapper.getInstance(context);
    }

    @Override
    public void onReceived() throws InvalidNotificationException {
        if (!mAppLifecycleFacade.isAppVisible()) {
            mLogger.i("onReceived", "App is not visible, posting notification");
            postNotification(null);
        } else {
            mLogger.i("onReceived", "App is visible, notifying JS");
        }
        notifyReceivedToJS();
    }

    @Override
    public void onOpened(String action) {
        if (action != null) {
            mNotificationProps.setAction(action);
        }
        digestNotification();
    }

    @Override
    public void onOpened() {
        onOpened(null);
    }

    @Override
    public int onPostRequest(Integer notificationId) {
        return postNotification(notificationId);
    }

    @Override
    public PushNotificationProps asProps() {
        return mNotificationProps.copy();
    }

    protected int postNotification(Integer notificationId) {
        int id = notificationId != null ? notificationId : createNotificationId();
        final PendingIntent pendingIntent = getCTAPendingIntent(id);
        final Notification notification = buildNotification(pendingIntent);
        postNotification(id, notification);
        return id;
    }

    protected void digestNotification() {
        if (!mAppLifecycleFacade.isReactInitialized()) {
            setAsInitialNotification();
            launchOrResumeApp();
            return;
        }

        final ReactContext reactContext = mAppLifecycleFacade.getRunningReactContext();
        if (reactContext.getCurrentActivity() == null) {
            setAsInitialNotification();
        }

        if (mAppLifecycleFacade.isAppVisible()) {
            dispatchImmediately();
        } else if (mAppLifecycleFacade.isAppDestroyed()) {
            launchOrResumeApp();
        } else {
            dispatchUponVisibility();
        }
    }

    protected PushNotificationProps createProps(Bundle bundle) {
        return new PushNotificationProps(bundle);
    }

    protected void setAsInitialNotification() {
        InitialNotificationHolder.getInstance().set(mNotificationProps);
    }

    protected void dispatchImmediately() {
        notifyOpenedToJS();
    }

    protected void dispatchUponVisibility() {
        mAppLifecycleFacade.addVisibilityListener(getIntermediateAppVisibilityListener());

        // Make the app visible so that we'll dispatch the notification opening when visibility changes to 'true' (see
        // above listener registration).
        launchOrResumeApp();
    }

    protected AppVisibilityListener getIntermediateAppVisibilityListener() {
        return mAppVisibilityListener;
    }

    protected PendingIntent getCTAPendingIntent(int notificationId) {
        final Intent cta = new Intent(mContext, ProxyService.class);
        mNotificationProps.setId(notificationId);
        return NotificationIntentAdapter.createPendingNotificationIntent(mContext, cta, mNotificationProps);
    }

    protected Notification buildNotification(PendingIntent intent) {
        return getNotificationBuilder(intent).build();
    }

    protected Notification.Builder getNotificationBuilder(PendingIntent intent) {

        String CHANNEL_ID = "channel_01";
        String CHANNEL_NAME = "Channel Name";

        String title = mNotificationProps.getTitle("title");
        if (title == null) {
            title = mContext.getString(R.string.fcm_default_title);
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final Notification.Builder notification = new Notification.Builder(mContext)
                .setContentTitle(title)
                .setContentText(mNotificationProps.getBody("service"))
                .setContentIntent(intent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(soundUri)
                .setAutoCancel(true);


        setActions(notification);

        setUpIcon(notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String rawTimeoutTs = mNotificationProps.asBundle().getString("expired_time");
            long expiredTime = getExpiredTime(rawTimeoutTs);
            if (expiredTime > 0) {
                notification.setTimeoutAfter(expiredTime);
            }

            final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = mContext.getString(R.string.channel_id);
            NotificationChannel foundChannel = notificationManager.getNotificationChannel(channelId);
            if (foundChannel == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
                channelId = CHANNEL_ID;
            }
            notification.setChannelId(channelId);
        }

        return notification;
    }

    private void setUpIcon(Notification.Builder notification) {
        int iconResId = getAppResourceId("ic_notification", "mipmap");
        if (iconResId != 0) {
            notification.setSmallIcon(iconResId);
        } else {
            notification.setSmallIcon(mContext.getApplicationInfo().icon);
        }

        setUpIconColor(notification);
    }

    private void setUpIconColor(Notification.Builder notification) {
        int colorResID = getAppResourceId("colorAccent", "color");
        if (colorResID != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = mContext.getResources().getColor(colorResID);
            notification.setColor(color);
        }
    }

    protected void postNotification(int id, Notification notification) {
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    protected void cancelNotification(int id) {
        final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    protected int createNotificationId() {
        return (int) System.nanoTime();
    }

    private void notifyReceivedToJS() {
        mJsIOHelper.sendEventToJS(NOTIFICATION_RECEIVED_EVENT_NAME, mNotificationProps.asBundle(), mAppLifecycleFacade.getRunningReactContext());
    }

    private void notifyOpenedToJS() {
        Bundle response = new Bundle();
        response.putBundle("notification", mNotificationProps.asBundle());

        mJsIOHelper.sendEventToJS(NOTIFICATION_OPENED_EVENT_NAME, response, mAppLifecycleFacade.getRunningReactContext());

        cancelNotification(mNotificationProps.getId());
    }

    protected void launchOrResumeApp() {
        final Intent intent = mAppLaunchHelper.getLaunchIntent(mContext);
        mContext.startActivity(intent);
    }

    private int getAppResourceId(String resName, String resType) {
        return mContext.getResources().getIdentifier(resName, resType, mContext.getPackageName());
    }

    private long getExpiredTime(String expiredTimeTs) {
        long delayMillisToExpiredTime = 0;
        if (expiredTimeTs != null) {
            long currentTimeMillis = System.currentTimeMillis();
            long timeoutTs = Long.parseLong(expiredTimeTs);
            if (timeoutTs == 0) {
                timeoutTs = currentTimeMillis + 10 * 1000;
            }
            delayMillisToExpiredTime = Math.max(timeoutTs - currentTimeMillis, 0);
        }
        return delayMillisToExpiredTime;
    }

    private void setActions(Notification.Builder notification) {
        String actionsJson = mContext.getString(R.string.fcm_actions);
        JSONArray actionsArray = null;
        try {
            actionsArray = new JSONArray(actionsJson);
        } catch (JSONException e) {
            Log.e("PushNotification", "Exception while converting actions to JSON object.", e);
        }

        if (actionsArray != null) {
            for (int i = 0; i < actionsArray.length(); i++) {
                String actionName;
                String actionColor;
                try {
                    JSONObject actionObject = actionsArray.getJSONObject(i);
                    actionName = actionObject.getString("name");
                    actionColor = actionObject.getString("color");
                } catch (JSONException e) {
                    Log.e("PushNotification", "Exception while getting action from actionsArray.", e);
                    continue;
                }

                Intent actionIntent = new Intent(mContext, NotificationBackgroundService.class);
                actionIntent.setAction(NotificationBackgroundService.NOTIFICATION_ACTION_CLICK);
                PendingIntent pendingActionIntent = NotificationIntentAdapter.createPendingNotificationIntent(mContext, actionIntent, mNotificationProps, actionName);

                Spanned actionStyle = HtmlCompat.fromHtml(
                        "<font color=\"" + Color.parseColor(actionColor) + "\">" + actionName,
                        HtmlCompat.FROM_HTML_MODE_LEGACY);

                notification.addAction(0, actionStyle, pendingActionIntent);
            }
        }
    }

}

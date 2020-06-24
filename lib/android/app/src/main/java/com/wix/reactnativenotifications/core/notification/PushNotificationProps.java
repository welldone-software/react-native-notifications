package com.wix.reactnativenotifications.core.notification;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class PushNotificationProps {

    private final static String ACTION = "action";
    protected Bundle mBundle;

    public PushNotificationProps(Bundle bundle) {
        mBundle = bundle;
    }

    public String getTitle(String defaultTitle) {
        return getBundleStringFirstNotNull("gcm.notification.title", defaultTitle);
    }

    public String getBody(String defaultBody) {
        return getBundleStringFirstNotNull("gcm.notification.body", defaultBody);
    }

    public void setAction(String action) {
        mBundle.putString(ACTION, action);
    }

    public Bundle asBundle() {
        return (Bundle) mBundle.clone();
    }

    public boolean isFirebaseBackgroundPayload() {
        return mBundle.containsKey("google.message_id");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(1024);
        for (String key : mBundle.keySet()) {
            sb.append(key).append("=").append(mBundle.get(key)).append(", ");
        }
        return sb.toString();
    }

    protected PushNotificationProps copy() {
        return new PushNotificationProps((Bundle) mBundle.clone());
    }

    private String getBundleStringFirstNotNull(String key1, String key2) {
        String result = mBundle.getString(key1);
        return result == null ? mBundle.getString(key2) : result;
    }
}

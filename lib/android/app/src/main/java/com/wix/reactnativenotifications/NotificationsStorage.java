package com.wix.reactnativenotifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.wix.reactnativenotifications.core.notification.PushNotificationProps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class NotificationsStorage {

    private static NotificationsStorage mInstance;

    private SharedPreferences mPreferences;
    private static final String PREFERENCES_NAME = "react-native";
    private static final String NOTIFICATIONS = "notifications";
    private static final String DEFAULT_VALUE = "{}";
    public static final String MFA_REQUEST_ID = "mfa_request_id";

    private NotificationsStorage(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static NotificationsStorage getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotificationsStorage(context);
        }
        return mInstance;
    }

    private void saveNotifications(JSONObject notificationsJson) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(NOTIFICATIONS, notificationsJson.toString());
        editor.apply();
    }

    private JSONObject getNotificationJson(PushNotificationProps notificationProps) throws JSONException {
        Bundle bundle = notificationProps.asBundle();
        JSONObject json = new JSONObject();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof String) {
                json.put(key, (String) value);
            } else if (value instanceof Integer) {
                json.put(key, (int) value);
            } else if (value instanceof Long) {
                json.put(key, (long) value);
            } else if (value instanceof Double) {
                json.put(key, (double) value);
            }
        }
        return json;
    }

    public void saveNotification(PushNotificationProps notificationProps) {
        String rawJson = mPreferences.getString(NOTIFICATIONS, DEFAULT_VALUE);
        try {
            JSONObject notificationsJson = new JSONObject(rawJson);
            JSONObject notificationJson = getNotificationJson(notificationProps);
            String mfaRequestId = notificationProps.asBundle().getString(MFA_REQUEST_ID);
            notificationsJson.put(mfaRequestId, notificationJson);
            saveNotifications(notificationsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int removeNotification(String mfaRequestId) {
        String rawJson = mPreferences.getString(NOTIFICATIONS, DEFAULT_VALUE);
        int id = -1;
        try {
            JSONObject notificationsJson = new JSONObject(rawJson);
            JSONObject notificationJson = notificationsJson.getJSONObject(mfaRequestId);
            id = Integer.parseInt(notificationJson.getString(PushNotificationProps.ID));
            notificationsJson.remove(mfaRequestId);
            saveNotifications(notificationsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public int getNotificationId(String mfaRequestId) {
        String rawJson = mPreferences.getString(NOTIFICATIONS, DEFAULT_VALUE);
        try {
            JSONObject notificationsJson = new JSONObject(rawJson);
            JSONObject notificationJson = notificationsJson.getJSONObject(mfaRequestId);
            return Integer.parseInt(notificationJson.getString(PushNotificationProps.ID));
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getDeliveredNotifications() throws JSONException {
        JSONArray notificationsArrayJson = new JSONArray();
        String rawJson = mPreferences.getString(NOTIFICATIONS, DEFAULT_VALUE);
        JSONObject notificationsJson = new JSONObject(rawJson);
        Iterator<String> keys = notificationsJson.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            JSONObject notificationJson = notificationsJson.getJSONObject(key);
            notificationsArrayJson.put(notificationJson);
        }
        return notificationsArrayJson.toString();
    }

    public void clearAll() {
        saveNotifications(new JSONObject());
    }

}

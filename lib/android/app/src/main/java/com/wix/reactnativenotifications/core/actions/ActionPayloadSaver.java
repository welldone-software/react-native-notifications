package com.wix.reactnativenotifications.core.actions;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ActionPayloadSaver {

    private static ActionPayloadSaver mInstance;

    private SharedPreferences mPreferences;
    private static final String PUSH_NOTIFICATION_EXTRA = "pushNotification";
    private static final String EXPIRED_TIME_EXTRA = "expired_time";
    private static final String ACTION_EXTRA = "action";
    private static final String PREFERENCES_NAME = "react-native";
    private static final String AWAITING_ACTION = "awaiting_action";
    private static final String DEFAULT_VALUE = "{}";

    private ActionPayloadSaver(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static ActionPayloadSaver getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ActionPayloadSaver(context);
        }
        return mInstance;
    }

    private JSONObject getNotificationJson(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();

        Bundle bundlePayload = bundle.getBundle(PUSH_NOTIFICATION_EXTRA);
        if (bundlePayload != null) {
            JSONObject jsonPayload = new JSONObject();
            for (String key : bundlePayload.keySet()) {
                Object value = bundlePayload.get(key);
                if (value instanceof String) {
                    jsonPayload.put(key, value);
                } else if (value instanceof Integer) {
                    jsonPayload.put(key, (int) value);
                } else if (value instanceof Long) {
                    jsonPayload.put(key, (long) value);
                } else if (value instanceof Double) {
                    jsonPayload.put(key, (double) value);
                }
            }
            json.put(PUSH_NOTIFICATION_EXTRA, jsonPayload);
            json.put(ACTION_EXTRA, bundle.getString(ACTION_EXTRA));
        }

        return json;
    }

    public void saveAwaitingAction(Bundle bundle) {
        try {
            JSONObject notificationJson = getNotificationJson(bundle);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(AWAITING_ACTION, notificationJson.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearAwaitingAction() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(AWAITING_ACTION, DEFAULT_VALUE);
        editor.apply();
    }

    public Bundle getAwaitingAction() {
        String rawJson = mPreferences.getString(AWAITING_ACTION, DEFAULT_VALUE);
        try {
            JSONObject notificationJson = new JSONObject(rawJson);
            Bundle bundle = new Bundle();
            bundle.putString(ACTION_EXTRA, notificationJson.getString(ACTION_EXTRA));

            JSONObject jsonPayload = notificationJson.getJSONObject(PUSH_NOTIFICATION_EXTRA);
            Bundle payload = new Bundle();
            for (Iterator<String> it = jsonPayload.keys(); it.hasNext(); ) {
                String key = it.next();
                Object value = jsonPayload.get(key);
                if (value instanceof String) {
                    payload.putString(key, (String) value);
                } else if (value instanceof Integer) {
                    payload.putInt(key, (int) value);
                } else if (value instanceof Long) {
                    payload.putLong(key, (long) value);
                } else if (value instanceof Double) {
                    payload.putDouble(key, (double) value);
                }
            }
            bundle.putBundle(PUSH_NOTIFICATION_EXTRA, payload);
            return bundle;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getTimeLeft() {
        Bundle notification = getAwaitingAction();
        if (notification == null) {
            return 0;
        }
        Bundle payload = notification.getBundle(PUSH_NOTIFICATION_EXTRA);
        if (payload == null) {
            return 0;
        }
        String rawExpiredTime = payload.getString(EXPIRED_TIME_EXTRA);
        if (rawExpiredTime == null) {
            return 0;
        }
        long expiredTime = Long.parseLong(rawExpiredTime);
        long timeLeft = expiredTime - System.currentTimeMillis();
        return timeLeft > 0 ? timeLeft : 0;
    }

}

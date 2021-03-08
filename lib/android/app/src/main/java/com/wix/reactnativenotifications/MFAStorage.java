package com.wix.reactnativenotifications;

import android.content.Context;
import android.content.SharedPreferences;

import com.facebook.react.bridge.ReadableArray;
import com.wix.reactnativenotifications.core.notification.PushNotificationProps;
import com.wix.reactnativenotifications.core.notificationdrawer.IPushNotificationsDrawer;
import com.wix.reactnativenotifications.core.notificationdrawer.PushNotificationsDrawer;
import com.wix.reactnativenotifications.utils.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MFAStorage {

    private static MFAStorage mInstance;

    private static final String PREFERENCES_NAME = "react-native";
    private static final String NOTIFICATIONS = "notifications";
    private static final String DEFAULT_VALUE = "{}";
    private static final String ANSWER_KEY = "answer";
    private static final String EXPIRED_TIME_KEY = "expired_time";
    private static final int MFA_SAVE_LIMIT = 200;

    public static final String REQUEST_ID_KEY = "mfa_request_id";


    private final SharedPreferences mPreferences;
    private final IPushNotificationsDrawer mNotificationsDrawer;

    private MFAStorage(Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        mNotificationsDrawer = PushNotificationsDrawer.get(context.getApplicationContext());
    }

    public static MFAStorage getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MFAStorage(context);
        }
        return mInstance;
    }

    private void saveNotifications(JSONObject mfasJson) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(NOTIFICATIONS, mfasJson.toString());
        editor.apply();
    }

    private JSONObject getPendingMFAsJson() throws JSONException {
        String rawJson = mPreferences.getString(NOTIFICATIONS, DEFAULT_VALUE);
        return new JSONObject(rawJson);
    }

    private JSONObject clearOverLimit (JSONObject json) {
        int savedCount = json.length();
        if (savedCount > MFA_SAVE_LIMIT) {
            int amountToDelete = savedCount - MFA_SAVE_LIMIT;
            int amountDeleted = 0;
            Iterator<String> keys = json.keys();
            while(keys.hasNext()) {
                json.remove(keys.next());
                amountDeleted++;
                if (amountDeleted >= amountToDelete) {
                    break;
                }
            }
        }
        return json;
    }

    public void dismissNotification(JSONObject json) throws JSONException {
        dismissNotification(json.getString(REQUEST_ID_KEY));
    }

    public void dismissNotification(String requestId) {
        mNotificationsDrawer.onNotificationClearRequest(getNotificationId(requestId));
    }

    public void saveMFA(PushNotificationProps notificationProps) {
        try {
            JSONObject mfasJson = getPendingMFAsJson();
            String mfaRequestId = notificationProps.asBundle().getString(REQUEST_ID_KEY);
            if (!mfasJson.has(mfaRequestId)) {
                JSONObject mfaJson = JsonConverter.convertBundleToJson(notificationProps.asBundle());
                mfasJson.put(mfaRequestId, mfaJson);
                saveNotifications(clearOverLimit(mfasJson));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveMFAs(ReadableArray mfaObjects) throws JSONException {
        JSONObject mfasJson = getPendingMFAsJson();
        JSONArray mfasToAddJson = JsonConverter.convertArrayToJson(mfaObjects);
        for (int i = 0; i < mfasToAddJson.length(); i++) {
            JSONObject mfaToAdd = mfasToAddJson.getJSONObject(i);
            if (mfaToAdd == null) {
                continue;
            }
            String requestId = mfaToAdd.getString(REQUEST_ID_KEY);
            if (!mfasJson.has(requestId)) {
                mfasJson.put(requestId, mfaToAdd);
            }
        }
        saveNotifications(clearOverLimit(mfasJson));
    }

    public void updateMFA(String mfaRequestId, boolean answer) {
        try {
            JSONObject mfasJson = getPendingMFAsJson();
            JSONObject mfaJson = mfasJson.getJSONObject(mfaRequestId);
            mfaJson.put(ANSWER_KEY, answer);
            mfasJson.put(mfaRequestId, mfaJson);
            saveNotifications(clearOverLimit(mfasJson));
            dismissNotification(mfaJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getNotificationId(String mfaRequestId) {
        try {
            JSONObject mfasJson = getPendingMFAsJson();
            JSONObject mfaJson = mfasJson.getJSONObject(mfaRequestId);
            return Integer.parseInt(mfaJson.getString(PushNotificationProps.ID));
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getPendingMFAs() throws JSONException {
        JSONArray notificationsArrayJson = new JSONArray();
        JSONObject mfasJson = getPendingMFAsJson();
        Iterator<String> keys = mfasJson.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            JSONObject mfaJson = mfasJson.getJSONObject(key);
            if (mfaJson == null) {
                continue;
            }

            boolean hasNotAnswered = !mfaJson.has(ANSWER_KEY);
            boolean isRelevant = mfaJson.getLong(EXPIRED_TIME_KEY) < System.currentTimeMillis();
            if (hasNotAnswered && isRelevant) {
                notificationsArrayJson.put(mfaJson);
            } else {
                dismissNotification(mfaJson);
            }
        }
        return notificationsArrayJson.toString();
    }

    public void clearAll() {
        saveNotifications(new JSONObject());
    }

}

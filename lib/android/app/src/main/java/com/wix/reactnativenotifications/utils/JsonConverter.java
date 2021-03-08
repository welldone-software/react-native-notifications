package com.wix.reactnativenotifications.utils;

import android.os.Bundle;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonConverter {

    public static JSONObject convertBundleToJson(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value instanceof String) {
                json.put(key, value);
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

    public static JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    ReadableMap mapValue = readableMap.getMap(key);
                    if (mapValue != null) {
                        object.put(key, convertMapToJson(mapValue));
                    }
                    break;
                case Array:
                    ReadableArray arrayValue = readableMap.getArray(key);
                    if (arrayValue != null) {
                        object.put(key, convertArrayToJson(arrayValue));
                    }
                    break;
            }
        }
        return object;
    }

    public static JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    ReadableMap mapValue = readableArray.getMap(i);
                    if (mapValue != null) {
                        array.put(convertMapToJson(mapValue));
                    }
                    break;
                case Array:
                    ReadableArray arrayValue = readableArray.getArray(i);
                    if (arrayValue != null) {
                        array.put(convertArrayToJson(arrayValue));
                    }
                    break;
            }
        }
        return array;
    }
}

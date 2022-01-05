package com.sampleapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.ParameterizedType;

public class Prefs {
    public static final ValueWrapper<String> APP_ID = new ValueWrapper<String>("APP_ID", "") {
    };
    public static final ValueWrapper<String> ENCODED_CREDENTIALS = new ValueWrapper<String>("ENCODED_CREDENTIALS", "") {
    };
    public static final ValueWrapper<String> TOKEN = new ValueWrapper<String>("TOKEN", "<default>") {
    };
    public static final ValueWrapper<String> SOFT_AP_SSID = new ValueWrapper<String>("SOFT_AP_SSID", "") {
    };
    public static final ValueWrapper<String> CA_BLE_PREFIX = new ValueWrapper<String>("CA_BLE_PREFIX", "") {
    };
    public static final ValueWrapper<String> PRIVATE_SSID = new ValueWrapper<String>("PRIVATE_SSID", "") {
    };
    public static final ValueWrapper<Integer> WIFI_NETWORK_ID = new ValueWrapper<Integer>("WIFI_NETWORK_ID", -1) {
    };
    public static final ValueWrapper<String> ACCOUNT_ID = new ValueWrapper<String>("ACCOUNT_ID", "") {
    };
    public static final ValueWrapper<Boolean> IS_SOFTAP_ONBOARDING_ACTIVATED = new ValueWrapper<Boolean>("IS_SOFTAP_ONBOARDING_ACTIVATED", false) {
    };
    public static final ValueWrapper<String> ONBOARDING_HISTORY = new ValueWrapper<String>("ONBOARDING_HISTORY", "") {
    };

    public static abstract class ValueWrapper<T> {
        private final T defaultValue;
        private final String name;
        private final Class type;

        public ValueWrapper(String name) {
            this(name, null);
        }

        public ValueWrapper(String name, T defaultValue) {
            this.defaultValue = defaultValue;
            this.name = name;
            this.type = getType();
        }

        public T getValue() {
            SharedPreferences sharedPreferences = getSharedPreferences();
            if (!sharedPreferences.contains(name)) {
                return defaultValue;
            }
            Object result = defaultValue;
            if (Integer.class.isAssignableFrom(type)) {
                result = sharedPreferences.getInt(name, (Integer) defaultValue);
            } else if (String.class.isAssignableFrom(type)) {
                result = sharedPreferences.getString(name, (String) defaultValue);
            } else if (Long.class.isAssignableFrom(type)) {
                result = sharedPreferences.getLong(name, (Long) defaultValue);
            } else if (Boolean.class.isAssignableFrom(type)) {
                result = sharedPreferences.getBoolean(name, (Boolean) defaultValue);
            } else if (Float.class.isAssignableFrom(type)) {
                result = sharedPreferences.getFloat(name, (Float) defaultValue);
            }
            return (T) result;
        }

        public void remove() {
            SharedPreferences.Editor edit = getSharedPreferences().edit();
            edit.remove(name);
            edit.apply();
        }

        public boolean exists() {
            return getSharedPreferences().contains(name);
        }

        public void setValue(T value) {
            SharedPreferences.Editor edit = getSharedPreferences().edit();
            if (Integer.class.isAssignableFrom(type)) {
                edit.putInt(name, (Integer) value);
            } else if (String.class.isAssignableFrom(type)) {
                edit.putString(name, (String) value);
            } else if (Long.class.isAssignableFrom(type)) {
                edit.putLong(name, (Long) value);
            } else if (Boolean.class.isAssignableFrom(type)) {
                edit.putBoolean(name, (Boolean) value);
            } else if (Float.class.isAssignableFrom(type)) {
                edit.putFloat(name, (Float) value);
            }
            edit.apply();
        }

        private SharedPreferences getSharedPreferences() {
            final String appName = "Cirrent";
            Context appContext = CirrentApplication.getAppContext();
            return appContext.getSharedPreferences(appName, Context.MODE_PRIVATE);
        }

        private Class<?> getType() {
            return (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }
    }
}

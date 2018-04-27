package com.devbatch.ecommerce.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.devbatch.ecommerce.BuildConfig;

import static com.devbatch.ecommerce.Application.context;


public final class SharedPrefUtil {
    private static final String PREF_NAME = BuildConfig.APPLICATION_ID + "_pref";

    public static boolean setString(String key, String data) {
        SharedPreferences sp = getSharedPreferences(context());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, data);
        return editor.commit();
    }

    public static boolean setInt(String key, int data) {
        SharedPreferences sp = getSharedPreferences(context());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key, data);
        return editor.commit();
    }


    public static boolean setLong(String key, long data) {
        SharedPreferences sp = getSharedPreferences(context());
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key, data);
        return editor.commit();
    }

    public static boolean setBoolean(String key, boolean data) {
        SharedPreferences sp = getSharedPreferences(context());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, data);
        return editor.commit();
    }

    /**
     * @return Returns String or null, default value is null.
     */
    public static String getString(String key) {
        SharedPreferences sp = getSharedPreferences(context());
        return sp.getString(key, null);
    }

    /**
     * @return Return an integer value, default value is 0..
     */
    public static int getInt(String key) {
        SharedPreferences sp = getSharedPreferences(context());
        return sp.getInt(key, 0);
    }

    /**
     * @return Return long value, default value is 0..
     */
    public static long getLong(Context context, String key) {
        SharedPreferences sp = getSharedPreferences(context());
        return sp.getLong(key, 0);
    }

    /**
     * @return return boolean value, default value is false.
     */
    public static boolean getBoolean(String key) {
        SharedPreferences sp = getSharedPreferences(context());
        return sp.getBoolean(key, false);
    }

    public static SharedPreferences getSharedPreferences(Context context) {

        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}

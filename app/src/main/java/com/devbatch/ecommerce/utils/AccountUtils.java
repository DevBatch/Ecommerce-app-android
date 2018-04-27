package com.devbatch.ecommerce.utils;

import android.content.Context;
import android.preference.PreferenceManager;


import com.devbatch.ecommerce.communication.network.rersponse.User;


import static com.devbatch.ecommerce.Application.context;


public class AccountUtils {
    private static final String EMAIL = "email";
    private static final String ID = "_id";
    private static final String USER = "_user";
    private static final String API_TOKEN = "api_token";
    private static final String USER_PASS = "user_pass";
    private static final String USER_EMAIL = "user_emil";
    private static final String USER_PHONE = "user_phone";


    public static User getUser() {
        String jSon = getString(context(), USER);
        if (jSon == null) {
            return new User();
        }
        return GsonUtils.fromJson(jSon, User.class);
    }

    public static void logOut() {
        LoginManager.setLoginFinish(false);
        LoginManager.setFirstFlowFinish(false);
        AccountUtils.setUserId(0);
        AccountUtils.setApiToke(null);
        AccountUtils.setUser(null);
        AccountUtils.setEmail(null);
        AccountUtils.setPhone(null);

    }

    public static void setUser(User user) {
        String jSon = GsonUtils.getGson().toJson(user);
        persist(context(), USER, jSon);
        setUserId(user.iD);
        setEmail(user.emailAddress);
    }

    private static void persist(Context context, String key, String toPersist) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, toPersist).apply();
    }

    public static void setEmail(String email) {
        persist(context(), USER_EMAIL, email);
    }

    public static String getEmail() {
        return getString(context(), USER_EMAIL);
    }

    public static void setPhone(String email) {
        persist(context(), USER_PHONE, email);
    }

    public static String getPhone() {
        return getString(context(), USER_PHONE);
    }

    private static String getString(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }

    public static void setUserId(long id) {
        PreferenceManager.getDefaultSharedPreferences(context()).edit().putLong(ID, id).apply();
    }

    public static void setApiToke(String apiToken) {
        persist(context(), API_TOKEN, apiToken);
    }

    public static void setUserPass(String apiToken) {
        persist(context(), USER_PASS, apiToken);
    }

    public static String getApiToke() {
        return getString(context(), API_TOKEN);
    }

    public static String getUserPass() {
        return getString(context(), USER_PASS);
    }

    public static long getUserId() {
        return PreferenceManager.getDefaultSharedPreferences(context()).getLong(ID, 0);
    }

    public static boolean isLoggedIn() {
        if (context() == null) {
            return false;
        }
        return AccountUtils.getUser() != null;
    }
}

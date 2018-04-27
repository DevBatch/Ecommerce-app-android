package com.devbatch.ecommerce.utils;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.devbatch.ecommerce.FrameActivity;
import com.devbatch.ecommerce.fragment.LoginFragment;
import com.devbatch.ecommerce.fragment.PhoneVerificationFragment;
import com.devbatch.ecommerce.fragment.RegisterFragment;

import static com.devbatch.ecommerce.utils.CommonKeys.FRAGMENT_CLASS;
import static com.devbatch.ecommerce.utils.CommonKeys.SHOW_TOOLBAR;

public final class LoginManager {
    private static final String NEXT_FRAGMENT = "_next_fragment";
    private static final String LOGIN_FLOW_FINISHED = "_login_flow_finished";
    private static final String FIRST_FLOW_FINISH = "_first_flow_finish";
    private static final String SKIP = "skip";

    public static boolean isLoginFinish() {
        return SharedPrefUtil.getBoolean(LOGIN_FLOW_FINISHED);
    }


    public static boolean setLoginFinish(boolean value) {
        return SharedPrefUtil.setBoolean(LOGIN_FLOW_FINISHED, value);
    }

    public static boolean isFirstFlowFinish() {
        return SharedPrefUtil.getBoolean(FIRST_FLOW_FINISH);
    }

    public static boolean setFirstFlowFinish(boolean value) {
        return SharedPrefUtil.setBoolean(FIRST_FLOW_FINISH, value);
    }

    public static boolean setSkip(boolean value) {
        return SharedPrefUtil.setBoolean(SKIP, value);
    }

    public static boolean isSkip() {
        return SharedPrefUtil.getBoolean(SKIP);
    }


    public static boolean setNextFragment(String fragmentName) {
        return SharedPrefUtil.setString(NEXT_FRAGMENT, fragmentName);
    }

    public static void launchLoginFragment(Context context) {
        String nextFragment = SharedPrefUtil.getString(NEXT_FRAGMENT);
        if (nextFragment == null) {
            nextFragment = PhoneVerificationFragment.class.getName();
        }
        launchFragment(context, nextFragment);
    }

//
    private static void launchFragment(Context context, String fragmentName) {
        Log.d("NextFragment", fragmentName);
        Intent intent = new Intent(context, FrameActivity.class);
        intent.putExtra(FRAGMENT_CLASS, fragmentName);
        intent.putExtra(SHOW_TOOLBAR, false);
        context.startActivity(intent);
    }
}

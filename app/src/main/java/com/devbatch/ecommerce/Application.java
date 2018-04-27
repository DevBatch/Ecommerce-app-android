package com.devbatch.ecommerce;


import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class Application extends android.app.Application {
    private static final String TAG = Application.class.getSimpleName();

    private static Application sContext;

    public Application() {
        sContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.ENABLE_CRASH_REPORTING) {
            Fabric.with(this, new Crashlytics());
        }
        sContext = this;

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //MultiDex.install(this);
    }

    public static AssetManager assets() {
        return sContext.getResources().getAssets();
    }

    public static Application get() {
        return sContext;
    }

    public static Context context() {
        return sContext;
    }

    public static void toast(int resId) {
        toast(string(resId));
    }

    public static void toast(String message) {
        if (message != null)
            toast(message, Toast.LENGTH_SHORT);
    }

    public static void toast(String message, int length) {
        Toast t = Toast.makeText(context(), message, length);
        // t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    public static void snakeBar(View view, int resId) {
        Snackbar snackbar = Snackbar.make(view, string(resId), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public static void snakeBar(View view, String message) {
        if (view != null) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    public static Resources resources() {
        return context().getResources();
    }

    public static String packageName() {
        return context().getPackageName();
    }


    @StringRes
    public static String string(int resId) {
        return resources().getString(resId);
    }

    @StringRes
    public static String string(int resId, Object... args) {
        return resources().getString(resId, args);
    }

    @ArrayRes
    public static String[] stringArray(int resId) {
        return resources().getStringArray(resId);
    }

    @DrawableRes
    public static Drawable drawable(int resId) {
        return ResourcesCompat.getDrawable(resources(), resId, context().getTheme());

    }


    @ColorRes
    public static int color(int resId) {
        return resources().getColor(resId);
    }

    @DimenRes
    public static int dimen(int resId) {
        return resources().getDimensionPixelSize(resId);
    }


//    private void initlizeParsePush() {
//        Parse.initialize(this, ParseUtils.PARSE_APP_ID, ParseUtils.PARSE_CLIENT_KEY);
//        PushService.startServiceIfRequired(this);
//        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
//        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e != null) {
//                    Log.d(TAG, " successfully subscribed to the broadcast channel.");
//                } else {
//                    Log.d(TAG, " failed to subscribe for push");
////                    try {
////                        throw new Exception(TAG + " Failed to subscribe for parse push notification!!");
////                    } catch (Exception exp) {
////                        LogUtils.LOGD("Error: ", exp.getMessage());
////                        Crashlytics.logException(exp);
////
////                    }
//                }
//            }
//        });
//    }

    /**
     * A tree which logs important information for crash reporting.
     */
//    private static class CrashReportingTree extends Timber.Tree {
//        @Override
//        protected void log(int priority, String tag, String message, Throwable t) {
//            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
//                return;
//            }
//
//            if (t != null) {
//                if (priority == Log.ERROR || priority == Log.WARN) {
//                    //ExceptionHandler.saveException(t, null);
//                }
//            }
//        }
//    }
}

package com.devbatch.ecommerce.utils;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.IntentCompat;

import com.devbatch.ecommerce.FrameActivity;
import com.devbatch.ecommerce.fragment.LoginFragment;
import com.devbatch.ecommerce.fragment.RegisterFragment;

import static com.devbatch.ecommerce.utils.CommonKeys.FRAGMENT_CLASS;
import static com.devbatch.ecommerce.utils.CommonKeys.SHOW_TOOLBAR;
import static com.devbatch.ecommerce.utils.CommonKeys.THEME;


public class IntentBuilder {
    public static Intent buildIntentWithFragment(Context context, String fragmentName) {
        Intent intent = new Intent(context, FrameActivity.class);
        intent.putExtra(FRAGMENT_CLASS, fragmentName);
        return intent;
    }

    public static Intent buildIntentWithFragment(Context context, String fragmentName, boolean showToolbar) {
        Intent intent = new Intent(context, FrameActivity.class);
        intent.putExtra(FRAGMENT_CLASS, fragmentName);
        intent.putExtra(SHOW_TOOLBAR, showToolbar);
        return intent;
    }

    public static Intent buildIntentWithFragment(Context context, String fragmentName, boolean showToolbar, int themeResId) {
        Intent intent = new Intent(context, FrameActivity.class);
        intent.putExtra(FRAGMENT_CLASS, fragmentName);
        intent.putExtra(SHOW_TOOLBAR, showToolbar);
        intent.putExtra(THEME, themeResId);
        return intent;
    }


    public static Intent buildRegisterFragmentFragmentIntent(Context context) {
        Intent intent = new Intent(context, FrameActivity.class);
        intent.putExtra(FRAGMENT_CLASS, RegisterFragment.class.getName());
        intent.putExtra(CommonKeys.SHOW_TOOLBAR, false);
        return intent;
    }

    public static Intent buildLoginFragmentFragmentIntent(Context context) {
        Intent intent = new Intent(context, FrameActivity.class);
        intent.putExtra(FRAGMENT_CLASS, LoginFragment.class.getName());
        intent.putExtra(CommonKeys.SHOW_TOOLBAR, false);
        return intent;
    }

    public static Intent buildRestartAppIntent(Context context) {
        Intent intent = new Intent(context, Manifest.class);
        ComponentName componentName = intent.getComponent();
        return IntentCompat.makeRestartActivityTask(componentName);
    }


    public static void lunchDialerIntent(Context mContext, String pnoneNo) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + pnoneNo));
        mContext.startActivity(intent);
    }

    public static void lunchGoogleMapApplication(Context context, double lat, double lan, String label) {
        try {
            //String label = label;
            String uriBegin = "geo:" + lat + "," + lan;
            String query = lat + "," + lan + "(" + label + ")";
            String encodedQuery = Uri.encode(query);
            String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
            Uri gmmIntentUri = Uri.parse(uriString);
            //Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lan + "?z=16");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps"));
            context.startActivity(webIntent);
        }
    }

}

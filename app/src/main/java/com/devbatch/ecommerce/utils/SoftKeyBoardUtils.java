package com.devbatch.ecommerce.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class SoftKeyBoardUtils {
    public static void hideSoftInput(View focusedView) {
        if (focusedView == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) focusedView.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showSoftInput(View focusedView) {
        if (focusedView == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) focusedView.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(focusedView, 0);
        }
    }
}
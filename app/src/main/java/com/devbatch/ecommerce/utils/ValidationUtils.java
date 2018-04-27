package com.devbatch.ecommerce.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.widget.EditText;

/**
 * Created by irfan arshad on 4/24/2016.
 */
public class ValidationUtils {
    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidEmail(Editable email) {
        String mail = email.toString().trim();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches();
    }

    public static boolean isValidEmail(EditText email) {
        if (email != null) {
            return isValidEmail(email.getText());
        }
        return false;
    }

    public static boolean isFullName(String str) {
        String expression = "^[a-zA-Z\\s]+";
        return str.matches(expression);
    }

    public static boolean isFullName(EditText editText) {
        if (editText != null) {
            return isFullName(editText.getText().toString().trim());
        }
        return false;

    }

    public static boolean hasText(Editable editable) {
        String text = editable.toString().trim();
        return text != null && text.length() > 0;
    }

    public static boolean hasText(EditText editText) {
        return editText != null && hasText(editText.getText());
    }


    public static boolean hasText(String text) {
        text = text.toString().trim();
        return text != null && text.length() > 0;
    }

    public static boolean isValidPassword(EditText editText) {
        if (editText == null) {
            return false;
        }
        String text = editText.getText().toString().trim();
        return text != null && text.length() >= 6;
    }

    public static boolean isPinAndConfirmPinMatch(String pin,
                                                  String confirmPin) {
        boolean pStatus = false;
        if (confirmPin != null && pin != null) {
            if (pin.equals(confirmPin)) {
                pStatus = true;
            }
        }
        return pStatus;
    }

    public static boolean isValidPin(Editable pin) {
        String pin_no = pin.toString().trim();
        return !TextUtils.isEmpty(pin_no) && pin_no.length() == 5;
    }

    public static boolean isValidPin(EditText pin) {
        String pin_no = pin.getText().toString().trim();
        return !TextUtils.isEmpty(pin_no) && pin_no.length() == 5;
    }
}

package com.devbatch.ecommerce.utils;

import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;

public class StringUtils {


    public static String getString(EditText editText) {
        if (editText != null) {
            return editText.getText().toString().trim();
        }
        return "";
    }

    public static String getString(Editable editable) {
        String text = editable.toString().trim();
        if (text != null && text.length() > 0) {
            return text;
        }
        return "";
    }

    public static String getString(TextView textView) {
        if (textView != null) {
            return textView.getText().toString().trim();
        }
        return "";
    }

    /*
 * checks if string is null or empty
 */
    public static Boolean isEmpty(String data) {
        return data == null || data.equals("") || data.contentEquals("null");
    }


    public static String getFullName(String first_name, String last_name) {
        StringBuilder temp = new StringBuilder("");
        if (first_name != null) {
            temp.append(first_name);
        }
        if (last_name != null) {
            temp.append(" ").append(last_name);
        }
        return temp.toString();
    }

    public static String getFirstName(String fullName) {
        String[] temp = fullName.split(" ");
        return temp.length > 0 ? temp[0].trim() : fullName.trim();
    }

    public static String getLastName(String fullName) {
        String[] temp = fullName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < temp.length; i++) {
            builder.append(temp[i]).append(" ");
        }
        return temp.length > 1 ? builder.toString().trim() : "";
    }

    public static String getLastNameFromFull(String fullName) {
        String[] temp = fullName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < temp.length; i++) {
            builder.append(temp[i]).append(" ");
        }
        return temp.length > 1 ? builder.toString().trim() : null;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

}

package com.devbatch.ecommerce.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import static com.devbatch.ecommerce.Application.context;


/**
 * Created by irfan arshad on 4/24/2016.
 */
public class PhoneNumberUtils {
    public static String getNationalNumber(Editable editable, int code, String codeString) {


        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String tempNo = editable.toString().trim();
        String number = null;
        try {
            Phonenumber.PhoneNumber ph = phoneUtil.parse(tempNo, codeString);
            ph.setCountryCode(code);
            number = "+" + ph.getCountryCode() + "" + ph.getNationalNumber();
            return number;
        } catch (NumberParseException e) {
            LogUtils.LOGD("NumberParseException", e.toString());
        }
        return number;
    }

    public static String getCodeFromPhone(String pNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(pNumber, "");
            int countryCode = numberProto.getCountryCode();
            return "+" + countryCode;
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        return " ";
    }

    public static String getInterNationalNumber(Editable editable, String codeString) {


        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String tempNo = editable.toString().trim();
        String number = null;
        try {
            Phonenumber.PhoneNumber ph = phoneUtil.parse(tempNo, codeString);
            ph.setCountryCode(ph.getCountryCode());
            number = phoneUtil.format(ph, PhoneNumberUtil.PhoneNumberFormat.E164);
            //number = "+" + ph.getCountryCode() + "" + ph.getNationalNumber();
            return number;
        } catch (NumberParseException e) {
            LogUtils.LOGD("NumberParseException", e.toString());
        }
        return number;
    }

    public static String getNationalNumber(String text, int code, String codeString) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String tempNo = text.toString().trim();
        String number = null;
        try {
            Phonenumber.PhoneNumber ph = phoneUtil.parse(tempNo, codeString);
            ph.setCountryCode(code);

            number = "+" + ph.getCountryCode() + "" + ph.getNationalNumber();
            return number;
        } catch (NumberParseException e) {
            LogUtils.LOGD("NumberParseException", e.toString());


        }
        return number;
    }

    public static String getNationalNumber(String number) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber ph = phoneUtil.parse(number, "");
            ph.getNationalNumber();
            return "" + ph.getNationalNumber();
        } catch (NumberParseException e) {
            LogUtils.LOGD("NumberParseException", e.toString());


        }
        return " ";
    }

    public static boolean isValidPhoneNumber(Editable no, String code) {
        if (TextUtils.isEmpty(no)) {
            return false;
        }
        String number = no.toString().trim();
        return isValidPhoneNumber(number, code);
    }


    public static boolean isValidPhoneNumber(String number, String code) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        boolean isValid = false;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumberUtil.PhoneNumberType isMobile = null;
        try {
            Phonenumber.PhoneNumber ph = phoneUtil.parse(number, code);
            ph.setCountryCode(phoneUtil.getCountryCodeForRegion(code));
            isValid = phoneUtil.isValidNumber(ph) && !number.startsWith("0");
            isMobile = phoneUtil.getNumberType(ph);
            return isValid && (PhoneNumberUtil.PhoneNumberType.MOBILE == isMobile ||
                    PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE == isMobile);
        } catch (NumberParseException e) {
            LogUtils.LOGD("NumberParseException", e.toString());
            return isValid;
        }
    }

    public static String parseContact(String contact, String code) {
        Phonenumber.PhoneNumber phoneNumber = null;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String finalNumber = null;
        // String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countrycode));
        boolean isValid = false;
        PhoneNumberUtil.PhoneNumberType isMobile = null;
        try {
            phoneNumber = phoneNumberUtil.parse(contact, code);
            isValid = phoneNumberUtil.isValidNumber(phoneNumber);
            isMobile = phoneNumberUtil.getNumberType(phoneNumber);

        } catch (NumberParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        if (isValid
                && (PhoneNumberUtil.PhoneNumberType.MOBILE == isMobile ||
                PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE == isMobile)) {
            finalNumber = phoneNumberUtil.format(phoneNumber,
                    PhoneNumberUtil.PhoneNumberFormat.E164).substring(1);
        }

        return finalNumber;
    }

    public static String getCurrentCountryCode() {
        TelephonyManager tm = (TelephonyManager) context().getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso().toUpperCase();

    }

    public static int getCountryCodeForRegion(String code) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.getCountryCodeForRegion(code.toUpperCase());

    }

    public static String getCountryCodeForRegion(int code) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.getRegionCodeForCountryCode(code);

    }

}

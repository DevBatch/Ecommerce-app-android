package com.devbatch.ecommerce.utils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceUuidFactory {
    protected static final String PREFS_FILE = "device_id";//dont add ".xml" to the end of the file
    protected static final String PREFS_DEVICE_ID = "device_id";

    public static String getUuid(Context context) {
        String id = SharedPrefUtil.getString(PREFS_DEVICE_ID);
        UUID uuid = null;
        if (id != null) {
            // Use the ids previously computed and stored in the
            // prefs file
            uuid = UUID.fromString(id);
        } else {
            final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

            // Use the Android ID unless it's broken, in which case
            // fallback on deviceId,
            // unless it's not available, then fallback on a random
            // number which we store
            // to a prefs file
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            SharedPrefUtil.setString(PREFS_DEVICE_ID, uuid.toString());

        }
        return uuid.toString();
    }

}

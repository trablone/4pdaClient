package org.softeg.slartus.forpda.classes.common;

import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * User: slinkin
 * Date: 03.10.11
 * Time: 11:05
 */
public class ExtPreferences {
    public static float parseFloat(SharedPreferences prefs, String key, float defValue) {
        String res = prefs.getString(key, Float.toString(defValue));
        if (TextUtils.isEmpty(res)) return defValue;

        return Float.parseFloat(res);
    }

    public static int parseInt(SharedPreferences prefs, String key, int defValue) {
        try {
            String res = prefs.getString(key, Integer.toString(defValue));
            if (TextUtils.isEmpty(res)) return defValue;

            return Integer.parseInt(res);
        } catch (Exception ex) {
            return defValue;
        }

    }
}

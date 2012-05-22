package org.softeg.slartus.forpda.classes.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 29.09.11
 * Time: 20:37
 * To change this template use File | Settings | File Templates.
 */
public class Functions {

    private static SimpleDateFormat parseDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
    private static SimpleDateFormat fullDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public static Boolean isWebviewAllowJavascriptInterface(Context context){
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("system.WebviewAllowJavascriptInterface", true);
//        return !Build.VERSION.RELEASE.startsWith("2.3")|| Build.VERSION.RELEASE.equals("2.3.7")
//                || Build.VERSION.RELEASE.equals("2.3.4");
    }
    
    public static String getToday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getYesterToday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return dateFormat.format(nowCalendar.getTime());
    }

    public static Date parseForumDateTime(String dateTime, String today, String yesterday) {
        try {
            Date res= parseDateTimeFormat.parse(dateTime.toString().replace("Сегодня", today).replace("Вчера", yesterday));
            if(res.getYear()<100)
                res.setYear(2000+res.getYear());
            return res;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getForumDateTime(Date date) {

        if (date == null) return "";
        return parseDateTimeFormat.format(date);
    }

    public static String getFullDateString(Date date) {

        if (date == null) return "";
        return fullDateTimeFormat.format(date);
    }

    public static Date getFullDate(String dateString, Date defaultValue) {

        if (TextUtils.isEmpty(dateString)) return defaultValue;
        try {
            return fullDateTimeFormat.parse(dateString);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    public static Boolean isImageUrl(String url) {
        return Pattern.compile("(png|jpeg|jpg)$").matcher(url).find();
    }

    public static String getSizeText(long bytes) {
        if (bytes < 1000)
            return bytes + "Б";
        if (bytes < 1000 * 1024)
            return String.format("%.1fКб", (float) bytes / 1024);
        if (bytes < 1000 * 1024 * 1024)
            return String.format("%.1fМб", (float) bytes / 1024 / 1024);

        return String.format("%.1fГб", (float) bytes / 1024 / 1024 / 1024);
    }

    public static int getUniqueDateInt() {
        Calendar calendar = new GregorianCalendar();
        // максимум: 2147483647
        // 2.14ч.74м.83с.647мс

        int res = calendar.get(Calendar.HOUR_OF_DAY) * 10000000 +
                calendar.get(Calendar.MINUTE) * 100000 +
                calendar.get(Calendar.SECOND) * 1000 +
                calendar.get(Calendar.MILLISECOND);
        return res;
    }

}

package org.softeg.slartus.forpda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.softeg.slartus.forpda.classes.common.ArrayUtils;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;

/**
 * User: slinkin
 * Date: 05.08.11
 * Time: 8:03
 */
public class MyApp extends android.app.Application {
    public static final int THEME_WHITE = 0;
    public static final int THEME_BLACK = 1;

    public static final int THEME_PLASTICKBLACK_REMIE = 11;
    public static final int THEME_WHITE_REMIE = 2;
    public static final int THEME_BLACK_REMIE = 3;
    public static final int THEME_GREEN_REMIE = 10;
    public static final int THEME_WHITE_BEZIPHONA = 4;
    public static final int THEME_WHITER_REMIE = 5;
    public static final int THEME_GRAY_REMIE = 6;
    public static final int THEME_WHITE_VETALORLOV = 7;

    public static final int THEME_GRAY_BEZIPHONA = 9;
    public static final int THEME_CUSTOM_CSS = 99;

    private final Integer[] WHITE_THEMES = {THEME_WHITE_BEZIPHONA, THEME_WHITE_VETALORLOV, THEME_WHITE_REMIE,
            THEME_WHITER_REMIE, THEME_WHITE, THEME_GREEN_REMIE};

    private static boolean m_IsDebugModeLoaded = false;
    private static boolean m_IsDebugMode = false;

    public static boolean getIsDebugMode() {
        if (!m_IsDebugModeLoaded) {
            m_IsDebugMode = PreferenceManager
                    .getDefaultSharedPreferences(INSTANCE).getBoolean("DebugMode", false);
            m_IsDebugModeLoaded = true;
        }
        return m_IsDebugMode;
    }

    public static void showMainActivityWithoutBack(Activity activity) {
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static MyApp INSTANCE = new MyApp();

    public int getThemeStyleResID() {
        return isWhiteTheme() ? R.style.Theme_White : R.style.Theme_Black;
    }

    public boolean isWhiteTheme() {

        return ArrayUtils.indexOf(getCurrentTheme(), WHITE_THEMES) != -1;
    }

    public int getThemeStyleWebViewBackground() {
        return isWhiteTheme() ? getResources().getColor(R.color.white_theme_webview_background) : Color.BLACK;
    }


    public int getCurrentTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return ExtPreferences.parseInt(preferences, "appstyle", THEME_WHITE);

    }

    public String getCurrentThemeName() {
        return isWhiteTheme() ? "white" : "black";
    }

    public String getThemeCssFileName() {
        String path = "/android_asset/forum/css/";
        String cssFile = "white.css";
        int theme = MyApp.INSTANCE.getCurrentTheme();
        switch (theme) {
            case THEME_WHITE:
                cssFile = "white.css";
                break;
            case THEME_BLACK:
                cssFile = "black.css";
                break;
            case THEME_WHITE_REMIE:
                cssFile = "white_Remie-l.css";
                break;
            case THEME_BLACK_REMIE:
                cssFile = "black_Remie-l.css";
                break;
            case THEME_PLASTICKBLACK_REMIE:
                cssFile = "plasticblack_Remie-l.css";
                break;
            case THEME_WHITE_BEZIPHONA:
                cssFile = "white_beziphona.css";
                break;
            case THEME_WHITER_REMIE:
                cssFile = "whiter_Remie-l.css";
                break;
            case THEME_GRAY_REMIE:
                cssFile = "gray_Remie-l.css";
                break;
            case THEME_GREEN_REMIE:
                cssFile = "green_Remie-l.css";
                break;
            case THEME_WHITE_VETALORLOV:
                cssFile = "white_vetalorlov.css";
                break;

            case THEME_GRAY_BEZIPHONA:
                cssFile = "gray_beziphona.css";
                break;
            case THEME_CUSTOM_CSS:
                return "/mnt/sdcard/style.css";

        }
        return path + cssFile;

    }


    public MyApp() {
        INSTANCE = this;

    }

    public static boolean isBetaVersion(Context context) {
        String packageName = context.getPackageName();

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);
            return pInfo.versionName.contains("beta");
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return false;
    }

    public static boolean isDonateVersion(Context context) {
        String packageName = context.getPackageName();
        return packageName.toLowerCase().equals(
                "org.softeg.slartus.forpda.forpda");

    }

    private static Boolean s_PromoChecked = false;

    public void showPromo(final SherlockFragmentActivity sherlockFragmentActivity) {
//        if (s_PromoChecked) return;
//        s_PromoChecked = true;
//
//    //    if (isBetaVersion(sherlockFragmentActivity)) return;
//    //    if (isDonateVersion(sherlockFragmentActivity)) return;
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String appVersion = getAppVersion(sherlockFragmentActivity);
//    //    if (prefs.getString("DonateShowVer", "").equals(appVersion)) return;
//
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("DonateShowVer", appVersion);
//        editor.commit();
//
//        SherlockDialogFragment dialogFragment = new SherlockDialogFragment() {
//            @Override
//            public Dialog onCreateDialog(Bundle savedInstanceState) {
//                return new AlertDialog.Builder(getActivity())
//                        .setTitle("Вас приветствует 4pda-клиент!")
//                        .setMessage("Хотите помочь проекту и приобрести forpda-версию?")
//                        .setPositiveButton(android.R.string.yes,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int which) {
//                                        Intent marketIntent = new Intent(
//                                                Intent.ACTION_VIEW,
//                                                Uri.parse("http://market.android.com/details?id=org.softeg.slartus.gpstaxi.pro"));
//                                        startActivity(marketIntent);
//
//                                    }
//                                })
//                        .setNegativeButton(android.R.string.cancel,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int which) {
//
//                                    }
//                                }).create();
//            }
//        };
//        dialogFragment.show(sherlockFragmentActivity.getSupportFragmentManager(), "dialog");

    }

    private static String getAppVersion(Context context) {
        try {
            String packageName = context.getPackageName();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_META_DATA);

            return pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e1) {
            Log.e(context, e1);
        }
        return "";
    }

    public static Context getContext() {
        return INSTANCE;
    }


}
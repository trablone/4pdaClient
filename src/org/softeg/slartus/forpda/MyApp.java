package org.softeg.slartus.forpda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;

/**
 * User: slinkin
 * Date: 05.08.11
 * Time: 8:03
 */
public class MyApp extends android.app.Application {
    public static final int THEME_WHITE=0;
    public static final int THEME_BLACK=1;

    public static final int THEME_WHITE_REMIE=2;
    public static final int THEME_BLACK_REMIE=3;
    public static final int THEME_WHITE_BEZIPHONA=4;
    public static final int THEME_WHITER_REMIE=5;
    public static final int THEME_GRAY_REMIE=6;
    public static final int THEME_WHITE_VETALORLOV=7;

    public static final int THEME_LIGHT_BEZIPHONA=9;
    public static final int THEME_CUSTOM_CSS=99;

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
    
    public static void showMainActivityWithoutBack(Activity activity){
        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static MyApp INSTANCE = new MyApp();

    public int getThemeStyleResID() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        switch (ExtPreferences.parseInt(preferences, "appstyle", THEME_WHITE)){
            case THEME_WHITE_BEZIPHONA:
            case THEME_WHITE_VETALORLOV:

            case THEME_WHITE_REMIE:
            case THEME_WHITER_REMIE:

            case THEME_WHITE:
                return R.style.Theme_White;
            case THEME_BLACK_REMIE:
            case THEME_GRAY_REMIE:
            case THEME_LIGHT_BEZIPHONA:
            case THEME_BLACK:
                return R.style.Theme_Black;
            default:
                return R.style.Theme_White;
        }
    }

    public int getThemeStyleWebViewBackground() {
        switch (getCurrentTheme()){
            case THEME_WHITE_BEZIPHONA:
            case THEME_WHITE_VETALORLOV:

            case THEME_WHITE_REMIE:
            case THEME_WHITER_REMIE:

            case THEME_WHITE:
                return getResources().getColor(R.color.white_theme_webview_background) ;
            case THEME_BLACK_REMIE:
            case THEME_LIGHT_BEZIPHONA:
            case THEME_GRAY_REMIE:
            case THEME_BLACK:
                return Color.BLACK;
            default:
                return Color.WHITE;
        }
    }


    public int getCurrentTheme(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return ExtPreferences.parseInt(preferences, "appstyle", THEME_WHITE) ;

    }

    public String getCurrentThemeName(){
        String cssFile = "white";
        int theme = MyApp.INSTANCE.getCurrentTheme();
        switch (theme) {
            case THEME_WHITE_BEZIPHONA:
            case THEME_WHITE_VETALORLOV:

            case THEME_WHITE_REMIE:
            case THEME_WHITER_REMIE:

            case THEME_WHITE:
                cssFile = "white";
                break;
            case THEME_LIGHT_BEZIPHONA:
            case THEME_BLACK_REMIE:
            case THEME_GRAY_REMIE:
            case THEME_BLACK:
                cssFile = "black";
                break;
        }
        return cssFile;

    }

    public String getThemeCssFileName(){
        String path="/android_asset/forum/css/";
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
            case THEME_WHITE_BEZIPHONA:
                cssFile = "white_beziphona.css";
                break;
            case THEME_WHITER_REMIE:
                cssFile = "whiter_Remie-l.css";
                break;
            case THEME_GRAY_REMIE:
                cssFile = "gray_Remie-l.css";
                break;
            case THEME_WHITE_VETALORLOV:
                cssFile = "white_vetalorlov.css";
                break;

            case THEME_LIGHT_BEZIPHONA:
                cssFile = "gray_beziphona.css";
                break;
            case THEME_CUSTOM_CSS:
                return "/mnt/sdcard/style.css";

        }
        return path+cssFile;

    }



    public MyApp() {
        INSTANCE = this;

    }
    
    public void showPromo(final Context context){
//        new AlertDialog.Builder(context)
//                .setTitle("Просьба")
//                .setMessage("Прошу проголосовать ВКонтакте за моего друга Мисс Крисс.\nЭто сообщение больше не будет вам показано")
//                .setPositiveButton("Перейти", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                        IntentActivity.showInDefaultBrowser(context, "http://vk.com/topic-1083569_26632291");
//                    }
//                })
//                .setNegativeButton("Закрыть",new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                })
//                .create().show();
    }

    public static Context getContext() {
        return INSTANCE;
    }


}
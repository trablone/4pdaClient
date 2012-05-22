package org.softeg.slartus.forpda;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;

/**
 * User: slinkin
 * Date: 07.12.11
 * Time: 13:24
 */
public class BaseActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
        setTheme(MyApp.INSTANCE.getThemeStyleResID());


    }


}

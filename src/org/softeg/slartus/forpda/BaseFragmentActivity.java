package org.softeg.slartus.forpda;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;

/**
 * User: slinkin
 * Date: 14.03.12
 * Time: 12:51
 */
public class BaseFragmentActivity extends SherlockFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));




        setTheme(MyApp.INSTANCE.getThemeStyleResID());

    }
}

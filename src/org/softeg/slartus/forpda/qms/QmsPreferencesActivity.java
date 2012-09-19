package org.softeg.slartus.forpda.qms;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.softeg.slartus.forpda.R;

/**
 * User: slinkin
 * Date: 18.06.12
 * Time: 14:55
 */
public class QmsPreferencesActivity extends SherlockPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        addPreferencesFromResource(R.xml.qms_prefs);
    }
}

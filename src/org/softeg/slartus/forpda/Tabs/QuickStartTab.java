package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.pm.PackageInfo;
import org.softeg.slartus.forpda.Client;

import java.io.IOException;
import java.util.List;

/**
 * User: slinkin
 * Date: 29.11.11
 * Time: 16:01
 */
public class QuickStartTab extends ThemesTab {
    List<PackageInfo> m_Applications;

    public static final String TEMPLATE = Tabs.TAB_QUICK_START;
     public static final String TITLE = "Быстрый доступ";

    public QuickStartTab(Context context, String tabTag) {
        super(context, tabTag);

    }

    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }


    @Override
    public void getThemes(Client.OnProgressChangedListener progressChangedListener) throws IOException {

    }

}

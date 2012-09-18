package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ThemeOpenParams;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.Comparator;


public class FavoritesTab extends ThemesTab {
    public FavoritesTab(Context context, String tabTag) {
        super(context,tabTag);
    }

    public String getTemplate() {
        return Tabs.TAB_FAVORITES;
    }

    @Override
    public String getTitle() {
        return "Избранное";
    }

    @Override
    protected String getDefaultOpenThemeParams() {
        return ThemeOpenParams.getUrlParams(ThemeOpenParams.NEW_POST,
                super.getDefaultOpenThemeParams());
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {
        Client client = Client.INSTANCE;
        client.getFavoritesThemes(m_Themes,progressChangedListener);

    }

    @Override
    protected Comparator<Topic> getSortComparator(){
        return new Comparator<Topic>() {
            public int compare(Topic theme, Topic theme1) {
                if (theme1.getIsNew() == theme.getIsNew())
                    return theme1.getLastMessageDate().compareTo(theme.getLastMessageDate());
                return theme1.getIsNew() ? 1 : 0;
            }
        };
    }


    @Override
    public void refresh() {
        if (!Client.INSTANCE.getLogined()&&!Client.INSTANCE.hasLoginCookies()) {
            Client.INSTANCE.showLoginForm(getContext(), new Client.OnUserChangedListener() {
                public void onUserChanged(String user, Boolean success) {
                    if (success)
                        FavoritesTab.super.refresh();
                }
            });
        } else
            super.refresh();
    }


}

package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.ThemeOpenParams;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 29.09.11
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
public class SubscribesTab extends ThemesTab {
    public SubscribesTab(Context context, String tabTag) {
        super(context, tabTag);
    }


    @Override
    public String getTitle() {
        return "Подписки";
    }
    public String getTemplate() {
        return Tabs.TAB_SUBSCRIBES;
    }

    @Override
    protected String getDefaultOpenThemeParams() {
        return ThemeOpenParams.getUrlParams(ThemeOpenParams.NEW_POST,
                super.getDefaultOpenThemeParams());
    }

    @Override
    protected void modifyThemesListAfterLoad() {
        Collections.sort(m_Themes, new Comparator<Topic>() {
            public int compare(Topic topic, Topic topic1) {
                return topic1.getLastMessageDate().compareTo(topic.getLastMessageDate());
            }
        });
    }

    @Override
    public void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {
        Client client = Client.INSTANCE;
        client.getSubscribes(m_Themes,progressChangedListener);
    }

    @Override
    public void refresh() {
        m_Refreshed = true;
        m_Themes.clear();
        if (!Client.INSTANCE.getLogined()&&!Client.INSTANCE.hasLoginCookies()) {
            Client.INSTANCE.showLoginForm(getContext(), new Client.OnUserChangedListener() {
                public void onUserChanged(String user, Boolean success) {
                    if (success)
                        SubscribesTab.super.refresh();
                }
            });
        } else
            super.refresh();
    }


    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }

}

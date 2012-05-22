package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.classes.Themes;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 11.11.11
 * Time: 8:18
 */
public class AppsTab extends ThemesTab {
    List<PackageInfo> m_Applications;
    private static final int MENU_OPTIONS_BASE = 0;
    public static final int FILTER_APPS_THIRD_PARTY = MENU_OPTIONS_BASE + 1;

    public AppsTab(Context context, String tabTag) {
        super(context, tabTag);

    }

    public String getTemplate() {
        return Tabs.TAB_APPS;
    }

    public static final String TITLE = "Приложения";

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
        if (progressChangedListener != null) {
            progressChangedListener.onProgressChanged("Получение списка приложений...");
        }
        PackageManager packageManager = getContext().getPackageManager();
        m_Applications = packageManager.getInstalledPackages(0);
        Client client = Client.INSTANCE;

        ArrayList<String> appsName = new ArrayList<String>();
        Themes apps = m_Themes;

        for (int n = 0; n < m_Applications.size(); n++) {
            PackageInfo p = m_Applications.get(n);
            if (!filterApp(p.applicationInfo)) continue;

            String title = "";
            title = p.applicationInfo.loadLabel(packageManager).toString();

            // if (title.equals(p.packageName)) continue;

            Topic topic = new Topic("", title);
            topic.setDescription(p.packageName);
            topic.setLastMessageAuthor(p.versionName);
            topic.setIsOld(true);
            apps.add(topic);

            int i = apps.size() - 1;

            appsName.add(normalizeTitle(title));
        }

        getCatalogThemes(apps, appsName, progressChangedListener);
        Collections.sort(m_Themes, new Comparator<Topic>() {
            public int compare(Topic topic, Topic topic1) {
                if (topic.getIsNew() != topic1.getIsNew())
                    return topic1.getIsNew() ? 1 : -1;
                if (topic.getIsOld() != topic1.getIsOld())
                    return topic.getIsOld() ? 1 : -1;
                return topic.getTitle().toString().toUpperCase().compareTo(topic1.getTitle().toString().toUpperCase());
            }
        });
    }

    private Boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }


    private static final Pattern normalizePattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+)|(\\d+\\.\\d+)|(\\d+)|(\\(?hd\\)?$)|(\\(?premium\\)?$)|(\\(?ultimate\\)?$)|(\\(?pc\\)?$)|(\\(?free\\)?$)|(\\(?beta\\)?$)|(\\(?alpha\\)?$)|(\\(?lite\\)?$)|(\\(?pro\\)?$)|(\\(?forandroid\\)?$)");

    public static String normalizeTitle(String title) {
        return normalizePattern.matcher(title.toLowerCase().replace(" ", "")).replaceAll("");

    }


    private static final String appCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=112220";
    private static final String gameCatalogUrl = "http://4pda.ru/forum/index.php?showtopic=117270";

    private void getCatalogThemes(Themes apps, ArrayList<String> appsName, Client.OnProgressChangedListener progressChangedListener) throws IOException {
        Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Получение данных...");
        Pattern pattern = Pattern.compile("<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+).*?\" target=\"_blank\">(.*?)</a>(.*?)</li>");

        String gamesBody = Client.INSTANCE.loadPageAndCheckLogin(gameCatalogUrl, null);
        String appsBody = Client.INSTANCE.loadPageAndCheckLogin(appCatalogUrl, null);
        Client.INSTANCE.doOnOnProgressChanged(progressChangedListener, "Обработка данных...");


        Matcher m = pattern.matcher(gamesBody);
        while (m.find()) {
            String normTitle = AppsTab.normalizeTitle(m.group(2));
            int ind = appsName.indexOf(normTitle);
            if (ind == -1) continue;
            Topic app = apps.get(ind);

            app.setId(m.group(1));
            app.setDescription(m.group(3));
            app.setIsOld(false);
            appsName.set(ind, null);
        }

        m = pattern.matcher(appsBody);
        while (m.find()) {
            String normTitle = AppsTab.normalizeTitle(m.group(2));
            int ind = appsName.indexOf(normTitle);
            if (ind == -1) continue;
            Topic app = apps.get(ind);

            app.setId(m.group(1));
            app.setDescription(m.group(3));
            app.setIsOld(false);
            appsName.set(ind, null);
        }

    }


}

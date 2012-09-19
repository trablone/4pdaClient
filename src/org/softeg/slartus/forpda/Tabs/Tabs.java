package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.softeg.slartus.forpdaapi.NotReportException;

/**
 * User: slinkin
 * Date: 22.11.11
 * Time: 8:07
 */
public final class Tabs {
    public static final String TAB_SEARCH = "SearchTab";
    public static final String TAB_FORUMS = "ForumsTab";
    public static final String TAB_FAVORITES = "FavoritesTab";
    public static final String TAB_SUBSCRIBES = "SubscribesTab";
    public static final String TAB_DIGEST = "DigestTab";
    public static final String TAB_CATALOG = "CatalogTab";
    public static final String TAB_APPS = "AppsTab";
    public static final String TAB_FIRST_HELP = "FirstHelpTab";
    public static final String TAB_DEVDB = "DevDB";
    public static final String TAB_QUICK_START = "QuickStart";
    public static final String TAB_NEWS = "NewsTab";
    public static final String[] templates = {TAB_NEWS,TAB_SEARCH, TAB_FORUMS, TAB_FAVORITES,
            TAB_SUBSCRIBES, TAB_DIGEST, TAB_CATALOG, TAB_APPS,TAB_FIRST_HELP,TAB_DEVDB};

    public static ThemesTab create(Context context, String template, String tabId) {

        if (template.equals(Tabs.TAB_FORUMS)) {
            return new ForumTreeTab(context, tabId);
        } else if (template.equals(Tabs.TAB_SUBSCRIBES)) {
            return new SubscribesTab(context, tabId);
        } else if (template.equals(Tabs.TAB_FAVORITES)) {
            return new FavoritesTab(context, tabId);
        } else if (template.equals(Tabs.TAB_SEARCH)) {
            return new SearchTab(context, tabId);
        } else if (template.equals(Tabs.TAB_DIGEST)) {
            return new DigestTab(context, tabId);
        } else if (template.equals(Tabs.TAB_CATALOG)) {
            return new CatalogTab(context, tabId);
        } else if (template.equals(Tabs.TAB_APPS)) {
            return new AppsTab(context, tabId);
        }  else if (template.equals(FirstHelpTab.TEMPLATE)) {
            return new FirstHelpTab(context, tabId);
        }else if (template.equals(DevicesTab.TEMPLATE)) {
            return new DevicesTab(context, tabId);
        } else if (template.equals(QuickStartTab.TEMPLATE)) {
            return new QuickStartTab(context, tabId);
        } else if (template.equals(NewsTab.TEMPLATE)) {
            return new NewsTab(context, tabId);
        }


        return new SearchTab(context, tabId);
    }

    public static void configTabsData(PreferenceActivity preferenceActivity) throws NotReportException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preferenceActivity);
        configTabData(preferenceActivity,prefs, "Tab1", TAB_NEWS);
        configTabData(preferenceActivity,prefs, "Tab2", TAB_FORUMS);
        configTabData(preferenceActivity,prefs, "Tab3", TAB_FAVORITES);
        configTabData(preferenceActivity,prefs, "Tab4", TAB_SUBSCRIBES);
        configTabData(preferenceActivity,prefs, "Tab5", TAB_CATALOG);
    }

    private static void configTabData(final PreferenceActivity preferenceActivity,
                                      SharedPreferences prefs, final String tabId,
                                      final String defaulTemplate) throws NotReportException {
        Preference preference = preferenceActivity.findPreference("tabs." + tabId + ".Data");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(preferenceActivity, TabDataSettingsActivity.class);
                intent.putExtra("tabId", tabId);
                intent.putExtra("template", defaulTemplate);
                preferenceActivity.startActivity(intent);
                return true;
            }
        });
        preference.setSummary(getTabName(prefs, tabId));
        preferenceActivity.findPreference("tabs." + tabId).setSummary(getTabName(prefs, tabId));
    }

    public static String getTabName(SharedPreferences prefs, String tabId) throws NotReportException {
        String template = getTemplate(prefs, tabId);
        if (!template.equals(TAB_SEARCH)
                &&!template.equals(TAB_FORUMS))
            return getDefaultTemplateName(template);

        String name = prefs.getString(tabId + ".Template.Name", "");
        if (!TextUtils.isEmpty(name)) return name;

        if (template.equals(TAB_SEARCH))
            return getDefaultTemplateName(template);
        if (template.equals(TAB_FORUMS))
            return getDefaultTemplateName(template);


        throw new NotReportException("Неизвестный шаблон");
    }

    public static String getDefaultTemplateName(String template) throws NotReportException {

        if (template.equals(TAB_FAVORITES))
            return "Избранное";
        if (template.equals(TAB_SUBSCRIBES))
            return "Подписки";
        if (template.equals(TAB_DIGEST))
            return "Дайджест";
        if (template.equals(TAB_CATALOG))
            return CatalogTab.TITLE;
        if (template.equals(TAB_APPS))
            return AppsTab.TITLE;
        if (template.equals(TAB_SEARCH))
            return "Последние";
        if (template.equals(TAB_FORUMS))
            return "Форумы";
        if (template.equals(FirstHelpTab.TEMPLATE))
            return FirstHelpTab.TITLE;
        if (template.equals(DevicesTab.TEMPLATE))
            return DevicesTab.TITLE;
        if (template.equals(NewsTab.TEMPLATE))
            return NewsTab.TITLE;


        throw new NotReportException("Неизвестный шаблон");
    }

    public static String getTemplate(SharedPreferences prefs, String tabId) {
        String template = prefs.getString(tabId + ".Template", "");
        if (!TextUtils.isEmpty(template)) return template;

        if (tabId.equals("Tab1"))
            return TAB_NEWS;
        else if (tabId.equals("Tab2"))
            return TAB_FORUMS;
        else if (tabId.equals("Tab3"))
            return TAB_FAVORITES;
        else if (tabId.equals("Tab4"))
            return TAB_SUBSCRIBES;
        else if (tabId.equals("Tab5"))
            return TAB_CATALOG;
        return TAB_SEARCH;
    }

}

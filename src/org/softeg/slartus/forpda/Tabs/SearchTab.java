package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.classes.Themes;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.common.Log;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 15.10.11
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class SearchTab extends ThemesTab {

    @Override
    public String getTitle() {
        return m_Name;
    }

    public String getTemplate() {
        return Tabs.TAB_SEARCH;
    }

    public SearchTab(Context context, String tabTag) {
        super(context, tabTag);
        loadSettings();
    }


    @Override
    protected Boolean isShowForumTitle() {
        return true;
    }


    @Override
    public void getThemes(Client.OnProgressChangedListener progressChangedListener) throws IOException {
        String params = "&source=" + m_Source;
        params += "&sort=" + m_Sort;
        Enumeration<String> keys = m_CheckedIds.keys();
        for (int i = 0; i < m_CheckedIds.size(); i++) {
            String key = keys.nextElement();

            params += "&forums%5B%5D=" + key;
        }
        if (m_CheckedIds.size() == 0) {
            params += "&forums%5B%5D=all";
        }
        params += "&subforums=" + (m_Subforums ? "1" : "0");

        getSearchThemes(super.m_Themes, m_Query, m_UserName, params, progressChangedListener);
    }

    private String m_Query;
    private String m_UserName;

    private String m_Source;
    private String m_Name = "Поиск";
    private String m_Sort;
    private Boolean m_Subforums;


    private Hashtable<String, CharSequence> m_CheckedIds = new Hashtable<String, CharSequence>();

    public void search(String query, String userName, String source, String sort, Boolean subforums,
                       Hashtable<String, CharSequence> checkedIds) {
        m_Query = query;
        m_UserName = userName;
        m_Source = source;
        m_Sort = sort;
        m_Subforums = subforums;
        m_CheckedIds = checkedIds;
        refresh();
    }


    private void loadSettings() {
        String tabTag = getTabId();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_Source = preferences.getString(tabTag + ".Template.Source", "all");
        m_Name = preferences.getString(tabTag + ".Template.Name", "Последние");

        m_Sort = preferences.getString(tabTag + ".Template.Sort", "dd");
        m_UserName = preferences.getString(tabTag + ".Template.UserName", "");
        m_Query = preferences.getString(tabTag + ".Template.Query", "");

        loadChecks(preferences.getString(tabTag + ".Template.Forums", ""));
        m_Subforums = preferences.getBoolean(tabTag + ".Template.Subforums", true);

        if (TextUtils.isEmpty(m_Name)) {
            if (m_Source.equals("all") && m_Sort.equals("dd") && TextUtils.isEmpty(m_UserName)
                    && (m_CheckedIds.size() == 0) && m_Subforums)
                m_Name = "Последние";
            else
                m_Name = "Поиск";
        }

    }

    private void loadChecks(String checksString) {
        m_CheckedIds = new Hashtable<String, CharSequence>();
        if (TextUtils.isEmpty(checksString)) return;
        try {
            String[] pairs = checksString.split(TabDataSettingsActivity.pairsDelimiter);
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                if (TextUtils.isEmpty(pair)) continue;
                String[] vals = pair.split(TabDataSettingsActivity.pairValuesDelimiter);
                m_CheckedIds.put(vals[0], vals[1]);
            }
        } catch (Exception ex) {
            Log.e(getContext(), ex);
        }

    }


    private void getSearchThemes(Themes themes, String query, String userName, String params, Client.OnProgressChangedListener progressChangedListener) throws IOException {


        String pageBody = Client.INSTANCE.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?act=search&query=" + URLEncoder.encode(query, "windows-1251") + "&username=" + URLEncoder.encode(userName, "windows-1251") + "&subforums=1&result=topics" + params + "&st=" + themes.size()
                , progressChangedListener);

        Pattern pattern = Pattern.compile("<tr>(.*?)<a href=\"/forum/index.php\\?showtopic=(\\d+)\">(.*?)</a><br /><span class=\"desc\">(.*?)</span></td>" +
                "<td class=\"row2\" width=\"15%\"><span class=\"forumdesc\"><a href=\"/forum/index.php\\?showforum=(\\d+)\" title=\".*?\">(.*?)</a></span></td>" +
                "<td align=\"center\" class=\"row1\" width=\"10%\"><a href=\"/forum/index.php\\?showuser=\\d+\">.*?</a></td>" +
                "<td align=\"center\" class=\"row2\"><a href=\"javascript:who_posted\\(\\d+\\);\">(\\d+)</a></td>" +
                "<td align=\"center\" class=\"row1\">\\d+</td><td class=\"row1\"><span class=\"desc\">(.*?)<br /><a href=\"/forum/index.php\\?showtopic=\\d+&amp;view=getlastpost\">Послед.:</a> <b><a href=\"/forum/index.php\\?showuser=(\\d+)\">(.*?)</a>");
        Pattern pagesCountPattern = Pattern.compile("<a href=\"/forum/index.php.*?st=(\\d+)\">");

        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();

        Matcher m = pattern.matcher(pageBody);

        while (m.find()) {

            Topic topic = new Topic(m.group(2), m.group(3));
            topic.setIsNew(m.group(1).contains("view=getnewpost"));
            topic.setDescription(m.group(4));
            topic.setForumId(m.group(5));
            topic.setForumTitle(m.group(6));

            topic.setPostsCount(m.group(7));
            topic.setLastMessageDate(Functions.parseForumDateTime(m.group(8), today, yesterday));

            topic.setLastMessageAuthorId(m.group(9));
            topic.setLastMessageAuthor(m.group(10));
            themes.add(topic);
        }

        m = pagesCountPattern.matcher(pageBody);
        int themesCount = 0;
        while (m.find()) {
            themesCount=Math.max(Integer.parseInt(m.group(1)),themesCount);
        }
        themes.setThemesCountInt(themesCount);

    }

}

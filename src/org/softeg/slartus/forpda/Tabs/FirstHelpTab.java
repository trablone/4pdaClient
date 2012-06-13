package org.softeg.slartus.forpda.Tabs;

import android.content.Context;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.Themes;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 22.11.11
 * Time: 10:28
 */
public class FirstHelpTab extends TreeTab {


    public FirstHelpTab(Context context, String tabTag) {
        super(context, tabTag);

    }

    public static final String TEMPLATE = Tabs.TAB_FIRST_HELP;
    public static final String TITLE = "Android - Первая помощь";


    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Boolean onParentBackPressed() {
        if (m_CurrentItem == null || m_CurrentItem.getParent() == null) {
            return false;
        }
        showForum(m_CurrentItem.getParent());
        return true;
    }

    @Override
    protected void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws IOException {
        if(forum.getParent()==null)
            parse(forum, progressChangedListener);
        else
            ForumTreeTab.loadThemes(forum,progressChangedListener);
    }

     @Override
    protected void getThemes(OnProgressChangedListener progressChangedListener) throws IOException {

        if (m_ForumForLoadThemes.LoadMore || m_Themes.size() == 0) {

            ForumTreeTab.loadThemes(m_ForumForLoadThemes, progressChangedListener);
        }
    }
    public void parse(Forum forum, OnProgressChangedListener progressChangedListener) throws IOException {

        String pageBody = Client.INSTANCE.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?showforum=282", progressChangedListener);


        Pattern topicPattern = Pattern.compile("<a href=\"http://4pda.ru/forum/index.php\\?showtopic=(\\d+)\".*?<b>(.*?)</b>");
        Pattern categoryPattern = Pattern.compile("<span style=\"color:coral\"><!--/coloro-->(.*?)<!--colorc--></span>");
        Pattern otherPattern = Pattern.compile("http://4pda.ru/forum/index.php\\?showforum=(\\d+)");
        int id = 0;
        int emptyCount = 0;
        for (String part : pageBody.split("<b><!--sizeo:4-->")) {
            Matcher m = categoryPattern.matcher(part);
            if (!m.find()) continue;
            Forum f = new Forum(Integer.toString(id++), m.group(1));


            Themes themes = new Themes();
            m = topicPattern.matcher(part);
            while (m.find()) {
                themes.add(new Topic(m.group(1), m.group(2)));
            }
            emptyCount++;

            m = otherPattern.matcher(part);
            if (m.find()) {
                f.addForum(new Forum(m.group(1), "И прочее..."));
            }
            forum.addForum(f);

            if (themes.size() != 0) {
                for (int i = 0; i < emptyCount; i++) {
                    Forum f1 = forum.getForums().get(forum.getForums().size() - (emptyCount - i));

                    Forum themesItem = f1.addForum(0, new Forum(Integer.toString(id++), "Основные темы"));

                    int themesPart = themes.size() / emptyCount;
                    for (int c = 0; c < themesPart; c++) {
                        themesItem.addTheme(themes.get(c * (i + 1)));
                    }
                }

                emptyCount = 0;
            }
        }

    }


}

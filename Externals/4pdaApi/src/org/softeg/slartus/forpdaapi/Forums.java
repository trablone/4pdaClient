package org.softeg.slartus.forpdaapi;

import android.text.Html;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:41
 */
public class Forums extends ArrayList<Forum> {
    /**
     * Загрузка дерева разделов форума
     * @param httpClient
     * @return
     * @throws Exception
     */
    public static  Forum loadForums(IHttpClient httpClient) throws Exception {
        Forum mainForum= new Forum("-1", "4PDA");
        byte tryCount=3;// Не всегда загружается в текстовом режиме. будем пробовать 3 раза.
        Boolean regimeChecked = false;
        while(tryCount>0){
            String pageBody = httpClient.performGet("http://4pda.ru/forum/lofiversion/index.php");


            String[] strings = pageBody.split("\n");
            pageBody = null;
            Pattern checkRegimePattern = Pattern.compile("<div id='largetext'>Полная версия этой страницы");
            Pattern forumPattern = Pattern.compile("<a href='http://4pda.ru/forum/lofiversion/index.php\\?f(\\d+).html'>(.*?)</a>");

            Forum forum = mainForum;

            for (String str : strings) {
                regimeChecked = regimeChecked || checkRegimePattern.matcher(str).find();
                Matcher m = forumPattern.matcher(str);
                if (m.find()) {
                    if (forum.getParent() != null && forum.getParent() != mainForum && forum.getForums().size() == 0)
                        forum.addForum(new Forum(forum.getId(), forum.getTitle() + " @ темы"));
                    forum.addForum(new Forum(m.group(1), Html.fromHtml(m.group(2)).toString()));
                } else if (str.endsWith("<ul>")) {
                    forum = forum.getLastChild();
                } else if (str.trim().startsWith("</ul></li>")) {
                    forum = forum.getParent();
                    if (forum == null)
                        forum = mainForum;
                }
            }
            if (!regimeChecked)
                tryCount--;
            else
                break;
        }
        if (!regimeChecked)
            throw new NotReportException("Страница загрузилась не в текстовом режиме! Попробуйте залогиниться");

        return mainForum;
    }
    
    public static void markAllAsRead(IHttpClient httpClient) throws Exception{
        httpClient.performGet("http://4pda.ru/forum/index.php?act=Login&CODE=05");
    }
}

package org.softeg.slartus.forpdaapi;

import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 12:06
 */
public class Topic {

    /**
     * Подписка на тему
     * @param httpClient
     * @param authKey
     * @param forumId
     * @param topicId
     * @param emailtype тип уведомления:
     *                  "none" - не уведомлять,
     *                  "delayed" - Уведомление с задержкой,
     *                  "immediate" - Немедленное уведомление,
     *                  "daily" - Ежедневное уведомление,
     *                  "weekly" - Еженедельное уведомление
     * @return текст ошибки или пусто в случае успеха
     * @throws IOException
     */
    public static String subscribe(IHttpClient httpClient, String authKey,String forumId, String topicId,String emailtype) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "usercp");
        additionalHeaders.put("CODE", "end_subs");
        additionalHeaders.put("method", "topic");
        additionalHeaders.put("auth_key", authKey);
        additionalHeaders.put("tid", topicId);
        additionalHeaders.put("fid", forumId);
        additionalHeaders.put("st", "0");
        additionalHeaders.put("emailtype", emailtype);
        String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

        Pattern p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE);
        Matcher m = p.matcher(res);
        if (m.find()) {

            return "Ошибка подписки: " + m.group(1);
        }
        return "";
    }

    /**
     * Отписаться от темы
     * @param httpClient
     * @param topicId
     * @return Текст ошибки или пусто
     * @throws IOException
     */
    public static String unSubscribe(IHttpClient httpClient,String topicId) throws IOException {
        // сначала получим список подписок
        String body = httpClient.performGet("http://4pda.ru/forum/index.php?act=UserCP&CODE=26");

        Pattern pattern = Pattern.compile("(<td colspan=\"6\" class=\"row1\"><b>(.*?)</b></td>)?\n" +
                "\\s*</tr><tr>\n" +
                "\\s*<td class=\"row2\" align=\"center\" width=\"5%\">(<font color='.*?'>)?(.*?)(</font>)?</td>\n" +
                "\\s*<td class=\"row2\">\n" +
                "\\s*<a href=\"http://4pda.ru/forum/index.php\\?showtopic=" + topicId + "\">(.*?)</a>&nbsp;\n" +
                "\\s*\\( <a href=\"http://4pda.ru/forum/index.php\\?showtopic=" + topicId + "\" target=\"_blank\">В новом окне</a> \\)\n" +
                "\\s*<div class=\"desc\">((.*?)<br />)?.*?\n" +
                "\\s*<br />\n" +
                "\\s*Тип: .*?\n" +
                "\\s*</div>\n" +
                "\\s*</td>\n" +
                "\\s*<td class=\"row2\" align=\"center\"><a href=\"javascript:who_posted\\(\\d+\\);\">(\\d+)</a></td>\n" +
                "\\s*<td class=\"row2\" align=\"center\">\\d+</td>\n" +
                "\\s*<td class=\"row2\">(.*?)<br />автор: <a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a></td>" +
                "\\s*<td class=\"row1\" align=\"center\" style='padding: 1px;'><input class='checkbox' type=\"checkbox\" name=\"id-(\\d+)\" value=\"yes\" /></td>\n");

        Matcher m = pattern.matcher(body);
        body = null;
        if (m.find()) {
            Map<String, String> additionalHeaders = new HashMap<String, String>();
            additionalHeaders.put("act", "UserCP");
            additionalHeaders.put("CODE", "27");
            additionalHeaders.put("id-" + m.group(13), "yes");
            additionalHeaders.put("trackchoice", "unsubscribe");
            httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

            return "";
        } else {
            return "Тема в подписках не найдена";
        }
    }

    /**
     * Добавить тему в Избранное
     * @param httpClient
     * @param forumId
     * @param topicId
     * @return
     * @throws IOException
     */
    public static String addToFavorites(IHttpClient httpClient,String forumId, String topicId) throws IOException {

        String res = httpClient.performGet("http://4pda.ru/forum/index.php?autocom=favtopics&CODE=03&f=" + forumId + "&t=" + topicId + "&st=0");

        Pattern pattern = Pattern.compile("\\s*<div class=\"tablepad\">\\s*(.*)\\s*<ul>", Pattern.MULTILINE);
        Matcher m = pattern.matcher(res);
        if (m.find()) {
            return m.group(1);
        } else {
            pattern = Pattern.compile("\\s*<h4>Причина:</h4>\\s*<p>(.*?)</p>", Pattern.MULTILINE);
            m = pattern.matcher(res);
            if (m.find()) {
                return m.group(1);
            }
        }
        return "Результат неизвестен. Сообщите разработчику";
    }

    /**
     * Удалить тему из избранного
     * @param httpClient
     * @param forumId
     * @param topicId
     * @return
     * @throws IOException
     */
    public static String removeFromFavorites(IHttpClient httpClient,String forumId, String topicId) throws IOException {

        String query="http://4pda.ru/forum/index.php?autocom=favtopics&CODE=02&selectedtids="
                + topicId + "&cb=1&t=" + topicId + "&st=0";
        if(!TextUtils.isEmpty(forumId))
            query+="&f=" + forumId;
        httpClient.performGet(query);

        return "Выбранная Вами тема удалена из избранного";
    }
}

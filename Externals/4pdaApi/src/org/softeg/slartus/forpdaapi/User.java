package org.softeg.slartus.forpdaapi;

import android.text.Html;
import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 11:56
 */
public class User {


    private static final Pattern checkLoginPattern = Pattern.compile("<a href=\"(http://4pda.ru)?/forum/index.php\\?showuser=(\\d+)\">(.*?)</a></b> \\( <a href=\"(http://4pda.ru)?/forum/index.php\\?act=Login&amp;CODE=03&amp;k=([a-z0-9]{32})\">Выход</a>");

    /**
     *
     * @param httpClient
     * @param login
     * @param password
     * @param privacy
     * @param outParams LoginFailedReason - текст ошибки, в случае провала логина,
     *                  SessionId - идентификатор сессии,
     *                  User - логин пользователя
     *                  K - еще какой-то идентификатор сессии
     * @return
     * @throws Exception
     */
    public static Boolean login(IHttpClient httpClient, String login, String password, Boolean privacy,
                                Map<String, String> outParams) throws Exception {
        String outLoginFailedReason=""; 
        String outSessionId=""; 
        String outUser=""; 
        String outK="";
        Boolean logined=false;
        try{
            outLoginFailedReason = null;

            Map<String, String> additionalHeaders = new HashMap<String, String>();

            additionalHeaders.put("UserName", login.replace(" ", "\\ "));
            additionalHeaders.put("PassWord", password);
            additionalHeaders.put("CookieDate", "1");
            additionalHeaders.put("Privacy", privacy ? "1" : "0");
            UUID uuid = UUID.randomUUID();

            outSessionId = uuid.toString().replace("-", "");
            additionalHeaders.put("s", outSessionId);
            additionalHeaders.put("act", "Login");
            additionalHeaders.put("CODE", "01");

            additionalHeaders.put("referer", "http://4pda.ru/forum/index.php?s=" + outSessionId + "&amp;amp;s=" + outSessionId + "&amp;act=Login&amp;CODE=01");


            String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

            if (TextUtils.isEmpty(res)) {
                outLoginFailedReason = "Сервер вернул пустую страницу";
                return false;
            }

            Matcher m1 = checkLoginPattern.matcher(res);
            if (m1.find()) {
                outUser = m1.group(3);
                outK = m1.group(5);
                logined = true;
            } else {
                logined = false;
                outUser = "гость";
            }

            if (!logined) {
                Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                        "\n" +
                        "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
                Matcher m = checkPattern.matcher(res);
                if (m.find()) {
                    outLoginFailedReason = m.group(1);
                } else {
                    checkPattern = Pattern.compile("\t<div class=\"formsubtitle\">Обнаружены следующие ошибки:</div>\n" +
                            "\t<div class=\"tablepad\"><span class=\"postcolor\">(.*?)</span></div>");
                    m = checkPattern.matcher(res);
                    if (m.find()) {
                        outLoginFailedReason = m.group(1);
                    } else {
                        outLoginFailedReason = Html.fromHtml(res).toString();
                    }
                }
            } 
        } finally{
            outParams.put("LoginFailedReason",outLoginFailedReason);
            outParams.put("SessionId",outSessionId);
            outParams.put("User",outUser);
            outParams.put("K",outK);
        }
        

        return logined;
    }

    /**
     * ЛОгаут
     * @param httpClient
     * @param k идентификатор, полученный при логине
     * @return
     * @throws IOException
     */
    public static String logout(IHttpClient httpClient, String k) throws IOException {

        return httpClient.performGet("http://4pda.ru/forum/index.php?act=Login&CODE=03&k=" + k);

    }
    /**
     * Изменение репутации пользователя
     *
     * @param httpClient
     * @param postId     Идентификатор поста, за который поднимаем репутацию. 0 - "в профиле"
     * @param userId
     * @param type       "add" - поднять, "minus" - опустить
     * @param message
     * @return Текст ошибки или пустая строка в случае успеха
     * @throws IOException
     */
    public static String changeReputation(IHttpClient httpClient, String postId, String userId, String type, String message) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("act", "rep");
        additionalHeaders.put("p", postId);
        additionalHeaders.put("mid", userId);
        additionalHeaders.put("type", type);
        additionalHeaders.put("message", message);

        String res = httpClient.performPost("http://4pda.ru/forum/index.php", additionalHeaders);

        Pattern p = Pattern.compile("<title>(.*?)</title>");
        Matcher m = p.matcher(res);
        if (m.find()) {
            if (m.group(1) != null && m.group(1).equals("Ошибка")) {
                p = Pattern.compile("<div class='maintitle'>(.*?)</div>");
                m = p.matcher(res);
                if (m.find()) {
                    return "Ошибка изменения репутации: " + m.group(1);
                }
                return "Ошибка изменения репутации";
            }
            return "Репутация: " + m.group(1);
        }
        return "Репутация изменена";
    }

    /**
     * Загружает историю репутации пользователя
     * @param httpClient
     * @param reputations Массив уже загруженных изменений репутации. Догружает с того количества, которое уже в массиве 
     * @param beforeGetPage
     * @param afterGetPage
     * @throws IOException
     */
    public static void loadReputation(IHttpClient httpClient,String userId, Reputations reputations, OnProgressChangedListener beforeGetPage, OnProgressChangedListener afterGetPage) throws IOException {

        String body = httpClient.performGetWithCheckLogin("http://4pda.ru/forum/index.php?act=rep&type=history&mid=" + userId + "&st=" + reputations.size(), beforeGetPage,afterGetPage);

        reputations.userId=userId;
        Pattern pattern = Pattern.compile("<div class='maintitle'>(.*?)<div");
        Matcher m = pattern.matcher(body);
        if (m.find())
            reputations.description = m.group(1);

        if (reputations.fullListCount == 0) {
            pattern = Pattern.compile("parseInt\\((\\d+)/\\d+\\)");
            m = pattern.matcher(body);
            if (m.find())
                reputations.fullListCount = Integer.parseInt(m.group(1));
        }

        pattern = Pattern.compile("\\s*<td class='row2' align='left'><b><a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*)</a></b></td>\n" +
                "\\s*<td class='row2' align='left'>(<b>)?<a href='(.*)'>(.*?)</a>(</b>)?</td>\n" +
                "\\s*<td class='row2' align='left'>(.*?)</td>\n" +
                "\\s*<td class='row1' align='center'><img border='0' src='style_images/1/(.*?).gif' /></td>\n" +
                "\\s*<td class='row1' align='center'>(.*)</td>", Pattern.MULTILINE);
        m = pattern.matcher(body);

        while (m.find()) {
            Reputation rep = new Reputation();
            rep.userId = m.group(1);
            rep.user = m.group(2);
            rep.sourceUrl = m.group(4);
            rep.source = Html.fromHtml(m.group(5));
            rep.description = Html.fromHtml(m.group(7));
            rep.level = m.group(8);
            rep.date = m.group(9);
            reputations.add(rep);
        }


    }


}

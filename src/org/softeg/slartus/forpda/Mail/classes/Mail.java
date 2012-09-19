package org.softeg.slartus.forpda.Mail.classes;

import android.text.Html;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.EditPostActivity;
import org.softeg.slartus.forpda.classes.Exceptions.AdditionalInfoException;
import org.softeg.slartus.forpdaapi.NotReportException;
import org.softeg.slartus.forpda.classes.common.Functions;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 13.03.12
 * Time: 11:51
 */
public class Mail {
    public static final int MAIL_TYPE_INCOMING = 0;
    public static final int MAIL_TYPE_OUTGOING = 1;
    private String id;
    private boolean isNew;
    private String theme;
    private String userId;
    private String user;
    private String date;
    private String body;

    public Boolean IsChecked = false;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setIsNew(boolean aNew) {
        isNew = aNew;
    }


    public boolean getIsNew() {
        return this.isNew;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme() {
        return this.theme;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }

    public void setDate(Date date) {
        this.date = Functions.getForumDateTime(date);
    }

    public String getDate() {
        return this.date;
    }

    public static Mail load(String url) throws Exception {
        String body = Client.INSTANCE.performGet(url);
        Matcher errorMatcher = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE).matcher(body);

        if (errorMatcher.find()) {

            throw new NotReportException("Ошибка: " + errorMatcher.group(1));
        }

        Matcher partsMatcher = Pattern.compile("<div class=\"subtitle\">Сообщение</div>([\\s\\S]*?)<!-- end main CP area -->").matcher(body);
        if (!partsMatcher.find())
            throw new AdditionalInfoException("Ошибка разбора страницы", "url", url);


        Mail mail = new Mail();
        Matcher idMatcher = Pattern.compile("MSID=(\\d+)").matcher(url);
        if (idMatcher.find())
            mail.setId(idMatcher.group(1));
        else
            throw new AdditionalInfoException("Ошибка разбора страницы", "url", url);

        Matcher userPartMatcher = Pattern.compile("<tr>[\\s\\S]*?<td.*?>([\\s\\S]*?)</td>").matcher(partsMatcher.group(1));
        if (userPartMatcher.find()) {
            Matcher userMatcher = Pattern.compile("<a href=\"http://4pda.ru/forum/index.php\\?showuser=(\\d+)\">(.*?)</a>").matcher(userPartMatcher.group(1).trim());
            if (userMatcher.find()) {
                mail.setUserId(userMatcher.group(1));
                mail.setUser(userMatcher.group(2));
            } else
                mail.setUser(Html.fromHtml(userPartMatcher.group(1).trim()).toString());
        }


        Matcher bodyMatcher = Pattern.compile("<td width=\"100%\" valign=\"top\" class=\"post1\">([\\s\\S]*?)</td>").matcher(partsMatcher.group(1));
        if (bodyMatcher.find()) {
            mail.body = bodyMatcher.group(1);
        }

        Matcher postdetailsMatcher = Pattern.compile("<span class=\"postdetails\"><b>(.*?)</b>, (.*?)</span>").matcher(partsMatcher.group(1));
        if (postdetailsMatcher.find()) {
            String today = Functions.getToday();
            String yesterday = Functions.getYesterToday();
            mail.setTheme(Html.fromHtml(postdetailsMatcher.group(1)).toString());
            mail.setDate(Functions.parseForumDateTime(postdetailsMatcher.group(2), today, yesterday));
        }
        return mail;
    }

    public String getHtmlBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        sb.append("<head>\n");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        EditPostActivity.addStyleSheetLink(sb);
        sb.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");
        sb.append("<script type=\"text/javascript\" src=\"file:///android_asset/blockeditor.js\"></script>\n");
        sb.append("<title>" + getTheme() + "</title>\n");
        sb.append("</head>\n");
        sb.append(body);
        sb.append("</body>\n");
        sb.append("</html>\n");
        return sb.toString();
    }

    public void delete() throws IOException {
        String res=Client.INSTANCE.performGet("http://4pda.ru/forum/index.php?CODE=05&act=Msg&MSID="+id+"&VID=in");
    }
}

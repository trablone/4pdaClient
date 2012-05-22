package org.softeg.slartus.forpda.Mail.classes;

import android.text.Html;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.common.Functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 13.03.12
 * Time: 11:51
 */
public class Mails extends ArrayList<Mail> {
    public Mails() {

    }

    public Mails(String vid) {
        this();
        m_Vid = vid;
    }
    
    public void setVid(String vid){
        m_Vid=vid;
    }

    private int fullLength;
    private String m_Vid;

    public int getFullLength() {
        return Math.max(fullLength, size());
    }

    public void setFullLength(int fullLength) {
        this.fullLength = fullLength;
    }

    public void loadItems(String startCount) throws IOException {
        String today = Functions.getToday();
        String yesterday = Functions.getYesterToday();
        String body = Client.INSTANCE.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?act=Msg&CODE=01&VID=" + m_Vid + "&st=" + startCount,null);

        final Pattern mailsPattern = Pattern.compile("<tr id=\"(\\d+)\">[\\s\\S]*?<td.*?><img src='(.*?)'.*?></td>[\\s\\S]*?<td.*?>&nbsp;<a href=\".*?\">(.*?)</a></td>[\\s\\S]*?<td.*?>(.*?)</td>[\\s\\S]*?<td.*?>(.*?)</td>[\\s\\S]*?</tr>");
        final Pattern userPattern=Pattern.compile("<a href='http://4pda.ru/forum/index.php\\?showuser=(\\d+)'>(.*?)</a>");
        Matcher m = mailsPattern.matcher(body);
        while (m.find()) {
            Mail mail = new Mail();
            mail.setId(m.group(1));
            mail.setIsNew(!m.group(2).endsWith("f_norm_no.gif"));
            mail.setTheme(Html.fromHtml(m.group(3)).toString());
            if(m.group(4)!=null){
                Matcher userMatcher=userPattern.matcher(m.group(4));
                if (userMatcher.find()){
                    mail.setUserId(userMatcher.group(1));
                    mail.setUser(userMatcher.group(2));
                    
                }else
                    mail.setUser(Html.fromHtml(m.group(4)).toString());
            }
            mail.setDate(Functions.parseForumDateTime(m.group(5), today, yesterday));
            add(mail);
        }

        final Pattern pagesPattern = Pattern.compile("http://4pda.ru/forum/index.php\\?act=Msg&amp;CODE=1&amp;VID=" + m_Vid + "&amp;sort=&amp;st=(\\d+)");
        m = pagesPattern.matcher(body);
        int st = 0;
        while (m.find()) {
            st = Math.max(st, Integer.parseInt(m.group(1)));

        }
        fullLength = st + 1;
    }
    
    public void delete(String id){
        for (int i=0;i<size();i++){
            if(this.get(i).getId().equals(id)){
                this.remove(i);
                break;
            }

        }
    }
}

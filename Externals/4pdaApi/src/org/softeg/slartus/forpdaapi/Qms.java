package org.softeg.slartus.forpdaapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 10:50
 */
public class Qms {

    /**
     *
     * @param httpClient
     * @param userId
     * @param msgCount кол-во сообщений, которые загрузятся
     * @return страницу с чатом
     * @throws IOException
     */
    public static String getChat(IHttpClient httpClient, String userId, String msgCount) throws IOException {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?autocom=qms&a=load&mid="+userId+"&num="+msgCount);
        return pageBody;
    }

    public static QmsUsers getQmsSubscribers(IHttpClient httpClient) throws IOException {
        String pageBody = httpClient.performGet("http://4pda.ru/forum/index.php?autocom=qms&a=list");
        Matcher m= Pattern.compile("<tr class=\"rowtext\"><td><a href=\"http://4pda.ru/forum/index.php\\?autocom=qms&mid=(\\d+)\"><span style='.*?color:(.*?)'>(.*?)</span></a> \\(<a href=\"http://4pda.ru/forum/index.php\\?showuser=\\d+\" target=\"_blank\">\\?</a>\\)</td><td>(.*?)</td><td>(<font color=\"red\">(\\d+)</font>/)?(\\d+)</td></tr>").matcher(pageBody);
        QmsUsers res=new QmsUsers();
        while (m.find()){
            QmsUser qmsUser=new QmsUser();
            qmsUser.setMid(m.group(1));
            qmsUser.setHtmlColor(m.group(2));
            qmsUser.setNick(m.group(3));
            qmsUser.setLastMessageDateTime(m.group(4));
            qmsUser.setNewMessagesCount(m.group(6));
            qmsUser.setMessagesCount(m.group(7));
            res.add(qmsUser);
        }
        return res;
    }
    
    public static String getLastMessageId(String chatBody){
        Matcher m= Pattern.compile("<body onload=\"top.han_result \\( \\d+, 1 \\);\">").matcher(chatBody);
        if(m.find()){
            return m.group(1); 
        }
        return null;
    }

    public static String sendMessage(IHttpClient httpClient, String userId, String message, String lastMessageId, String msgCount) throws IOException {
        Map<String, String> additionalHeaders = new HashMap<String, String>();

        additionalHeaders.put("msg", message);
        additionalHeaders.put("from", lastMessageId);
        String pageBody = httpClient.performPost("http://4pda.ru/forum/index.php?autocom=qms&a=load&mid="+userId+"&num="+msgCount,additionalHeaders);
        return pageBody;
    }

}

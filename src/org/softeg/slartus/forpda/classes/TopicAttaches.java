package org.softeg.slartus.forpda.classes;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 22.05.12
 * Time: 9:39
 */
public class TopicAttaches extends ArrayList<TopicAttach> {
    private final Pattern FULL_ATTACH_PATTERN = Pattern.compile("/forum/dl/post/(.*?)</span>");
    private final Pattern ATTACH_PATTERN = Pattern.compile("(\\d+/.*?)\".*>(.*?)</a> \\( (.*?) \\)<span class=\"desc\">Кол-во скачиваний: (\\d+)");

    public void parseAttaches(String postBody) {
        final Matcher matcher = FULL_ATTACH_PATTERN.matcher(postBody);
        while (matcher.find()) {
            final Matcher m = ATTACH_PATTERN.matcher(matcher.group(1));
            while (m.find()) {
                add("",m.group(4),"http://4pda.ru/forum/dl/post/"+m.group(1), m.group(2),m.group(3),m.group(4));
            }
        }

    }


    public void parseAttaches(String postId, String postNum, String postBody) {
        final Matcher matcher = ATTACH_PATTERN.matcher(postBody);
        while (matcher.find()) {
            add(postId,postNum,"http://4pda.ru"+matcher.group(1), matcher.group(2),"","");
        }
    }
    
    private void add(String postId, String postNum, String url, String fileName, String fileSize,String downloadsCount){
        add(new TopicAttach(postId,  postNum,  url,  fileName, fileSize, downloadsCount));
    }
    
    public CharSequence[] getList(){
        CharSequence[] res=new CharSequence[size()];
        for(int i=0;i<size();i++){
            res[i]=get(i).toString();
        }
        return res;
    }

}

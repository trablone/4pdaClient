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
    private final Pattern ATTACH_PATTERN = Pattern.compile("<a href=\"((http://4pda.ru)?/forum/dl/post/\\d+/.*?)\" title=\"Скачать файл\" target=\"_blank\"><img.*?/>(.*?)</a> \\( (.*?) \\)<span class=\"desc\">Кол-во скачиваний: (\\d+)");

    public void parseAttaches(String postId, String postNum, String postBody) {
        final Matcher matcher = ATTACH_PATTERN.matcher(postBody);
        while (matcher.find()) {
            add(postId,postNum,"http://4pda.ru"+matcher.group(1).replace("http://4pda.ru",""),matcher.group(3),matcher.group(4),matcher.group(5));
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

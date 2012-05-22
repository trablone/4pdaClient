package org.softeg.slartus.forpda.classes;

import android.text.TextUtils;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.EditPostActivity;
import org.softeg.slartus.forpda.MyApp;

/**
 * User: slinkin
 * Date: 26.03.12
 * Time: 16:50
 */
public class TopicBodyBuilder {
    private StringBuilder m_Body = new StringBuilder();
    private Boolean m_Logined, m_Enablesig, m_EnableEmo, m_HidePostForm, m_IsWebviewAllowJavascriptInterface;
    private Topic m_Topic;
    private String m_UrlParams, m_PostBody;
    private TopicAttaches m_TopicAttaches=new TopicAttaches();

    public TopicBodyBuilder(Boolean logined, Topic topic, String urlParams, Boolean enableSig,
                            Boolean enableEmo, String postBody, Boolean hidePostForm,Boolean isWebviewAllowJavascriptInterface) {
        m_IsWebviewAllowJavascriptInterface=isWebviewAllowJavascriptInterface;
        m_Logined = logined;
        m_UrlParams = urlParams;
        m_Topic = topic;
        m_Enablesig = enableSig;
        m_EnableEmo = enableEmo;
        m_PostBody = postBody;
        m_HidePostForm = hidePostForm;
    }

    public void beginTopic() {
        m_Body.append("<html xml:lang=\"en\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n");
        m_Body.append("<head>\n");
        m_Body.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1251\" />\n");
        EditPostActivity.addStyleSheetLink(m_Body);
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/theme.js\"></script>\n");
        m_Body.append("<script type=\"text/javascript\" src=\"file:///android_asset/blockeditor.js\"></script>\n");
        m_Body.append("<title>" + m_Topic.getTitle() + "</title>\n");
        m_Body.append("</head>\n");
        m_Body.append("<body>\n");

        if (m_Topic.getPagesCount() > 1) {
            addButtons(m_Body);
        }

        m_Body.append(getTitleBlock());
    }

    public void endTopic() {
        m_Body.append("<div id=\"entryEnd\"></div>\n");
        m_Body.append("<br/><br/>");
        if (m_Topic.getPagesCount() > 1) {
            addButtons(m_Body);
        }

        m_Body.append("<br/><br/>");
        addPostForm(m_Body);

        m_Body.append(getTitleBlock());


        m_Body.append("<br/><br/><br/><br/><br/><br/>\n");
        m_Body.append("</body>\n");
        m_Body.append("</html>\n");

    }

    public void addPost(Post post, Boolean spoil) {
        m_Body.append("<div id=\"entry" + post.getId() + "\"></div>\n");

        addPostHeader(m_Body, post, post.getId());

        m_Body.append("<div id=\"msg" + post.getId() + "\">");

        if (spoil)
            m_Body.append("<div class='hidetop' style='cursor:pointer;' onclick=\"var _n=this.parentNode.getElementsByTagName('div')[1];if(_n.style.display=='none'){_n.style.display='';}else{_n.style.display='none';}\">Спойлер (+/-) <b>( &gt;&gt;&gt;ШАПКА ТЕМЫ&lt;&lt;&lt;)</b></div><div class='hidemain' style=\"display:none\">");
        String postBody=post.getBody().trim();
        m_TopicAttaches.parseAttaches(post.getId(),post.getNumber(),postBody);
        m_Body.append(postBody);
        if (spoil)
            m_Body.append("</div>");
        m_Body.append("</div>\n\n");
        addFooter(m_Body, post, "End");
        m_Body.append("<div class=\"between_messages\"></div>");
    }

    public String getBody() {
        return m_Body.toString();
    }
    
    public TopicAttaches getTopicAttaches(){
        return m_TopicAttaches;
    }

    public void clear() {
        m_Topic = null;
        m_Body = null;
    }

    private String getTitleBlock() {
        return "<div class=\"topic_title_post\"><a href=\"http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + (TextUtils.isEmpty(m_UrlParams) ? "" : ("&" + m_UrlParams)) + "\">" + m_Topic.getTitle() + "</a></div>\n";
    }

    private void addButtons(StringBuilder sb) {
        Boolean prevDisabled = m_Topic.getCurrentPage() == 1;
        Boolean nextDisabled = m_Topic.getCurrentPage() == m_Topic.getPagesCount();
        sb.append("<center>\n");
        sb.append("<a " + (prevDisabled ? "#" : getHtmlout("firstPage")) + " class=\"href_button" + (prevDisabled ? "_disable" : "") + "\">&lt;&lt;</a>&nbsp;\n");
        sb.append("<a " + (prevDisabled ? "#" : getHtmlout("prevPage")) + " class=\"href_button" + (prevDisabled ? "_disable" : "") + "\">  &lt;  </a>&nbsp;\n");
        sb.append("<a " + getHtmlout("jumpToPage") + " class=\"href_button\">Выбор</a>&nbsp;\n");
        sb.append("<a " + (nextDisabled ? "#" : getHtmlout("nextPage")) + " class=\"href_button" + (nextDisabled ? "_disable" : "") + "\">  &gt;  </a>&nbsp;\n");
        sb.append("<a " + (nextDisabled ? "#" : getHtmlout("lastPage")) + " class=\"href_button" + (nextDisabled ? "_disable" : "") + "\">&gt;&gt;</a>\n");
        sb.append("</center>\n");

    }

    private void addPostForm(StringBuilder sb) {
        Boolean hidePost = !Client.INSTANCE.getLogined() || TextUtils.isEmpty(m_Topic.getForumId()) || TextUtils.isEmpty(m_Topic.getId()) || TextUtils.isEmpty(m_Topic.getAuthKey());
        if (hidePost) return;
        if (m_HidePostForm)
            sb.append("<div><div class='hidetop' style='cursor:pointer;' id=\"hidetxtinput\"  onclick=\"var _n=this.parentNode.getElementsByTagName('div')[1];if(_n.style.display=='none'){_n.style.display='';}else{_n.style.display='none';}\">Спойлер (+/-) <b>( &gt;&gt;&gt;ФОРМА ОТВЕТА&lt;&lt;&lt;)</b></div><div class='hidemain' style=\"display:none\">");

        sb.append(EditPostActivity.getPostForm(m_Enablesig, m_EnableEmo, m_PostBody, m_Topic.isModerator()));
        if (m_HidePostForm)
            sb.append("</div></div><br/><br/><br/>");
    }

    private String getHtmlout(String methodName, String val1, String val2) {
        return getHtmlout(methodName, new String[]{val1, val2});
    }

    private String getHtmlout(String methodName, String val1) {
        return getHtmlout(methodName, new String[]{val1});
    }

    private String getHtmlout(String methodName) {
        return getHtmlout(methodName, new String[0]);
    }

    private String getHtmlout(String methodName, String[] paramValues) {
        StringBuilder sb = new StringBuilder();
        if (!m_IsWebviewAllowJavascriptInterface) {
            sb.append("href=\"http://www.HTMLOUT.ru/");
            sb.append(methodName + "?");
            int i = 0;
            for (String paramName : paramValues) {
                sb.append("val" + i + "=" + paramName + "&");
                i++;
            }

            sb = sb.delete(sb.length() - 1, sb.length());
            sb.append("\"");
        } else {
            sb.append("onclick=\"window.HTMLOUT." + methodName + "(");
            for (String paramName : paramValues) {
                sb.append("'" + paramName + "',");
            }
            if (paramValues.length > 0)
                sb.delete(sb.length() - 1, sb.length());
            sb.append(")\"");
        }
        return sb.toString();
    }


    private void addPostHeader(StringBuilder sb, Post msg, String msgId) {
        String style = MyApp.INSTANCE.getCurrentThemeName();
        String nick = msg.getNick();

        String nickLink = m_Logined ? ("<a " + getHtmlout("showUserMenu", msg.getUserId(), msg.getNick()) + " class=\"system_link\">" + nick + "</a>") : nick;
        String userStateImg = "file:///android_asset/forum/style_images/1/folder_editor_buttons_" + style + (msg.getUserState() ? "/online.png" : "/offline.png");
        String userState = msg.getUserState() ? "post_nick_online" : "post_nick";

        sb.append("<div class=\"post_header\">\n");
        sb.append("\t<table width=\"100%\">\n");
        sb.append("\t\t<tr><td><span class=\""+userState+"\">" + nickLink + "</span></td>\n");
        sb.append("\t\t\t<td><div align=\"right\"><span class=\"post_date\">" + msg.getDate() + "|<a "
                + getHtmlout("showPostLinkMenu", msg.getId()) + ">#" + msg.getNumber() + "</a></span></div></td>\n");
        sb.append("\t\t</tr>\n");
        sb.append("\t\t<tr>\n");
        sb.append("\t\t\t<td>" + getReputation(msg) + "</td>\n");
        if (Client.INSTANCE.getLogined())
            sb.append("\t\t\t<td><div align=\"right\"><a " + getHtmlout("showPostMenu", new String[]{msgId, msg.getCanEdit() ? "1" : "0", (msg.getCanDelete() ? "1" : "0")})
                    + " class=\"system_link\">меню</a></div></td>");
        sb.append("\t\t</tr>");
        sb.append("\t</table>\n");
        sb.append("</div>\n");
    }

    private String getReputation(Post msg) {
        String style = MyApp.INSTANCE.getCurrentThemeName();
        String[] params = new String[]{msg.getId(), msg.getUserId(), msg.getNick(),msg.getCanPlusRep()?"1":"0",msg.getCanMinusRep()?"1":"0"};
        String rep = "<a " + getHtmlout("showRepMenu", params) + "  class=\"system_link\" ><span class=\"post_date\">Реп(" + msg.getUserReputation() + ")</span></a>";
//        if (!msg.getCanMinusRep() || !msg.getCanPlusRep())
        return rep;

//        return "<a " + getHtmlout("minusRep", params) + "><img src=\"file:///android_asset/forum/style_images/1/folder_editor_buttons_" + style
//                + "/r_minus1.gif\" border=\"0\" alt=\"-\" title=\"-\"  style=\"margin-right:3px;\" ></a>"
//                + rep
//                + "<a " + getHtmlout("plusRep", params) + "><img src=\"file:///android_asset/forum/style_images/1/folder_editor_buttons_"
//                + style + "/r_add1.gif\" border=\"0\" alt=\"+\" title=\"+\"  style=\"margin-left:3px;\" ></a>";

    }

    private void addFooter(StringBuilder sb, Post post, String lastMessageId) {

        String style = MyApp.INSTANCE.getCurrentThemeName();
        sb.append("<div class=\"post_footer\"><table width=\"100%\"><tr>");
      //  sb.append("<td width=\"60\"><a href=\"javascript:scroll(0,0);\"><img src=\"file:///android_asset/forum/style_images/1/folder_editor_buttons_" + style + "/p_up.gif\" border=\"0\"  alt=\"^\" /></a>&nbsp;");
        sb.append("<td width=\"50\"><a href=\"javascript:scroll(0,0);\" class=\"system_link\"><span class=\"post_date\">вверх</span></a></td>");
        //sb.append("<a href=\"javascript:scrollToElement('entry" + lastMessageId + "');\"><img src=\"file:///android_asset/forum/style_images/1/folder_editor_buttons_" + style + "/p_down.gif\" border=\"0\"  alt=\"^\" /></a></td>");
        sb.append("<td width=\"50\"><a href=\"javascript:scrollToElement('entry" + lastMessageId + "');\" class=\"system_link\"><span class=\"post_date\">вниз</span></a></td>");
        if (m_Logined) {
            sb.append("<td></td>");
            String params = "'" + post.getId() + "','" + post.getDate() + "','" + post.getNick() + "'";

            sb.append("<td><div style=\"text-align:right\"><a class=\"system_link\" onclick=\"return postQuote(" + params + ");\" >цитата</a></div></td>");
        }
        sb.append("</tr></table></div>\n\n");
    }

    public Topic getTopic() {
        return m_Topic;
    }
}

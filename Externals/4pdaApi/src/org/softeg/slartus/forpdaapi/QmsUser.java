package org.softeg.slartus.forpdaapi;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 11:06
 */
public class QmsUser extends User {
    private String lastMessageDateTime;

    public String getLastMessageDateTime() {
        return lastMessageDateTime;
    }

    public void setLastMessageDateTime(String lastMessageDateTime) {
        this.lastMessageDateTime = lastMessageDateTime;
    }
    
    private String newMessagesCount;
    
    private String messagesCount;

    public String getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(String messagesCount) {
        this.messagesCount = messagesCount;
    }
    
    private String htmlColor;

    public String getHtmlColor() {
        return htmlColor;
    }

    public void setHtmlColor(String htmlColor) {
        this.htmlColor = htmlColor;
    }

    public String getNewMessagesCount() {
        return newMessagesCount;
    }

    public void setNewMessagesCount(String newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }
}

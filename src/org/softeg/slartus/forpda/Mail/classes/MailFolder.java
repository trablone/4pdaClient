package org.softeg.slartus.forpda.Mail.classes;

/**
 * User: slinkin
 * Date: 14.03.12
 * Time: 10:57
 */
public class MailFolder {
    private String m_Title;
    private String m_Vid;
    private int m_Count;

    public MailFolder(String vid, String title, int count) {
        m_Vid = vid;
        m_Title = title;
        m_Count = count;
    }

    public String getTitle() {
        return m_Title;
    }

    public void setTitle(String value) {
        this.m_Title = value;
    }

    public String getVid() {
        return m_Vid;
    }

    public void setVid(String value) {
        this.m_Vid = value;
    }

    public int getCount() {
        return m_Count;
    }

    public void setCount(int count) {
        this.m_Count = m_Count;
    }

    @Override
    public String toString() {
        return m_Title;// + (m_Count > 0 ? (" (" + m_Count + ")") : "");
    }
}

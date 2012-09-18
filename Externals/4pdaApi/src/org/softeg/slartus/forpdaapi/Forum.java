package org.softeg.slartus.forpdaapi;

/**
 * User: slinkin
 * Date: 08.06.12
 * Time: 13:37
 */
public class Forum {
    protected String m_Id;
    private Forum parent;
    protected String m_Title;
    private Forums m_Forums = new Forums();
    public int level = 0;

    public Forum(String id, String title) {
        m_Id = id;
        m_Title = title;
    }
    public String getId() {
        return m_Id;
    }

    public String getTitle() {
        return m_Title;
    }

    public Forum addForum(Forum forum) {
        forum.level = level + 1;
        m_Forums.add(forum);
        forum.setParent(this);
        return forum;
    }

    @Override
    public String toString() {
        return m_Title.toString();
    }

    public void setParent(Forum parent) {
        this.parent = parent;
    }

    public Forum getParent() {
        return parent;
    }

    public Forums getForums() {
        return m_Forums;
    }

    public Forum getLastChild() {
        if (m_Forums.size() == 0)
            return this;
        return m_Forums.get(m_Forums.size() - 1);
    }
}

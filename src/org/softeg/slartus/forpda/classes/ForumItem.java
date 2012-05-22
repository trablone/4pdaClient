package org.softeg.slartus.forpda.classes;

import android.text.Spanned;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 19.09.11
 * Time: 15:17
 * To change this template use File | Settings | File Templates.
 */
public class ForumItem {

    protected String m_Id;

    protected String m_Title;


    public String getId() {
        return m_Id;
    }

    @Override
    public String toString() {
        return m_Title.toString();
    }
}

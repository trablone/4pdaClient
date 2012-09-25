package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 25.10.11
 * Time: 9:59
 */
public class Reputations extends ArrayList<Reputation> {
    public String description;
    public String userId;
    public String user;
    // всего на всех страницах
    public int fullListCount=0;

    public int getMaxCount() {
        return Math.max(fullListCount, size());
    }

}

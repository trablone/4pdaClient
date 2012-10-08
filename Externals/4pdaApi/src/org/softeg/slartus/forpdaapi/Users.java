package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 10:53
 */
public class Users extends ArrayList<User> {
    private int fullLength;

    public Boolean needLoadMore(){
        return getFullLength()>size();
    }

    public int getFullLength() {
        return Math.max(fullLength+1, size());
    }

    public void setFullLength(int fullLength) {
        this.fullLength = fullLength;
    }
}

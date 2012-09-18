package org.softeg.slartus.forpdaapi;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 11:06
 */
public class QmsUsers extends ArrayList<QmsUser>{
    public Boolean hasUnreadMessage(){
        for(int i=0;i<this.size();i++){
            if(!TextUtils.isEmpty(this.get(i).getNewMessagesCount()))
                return true;
        }
        return false;
    }

    public String unreadMessageUsers(){
        String senders = "";

        for(int i=0;i<this.size();i++){
            if(!TextUtils.isEmpty(this.get(i).getNewMessagesCount()))
                senders += this.get(i).getNick() + ",";
        }
        return senders;
    }
}

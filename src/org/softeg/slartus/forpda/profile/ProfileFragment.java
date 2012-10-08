package org.softeg.slartus.forpda.profile;

import android.os.Handler;
import com.actionbarsherlock.app.SherlockFragment;
import org.softeg.slartus.forpdaapi.UserProfile;

/**
 * User: slinkin
 * Date: 27.09.12
 * Time: 10:51
 */
public class ProfileFragment extends SherlockFragment {
    public Boolean Active;
    private Handler mHandler = new Handler();
    public void startLoad() {

    }

    public interface OnProfileChanged {
        void onProfileChanged(UserProfile userProfile);
    }

    public void doOnProfileChanged(UserProfile userProfile) {
        if(m_OnProfileChangedLisener!=null)
            m_OnProfileChangedLisener.onProfileChanged(userProfile);

    }
    private OnProfileChanged m_OnProfileChangedLisener;
    
    public void setOnProfileChangedListener(OnProfileChanged listener){
        m_OnProfileChangedLisener=listener;
    }

    private UserProfile m_UserProfile;
    public void setUserProfile(UserProfile userProfile){
        m_UserProfile=userProfile;
    }
    
    public UserProfile getUserProfile(){
        return m_UserProfile;
    }
    
}

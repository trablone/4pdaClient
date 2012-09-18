package org.softeg.slartus.forpda.classes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.LoginDialog;
import org.softeg.slartus.forpda.Mail.MailBoxActivity;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.ReputationActivity;
import org.softeg.slartus.forpda.qms.QmsContactsActivity;

/**
 * User: slinkin
 * Date: 04.04.12
 * Time: 9:29
 */
public class ProfileMenuFragment extends SherlockFragment {
    private com.actionbarsherlock.view.SubMenu mUserMenuItem;
    private int m_MailItemId = 1234;
    private int m_QmsItemId = 4321;
    private com.actionbarsherlock.view.Menu m_Menu;

    public ProfileMenuFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //
//    public void setMail() {
//        if(m_Menu==null)return ;
//
//        if(m_Menu.findItem(m_MailItemId)!=null){
//            if(Client.INSTANCE.getMailsCount()==0)
//                m_Menu.removeItem(m_MailItemId);
//            else
//                m_Menu.findItem(m_MailItemId).setTitle("Новых писем: "+Client.INSTANCE.getMailsCount());
//        }else{
//            if(Client.INSTANCE.getMailsCount()>0){
//                MenuItem item= m_Menu.add(Menu.NONE,Menu.NONE,0, "Новых писем: "+Client.INSTANCE.getMailsCount()).setIcon(R.drawable.mail);
//                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(MenuItem item) {
//                        Intent intent = new Intent(getActivity(), MailBoxActivity.class);
//
//
//                        getActivity().startActivity(intent);
//                        return true;
//                    }
//                });
//                item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
//                m_MailItemId=item.getItemId();
//            }
//        }
//
//    }
//
    private int getUserIconRes() {
        Boolean logged = Client.INSTANCE.getLogined();
        if (logged) {
//            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
//            Boolean showToast = preferences.getBoolean("ShowNewQmsLsToast", false);
//            String message = null;
            try {
                if (Client.INSTANCE.getMailsCount() > 0 && !TextUtils.isEmpty(Client.INSTANCE.getQms())) {
//                    if (showToast)
//                        message = "QMS: " + Client.INSTANCE.getQms() + "\nЛС: " + Client.INSTANCE.getMailsCount();
                    return R.drawable.user_mail_qms;
                }
                if (Client.INSTANCE.getMailsCount() > 0) {
//                    if (showToast)
//                       // message = "QMS: " + Client.INSTANCE.getQms() + "\nЛС: " + Client.INSTANCE.getMailsCount();
//                        message = "ЛС: " + Client.INSTANCE.getMailsCount();
                    return R.drawable.user_mail;
                }
                if (!TextUtils.isEmpty(Client.INSTANCE.getQms())) {
//                    if (showToast)
//                        message = "QMS: " + Client.INSTANCE.getQms();
                    return R.drawable.user_qms;
                }
                return R.drawable.user_online;
            } finally {
//                if (!TextUtils.isEmpty(message))
//                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        } else {
            return R.drawable.user_offline;
        }

    }

    public void setUserMenu() {
        if (mUserMenuItem == null) return;
        Boolean logged = Client.INSTANCE.getLogined();

        mUserMenuItem.getItem().setIcon(getUserIconRes());
        mUserMenuItem.getItem().setTitle(Client.INSTANCE.getUser());
        mUserMenuItem.clear();
        if (logged) {
            mUserMenuItem.add("Личный ящик").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent intent = new Intent(getActivity(), MailBoxActivity.class);

                    intent.putExtra("activity", getActivity().getClass().toString());
                    getActivity().startActivity(intent);


                    return true;
                }
            });

            mUserMenuItem.add("QMS").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent intent = new Intent(getActivity(), QmsContactsActivity.class);

                    intent.putExtra("activity", getActivity().getClass().toString());
                    getActivity().startActivity(intent);


                    return true;
                }
            });

            mUserMenuItem.add("Репутация").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    ReputationActivity.showRep(getActivity(), Client.INSTANCE.UserId);


                    return true;
                }
            });

            mUserMenuItem.add("Выход").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    LoginDialog.logout(getActivity());


                    return true;
                }
            });
        } else {
            mUserMenuItem.add("Вход").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    LoginDialog.showDialog(getActivity(), null);
                    return true;
                }
            });

            mUserMenuItem.add("Регистрация").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent marketIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://4pda.ru/forum/index.php?act=Reg&CODE=00"));
                    getActivity().startActivity(marketIntent);
                    //
                    return true;
                }
            });
        }
    }

    private void createUserMenu(com.actionbarsherlock.view.Menu menu) {
        m_Menu = menu;
        mUserMenuItem = menu.addSubMenu(Client.INSTANCE.getUser());

        mUserMenuItem.getItem().setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        m_Menu = menu;
        createUserMenu(menu);

    }
}

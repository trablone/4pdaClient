package org.softeg.slartus.forpda.qms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.webkit.WebView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.Qms;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 15:50
 */
public class QmsChatActivity extends BaseFragmentActivity {
    private WebView wvChat;
    private String m_Id;
    private String m_Nick;
    MenuFragment mFragment1;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qms_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

        wvChat=(WebView)findViewById(R.id.wvChat);
        wvChat.getSettings().setBuiltInZoomControls(true);
        wvChat.getSettings().setSupportZoom(true);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString("UserId");
        m_Nick  = extras.getString("UserNick");
        setTitle(m_Nick + " - QMS");
        reLoadChat();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private void reLoadChat(){
        new LoadTask(this).execute();
    }


    private void createActionMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            com.actionbarsherlock.view.MenuItem item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    ((QmsChatActivity)getActivity()).reLoadChat();
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;
        public String m_ChatBody;


        public LoadTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                m_ChatBody = Qms.getChat(Client.INSTANCE,m_Id,"10");

                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка истории...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", m_ChatBody, "text/html", "UTF-8", null);
            } else {
                if (ex != null)
                    Log.e(QmsChatActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }
}

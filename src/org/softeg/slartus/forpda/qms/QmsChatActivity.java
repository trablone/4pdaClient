package org.softeg.slartus.forpda.qms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.Qms;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 15:50
 */
public class QmsChatActivity extends BaseFragmentActivity {
    private WebView wvChat;
    private String m_Id;
    private String m_Nick;
    private String m_LastMessageId;
    private EditText edMessage;
    private String m_MessagesCount = "10";
    private MenuFragment mFragment1;
    private long m_UpdateTimeout = 15000;
    private Timer m_UpdateTimer = new Timer();
    private View pbLoading;
    final Handler uiHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qms_chat);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();
        pbLoading = findViewById(R.id.pbLoading);
        edMessage = (EditText) findViewById(R.id.edMessage);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startSendMessage();
            }
        });


        wvChat = (WebView) findViewById(R.id.wvChat);
        wvChat.getSettings().setBuiltInZoomControls(true);
        wvChat.getSettings().setSupportZoom(true);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        m_Id = extras.getString("UserId");
        m_Nick = extras.getString("UserNick");
        setTitle(m_Nick + " - QMS");

    }
    
    public static void openChat(Activity activity,String userId, String userNick){
        Intent intent = new Intent(activity, QmsChatActivity.class);
        intent.putExtra("UserId", userId);
        intent.putExtra("UserNick", userNick);

        activity.startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();
        startUpdateTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }


    @Override
    public void onStop() {
        super.onStop();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return true;
    }

    private void loadPrefs(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        m_MessagesCount= Integer.toString(ExtPreferences.parseInt(preferences,"qms.chat.messages_count",10));
        m_UpdateTimeout= ExtPreferences.parseInt(preferences,"qms.chat.update_timer",15)*1000;
    }

    private void reLoadChatSafe() {
        uiHandler.post(new Runnable() {
            public void run() {
                pbLoading.setVisibility(View.VISIBLE);
            }
        });
        String chatBody = null;
        Exception ex = null;
        try {
            chatBody = Qms.getChat(Client.INSTANCE, m_Id, m_MessagesCount);
        } catch (IOException e) {
            ex = e;
        }
        final Exception finalEx = ex;
        final String finalChatBody = chatBody;
        uiHandler.post(new Runnable() {
            public void run() {
                pbLoading.setVisibility(View.GONE);
                if (finalEx == null) {
                    m_LastMessageId = Qms.getLastMessageId(finalChatBody);
                    wvChat.loadDataWithBaseURL("\"file:///android_asset/\"", finalChatBody, "text/html", "UTF-8", null);
                } else {
                    if (finalEx != null)
                        Log.e(QmsChatActivity.this, finalEx);
                    else
                        Toast.makeText(QmsChatActivity.this, "Неизвестная ошибка",
                                Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void startUpdateTimer() {
        m_UpdateTimer.cancel();
        m_UpdateTimer.purge();
        m_UpdateTimer = new Timer();
        m_UpdateTimer.schedule(new TimerTask() { // Определяем задачу
            @Override
            public void run() {
                reLoadChatSafe();
            }
        }, 0L, m_UpdateTimeout);

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

    private String m_MessageText = null;

    private void startSendMessage() {
        m_MessageText = edMessage.getText().toString();
        if (TextUtils.isEmpty(m_MessageText)) {
            Toast.makeText(this, "Введите текст для отправки.", Toast.LENGTH_SHORT).show();
            return;
        }
        new SendTask(this).execute();
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;
        public String m_ChatBody;


        public SendTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                m_ChatBody = Qms.sendMessage(Client.INSTANCE, m_Id, m_MessageText, m_LastMessageId, m_MessagesCount);

                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                edMessage.getText().clear();
                m_LastMessageId = Qms.getLastMessageId(m_ChatBody);
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
                    ((QmsChatActivity) getActivity()).reLoadChatSafe();
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Настройки").setIcon(R.drawable.settings);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), QmsPreferencesActivity.class);
                    getActivity().startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }
}

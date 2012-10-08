package org.softeg.slartus.forpda.Mail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.classes.BbCodesPanel;
import org.softeg.slartus.forpda.common.HtmlUtils;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 15.03.12
 * Time: 14:26
 */
public class EditMailActivity extends BaseFragmentActivity {
    // Заголовок сообщения должен быть более 1 символа.
    // Тело сообщения должен быть более 1 символа.
    // самому себе можно
    // если какого-то пользователя нет-ошибка
    public static final String KEY_PARAMS = "params";
    public static final String KEY_REPLY = "reply";
    public static final String KEY_USER = "user";
    public static final String KEY_TITLE = "title";
    public static final String KEY_FROM_EDIT = "from_edit";
    public static final String KEY_RETERN_BACK = "retrun_back";
    private String m_AttachPostKey="";
    private LinearLayout lnrBbCodes;
    private EditText txtPost, entered_name, msg_title;
    private Button btnAddRecipient, btnRecipients;
    // подтверждение отправки
    private Boolean m_ConfirmSend = true, mReturnBack=false;
    private CheckBox mt_hide_cc, add_sent, add_tracking;
    MenuFragment mFragment1;
    Handler mHandler = new Handler();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_mail);

        createMenu();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        m_ConfirmSend = prefs.getBoolean("theme.ConfirmSend", true);

        lnrBbCodes = (LinearLayout) findViewById(R.id.lnrBbCodes);
        txtPost = (EditText) findViewById(R.id.txtPost);
        new BbCodesPanel(this, lnrBbCodes, txtPost);

        mt_hide_cc = (CheckBox) findViewById(R.id.mt_hide_cc);
        add_sent = (CheckBox) findViewById(R.id.add_sent);
        add_tracking = (CheckBox) findViewById(R.id.add_tracking);

        entered_name = (EditText) findViewById(R.id.entered_name);
        msg_title = (EditText) findViewById(R.id.msg_title);

        btnRecipients = (Button) findViewById(R.id.btnRecipients);
        btnRecipients.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showRecipientsListDialog();
            }
        });

        findViewById(R.id.btnAddRecipient).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showAddRecipientDialog();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                mReturnBack = extras.getBoolean(KEY_RETERN_BACK);
                if (extras.containsKey(KEY_REPLY))
                    setTitle("Ответное сообщение");
                if (extras.containsKey(KEY_PARAMS)) {
                    LoadTask loadTask = new LoadTask(this, extras.getString(KEY_PARAMS)
                            , extras.getString(KEY_USER), extras.getString(KEY_TITLE));
                    loadTask.execute();
                }
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void createMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }

    public static void sendMessage(Activity activity,String params, String userNick, Boolean returnBack){
        Intent intent = new Intent(activity, EditMailActivity.class);
        intent.putExtra(EditMailActivity.KEY_PARAMS, params);
        intent.putExtra(EditMailActivity.KEY_USER, userNick);
        intent.putExtra(EditMailActivity.KEY_RETERN_BACK, returnBack);
        activity.startActivity(intent);
    }
    
    private void showAddRecipientDialog() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance();

        newFragment.show(ft, "dialog");
    }

    public void addRecipient(String recipient) {
        recipient = recipient.trim();
        for (String existrecipient : m_Recipients) {
            if (existrecipient.toLowerCase().equals(recipient.toLowerCase())) {
                Toast.makeText(this, "Пользователь уже есть в списке", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        m_Recipients.add(recipient);
        updateRecipientsInfo();
    }

    private void deleteRecipient(String recipient) {
        m_Recipients.remove(recipient);
        updateRecipientsInfo();
    }

    private void updateRecipientsInfo() {
        btnRecipients.setText("Копии пользователям (" + m_Recipients.size() + ")");
        mt_hide_cc.setVisibility(m_Recipients.size() > 0 ? View.VISIBLE : View.GONE);
    }

    private Dialog mRecipientsListDialog;
    private ArrayList<String> m_Recipients = new ArrayList<String>();

    private void showRecipientsListDialog() {
        if (m_Recipients.size() == 0) {
            Toast.makeText(this, "Ни одного получателя не добавлено", Toast.LENGTH_SHORT).show();
            return;
        }

        RecipientsAdapter adapter = new RecipientsAdapter(m_Recipients, this);

        mRecipientsListDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setSingleChoiceItems(adapter, -1, null)
                .create();
        mRecipientsListDialog.show();
    }

    public void startReply() {
        // получатель
        final String enteredName = entered_name.getText().toString();
        final Boolean mtHideCc = mt_hide_cc.isChecked();
        final String msgTitle = msg_title.getText().toString();
        final String postBody = txtPost.getText().toString();
        final Boolean addSent = add_sent.isChecked();
        final Boolean addTracking = add_tracking.isChecked();

        if (TextUtils.isEmpty(enteredName)) {
            Toast.makeText(this, "Необходимо ввести имя пользователя!", Toast.LENGTH_SHORT).show();
            entered_name.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(msgTitle) || msgTitle.trim().length() < 2) {
            Toast.makeText(this, "Заголовок сообщения должен быть более 2 символов!", Toast.LENGTH_SHORT).show();
            msg_title.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(postBody) || postBody.trim().length() < 2) {
            Toast.makeText(this, "Текст сообщения должен быть более 2 символов!", Toast.LENGTH_SHORT).show();
            txtPost.requestFocus();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Подтвердите действие")
                .setMessage("Отправить сообщение?")
                .setPositiveButton("Отправить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        SendTask sendTask = new SendTask(EditMailActivity.this, enteredName,
                                mtHideCc,
                                msgTitle,
                                postBody,
                                addSent,
                                addTracking, m_Recipients);
                        sendTask.execute();

                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    public static final class MenuFragment extends SherlockFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            com.actionbarsherlock.view.MenuItem item = menu.add("Отправить").setIcon(android.R.drawable.ic_menu_send);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    ((EditMailActivity) getActivity()).startReply();
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

        }
    }

    private void showMailBox() {
        try {
            Intent intent = new Intent(this.getApplicationContext(), MailBoxActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(KEY_FROM_EDIT, true);

            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(this, ex);
        }


    }

    public static class MyDialogFragment extends SherlockDialogFragment {
        int mNum;

        /**
         * Create a new instance of MyDialogFragment, providing "num"
         * as an argument.
         */
        static MyDialogFragment newInstance() {
            MyDialogFragment f = new MyDialogFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();

            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mNum = getArguments().getInt("num");

            // Pick a style based on the num.
            int style = DialogFragment.STYLE_NORMAL;
            int theme = 0;

            setStyle(style, theme);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.edit_text_dialog, container, false);
            final TextView tv = (TextView) v.findViewById(R.id.text);


            // Watch for button clicks.
            Button button = (Button) v.findViewById(R.id.show);
            button.setText("Добавить");
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (TextUtils.isEmpty(tv.getText().toString())) {
                        Toast.makeText(getActivity(), "Введите имя получателя!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ((EditMailActivity) getActivity()).addRecipient(tv.getText().toString());
                    dismiss();
                }
            });

            return v;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setTitle("Введите имя пользователя");
            return dialog;

        }
    }

    public class RecipientsAdapter extends BaseAdapter {
        private Activity activity;
        private final ArrayList<String> content;

        public RecipientsAdapter(ArrayList<String> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public String getItem(int i) {
            return content.get(i);
        }

        public long getItemId(int i) {
            return i;
        }


        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                final LayoutInflater inflater = activity.getLayoutInflater();

                convertView = inflater.inflate(R.layout.attachment_spinner_item, parent, false);


                holder = new ViewHolder();


                holder.btnDelete = (ImageButton) convertView
                        .findViewById(R.id.btnDelete);
                holder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mRecipientsListDialog.dismiss();

                        String attach = (String) view.getTag();
                        deleteRecipient(attach);
                    }
                });

                holder.txtFile = (TextView) convertView
                        .findViewById(R.id.txtFile);
                holder.txtFile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mRecipientsListDialog.dismiss();

                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String attach = this.getItem(position);
            holder.btnDelete.setTag(attach);
            holder.txtFile.setText(attach);
            holder.txtFile.setTag(attach);

            return convertView;
        }

        public class ViewHolder {

            ImageButton btnDelete;
            TextView txtFile;
        }
    }

    private class SendTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        String enteredName;
        Boolean mtHideCc;
        String msgTitle;
        String postBody;
        Boolean addSent;
        Boolean addTracking;
        ArrayList<String> recipients;

        public SendTask(Context context, String enteredName,
                        Boolean mtHideCc,
                        String msgTitle,
                        String postBody,
                        Boolean addSent,
                        Boolean addTracking, ArrayList<String> recipients) {
            mContext = context;
            this.enteredName = enteredName;
            this.mtHideCc = mtHideCc;  //!
            this.msgTitle = msgTitle;
            this.postBody = postBody;
            this.addSent = addSent;
            this.addTracking = addTracking;
            this.recipients = recipients;
            dialog = new ProgressDialog(mContext);
        }

        private void send() throws IOException {
            Map<String, String> additionalHeaders = new HashMap<String, String>();
            additionalHeaders.put("act", "Msg");
            if (addSent)
                additionalHeaders.put("add_sent", "yes");
            if (addTracking)
                additionalHeaders.put("add_tracking", "1");
            additionalHeaders.put("attach_post_key", m_AttachPostKey);
            additionalHeaders.put("auth_key", Client.INSTANCE.getAuthKey());
            additionalHeaders.put("carbon_copy", TextUtils.join("\n", recipients));
            additionalHeaders.put("CODE", "04");
            additionalHeaders.put("entered_name", enteredName);
            if (mtHideCc)
                additionalHeaders.put("mt_hide_cc", "1");
            additionalHeaders.put("MODE", "01");
            additionalHeaders.put("msg_title", msgTitle);
            additionalHeaders.put("OID", "0");
            additionalHeaders.put("Post", postBody);
            additionalHeaders.put("removeattachid", "0");
            String res = Client.INSTANCE.performPost("http://4pda.ru/forum/index.php?act=msg", additionalHeaders);
           // Log.sendMail(mHandler, mContext,"Не отправляет ЛС","",res);
            checkErrors(res);
        }



        private void checkErrors(String body) throws NotReportException {
            Matcher errorsMatcher = Pattern.compile("<div class=\"errorwrap\"><h4>(.*?)</h4><p>(.*?)</p></div>").matcher(body);
            if (errorsMatcher.find()) {
                throw new NotReportException(Html.fromHtml(errorsMatcher.group(2)).toString());
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                send();

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
                if (mReturnBack)
                    finish();
                else
                    showMailBox();
//                MailActivity.this.setTitle(m_Mail.getTheme());
//                author_text.setText(m_Mail.getUser());
//                date_text.setText(m_Mail.getDate());
//                mWvBody.loadDataWithBaseURL("\"file:///android_asset/\"", m_Mail.getHtmlBody(), "text/html", "UTF-8", null);


            } else {
                if (ex != null)
                    Log.e(EditMailActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        private String m_Params;


        public LoadTask(Context context,
                        String params, String user, String title) {
            mContext = context;
            m_Params = params;
            enteredName = user;
            msgTitle = title;
            dialog = new ProgressDialog(mContext);
        }

        String enteredName = "";
        String postBody = "";
        String msgTitle = "";

        private void parseBody(String body) {
            Matcher attachPostKeyMatcher=Pattern.compile("name=\"attach_post_key\" value=\"(.*?)\"").matcher(body);
            Matcher enteredNameMatcher = Pattern.compile("<input type=\"text\" id='entered_name'.*?value=\"(.*?)\"").matcher(body);
            Matcher postMatcher = Pattern.compile("<textarea name=\"Post\".*?>([\\s\\S]*?)</textarea>").matcher(body);
            Matcher msgTitleMatcher = Pattern.compile("<input type=\"text\" name=\"msg_title\".*?value=\"(.*?)\" />").matcher(body);
            
            if(attachPostKeyMatcher.find()){
                m_AttachPostKey= attachPostKeyMatcher.group(1);
            }
            if (enteredNameMatcher.find()) {
                enteredName = enteredNameMatcher.group(1);
            }
            if (postMatcher.find()) {
                postBody = HtmlUtils.modifyHtmlQuote(postMatcher.group(1));
            }
            if (msgTitleMatcher.find()) {
                msgTitle = Html.fromHtml(msgTitleMatcher.group(1)).toString();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String body = Client.INSTANCE.performGet("http://4pda.ru/forum/index.php?" + m_Params);
                
                parseBody(body);

                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                entered_name.setText(enteredName);
                txtPost.setText(postBody);
                msg_title.setText(msgTitle);
//                MailActivity.this.setTitle(m_Mail.getTheme());
//                author_text.setText(m_Mail.getUser());
//                date_text.setText(m_Mail.getDate());
//                mWvBody.loadDataWithBaseURL("\"file:///android_asset/\"", m_Mail.getHtmlBody(), "text/html", "UTF-8", null);


            } else {
                if (ex != null)
                    Log.e(EditMailActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }
}

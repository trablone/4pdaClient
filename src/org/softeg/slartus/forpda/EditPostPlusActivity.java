package org.softeg.slartus.forpda;

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
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.lamerman.FileDialog;
import org.softeg.slartus.forpda.classes.BbCodesPanel;
import org.softeg.slartus.forpda.classes.common.FileUtils;
import org.softeg.slartus.forpda.common.HtmlUtils;
import org.softeg.slartus.forpda.common.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 08.11.11
 * Time: 12:42
 */
public class EditPostPlusActivity extends BaseFragmentActivity {
    private LinearLayout lnrBbCodes;
    private EditText txtPost;
    private CheckBox chkEnablesig, chkEnableEmo;
    private Button btnSelectFile;
    private Button  btnAddFile;
    private String forumId;
    private String attachFilePath;
    private String lastSelectDirPath = "/sdcard";
    private String themeId;
    private String postId;
    private String authKey;
    private String attachPostKey;
    // подтверждение отправки
    private Boolean m_ConfirmSend = true;
    // флаг добавлять подпись к сообщению
    private Boolean m_Enablesig = true;
    private Boolean m_EnableEmo = true;
    private int REQUEST_SAVE = 0;
    private MenuFragment mFragment1;
    private String postText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

//        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        setTheme(MyApp.INSTANCE.getThemeStyleResID());
        setContentView(R.layout.edit_post_plus);

        createActionMenu();
        lastSelectDirPath = prefs.getString("EditPost.AttachDirPath", lastSelectDirPath);
        m_ConfirmSend = prefs.getBoolean("theme.ConfirmSend", true);

        lnrBbCodes = (LinearLayout) findViewById(R.id.lnrBbCodes);
        txtPost = (EditText) findViewById(R.id.txtPost);
        new BbCodesPanel(this,lnrBbCodes,txtPost);
        chkEnablesig = (CheckBox) findViewById(R.id.chkEnablesig);
        chkEnableEmo = (CheckBox) findViewById(R.id.chkEnableEmo);

        btnSelectFile = (Button) findViewById(R.id.btnSelectFile);
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showAttachesListDialog();
            }
        });

        btnAddFile = (Button) findViewById(R.id.btnAddFile);
        btnAddFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (TextUtils.isEmpty(txtPost.getText().toString())) {
                    Toast.makeText(EditPostPlusActivity.this, "Вы должны ввести сообщение", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditPostPlusActivity.this.getBaseContext(),
                        FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, lastSelectDirPath);
                EditPostPlusActivity.this.startActivityForResult(intent, REQUEST_SAVE);
            }
        });


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        forumId = extras.getString("forumId");
        themeId = extras.getString("themeId");
        postId = extras.getString("postId");
        authKey = extras.getString("authKey");
        if (isNewPost()) {
            txtPost.setText(extras.getString("body"));
        }
        startLoadPost();
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

    private Boolean isNewPost() {
        return postId.equals("-1");
    }

    private Dialog mAttachesListDialog;

    private void showAttachesListDialog() {
        if (attaches.size() == 0) {
            Toast.makeText(this, "Ни одного файла не загружено", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] caps = new String[attaches.size()];
        int i = 0;
        for (Attach attach : attaches) {
            caps[i++] = attach.getName();
        }
        AttachesAdapter adapter = new AttachesAdapter(attaches, this);
        //  ListAdapter adapter = new ArrayAdapter<Attach>(this, R.layout.attachment_spinner_item, attaches);
        mAttachesListDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setSingleChoiceItems(adapter, -1, null)
                .create();
        mAttachesListDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_SAVE) {
                attachFilePath = data.getStringExtra(FileDialog.RESULT_PATH);

                saveAttachDirPath();

                m_Enablesig = chkEnablesig.isChecked();
                m_EnableEmo = chkEnableEmo.isChecked();
                new UpdateTask(EditPostPlusActivity.this).execute(txtPost.getText().toString());
            }
        }
    }

    private void saveAttachDirPath() {
        lastSelectDirPath = FileUtils.getDirPath(attachFilePath);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EditPost.AttachDirPath", lastSelectDirPath);
        editor.commit();
    }

    private void startLoadPost() {
        new LoadTask(this).execute();
    }

    private void sendPost(final String text) {
        m_Enablesig = chkEnablesig.isChecked();
        m_EnableEmo = chkEnableEmo.isChecked();
        if (isNewPost()) {
            new PostTask(EditPostPlusActivity.this).execute(text);
        } else {
            new AcceptEditTask(EditPostPlusActivity.this).execute(text);
        }
    }

    private void parsePody(String body) {
        String startFlag = "<textarea name=\"Post\" rows=\"8\" cols=\"150\" style=\"width:98%; height:160px\" tabindex=\"0\">";
        int startIndex = body.indexOf(startFlag);
        startIndex += startFlag.length();
        int endIndex = body.indexOf("</textarea>", startIndex);

        if (TextUtils.isEmpty(txtPost.getText().toString()))
            txtPost.setText(HtmlUtils.modifyHtmlQuote(body.substring(startIndex, endIndex)));

        Pattern pattern = Pattern.compile("name='attach_post_key' value='(.*?)'");
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            EditPostPlusActivity.this.attachPostKey = m.group(1);
        }
        parseAttaches(body);
    }

    private ArrayList<Attach> attaches = new ArrayList<Attach>();

    private void parseAttaches(String body) {
        Pattern pattern = Pattern.compile("onclick=\"insText\\('\\[attachment=(\\d+):(.*?)\\]'\\)");
        Pattern attachBodyPattern = Pattern.compile("<!-- ATTACH -->([\\s\\S]*?)</i>", Pattern.MULTILINE);
        Matcher m = attachBodyPattern.matcher(body);
        attaches = new ArrayList<Attach>();
        if (m.find()) {
            Matcher m1 = pattern.matcher(m.group(1));
            while (m1.find()) {
                attaches.add(new Attach(m1.group(1), m1.group(2)));
            }
        } else {
            Pattern checkPattern = Pattern.compile("\t\t<h4>Причина:</h4>\n" +
                    "\n" +
                    "\t\t<p>(.*?)</p>", Pattern.MULTILINE);
            m = checkPattern.matcher(body);
            if (m.find()) {
                Toast.makeText(this, m.group(1), Toast.LENGTH_LONG).show();
            }
        }
        btnSelectFile.setText("Управление текущими файлами (" + attaches.size() + ")");

    }

    public String getPostText() {
        return txtPost.getText().toString();
    }

    public boolean getConfirmSend() {
        return m_ConfirmSend;
    }

    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {
            super();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            com.actionbarsherlock.view.MenuItem item;

            item = menu.add("Отправить").setIcon(android.R.drawable.ic_menu_send);
            item.setVisible(Client.INSTANCE.getLogined());
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    final String body = ((EditPostPlusActivity)getActivity()).getPostText();
                    if (TextUtils.isEmpty(body))
                        return true;

                    if (((EditPostPlusActivity)getActivity()).getConfirmSend()) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Уверены?")
                                .setMessage("Подтвердите отправку")
                                .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        ((EditPostPlusActivity)getActivity()).sendPost(body);

                                    }
                                })
                                .setNegativeButton("Отмена", null)
                                .create().show();
                    } else {
                        ((EditPostPlusActivity)getActivity()).sendPost(body);
                    }

                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);


        }

        public NewsActivity getInterface() {
            return (NewsActivity)getActivity();
        }
    }

    private class UpdateTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public UpdateTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                body = Client.INSTANCE.attachFilePost(forumId, themeId, authKey, attachPostKey,
                        postId, m_Enablesig,m_EnableEmo, params[0], attachFilePath);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка файла...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parseAttaches(body);

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class DeleteTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public DeleteTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                body = Client.INSTANCE.deleteAttachFilePost(forumId, themeId, authKey, attachPostKey, postId, m_Enablesig,
                        params[0],
                        attachFilePath);
                return true;
            } catch (Exception e) {
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Удаление файла...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parseAttaches(body);

            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class AcceptEditTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public AcceptEditTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Client.INSTANCE.editPost(forumId, themeId, authKey, postId, m_Enablesig, m_EnableEmo,
                        params[0]);
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Редактирование сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                ThemeActivity.s_ThemeId = themeId;
                ThemeActivity.s_Params = "view=findpost&p=" + postId;
                EditPostPlusActivity.this.finish();
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class LoadTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public LoadTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        String body;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                body = Client.INSTANCE.getEditPostPlus(forumId, themeId, postId, authKey);
                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                parsePody(body);
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;
        private String mPostResult = null;

        public PostTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }


        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = Client.INSTANCE.reply(forumId, themeId, authKey, attachPostKey,
                        params[0], m_Enablesig, m_EnableEmo);

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
                if (!TextUtils.isEmpty(mPostResult)) {
                    Toast.makeText(mContext, "Ошибка: " + mPostResult, Toast.LENGTH_LONG).show();
                    return;
                }
                ThemeActivity.s_ThemeId = themeId;
                if (isNewPost())
                    ThemeActivity.s_Params = "view=getlastpost";
                else
                    ThemeActivity.s_Params = "view=findpost&p=" + postId;

                EditPostPlusActivity.this.finish();
            } else {
                if (ex != null)
                    Log.e(EditPostPlusActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }

    public class AttachesAdapter extends BaseAdapter {
        private Activity activity;
        private final ArrayList<Attach> content;

        public AttachesAdapter(ArrayList<Attach> content, Activity activity) {
            super();
            this.content = content;
            this.activity = activity;
        }

        public int getCount() {
            return content.size();
        }

        public Attach getItem(int i) {
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
                        mAttachesListDialog.dismiss();

                        Attach attach = (Attach) view.getTag();
                        attachFilePath = attach.getId();
                        new DeleteTask(EditPostPlusActivity.this).execute(txtPost.getText().toString());
                    }
                });

                holder.txtFile = (TextView) convertView
                        .findViewById(R.id.txtFile);
                holder.txtFile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        mAttachesListDialog.dismiss();
                        int selectionStart = txtPost.getSelectionStart();
                        if (selectionStart == -1)
                            selectionStart = 0;
                        Attach attach = (Attach) view.getTag();
                        txtPost.getText().insert(selectionStart, "[attachment=" + attach.getId() + ":" + attach.getName() + "]");
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Attach attach = this.getItem(position);
            holder.btnDelete.setTag(attach);
            holder.txtFile.setText(attach.getName());
            holder.txtFile.setTag(attach);

            return convertView;
        }

        public class ViewHolder {

            ImageButton btnDelete;
            TextView txtFile;
        }
    }

    private class Attach {
        private String mId;
        private String mName;

        public Attach(String id, String name) {
            mId = id;
            mName = name;
        }

        public String getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        @Override
        public String toString() {
            return mName;
        }
    }
}


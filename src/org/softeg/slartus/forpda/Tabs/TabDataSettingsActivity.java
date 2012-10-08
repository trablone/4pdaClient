package org.softeg.slartus.forpda.Tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.ForumsAdapter;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpdaapi.NotReportException;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * User: slinkin
 * Date: 25.10.11
 * Time: 16:10
 */
public class TabDataSettingsActivity extends Activity {

    private EditText username_edit, template_name_edit, query_edit;

    private CheckBox chkSubforums;


    private Spinner spnrSource, spnrSort, spnrTemplates;
    private Button btnAddForum;
    private Handler mHandler = new Handler();

    private String m_TabId;
    private String m_Template;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(MyApp.INSTANCE.getThemeStyleResID());
        setContentView(R.layout.tab_data_settings);

        spnrSource = (Spinner) findViewById(R.id.spnrSource);

        spnrTemplates = (Spinner) findViewById(R.id.spnrTemplates);


        spnrTemplates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setEnablesByTemplate();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        spnrSort = (Spinner) findViewById(R.id.spnrSort);
        spnrSort.setSelection(1);

        username_edit = (EditText) findViewById(R.id.username_edit);
        query_edit = (EditText) findViewById(R.id.query_edit);
        template_name_edit = (EditText) findViewById(R.id.template_name_edit);


        btnAddForum = (Button) findViewById(R.id.btnAddForum);
        btnAddForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (Client.INSTANCE.MainForum == null || Client.INSTANCE.MainForum.getForums().size() == 0) {
                    loadForums();
                } else {
                    showForums();
                }
            }
        });
        chkSubforums = (CheckBox) findViewById(R.id.chkSubforums);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        m_TabId = extras.getString("tabId");
        m_Template = extras.getString("template");

        loadSettings();
        setEnablesByTemplate();
    }

    private String getTabId() {
        return m_TabId;
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onBackPressed() {

        saveSettings();
        Toast.makeText(this, "Изменения вступят в силу после перезапуска программы!", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    private void loadSettings() {
        String tabTag = getTabId();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String template_source = preferences.getString(tabTag + ".Template.Source", "all");
        String template = preferences.getString(tabTag + ".Template", m_Template);
        String template_name = preferences.getString(tabTag + ".Template.Name", "");
        String template_sort = preferences.getString(tabTag + ".Template.Sort", "dd");
        String template_username = preferences.getString(tabTag + ".Template.UserName", "");
        String template_query = preferences.getString(tabTag + ".Template.Query", "");
        String template_forums = preferences.getString(tabTag + ".Template.Forums", "");
        m_CheckedIds = loadChecks(template_forums);
        Boolean template_Subforums = preferences.getBoolean(tabTag + ".Template.Subforums", true);

        if (template.equals(Tabs.TAB_SEARCH) && TextUtils.isEmpty(template_name)) {
            if (template_source.equals("all") && template_sort.equals("dd") && TextUtils.isEmpty(template_username)
                    && (m_CheckedIds.size() == 0) && template_Subforums)
                template_name = "Последние";
            else
                template_name = "Поиск";
        }


        setSpinnerValue(spnrSource, R.array.SearchSourceValues, template_source);
        setSpinnerValue(spnrTemplates, R.array.themesTemplatesValues, template);
        template_name_edit.setText(template_name);
        setSpinnerValue(spnrSort, R.array.SearchSortValues, template_sort);
        username_edit.setText(template_username);
        query_edit.setText(template_query);
        setSelectedFroumsText();
        chkSubforums.setChecked(template_Subforums);
    }


    private void setEnablesByTemplate() {
        String selectedTemplate = getSpinnerValue(spnrTemplates, R.array.themesTemplatesValues);
        Boolean isSearchTemplate = selectedTemplate.equals(Tabs.TAB_SEARCH);
        Boolean isForumsTemplate = selectedTemplate.equals(Tabs.TAB_FORUMS);

        int visibility = isSearchTemplate ? View.VISIBLE : View.GONE;

        findViewById(R.id.username_row).setVisibility(visibility);
        findViewById(R.id.query_row).setVisibility(visibility);
        findViewById(R.id.source_row).setVisibility(visibility);
        findViewById(R.id.sort_row).setVisibility(visibility);

        visibility = (isSearchTemplate || isForumsTemplate) ? View.VISIBLE : View.GONE;
        findViewById(R.id.name_row).setVisibility(visibility);
        findViewById(R.id.forums_row).setVisibility(visibility);
        findViewById(R.id.subforums_row).setVisibility(visibility);

    }


    private void saveSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        String tabTag = getTabId();
        editor.putString(tabTag + ".Template.Source", getSpinnerValue(spnrSource, R.array.SearchSourceValues));
        editor.putString(tabTag + ".Template", getSpinnerValue(spnrTemplates, R.array.themesTemplatesValues));
        editor.putString(tabTag + ".Template.Name", template_name_edit.getText().toString());
        editor.putString(tabTag + ".Template.Sort", getSpinnerValue(spnrSort, R.array.SearchSortValues));
        editor.putString(tabTag + ".Template.UserName", username_edit.getText().toString());
        editor.putString(tabTag + ".Template.Query", query_edit.getText().toString());
        editor.putString(tabTag + ".Template.Forums", getCheckedIdsString(m_CheckedIds));
        editor.putBoolean(tabTag + ".Template.Subforums", chkSubforums.isChecked());
        editor.commit();
    }

    private String getSpinnerValue(Spinner spinner, int valuesResId) {
        return getResources().getStringArray(valuesResId)[spinner.getSelectedItemPosition()];
    }

    private void setSpinnerValue(Spinner spinner, int valuesResId, String value) {
        ArrayList<String> values = new ArrayList();
        Collections.addAll(values, getResources().getStringArray(valuesResId));
        int index = values.indexOf(value);
        if (index == -1)
            index = 0;
        spinner.setSelection(index);

    }

    public static final String pairsDelimiter = "¶";
    public static final String pairValuesDelimiter = "µ";

    public static Hashtable<String, CharSequence> loadChecks(String checksString) {
        Hashtable<String, CharSequence> res = new Hashtable<String, CharSequence>();
        if (TextUtils.isEmpty(checksString)) return res;
        try {
            String[] pairs = checksString.split(pairsDelimiter);
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                if (TextUtils.isEmpty(pair)) continue;
                String[] vals = pair.split(pairValuesDelimiter);
                res.put(vals[0], vals[1]);
            }
        } catch (Exception ex) {
            Log.e(null, ex);
        }
        return res;
    }

    public static String getCheckedIdsString(Hashtable<String, CharSequence> checkedIds ) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> keys = checkedIds.keys();
        for (int k = 0; k < checkedIds.size(); k++) {
            String key = keys.nextElement();
            sb.append(key + pairValuesDelimiter);
            if (key.equals("all")) {
                sb.append("Все форумы;");
            } else {


                sb.append(checkedIds.get(key).toString().trim() + pairsDelimiter);
            }

        }
        return sb.toString();
    }


    private void loadForums() {
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Client.INSTANCE.loadForums(new OnProgressChangedListener() {
                        public void onProgressChanged(final String state) {
                            mHandler.post(new Runnable() {
                                public void run() {
                                    dialog.setMessage(state);
                                }
                            });
                        }

                        public void cancel() {

                        }

                        public void checkCanceled() throws NotReportException {

                        }
                    });
                    mHandler.post(new Runnable() {
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            showForums();
                        }
                    });

                } catch (Exception e) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Log.e(getContext(), e);
                }
            }
        }).start();
    }

    private Hashtable<String, CharSequence> m_CheckedIds = new Hashtable<String, CharSequence>();
    private ArrayList<String> m_VisibleIds = new ArrayList<String>();
    private ArrayList<CharSequence> forumCaptions;

    private void showForums() {
        if (forumCaptions == null || forumCaptions.size() < 2) {
            forumCaptions = new ArrayList<CharSequence>();
            addForumCaptions(forumCaptions, Client.INSTANCE.MainForum, null, "");
        }
        LayoutInflater factory = LayoutInflater.from(getContext());
        final View view = factory.inflate(R.layout.search_froums_list, null);

        ListView lstTree = (ListView) view.findViewById(R.id.lstTree);
        ForumsAdapter adapter = new ForumsAdapter(getContext(), R.layout.search_forum_item, forumCaptions,m_CheckedIds,m_VisibleIds);

        lstTree.setAdapter(adapter);


        new AlertDialog.Builder(new ContextThemeWrapper(getContext(), MyApp.INSTANCE.getThemeStyleResID()))
                .setTitle("Форумы")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setSelectedFroumsText();
                    }
                })
                .create().show();

    }

    private void setSelectedFroumsText() {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> keys = m_CheckedIds.keys();
        for (int k = 0; k < m_CheckedIds.size(); k++) {
            String key = keys.nextElement();
            if (key.equals("all")) {
                sb.append("Все форумы;");
            } else {
                //Forum forum = Client.INSTANCE.MainForum.findById(key, true, false);
                sb.append(m_CheckedIds.get(key));
            }
        }
        if (sb.toString().equals(""))
            sb.append("Все форумы");
        btnAddForum.setText(sb.toString());
    }

    private void addForumCaptions(ArrayList<CharSequence> forumCaptions, Forum forum, Forum parentForum, String node) {
        if (parentForum == null) {
            forumCaptions.add(">> Все форумы");
            m_VisibleIds.add("all");
        } else if (!parentForum.getId().equals(forum.getId())) {
            forumCaptions.add(node + forum.getTitle());
            m_VisibleIds.add(forum.getId());
        }
        if (parentForum == null)
            node = "  ";
        else if (node.trim().equals(""))
            node = "  |--";
        else
            node = node + "--";
        int childSize = forum.getForums().size();

        for (int i = 0; i < childSize; i++) {
            addForumCaptions(forumCaptions, forum.getForums().get(i), forum, node);
        }
    }


}

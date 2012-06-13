package org.softeg.slartus.forpda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.softeg.slartus.forpda.Tabs.SearchTab;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.ForumsAdapter;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 07.10.11
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class SearchActivity extends BaseActivity {
    private EditText username_edit;
    private EditText query_edit;
    private CheckBox chkSubforums;
    private ImageButton search_button, btnSettins;
    private SearchTab m_SearchTab;
    private LinearLayout lnrSettings;
    private Spinner spnrSource, spnrSort;
    private Button btnAddForum;
    private Handler mHandler = new Handler();
    protected Boolean m_UseVolumesScroll = false;

    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v,
                                    android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        m_SearchTab.onCreateContextMenu(menu, v, menuInfo, mHandler);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.search_activity);
        m_SearchTab = new SearchTab(this, "SearchThemes");
        m_SearchTab.getListView().setOnCreateContextMenuListener(this);
        ((LinearLayout) findViewById(R.id.lnrThemes)).addView(m_SearchTab, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        spnrSource = (Spinner) findViewById(R.id.spnrSource);
        spnrSort = (Spinner) findViewById(R.id.spnrSort);
        spnrSort.setSelection(1);
        lnrSettings = (LinearLayout) findViewById(R.id.lnrSettings);
        username_edit = (EditText) findViewById(R.id.username_edit);
        query_edit = (EditText) findViewById(R.id.query_edit);
        query_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    search();
                }
                return true;
            }
        });
        btnAddForum = (Button) findViewById(R.id.btnAddForum);

        btnAddForum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (Client.INSTANCE.MainForum == null) {
                    loadForums();
                } else {
                    showForums();
                }
            }
        }

        );
        chkSubforums = (CheckBox) findViewById(R.id.chkSubforums);

        btnSettins = (ImageButton) findViewById(R.id.btnSettins);

        btnSettins.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lnrSettings.setVisibility(lnrSettings.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        }

        );
        search_button = (ImageButton) findViewById(R.id.btnSearch);

        search_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                search();
            }
        }

        );
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            return m_SearchTab.dispatchKeyEvent(event);
        return super.dispatchKeyEvent(event);

    }

    private void search() {
        hideKeybord(username_edit);
        hideKeybord(query_edit);
        SearchActivity.this.lnrSettings.setVisibility(View.GONE);

        SearchActivity.this.m_SearchTab.search(query_edit.getText().toString(),
                username_edit.getText().toString(),
                getResources().getStringArray(R.array.SearchSourceValues)[SearchActivity.this.spnrSource.getSelectedItemPosition()],
                getResources().getStringArray(R.array.SearchSortValues)[SearchActivity.this.spnrSort.getSelectedItemPosition()],
                chkSubforums.isChecked(), m_CheckedIds);
    }


    private void loadForums() {
        final ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
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
                    Log.e(SearchActivity.this, e);
                }
            }
        }).start();
    }

    private Hashtable<String, CharSequence> m_CheckedIds = new Hashtable<String, CharSequence>();
    private ArrayList<String> m_VisibleIds = new ArrayList<String>();
    private ArrayList<CharSequence> forumCaptions;

    private void showForums() {
        if (forumCaptions == null) {
            forumCaptions = new ArrayList<CharSequence>();
            addForumCaptions(forumCaptions, Client.INSTANCE.MainForum, null, "");
        }
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.search_froums_list, null);

        ListView lstTree = (ListView) view.findViewById(R.id.lstTree);
        ForumsAdapter adapter = new ForumsAdapter(this, R.layout.search_forum_item, forumCaptions, m_CheckedIds, m_VisibleIds);

        lstTree.setAdapter(adapter);


        new AlertDialog.Builder(new ContextThemeWrapper(this, MyApp.INSTANCE.getThemeStyleResID()))
                .setTitle("Форумы")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder sb = new StringBuilder();
                        Enumeration<String> keys = m_CheckedIds.keys();
                        for (int k = 0; k < m_CheckedIds.size(); k++) {
                            String key = keys.nextElement();
                            if (key.equals("all")) {
                                sb.append("Все форумы;");
                            } else {
                                Forum forum = Client.INSTANCE.MainForum.findById(key, true, false);
                                sb.append(forum.getTitle() + ";");
                            }
                        }
                        if (sb.toString().equals(""))
                            sb.append("Все форумы");
                        btnAddForum.setText(sb.toString());
                    }
                })
                .create().show();

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

    private void hideKeybord(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}

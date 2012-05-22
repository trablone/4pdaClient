package org.softeg.slartus.forpda.Tabs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.*;
import org.softeg.slartus.forpda.common.Log;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 05.10.11
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class ThemesTab extends LinearLayout implements ITab {


    private View m_Header;
    protected View m_Footer;
    protected PullToRefreshListView lstTree;
    private TextView txtFroum, txtLoadMoreThemes;
    private ImageButton btnStar;
    private ImageButton btnSettings;
    private TextView txtPullToLoadMore;
    private ImageView imgPullToLoadMore;
    protected Boolean m_UseVolumesScroll = false;
    private OnTabTitleChangedListener m_OnTabTitleChangedListener;

    public interface OnTabTitleChangedListener {
        void onTabTitleChanged(String title);
    }

    public void doOnTabTitleChangedListener(String title) {
        if (m_OnTabTitleChangedListener != null) {
            m_OnTabTitleChangedListener.onTabTitleChanged(title);
        }
    }

    public void setOnTabTitleChangedListener(OnTabTitleChangedListener p) {
        m_OnTabTitleChangedListener = p;
    }


    public ThemesTab(Context context, String tabTag) {
        super(context);
        m_TabId = tabTag;

        addView(inflate(context, R.layout.forum_tree, null),
                new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        loadPreferences();

        lstTree = (PullToRefreshListView) findViewById(R.id.lstTree);
        lstTree.getRefreshableView().setCacheColorHint(0);

        //lstTree = pullToRefresh.getListView();
        lstTree.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listItemClick(adapterView, view, i, l);
            }
        });
        lstTree.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {

                refresh();
            }
        });


        m_Header = inflate(getContext(), R.layout.themes_list_header, null);
        txtFroum = (TextView) m_Header.findViewById(R.id.txtFroum);
        btnSettings = (ImageButton) m_Header.findViewById(R.id.btnSettings);
        btnSettings.setVisibility(View.GONE);
        btnSettings.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                settingsButtonClick();
            }
        });
        btnStar = (ImageButton) m_Header.findViewById(R.id.btnStar);
        btnStar.setVisibility(needShowStarButton() ? View.VISIBLE : View.INVISIBLE);
        btnStar.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                starButtonClick();
            }
        });
        lstTree.getRefreshableView().addHeaderView(m_Header);

        m_Footer = inflate(getContext(), R.layout.themes_list_footer, null);
        m_Footer.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (m_ShowLatestTask != null && m_ShowLatestTask.getStatus() == AsyncTask.Status.RUNNING)
                    return;
                if (m_Themes.getThemesCount() > m_Themes.size())
                    loadLatest();
            }
        });
        txtLoadMoreThemes = (TextView) m_Footer.findViewById(R.id.txtLoadMoreThemes);
        txtPullToLoadMore = (TextView) m_Footer.findViewById(R.id.txtPullToLoadMore);
        imgPullToLoadMore = (ImageView) m_Footer.findViewById(R.id.imgPullToLoadMore);
        lstTree.getRefreshableView().addFooterView(m_Footer);


        m_ThemeAdapter = new ThemeAdapter(getContext(), R.layout.theme_item, m_Themes);
        m_ThemeAdapter.showForumTitle(isShowForumTitle());
        setHeaderText(getTitle());
        beforeSetAdapterOnInit();
        lstTree.getRefreshableView().setAdapter(m_ThemeAdapter);
    }

    protected void beforeSetAdapterOnInit() {

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!m_UseVolumesScroll)
            return super.dispatchKeyEvent(event);
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        ListView scrollView = getListView();
        int visibleItemsCount = scrollView.getLastVisiblePosition() - scrollView.getFirstVisiblePosition();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    scrollView.setSelection(Math.max(scrollView.getFirstVisiblePosition() - visibleItemsCount, 0));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    scrollView.setSelection(Math.min(scrollView.getLastVisiblePosition(), scrollView.getCount() - 1));

                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    public String getTitle() {
        return "Пусто";
    }

    public String getTemplate() {
        return "Пусто";
    }

    protected String m_TabId;
    private String m_Template;

    public String getTabId() {
        return m_TabId;
    }

    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getContext(),i, l);
        if (l < 0||m_ThemeAdapter.getCount()<=l) return;

        if (m_ThemeAdapter == null) return;
        Topic topic = m_ThemeAdapter.getItem((int) l);
        if (TextUtils.isEmpty(topic.getId())) return;
        topic.setIsNew(false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getString("tabs." + m_TabId + ".Action", "getlastpost").equals("browser"))
            topic.showBrowser(getContext(), m_ThemeAdapter.getParams());
        else
            topic.showActivity(getContext(), getOpenThemeParams());
        m_ThemeAdapter.notifyDataSetChanged();
    }

    private void settingsButtonClick() {
        Intent intent = new Intent(getContext(), TabDataSettingsActivity.class);
        intent.putExtra("tabId", m_TabId);

        getContext().startActivity(intent);
    }

    protected void starButtonClick() {

    }

    protected Boolean addFavoritesMenu() {
        return true;
    }

    protected void setStarButtonState(Boolean on) {
        btnStar.setImageResource(on ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    protected Boolean needShowStarButton() {
        return false;
    }

    protected void showStarButton(Boolean show) {
        btnStar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        m_UseVolumesScroll = prefs.getBoolean("themeslist.UseVolumesScroll", false);
    }

    ShowLatestTask m_ShowLatestTask = new ShowLatestTask(getContext());

    protected void loadLatest() {


        if (m_ShowLatestTask != null && m_ShowLatestTask.getStatus() == AsyncTask.Status.RUNNING)
            m_ShowLatestTask.cancel(true);
        m_ShowLatestTask = new ShowLatestTask(getContext());

        m_ShowLatestTask.execute();
    }

    protected void modifyThemesListAfterLoad() {

    }


    public Boolean onParentBackPressed() {

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Boolean refreshed() {
        return m_Refreshed;
    }

    protected Boolean m_Refreshed = false;

    public void refresh() {
        refresh(null);
    }

    public void refresh(Bundle savedInstanceState) {
        if (m_ShowLatestTask != null && m_ShowLatestTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        m_Refreshed = true;
        m_ThemeAdapter = null;
        m_Themes.clear();

        loadLatest();
    }

    public ListView getListView() {
        return lstTree.getRefreshableView();
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        if (m_ThemeAdapter != null)
            m_ThemeAdapter.onCreateContextMenu(menu, v, menuInfo, addFavoritesMenu(), handler);
    }


    protected void getThemes(Client.OnProgressChangedListener progressChangedListener) throws Exception {

    }

    protected ThemeAdapter m_ThemeAdapter;
    Themes m_Themes = new Themes();

    protected String m_CurrentAdapter = "ThemeAdapter";

    private class ShowLatestTask extends AsyncTask<ForumItem, String, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public ShowLatestTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
        }


        int m_SelectedIndex = 0;

        @Override
        protected Boolean doInBackground(ForumItem... forums) {
            try {
                if (this.isCancelled()) return false;
                m_SelectedIndex = Math.max(Math.min(m_Themes.size(), lstTree.getRefreshableView().getFirstVisiblePosition()), 0);


                getThemes(new Client.OnProgressChangedListener() {
                    public void onProgressChanged(String state) {
                        publishProgress(state);
                    }
                });
                return true;
            } catch (Exception e) {
                //Log.e(getContext(), e);
                ex = e;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }


        // can use UI thread here
        protected void onPreExecute() {
            try {
                this.dialog.setMessage(getContext().getResources().getString(R.string.loading));
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private Exception ex;

        protected void onCancelled() {
            super.onCancelled();

        }


        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (success) {

                int allThemesCount = m_Themes.getThemesCount();

                txtLoadMoreThemes.setText("Всего: " + allThemesCount);
                modifyThemesListAfterLoad();

                if (!m_CurrentAdapter.equals("ThemeAdapter") || m_ThemeAdapter == null) {
                    setAdapter();
                }

                m_ThemeAdapter.showForumTitle(isShowForumTitle());

                m_ThemeAdapter.setParams(getOpenThemeParams());
                m_ThemeAdapter.notifyDataSetChanged();


                int loadMoreVisibility = allThemesCount > m_Themes.size() ? View.VISIBLE : View.GONE;
                txtPullToLoadMore.setVisibility(loadMoreVisibility);
                imgPullToLoadMore.setVisibility(loadMoreVisibility);

                setHeaderText(allThemesCount + " тем @ " + getTitle());
                if (m_SelectedIndex == 0) {
                    lstTree.getRefreshableView().setSelection(0);
                }

                afterOnPostSuccessExecute();

            } else {
                if (ex != null)
                    Log.e(mContext, ex);
            }

            lstTree.onRefreshComplete();
            super.onPostExecute(success);
        }

        private void setAdapter() {
            m_CurrentAdapter = "ThemeAdapter";
            m_ThemeAdapter = new ThemeAdapter(getContext(), R.layout.theme_item, m_Themes);
//            Comparator<Topic> topicComparator=getSortComparator();
//            if(topicComparator!=null)
//                m_ThemeAdapter.sort(topicComparator);

            m_ThemeAdapter.showForumTitle(isShowForumTitle());
            lstTree.getRefreshableView().addFooterView(m_Footer);

            lstTree.getRefreshableView().setAdapter(m_ThemeAdapter);

        }

    }
    
    protected Comparator<Topic> getSortComparator(){
        return null;
    }

    protected void afterOnPostSuccessExecute() {

    }

    protected void setHeaderText(String text) {
        txtFroum.setText(text);
    }

    protected Boolean isShowForumTitle() {
        return false;
    }

    protected String getDefaultOpenThemeParams() {
        return "";
    }

    private String getOpenThemeParams() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String themeActionPref = prefs.getString("tabs." + m_TabId + ".Action", null);
        return ThemeOpenParams.getUrlParams(themeActionPref, getDefaultOpenThemeParams());
    }


}

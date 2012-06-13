package org.softeg.slartus.forpda.Tabs;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.classes.Forum;
import org.softeg.slartus.forpda.classes.Forums;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 22.11.11
 * Time: 10:35
 */
public abstract class TreeTab extends ThemesTab {
    protected ArrayAdapter<Forum> m_ForumsAdapter;


    protected Forum m_Digest = new Forum("-1", getTitle());

    public TreeTab(Context context, String tabTag) {
        super(context, tabTag);

        m_CurrentAdapter = "ForumsAdapter";

        m_ForumsAdapter = new ArrayAdapter<Forum>(getContext(), R.layout.board_forum_name, new Forums());

        setHeaderText(getTitle());

        beforeSetAdapterOnInit();
        lstTree.getRefreshableView().setAdapter(m_ForumsAdapter);
    }


    @Override
    protected void listItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (m_CurrentItem == null) return;
        if (m_CurrentItem.hasChildForums()) {
            l = ListViewMethodsBridge.getItemId(getContext(),i, l);

            if (l < 0||m_ForumsAdapter.getCount()<=l) return;
            showForum(m_ForumsAdapter.getItem((int) l));
        } else {
            super.listItemClick(adapterView, view, i, l);
        }
    }

    protected void loadForums(Forum forum) {
        ShowForumsTask task = new ShowForumsTask(getContext());
        task.execute(forum);
    }

    protected void showForum(Forum forum) {
        if (!forum.hasChildForums()) {
            forum.LoadMore = false;
            showThemes(forum);
            return;
        }

        m_CurrentAdapter = "ForumsAdapter";
        m_ForumsAdapter = new ArrayAdapter<Forum>(getContext(), R.layout.board_forum_name, forum.getForums());
        if (lstTree.getRefreshableView().getFooterViewsCount() > 0)
            lstTree.getRefreshableView().removeFooterView(m_Footer);
        lstTree.getRefreshableView().setAdapter(m_ForumsAdapter);

        setCurrentForumItem(forum);
    }

    protected void showThemes(Forum forum) {
        m_ForumForLoadThemes = forum;
        m_Themes = m_ForumForLoadThemes.getThemes();

        loadLatest();
    }

    private void setCurrentForumItem(Forum forumItem) {
        m_CurrentItem = forumItem;
        setHeaderText(m_CurrentItem.getTitle().toString());
    }

    protected Forum m_CurrentItem;

    @Override
    public Boolean onParentBackPressed() {

        if (m_CurrentItem == null || m_CurrentItem.getParent() == null) {
            return false;
        }
        m_CurrentItem.getThemes().clear();
        showForum(m_CurrentItem.getParent());
        return true;
    }

    @Override
    public void refresh() {
        refresh(null);

    }

    @Override
    public void refresh(Bundle args) {
        m_Refreshed = true;
        if (m_CurrentItem != null && !m_CurrentItem.hasChildForums()) {
            m_CurrentItem.clearChildren();
            showThemes(m_CurrentItem);
            return;
        }

        //String url=m_CurrentItem.getId();
        loadForums(m_Digest);
    }

    @Override
    protected void loadLatest() {
        if (m_CurrentItem != null)
            m_CurrentItem.LoadMore = true;
        super.loadLatest();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Handler handler) {
        if (m_CurrentItem != null && !m_CurrentItem.hasChildForums())
            super.onCreateContextMenu(menu, v, menuInfo, handler);
    }

    protected abstract void loadForum(Forum forum, OnProgressChangedListener progressChangedListener) throws IOException;

    private class ShowForumsTask extends AsyncTask<Forum, String, Boolean> {
        Context mContext;
        private final ProgressDialog dialog;

        public ShowForumsTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
        }

        private Forum m_LoadForum;

        @Override
        protected Boolean doInBackground(Forum... forums) {
            try {
                m_LoadForum = forums[0];
                m_LoadForum.clearChildren();
                loadForum(m_LoadForum, new OnProgressChangedListener() {
                    public void onProgressChanged(String state) {
                        publishProgress(state);
                    }
                });

                return true;
            } catch (Exception e) {
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
            }
        }

        private Exception ex;

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
                showForum(m_LoadForum);
                m_ForumsAdapter.notifyDataSetChanged();


            } else {
                Log.e(mContext, ex);
            }
            lstTree.onRefreshComplete();
            super.onPostExecute(success);
        }

    }

    protected Forum m_ForumForLoadThemes;

    @Override
    protected void afterOnPostSuccessExecute() {
        //setStarButtonState(m_CurrentItem.getId().equals(Integer.toString(m_StartForumId)) && m_StartForumThemes);
        setCurrentForumItem(m_ForumForLoadThemes);
        setHeaderText(m_ForumForLoadThemes.getThemes().getThemesCount() + " тем @ " + m_ForumForLoadThemes.getTitle());
    }


}

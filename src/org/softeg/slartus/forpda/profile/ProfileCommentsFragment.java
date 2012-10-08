package org.softeg.slartus.forpda.profile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.ListViewMethodsBridge;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.Comment;
import org.softeg.slartus.forpdaapi.UserProfile;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 27.09.12
 * Time: 15:29
 */
public class ProfileCommentsFragment extends ProfileViewsFragment
    implements LoaderManager.LoaderCallbacks<UserProfile> {
    private UserViewsAdapter mAdapter;
    private ProgressBar m_ProgressBar;
    private PullToRefreshListView m_ListView;
    private TextView txtLoadMoreThemes, txtPullToLoadMore;
    private ImageView imgPullToLoadMore;

    private View m_ListFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        View pframe = inflater.inflate(R.layout.mails_list, null);

        mAdapter = new UserViewsAdapter(getActivity());
        m_ListView = (PullToRefreshListView) pframe.findViewById(R.id.pulltorefresh);
        m_ListView.getRefreshableView().addFooterView(createListFooter(inflater));
        m_ListView.getRefreshableView().setAdapter(mAdapter);
        m_ListView.getRefreshableView().setOnItemClickListener(this);

        setState(true);
        m_ListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {

                refreshData();
            }
        });

        return pframe;
    }

    public void refreshData() {
        if (getUserProfile() != null)
            getUserProfile().UserComments.clear();
        setState(true);
        if (getLoaderManager().getLoader(0) == null)
            getLoaderManager().initLoader(0, null, this);
        else
            getLoaderManager().restartLoader(0, null, this);
    }

    private View createListFooter(LayoutInflater inflater) {
        m_ListFooter = inflater.inflate(R.layout.list_footer, null);
        m_ListFooter.setVisibility(View.GONE);
        m_ListFooter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (getUserProfile().UserComments.needLoadMore())
                    loadMore();
            }
        });
        txtLoadMoreThemes = (TextView) m_ListFooter.findViewById(R.id.txtLoadMoreThemes1);
        txtPullToLoadMore = (TextView) m_ListFooter.findViewById(R.id.txtPullToLoadMore1);
        imgPullToLoadMore = (ImageView) m_ListFooter.findViewById(R.id.imgPullToLoadMore1);
        m_ProgressBar = (ProgressBar) m_ListFooter.findViewById(R.id.load_more_progress1);
        return m_ListFooter;
    }

    private void loadMore() {
        setState(true);
        Bundle bundle = new Bundle();
        bundle.putString("startcount", Integer.toString(getUserProfile().UserComments.size()));
        getLoaderManager().restartLoader(0, bundle, this);
    }

    @Override
    public  void onItemClick(android.widget.AdapterView<?> adapterView, android.view.View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getActivity(), i, l);
        if (l < 0 || mAdapter.getCount() <= l) return ;
        Comment item = getUserProfile().UserComments.get((int) l);
        ForumUser.showUserMenu(getActivity(), view, item.UserId, item.User);
    }

    @Override
    protected void fillData(UserProfile userProfile) {

    }

    @Override
    public Loader<UserProfile> onCreateLoader(int i, Bundle bundle) {
        LoadTask loadTask = new CommentsLoader(getActivity(), getUserProfile(), null);

        loadTask.UserId = getUserId();
        return loadTask;
    }

    private void updateDataInfo() {

        int loadMoreVisibility = (getUserProfile().UserComments.needLoadMore()) ? View.VISIBLE : View.GONE;
        txtPullToLoadMore.setVisibility(loadMoreVisibility);
        imgPullToLoadMore.setVisibility(loadMoreVisibility);
        txtLoadMoreThemes.setText("Всего: " + getUserProfile().UserComments.getFullLength());
        setHeaderText((getUserProfile().UserComments == null ? 0 : getUserProfile().UserComments.size()) + " сообщений");
        m_ListFooter.setVisibility(getUserProfile().UserComments.size() > 0 ? View.VISIBLE : View.GONE);
    }

    protected void setHeaderText(String text) {
        //m_ListHeaderTextView.setText(text);
    }

    private void setState(boolean loading) {
        if (loading)
            m_ListView.setRefreshing(false);
        else
            m_ListView.onRefreshComplete();

        if (imgPullToLoadMore == null) return;
        if (loading) {
            imgPullToLoadMore.setVisibility(View.GONE);
            txtPullToLoadMore.setVisibility(View.GONE);
        }
        m_ProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        m_ListFooter.setEnabled(!loading);
    }

    @Override
    public void onLoadFinished(Loader<UserProfile> loader, UserProfile data) {

        if (((LoadTask) loader).Ex != null) {
            Log.e(getActivity(), ((LoadTask) loader).Ex);
            setState(false);
            return;
        }
        setUserProfile(data);
        doOnProfileChanged(data);

        mAdapter.setData(getUserProfile());

        setState(false);

        mAdapter.notifyDataSetChanged();
        updateDataInfo();
    }


    private static class UserViewsAdapter extends ArrayAdapter<Comment> {
        private LayoutInflater m_Inflater;
        private int m_ThemeTitleSize = 13;
        private int m_TopTextSize = 10;
        private int m_BottomTextSize = 11;

        static class ViewHolder {
            View usericon;
            ImageView txtIsNew;
            TextView txtAuthor;
            TextView txtLastMessageDate;
            TextView txtTitle;
            TextView txtDescription;
            TextView txtForumTitle;

        }

        public void setData(UserProfile data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (Comment item : data.UserComments) {
                    add(item);
                }
            }
        }

        public UserViewsAdapter(Activity context) {
            super(context, R.layout.simple_list_item_2);


            m_Inflater = LayoutInflater.from(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                    "interface.themeslist.title.font.size", 13);
            m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
            m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                convertView = m_Inflater.inflate(R.layout.theme_item, parent, false);
                holder = new ViewHolder();
                holder.txtIsNew = (ImageView) convertView
                        .findViewById(R.id.txtIsNew);
                holder.usericon = convertView.findViewById(R.id.usericon);


                holder.txtAuthor = (TextView) convertView
                        .findViewById(R.id.txtAuthor);
                holder.txtAuthor.setTextSize(m_TopTextSize);

                holder.txtLastMessageDate = (TextView) convertView
                        .findViewById(R.id.txtLastMessageDate);
                holder.txtLastMessageDate.setTextSize(m_TopTextSize);

                holder.txtTitle = (TextView) convertView
                        .findViewById(R.id.txtTitle);
                holder.txtTitle.setVisibility(View.GONE);


                holder.txtDescription = (TextView) convertView
                        .findViewById(R.id.txtDescription);
                holder.txtDescription.setTextSize(m_BottomTextSize);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Comment item = this.getItem(position);

            holder.txtAuthor.setText(item.User);
            holder.txtLastMessageDate.setText(item.DateTime);
            holder.txtDescription.setText(item.Text);
            return convertView;
        }
    }

    public static class CommentsLoader extends LoadTask {

        String startCount = "0";

        public CommentsLoader(Context context, UserProfile userProfile, Bundle args) {
            super(context, userProfile);
            if (args != null && args.containsKey("startcount"))
                startCount = args.getString("startcount");
            if (startCount == null)
                startCount = "0";
        }

        @Override
        protected Boolean needLoad() {
            return Client.INSTANCE.getLogined()&&
                     (m_UserProfile == null || m_UserProfile.UserComments.size() == 0
                    || m_UserProfile.UserComments.size() == 5 || m_UserProfile.UserComments.needLoadMore())
                    // 5 комментов максимум отображается в "быстром просмотре" профиля.
                    // Так что неизвестно, есть ли еще, поэтому загружаем полный список
                    ;
        }

        @Override
        protected UserProfile loadData(UserProfile userProfile, String userId) throws IOException {

            return Client.INSTANCE.loadUserProfileComments(userProfile, userId);
        }
    }
}


package org.softeg.slartus.forpda.profile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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
import org.softeg.slartus.forpdaapi.User;
import org.softeg.slartus.forpdaapi.UserProfile;

import java.io.IOException;

/**
 * User: slinkin
 * Date: 27.09.12
 * Time: 16:55
 */
public class ProfileFriendsFragment extends ProfileViewsFragment {
    private UserViewsAdapter mAdapter;
    private ProgressBar m_ProgressBar;
    private PullToRefreshListView m_ListView;
    private TextView txtLoadMoreThemes, txtPullToLoadMore;
    private ImageView imgPullToLoadMore;

    private View m_ListFooter;

    @Override
    public  void onItemClick(android.widget.AdapterView<?> adapterView, android.view.View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(getActivity(), i, l);
        if (l < 0 || mAdapter.getCount() <= l) return ;
        User item = getUserProfile().Friends.get((int) l);
        ForumUser.showUserMenu(getActivity(), view, item.getMid(), item.getNick());
    }

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
        if(getUserProfile()!=null)
        getUserProfile().Friends.clear();
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
                if (getUserProfile().Friends.needLoadMore())
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
        bundle.putString("startcount", Integer.toString(getUserProfile().Friends.size()));
        getLoaderManager().restartLoader(0, bundle, this);
    }

    @Override
    protected void fillData(UserProfile userProfile) {

    }

    @Override
    protected void setState(Boolean load){
        if(m_ProgressBar!=null)
            m_ProgressBar.setVisibility(load?View.VISIBLE:View.GONE);
        if(m_ListView!=null)
            m_ListView.setVisibility(load ? View.GONE : View.VISIBLE);
    }

    @Override
    public Loader<UserProfile> onCreateLoader(int i, Bundle bundle) {
        LoadTask loadTask=new FriendsLoader(getActivity(),getUserProfile());

        loadTask.UserId=getUserId();
        return loadTask;
    }

    private void updateDataInfo() {

        int loadMoreVisibility = (getUserProfile().Friends.needLoadMore()) ? View.VISIBLE : View.GONE;
        txtPullToLoadMore.setVisibility(loadMoreVisibility);
        imgPullToLoadMore.setVisibility(loadMoreVisibility);
        txtLoadMoreThemes.setText("Всего: " + getUserProfile().Friends.getFullLength());
        setHeaderText((getUserProfile().Friends == null ? 0 : getUserProfile().Friends.size()) + " друзей");
        m_ListFooter.setVisibility(getUserProfile().Friends.size() > 0 ? View.VISIBLE : View.GONE);
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

        if(((LoadTask)loader).Ex!=null){
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


    private static class UserViewsAdapter extends ArrayAdapter<User> {


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

        public UserViewsAdapter(Activity context) {
            super(context, R.layout.theme_item);


            m_Inflater = LayoutInflater.from(context);


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                    "interface.themeslist.title.font.size", 13);
            m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
            m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);

        }

        public void setData(UserProfile data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (User item : data.Friends) {
                    add(item);
                }
            }
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            if (convertView == null) {

                convertView = m_Inflater.inflate(R.layout.theme_item, parent,false);
                holder = new ViewHolder();
                holder.txtIsNew = (ImageView) convertView
                        .findViewById(R.id.txtIsNew);
                holder.usericon=convertView.findViewById(R.id.usericon);


                holder.txtAuthor = (TextView) convertView
                        .findViewById(R.id.txtAuthor);
                holder.txtAuthor.setTextSize(m_TopTextSize);

                holder.txtLastMessageDate = (TextView) convertView
                        .findViewById(R.id.txtLastMessageDate);
                holder.txtLastMessageDate.setTextSize(m_TopTextSize);

                holder.txtTitle = (TextView) convertView
                        .findViewById(R.id.txtTitle);
                holder.txtTitle.setTextSize(m_ThemeTitleSize);

                holder.txtDescription = (TextView) convertView
                        .findViewById(R.id.txtDescription);
                holder.txtDescription.setTextSize(m_BottomTextSize);

                convertView.setTag(holder);

            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            User item = this.getItem(position);

            holder.txtAuthor.setText(item.LastVisit);
            if(TextUtils.isEmpty(item.MessagesCount))
                holder.txtLastMessageDate.setText(null);
            else
                holder.txtLastMessageDate.setText("Сообщений: "+item.MessagesCount);
            holder.txtTitle.setText(item.getNick());
            holder.txtDescription.setText(item.Group);


            if ("Online".equals(item.State)) {
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtIsNew.setImageBitmap(null);
            }


            return convertView;

        }
    }
    
    public static class FriendsLoader extends LoadTask{

        public FriendsLoader(Context context, UserProfile userProfile) {
            super(context, userProfile);
        }

        @Override
        protected Boolean needLoad(){
            return Client.INSTANCE.getLogined()&&(m_UserProfile==null||m_UserProfile.Friends.size()==0
                    ||m_UserProfile.Friends.size()==5|| m_UserProfile.Friends.needLoadMore())
            // 5 друзей максимум отображается в "быстром просмотре" профиля.
            // Так что неизвестно, есть ли еще, поэтому загружаем полный список
           ;
        }

        @Override
        protected UserProfile loadData(UserProfile userProfile,String userId) throws IOException {
            return Client.INSTANCE.loadUserProfileFriends(userProfile,userId);
        }
    }
}
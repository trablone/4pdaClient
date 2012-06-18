package org.softeg.slartus.forpda.qms;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.Tabs.ListViewMethodsBridge;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.Qms;
import org.softeg.slartus.forpdaapi.QmsUser;
import org.softeg.slartus.forpdaapi.QmsUsers;

import java.util.ArrayList;

/**
 * User: slinkin
 * Date: 15.06.12
 * Time: 10:46
 */
public class QmsContactsActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener, Loader.OnLoadCompleteListener<QmsUsers> {
    private QmsContactsAdapter mAdapter;
    private QmsUsers m_QmsUsers=new QmsUsers();
    private PullToRefreshListView m_ListView;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.qms_contacts_list);

        m_ListView = (PullToRefreshListView) findViewById(R.id.pulltorefresh);

        setState(true);
        m_ListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                refreshData();
            }
        });

        mAdapter=      new QmsContactsAdapter(this,R.layout.qms_contact_item, new QmsUsers());
        m_ListView.getRefreshableView().setAdapter(mAdapter);
        m_ListView.getRefreshableView().setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }


    public void refreshData() {
        m_QmsUsers.clear();

        setState(true);
        QmsUsersLoader qmsUsersLoader=new QmsUsersLoader(this);
        qmsUsersLoader.registerListener(0,this);
        qmsUsersLoader.startLoading();
    }

    private void updateDataInfo() {

//        int loadMoreVisibility = (m_QmsUsers.getFullLength() > m_QmsUsers.size()) ? View.VISIBLE : View.GONE;
//        txtPullToLoadMore.setVisibility(loadMoreVisibility);
//        imgPullToLoadMore.setVisibility(loadMoreVisibility);
//        txtLoadMoreThemes.setText("Всего: " + m_QmsUsers.getFullLength());
//        setHeaderText((m_Mails == null ? 0 : m_QmsUsers.size()) + " тем");
//        m_ListFooter.setVisibility(m_Mails.size() > 0 ? View.VISIBLE : View.GONE);
    }

    private void setState(boolean loading) {
        if (loading)
            m_ListView.setRefreshing(false);
        else
            m_ListView.onRefreshComplete();

    }

    public void onLoadComplete(Loader<QmsUsers> qmsUsersLoader, QmsUsers data) {
        if (data != null) {
            for (QmsUser item : data) {
                m_QmsUsers.add(item);
            }
            mAdapter.setData(m_QmsUsers);
        } else {
            m_QmsUsers = new QmsUsers();
            mAdapter.setData(m_QmsUsers);
        }

        updateDataInfo();
        setState(false);
        mAdapter.notifyDataSetChanged();

    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(this, i, l);
        if (l < 0||mAdapter.getCount()<=l) return;


        Intent intent = new Intent(this, QmsChatActivity.class);
        intent.putExtra("UserId", m_QmsUsers.get((int)l).getMid());
        intent.putExtra("UserNick", m_QmsUsers.get((int)l).getNick());
        intent.putExtra("activity", this.getClass().toString());
        startActivity(intent);
    }

    private static class QmsUsersLoader extends AsyncTaskLoader<QmsUsers> {

        QmsUsers mApps;

        Exception ex;

        public QmsUsersLoader(Context context) {
            super(context);
           
        }

        @Override
        public QmsUsers loadInBackground() {
            try {
                QmsUsers mails = Qms.getQmsSubscribers(Client.INSTANCE);
                return mails;
            } catch (Exception e) {
                ex = e;

            }
            return null;
        }

        @Override
        public void deliverResult(QmsUsers apps) {
            if (ex != null)
                Log.e(getContext(), ex);
            if (isReset()) {
                if (apps != null) {
                    onReleaseResources();
                }
            }
            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

            if (apps != null) {
                onReleaseResources();
            }
        }


        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(mApps);
            }

            if (takeContentChanged() || mApps == null) {
                // If the data has changed since the last time it was loaded
                // or is not currently available, start a load.
                forceLoad();
            }
        }


        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(QmsUsers apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources();
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (mApps != null) {
                onReleaseResources();
                mApps = null;
            }


        }

        protected void onReleaseResources() {
            if (mApps != null)
                mApps.clear();

            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }

    public class QmsContactsAdapter extends ArrayAdapter<QmsUser>{
        private LayoutInflater m_Inflater;

        public void setData(QmsUsers data) {
            if (getCount() > 0)
                clear();
            if (data != null) {
                for (QmsUser item : data) {
                    add(item);
                }
            }
        }

        public QmsContactsAdapter(Context context, int textViewResourceId, ArrayList<QmsUser> objects) {
            super(context, textViewResourceId, objects);

            m_Inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = m_Inflater.inflate(R.layout.qms_contact_item, parent, false);

                holder = new ViewHolder();
                holder.txtIsNew=(ImageView) convertView.findViewById(R.id.txtIsNew);
                holder.txtCount = (TextView) convertView.findViewById(R.id.txtMessagesCount);
                holder.txtNick=(TextView)convertView.findViewById(R.id.txtNick);
                holder.txtDateTime=(TextView)convertView.findViewById(R.id.txtDateTime);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            QmsUser user = this.getItem(position);

            holder.txtCount.setText(user.getMessagesCount());
            holder.txtNick.setText(user.getNick());
            holder.txtNick.setTextColor(Color.parseColor(user.getHtmlColor()));
            holder.txtDateTime.setText(user.getLastMessageDateTime());

            if (!TextUtils.isEmpty(user.getNewMessagesCount())) {
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtIsNew.setImageBitmap(null);
            }

            return convertView;
        }

        public class ViewHolder {
            ImageView txtIsNew;
            TextView txtNick;
            TextView txtDateTime;
            TextView txtCount;
        }
    }
}

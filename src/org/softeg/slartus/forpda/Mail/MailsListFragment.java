package org.softeg.slartus.forpda.Mail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.softeg.slartus.forpda.Mail.classes.Mail;
import org.softeg.slartus.forpda.Mail.classes.Mails;
import org.softeg.slartus.forpda.Mail.classes.MailsAdapter;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;

/**
 * User: slinkin
 * Date: 13.03.12
 * Time: 11:47
 */
public class MailsListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Mails> {
    private Handler mHandler = new Handler();
    private String m_Vid;
    private String m_Title;

    private MailsAdapter mAdapter;
    private ProgressBar m_ProgressBar;
    private PullToRefreshListView m_ListView;
    private TextView txtLoadMoreThemes, txtPullToLoadMore;
    private ImageView imgPullToLoadMore;
    private Mails m_Mails = null;
    private View m_ListFooter;


    @Override
    public void onHiddenChanged(boolean hidden) {
        Toast.makeText(getActivity(), Boolean.toString(hidden), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);


        if (m_Mails == null)
            m_Mails = new Mails(m_Vid);
        mAdapter = new MailsAdapter(getActivity());

        setListAdapter(mAdapter);

        if (m_NeedLoad) {

            startLoad();
        }

    }

    private View createListFooter(LayoutInflater inflater) {
        m_ListFooter = inflater.inflate(R.layout.list_footer, null);
        m_ListFooter.setVisibility(View.GONE);
        m_ListFooter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (m_Mails.getFullLength() > m_Mails.size())
                    loadMore();
            }
        });
        txtLoadMoreThemes = (TextView) m_ListFooter.findViewById(R.id.txtLoadMoreThemes1);
        txtPullToLoadMore = (TextView) m_ListFooter.findViewById(R.id.txtPullToLoadMore1);
        imgPullToLoadMore = (ImageView) m_ListFooter.findViewById(R.id.imgPullToLoadMore1);
        m_ProgressBar = (ProgressBar) m_ListFooter.findViewById(R.id.load_more_progress1);
        return m_ListFooter;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private Boolean m_loaded = false;
    private Boolean m_NeedLoad = false;

    public void refreshData() {
        m_loaded = true;


        m_Mails.clear();
        setState(true);
        if (getLoaderManager().getLoader(0) == null)
            getLoaderManager().initLoader(0, null, this);
        else
            getLoaderManager().restartLoader(0, null, this);
    }

    public void startLoad() {
        if (m_loaded) return;
        if (m_Mails == null)
            m_NeedLoad = true;
        if (m_loaded || (m_Mails == null)) return;
        refreshData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_Vid = getArguments() != null ? getArguments().getString("vid") : null;
        if (m_Mails != null)
            m_Mails.setVid(m_Vid);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View pframe = inflater.inflate(R.layout.mails_list, null);


        m_ListView = (PullToRefreshListView) pframe.findViewById(R.id.pulltorefresh);
        m_ListView.getRefreshableView().addFooterView(createListFooter(inflater));


        setState(true);
        m_ListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {

                refreshData();
            }
        });

        return pframe;
    }

    private void updateDataInfo() {

        int loadMoreVisibility = (m_Mails.getFullLength() > m_Mails.size()) ? View.VISIBLE : View.GONE;
        txtPullToLoadMore.setVisibility(loadMoreVisibility);
        imgPullToLoadMore.setVisibility(loadMoreVisibility);
        txtLoadMoreThemes.setText("Всего: " + m_Mails.getFullLength());
        setHeaderText((m_Mails == null ? 0 : m_Mails.size()) + " тем");
        m_ListFooter.setVisibility(m_Mails.size() > 0 ? View.VISIBLE : View.GONE);
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

    private void loadMore() {
        setState(true);
        Bundle bundle = new Bundle();
        bundle.putString("startcount", Integer.toString(m_Mails.size()));
        getLoaderManager().restartLoader(0, bundle, MailsListFragment.this);
    }


    protected void setHeaderText(String text) {
        //m_ListHeaderTextView.setText(text);
    }

    public Loader<Mails> onCreateLoader(int id, Bundle args) {
        return new ListLoader(getActivity(), m_Vid, args);
    }

    public void onLoadFinished(Loader<Mails> list, Mails data) {
        if (data != null) {

            for (Mail item : data) {
                m_Mails.add(item);
            }
            m_Mails.setFullLength(data.getFullLength());

            mAdapter.setData(m_Mails);


        } else {
            m_Mails = new Mails(m_Vid);
            mAdapter.setData(m_Mails);

        }

        updateDataInfo();
        setState(false);
        mAdapter.notifyDataSetChanged();
    }

    public void onLoaderReset(Loader<Mails> list) {
        mAdapter.setData(null);
        setState(false);

    }

    @Override
    public void onListItemClick(ListView listView, View view, int i, long l) {
        if (l < 0) return;
        try {
            Mail mail = mAdapter.getItem((int) l);
            Intent intent = new Intent(getActivity(), MailActivity.class);
            intent.putExtra("MailId", mail.getId());
            getActivity().startActivity(intent);
            mail.setIsNew(false);

        } catch (Exception e) {
            Log.e(getActivity(), e);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//        if (info.id < 0) return;
//        final Mail topic = mAdapter.getItem((int) info.id);
//        ArrayList<TopicAction> topicActions = m_List.getActions();
//        if (topicActions == null) return;
//        for (final TopicAction topicAction : topicActions) {
//            menu.add(topicAction.getTitle()).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    try {
//                        topicAction.run(getActivity(), mHandler, topic);
//                    } catch (IOException e) {
//                        Log.e(getActivity(), e);
//                    }
//                    return true;
//                }
//            });
//        }
    }

    public void deleteItem(String id) {
        m_Mails.delete(id);
        mAdapter.setData(m_Mails);


        updateDataInfo();

        mAdapter.notifyDataSetChanged();
    }

    private static class ListLoader extends AsyncTaskLoader<Mails> {

        Mails mApps;
        String mVid;
        String startCount = "0";
        Exception ex;

        public ListLoader(Context context, String vid, Bundle args) {
            super(context);
            mVid = vid;

            if (args != null && args.containsKey("startcount"))
                startCount = args.getString("startcount");
            if (startCount == null)
                startCount = "0";
        }

        @Override
        public Mails loadInBackground() {
            try {
                Mails mails = new Mails(mVid);
                mails.loadItems(startCount);
                return mails;
            } catch (Exception e) {
                ex = e;

            }
            return null;
        }

        @Override
        public void deliverResult(Mails apps) {
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
        public void onCanceled(Mails apps) {
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

}

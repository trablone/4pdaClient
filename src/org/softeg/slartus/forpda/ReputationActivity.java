package org.softeg.slartus.forpda;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import org.softeg.slartus.forpda.Tabs.ListViewMethodsBridge;
import org.softeg.slartus.forpda.classes.ForumUser;
import org.softeg.slartus.forpda.classes.Topic;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.OnProgressChangedListener;
import org.softeg.slartus.forpdaapi.Reputation;
import org.softeg.slartus.forpdaapi.Reputations;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 25.10.11
 * Time: 9:53
 */
public class ReputationActivity extends BaseFragmentActivity implements AdapterView.OnItemLongClickListener{
    private Handler mHandler = new Handler();
    protected PullToRefreshListView listView;
    private Reputations m_Reputations = new Reputations();
    private RepsAdapter m_Adapter;
    private View m_Header;
    private View m_Footer;
    private View m_EmptyView;

    private TextView txtFroum, txtLoadMoreThemes;
    private ImageButton btnStar;
    private ImageButton btnSettings;
    private TextView txtPullToLoadMore;
    private ImageView imgPullToLoadMore;
    private  MenuFragment mFragment1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setRequestedOrientation(ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1));
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(MyApp.INSTANCE.getThemeStyleResID());
        setContentView(R.layout.forum_tree);
        LayoutInflater factory = LayoutInflater.from(this);
        m_Header = factory.inflate(R.layout.themes_list_header, null);
        txtFroum = (TextView) m_Header.findViewById(R.id.txtFroum);

        m_Footer = factory.inflate(R.layout.themes_list_footer, null);
        m_Footer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (m_Reputations.getMaxCount() > m_Reputations.size())
                    new LoadTask(ReputationActivity.this).execute();
            }
        });
        txtLoadMoreThemes = (TextView) m_Footer.findViewById(R.id.txtLoadMoreThemes);
        txtPullToLoadMore = (TextView) m_Footer.findViewById(R.id.txtPullToLoadMore);
        imgPullToLoadMore = (ImageView) m_Footer.findViewById(R.id.imgPullToLoadMore);


        listView = (PullToRefreshListView) findViewById(R.id.lstTree);
        listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                refresh();
            }
        });
        listView.getRefreshableView().addFooterView(m_Footer);
        listView.getRefreshableView().addHeaderView(m_Header);
        listView.getRefreshableView().setOnItemLongClickListener(this);
        m_EmptyView = factory.inflate(R.layout.empty_view, null);
        addContentView(m_EmptyView,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        listView.setEmptyView(m_EmptyView);
        listView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (l < 0) return;
                Reputation rep = m_Reputations.get((int) l);

                Pattern p = Pattern.compile("http://4pda.ru/forum/index.php\\?showtopic=(\\d+)&view=findpost&p=(\\d+)");
                Matcher m = p.matcher(rep.sourceUrl);
                if (!m.find()) {
                    Toast.makeText(ReputationActivity.this, "Не реализовано", Toast.LENGTH_SHORT).show();
                    return;
                }

                Topic.showActivity(ReputationActivity.this, m.group(1), "view=findpost&p=" + m.group(2));

            }
        });
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        m_Reputations.userId = extras.getString("userId");


        m_Adapter = new RepsAdapter(this, R.layout.reputation_item, m_Reputations);
        listView.getRefreshableView().setAdapter(m_Adapter);
        refresh();
        createActionMenu();
    }
    
    public String getUserId(){
        return m_Reputations.userId;
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

    public static void showRep(Activity actiovity, final String userId) {
        Intent intent = new Intent(actiovity, ReputationActivity.class);
        intent.putExtra("userId", userId);

        actiovity.startActivity(intent);
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        l = ListViewMethodsBridge.getItemId(this, i, l);
        if (l < 0||m_Adapter.getCount()<=l) return false;
        Reputation rep = m_Reputations.get((int) l);
        ForumUser.showUserMenu(this,m_Header,rep.userId,rep.user);


        return true;

    }

    public void plusRep() {
        plusRep(m_Reputations.userId,m_Reputations.user);
    }

    public void minusRep() {
        minusRep(m_Reputations.userId,m_Reputations.user);
    }

    public void plusRep(String userId, String userNick) {
        plusRep(this,mHandler,"0",userId,userNick);
    }

    public void minusRep(String userId, String userNick) {
        minusRep(this,mHandler,"0",userId,userNick);
    }

    public static void plusRep(Activity activity, Handler handler, String postId, String userId, String userNick) {
        showChangeRep(activity, handler,postId, userId, userNick, "add", "Поднять репутацию");
    }

    public static void minusRep(Activity activity, Handler handler, String postId, String userId, String userNick) {
        showChangeRep(activity, handler,postId, userId, userNick, "minus", "Опустить репутацию");
    }

    private static void showChangeRep(Activity activity, Handler handler, final String postId, String userId, String userNick, final String type, String title) {
        ForumUser.startChangeRep(activity, handler, userId, userNick, postId, type, title);

    }


    private void refresh() {
        m_Reputations.clear();
        new LoadTask(this).execute();
    }

    private class RepsAdapter extends ArrayAdapter<Reputation> {
        private LayoutInflater m_Inflater;

        private int m_ThemeTitleSize = 13;
        private int m_TopTextSize = 10;
        private int m_BottomTextSize = 11;

        public RepsAdapter(Context context, int textViewResourceId, ArrayList<Reputation> objects) {
            super(context, textViewResourceId, objects);

            m_Inflater = LayoutInflater.from(context);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            m_ThemeTitleSize = ExtPreferences.parseInt(prefs,
                    "interface.themeslist.title.font.size", 13);
            m_TopTextSize = (int) Math.floor(10.0 / 13 * m_ThemeTitleSize);
            m_BottomTextSize = (int) Math.floor(11.0 / 13 * m_ThemeTitleSize);


        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {


                convertView = m_Inflater.inflate(R.layout.reputation_item, parent, false);


                holder = new ViewHolder();

                holder.txtIsNew = (ImageView) convertView
                        .findViewById(R.id.txtIsNew);
                holder.txtAuthor = (TextView) convertView
                        .findViewById(R.id.txtAuthor);
                holder.txtAuthor.setTextSize(m_TopTextSize);

                holder.txtLastMessageDate = (TextView) convertView
                        .findViewById(R.id.txtLastMessageDate);
                holder.txtLastMessageDate.setTextSize(m_TopTextSize);

                holder.txtTitle = (TextView) convertView
                        .findViewById(R.id.txtTitle);
                holder.txtTitle.setTextSize(m_ThemeTitleSize);


                holder.txtForumTitle = (TextView) convertView
                        .findViewById(R.id.txtForumTitle);

                holder.txtForumTitle.setTextSize(m_BottomTextSize);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Reputation rep = this.getItem(position);

            holder.txtAuthor.setText(rep.user);
            holder.txtLastMessageDate.setText(rep.date);
            holder.txtTitle.setText(rep.description);

            holder.txtForumTitle.setText("@" + rep.source);

            if (rep.level.equals("up")) {
                holder.txtIsNew.setImageResource(R.drawable.new_flag);
            } else {
                holder.txtIsNew.setImageResource(R.drawable.old_flag);
            }

            return convertView;
        }


        public class ViewHolder {

            TextView txtAuthor;
            TextView txtLastMessageDate;
            TextView txtTitle;
            ImageView txtIsNew;
            TextView txtForumTitle;

        }

    }

    public class LoadTask extends AsyncTask<String, String, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;
        public String Post;

        public LoadTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
            dialog.setCancelable(false);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            this.dialog.setMessage(progress[0]);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Client.INSTANCE.loadUserReputation(m_Reputations, new OnProgressChangedListener() {
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

        // can use UI thread here
        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка истории репутации...");
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
                m_Adapter.notifyDataSetChanged();
            } else {
                if (ex != null)
                    Log.e(ReputationActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
            txtLoadMoreThemes.setText("Всего: " + m_Reputations.getMaxCount());
            int loadMoreVisibility = m_Reputations.getMaxCount() > m_Reputations.size() ? View.VISIBLE : View.GONE;
            txtPullToLoadMore.setVisibility(loadMoreVisibility);
            imgPullToLoadMore.setVisibility(loadMoreVisibility);
            txtFroum.setText(m_Reputations.description);
            listView.onRefreshComplete();
        }

    }


    public static final class MenuFragment extends SherlockFragment {
        public MenuFragment() {

        }

        private ReputationActivity getReputationActivity(){

            return (ReputationActivity)getActivity();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            if(!Client.INSTANCE.getLogined() || Client.INSTANCE.UserId.equals(getReputationActivity().getUserId()))
                return;
            com.actionbarsherlock.view.MenuItem item;

            if(Client.INSTANCE.getLogined() &&!getReputationActivity().getUserId().equals(Client.INSTANCE.UserId)){
                item = menu.add("Повысить репутацию");
                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {

                        getReputationActivity().plusRep();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                item = menu.add("Понизить репутацию");
                item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        getReputationActivity().minusRep();
                        return true;
                    }
                });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }

        }
    }
}


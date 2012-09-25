package org.softeg.slartus.forpda.Mail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.Client;
import org.softeg.slartus.forpda.Mail.classes.MailFolder;
import org.softeg.slartus.forpda.Mail.classes.MailFolders;
import org.softeg.slartus.forpda.MyApp;
import org.softeg.slartus.forpda.R;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpdaapi.NotReportException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 13.03.12
 * Time: 11:05
 */
public class MailBoxActivity extends BaseFragmentActivity {
    TabHost mTabHost;
    ViewPager viewPager;
    TabsAdapter tabsAdapter;
    MenuFragment mFragment1;
    public static boolean Refresh;
    public static String DeleteMsgId=null;
    private Boolean m_FromEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mails_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createMenu();

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        viewPager = (ViewPager) findViewById(R.id.pager);
        tabsAdapter = new TabsAdapter(MailBoxActivity.this, mTabHost, viewPager);

        int sdk = new Integer(Build.VERSION.SDK).intValue();

        if (sdk < 8) {
            Bundle bundle = new Bundle();
            bundle.putString("vid", "in");
            tabsAdapter.addTab(mTabHost.newTabSpec("vid").setIndicator(""), MailsListFragment.class, bundle);
        }
        LoadFoldersTask loadFoldersTask = new LoadFoldersTask(this);
        loadFoldersTask.execute();


//        Bundle bundle = new Bundle();
//        bundle.putInt("mailstype", Mail.MAIL_TYPE_INCOMING);
//        tabsAdapter.addTab(mTabHost.newTabSpec("incoming").setIndicator("Входящие"), MailsListFragment.class, bundle);
//
//        bundle = new Bundle();
//        bundle.putInt("mailstype", Mail.MAIL_TYPE_OUTGOING);
//        tabsAdapter.addTab(mTabHost.newTabSpec("outgoing").setIndicator("Исходящие"), MailsListFragment.class, bundle);


//        if (savedInstanceState != null) {
//            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ) {
            goBack();
            return true;
        }

        return true;
    }
    
    @Override
    public void onResume(){
        super.onResume();
        if(Refresh){
            Refresh=true;
            ((MailsListFragment) tabsAdapter .getItem(mTabHost.getCurrentTab())).refreshData();
        }
        else if(DeleteMsgId!=null){
            String id=DeleteMsgId;
            DeleteMsgId=null;
            ((MailsListFragment) tabsAdapter .getItem(mTabHost.getCurrentTab())).deleteItem(id);
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = getIntent();
        if(intent!=null&&intent.getExtras()!=null&&intent.getExtras().containsKey("activity"))
            super.onBackPressed();
        else
            MyApp.showMainActivityWithoutBack(this);
    }

    private void goBack() {
        onBackPressed();
    }



    private void createMenu() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
        if (mFragment1 == null) {
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        ft.commit();
    }


    private MailFolders createDefaultFolders() {
        MailFolders mailFolders = new MailFolders();
        mailFolders.add(new MailFolder("in", "Inbox", 0));
        mailFolders.add(new MailFolder("sent", "Sent Items", 0));
        return mailFolders;
    }

    private MailFolders loadFolders() throws IOException {

        String body = Client.INSTANCE.loadPageAndCheckLogin("http://4pda.ru/forum/index.php?act=Msg&CODE=01", null);

        final Pattern foldersPattern = Pattern.compile("<!-- Messenger Links -->([\\s\\S]*?)<!-- End Messenger -->");

        Matcher m = foldersPattern.matcher(body);
        if (!m.find())
            throw new NotReportException("Ошибка загрузки директорий");
        final Pattern folderPattern = Pattern.compile("<img.*?/>.*?<a href=\"http://4pda.ru/forum/index.php\\?act=Msg&amp;CODE=01&amp;VID=(.*?)\">(.*?)( \\((\\d+)\\))?</a>");
        Matcher m1 = folderPattern.matcher(m.group(1));
        MailFolders res = new MailFolders();
        while (m1.find()) {
            int count = 0;
            if (m1.group(4) != null)
                count = Integer.parseInt(m1.group(4));
            res.add(new MailFolder(m1.group(1), Html.fromHtml(m1.group(2)).toString(), count));
        }
        return res;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }

    private class LoadFoldersTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;
        public String Post;
        private MailFolders mMailFolders = null;

        public LoadFoldersTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mMailFolders = loadFolders();

                return true;
            } catch (Exception e) {
                mMailFolders = createDefaultFolders();
                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Загрузка директорий...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (mMailFolders.size() > 0) {

                mTabHost.clearAllTabs();
                for (MailFolder mailFolder : mMailFolders) {
                    Bundle bundle = new Bundle();
                    bundle.putString("vid", mailFolder.getVid());

                    tabsAdapter.addTab(mTabHost.newTabSpec(mailFolder.getVid()).setIndicator(createTabView(MailBoxActivity.this, mailFolder.getTitle())), MailsListFragment.class, bundle);
                }


            }

            if (ex != null)
                Log.e(MailBoxActivity.this, ex);


        }

        private View createTabView(final Context context, final String text) {
            View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
            //view.seton
            TextView tv = (TextView) view.findViewById(R.id.tabsText);
            tv.setText(text);
            return view;
        }

    }

    public static final class MenuFragment extends SherlockFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


            com.actionbarsherlock.view.MenuItem item = menu.add("Новое сообщение").setIcon(R.drawable.ic_compose);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), EditMailActivity.class);

                    startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

//            item = menu.add("Удалить..").setIcon(android.R.drawable.ic_menu_delete);
//            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
//                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
//                    Intent intent = new Intent(getActivity(), EditMailActivity.class);
//
//                    startActivity(intent);
//                    return true;
//                }
//            });

        }
    }

    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {

            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {

                clss = _class;
                args = _args;
            }

            public void setFragment(Fragment fragment) {
                this.fragment = fragment;
            }

            public Fragment getFragment() {
                return fragment;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }


            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {

            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));


            TabInfo info = new TabInfo(clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            if (info.getFragment() == null) {
                Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
                info.setFragment(fragment);
            }
            return info.getFragment();
        }


        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
            ((MailsListFragment) getItem(position)).startLoad();
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }


        public void onPageSelected(int position) {
            mTabHost.setCurrentTab(position);
            ((MailsListFragment) getItem(position)).startLoad();

        }


        public void onPageScrollStateChanged(int state) {
        }
    }


}

package org.softeg.slartus.forpda;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import org.softeg.slartus.forpda.Tabs.ITab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.Tabs.ThemesTab;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.ProfileMenuFragment;
import org.softeg.slartus.forpda.common.Log;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 17.09.11
 * Time: 22:23
 * To change this template use File | Settings | File Templates.
 */
public class MainActivity extends BaseFragmentActivity {
    private Handler mHandler = new Handler();
    TabHost mTabHost;

    MenuFragment mFragment1;

    public TabHost getTabHost() {
        return mTabHost;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(false);

        setContentView(R.layout.main);

        createMenu();

        try {

            Client client = Client.INSTANCE;

            client.addOnUserChangedListener(new Client.OnUserChangedListener() {
                public void onUserChanged(String user, Boolean success) {
                    userChanged();
                }
            });
            client.addOnMailListener(new Client.OnMailListener() {
                public void onMail(int count) {
                    mailsChanged();
                }
            });
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!preferences.getBoolean("AutoLogin", false)) {
                client.clearCookies();
            }

            if (checkIntent())
                return;

            createTabHost();
            MyApp.INSTANCE.showPromo(this);

        } catch (Exception ex) {
            Log.e(getApplicationContext(), ex);
        }

    }

    private void createMenu() {
        try {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mFragment1 = (MenuFragment) fm.findFragmentByTag("f1");
            if (mFragment1 == null) {
                mFragment1 = new MenuFragment();
                ft.add(mFragment1, "f1");
            }
            ft.commit();
        } catch (Exception ex) {
            Log.e(this, ex);
        }

    }


    private boolean checkIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            String url = intent.getData().toString();
            if (IntentActivity.tryShowUrl(this, mHandler, url, false, true)) {

                return true;
            }


            Toast.makeText(this, "Не умею обрабатывать ссылки такого типа", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return false;
    }

    private void startActivity(Uri data) {
        Intent themeIntent = new Intent(this, ThemeActivity.class);
        themeIntent.setData(data);
        startActivity(themeIntent);
    }

//    @Override
//    public void setTitle(java.lang.CharSequence title) {
//        txtTitle.setText(title);
//    }

    private Boolean m_TabHostCreating = false;
    private String m_Defaulttab = "Tab1";

    private void createTabHost() throws NotReportException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        m_Defaulttab = prefs.getString("tabs.defaulttab", "Tab1");
        m_TabHostCreating = true;
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.clearAllTabs();
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String s) {
                if (m_TabHostCreating) return;

                ITab tab = (ITab) mTabHost.getCurrentView();
                if (tab != null && !tab.refreshed())
                    tab.refresh();
            }
        });
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
        MainTabContentFactory mainTabContentFactory = new MainTabContentFactory();

        for (int i = 1; i <= getResources().getStringArray(R.array.tabsArray).length; i++) {
            addTab(mainTabContentFactory, "Tab" + i, prefs);
        }


        mTabHost.setCurrentTab(-1);
        mTabHost.setCurrentTabByTag(m_Defaulttab);
        m_TabHostCreating = false;

        ITab tab = (ITab) mTabHost.getCurrentView();
        mTabHost.getCurrentView().invalidate();
        tab.refresh();
    }


    private void addTab(MainTabContentFactory mainTabContentFactory, String tabId,
                        SharedPreferences prefs) throws NotReportException {
        String label = Tabs.getTabName(prefs, tabId);


        View tabView = createTabView(mTabHost.getContext(), label);
        mTabHost.addTab(mTabHost.newTabSpec(tabId).setIndicator(tabView)
                .setContent(mainTabContentFactory));
    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        //view.seton
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    private Boolean m_ExitWarned = false;

    @Override
    public void onBackPressed() {
        if (mTabHost == null || mTabHost.getCurrentView() == null) {
            finish();
            System.exit(0);
        }
        if (mTabHost != null && mTabHost.getCurrentView() != null && !((ITab) mTabHost.getCurrentView()).onParentBackPressed()) {
            if (!m_ExitWarned) {
                Toast.makeText(getApplicationContext(), "Нажмите кнопку НАЗАД снова, чтобы выйти из программы", Toast.LENGTH_SHORT).show();
                m_ExitWarned = true;
            } else {
                finish();
                System.exit(0);
            }

        } else {
            m_ExitWarned = false;
        }
    }

    private void userChanged() {
        mHandler.post(new Runnable() {
            public void run() {

                mFragment1.setUserMenu();

            }
        });

    }

    private void mailsChanged() {
        mHandler.post(new Runnable() {
            public void run() {

                mFragment1.setUserMenu();

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        m_ExitWarned = false;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, android.view.View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ((ITab) mTabHost.getCurrentView()).onCreateContextMenu(menu, v, menuInfo, mHandler);

    }


    private class MainTabContentFactory implements TabHost.TabContentFactory {

        public View createTabContent(String tabId) {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String template = Tabs.getTemplate(prefs, tabId);
            ThemesTab tabContent = Tabs.create(MainActivity.this, template, tabId);
            tabContent.setOnTabTitleChangedListener(new ThemesTab.OnTabTitleChangedListener() {
                public void onTabTitleChanged(String title) {
                    View tabView = mTabHost.getCurrentTabView();
                    if (tabView != null) {
                        ((TextView) tabView.findViewById(R.id.tabsText)).setText(title);

                    }
                }
            });
            if (tabContent != null) {
                ITab tab = (ITab) tabContent;
                ListView listView = tab.getListView();
                if (listView != null) {
                    registerForContextMenu(tab.getListView());
                    tab.getListView().setOnCreateContextMenuListener(MainActivity.this);
                }

//                if (!m_TabHostCreating || m_Defaulttab.equals(s))
//                    tab.refresh();

            }
            return tabContent;
        }
    }


    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public static final class MenuFragment extends ProfileMenuFragment {

        private TabHost getTabHost() {
            if (getActivity() == null) return null;
            return ((MainActivity) getActivity()).getTabHost();
        }

        public MenuFragment() {
            super();
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);

            com.actionbarsherlock.view.MenuItem item = menu.add("Поиск").setIcon(R.drawable.search);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                    Intent intent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

            item = menu.add("Настройки").setIcon(android.R.drawable.ic_menu_preferences);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    Intent settingsActivity = new Intent(
                            getActivity(), PreferencesActivity.class);
                    startActivity(settingsActivity);


                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add("Обновить").setIcon(R.drawable.ic_menu_refresh);
            item.setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                    try {
                        View currentView = getTabHost() == null ? null : getTabHost().getCurrentView();
                        if (currentView == null)
                            ((MainActivity) getActivity()).createTabHost();
                        else
                            ((ITab) getTabHost().getCurrentView()).refresh();
                    } catch (Exception ex) {
                        Log.e(getActivity(), ex);
                    }


                    return true;
                }
            });
            item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_NEVER);


            com.actionbarsherlock.view.SubMenu miQuickStart = menu.addSubMenu("Быстрый доступ").setIcon(android.R.drawable.ic_menu_view);

            for (final String template : Tabs.templates) {
                try {
                    miQuickStart.add(Tabs.getDefaultTemplateName(template))
                            .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                                    Intent intent = new Intent(getActivity(), QuickStartActivity.class);

                                    intent.putExtra("template", template);

                                    getActivity().startActivity(intent);
                                    return true;
                                }
                            });
                } catch (NotReportException e) {
                    Log.e(getActivity(), e);
                }
            }

            menu.add("Закрыть программу")
                    .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                    .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {


                            getActivity().finish();
                            System.exit(0);
                            return true;
                        }
                    });

        }
    }
}

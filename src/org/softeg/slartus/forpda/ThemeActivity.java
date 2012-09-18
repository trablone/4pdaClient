package org.softeg.slartus.forpda;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import org.softeg.slartus.forpda.Mail.EditMailActivity;
import org.softeg.slartus.forpda.Tabs.ForumTreeTab;
import org.softeg.slartus.forpda.Tabs.Tabs;
import org.softeg.slartus.forpda.classes.*;
import org.softeg.slartus.forpda.classes.Exceptions.NotReportException;
import org.softeg.slartus.forpda.classes.common.ExtPreferences;
import org.softeg.slartus.forpda.classes.common.Functions;
import org.softeg.slartus.forpda.classes.common.StringUtils;
import org.softeg.slartus.forpda.common.HelpTask;
import org.softeg.slartus.forpda.common.Log;
import org.softeg.slartus.forpda.qms.QmsChatActivity;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: slinkin
 * Date: 28.09.11
 * Time: 14:43
 */
public class ThemeActivity extends BaseFragmentActivity  {
    private AdvWebView webView;
    private Handler mHandler = new Handler();
    private ImageButton btnPrevSearch, btnNextSearch, btnCloseSearch;
    private EditText txtSearch;
    private RelativeLayout pnlSearch;
    private String m_ThemeUrl;
    private String m_LastUrl;
    private String m_Params;
    private Topic m_Topic;
    private TopicAttaches m_TopicAttaches=new TopicAttaches();
    private Boolean m_SpoilFirstPost = true;
    private Boolean m_UsePR = false;
    private Boolean m_UseVolumesScroll = false;
    // подтверждение отправки
    private Boolean m_ConfirmSend = true;

    private ArrayList<History> m_History = new ArrayList<History>();
    private Boolean m_UseZoom = true;
    // флаг добавлять подпись к сообщению
    private Boolean m_Enablesig = true, m_HidePostForm = false;
    private Boolean m_EnableEmo = true;
    // текст редактирования сообщения при переходе по страницам
    private String m_PostBody = "";
    // id сообщения к которому скроллить
    private String m_ScrollElement = null;
    private Boolean m_FromHistory = false;
    private int m_ScrollY = 0;
    private int m_ScrollX = 0;
    // пост, с которым совершают какие-то действия в текущий момент

    public static String s_ThemeId = null;
    public static String s_Params = null;
    MenuFragment mFragment1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.theme);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        createActionMenu();

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

        //setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL); чтобы поиск начинался при вводе текста
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences(prefs);
        pnlSearch = (RelativeLayout) findViewById(R.id.pnlSearch);
        txtSearch = (EditText) findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {

            }
        });
        btnPrevSearch = (ImageButton) findViewById(R.id.btnPrevSearch);
        btnPrevSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(false);
            }
        });
        btnNextSearch = (ImageButton) findViewById(R.id.btnNextSearch);
        btnNextSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                webView.findNext(true);
            }
        });
        btnCloseSearch = (ImageButton) findViewById(R.id.btnCloseSearch);
        btnCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSearch();
            }
        });
        webView = (AdvWebView) findViewById(R.id.wvBody);
        registerForContextMenu(webView);
        webView.getSettings().setLoadsImagesAutomatically(prefs.getBoolean("theme.LoadsImagesAutomatically", true));
        webView.setKeepScreenOn(prefs.getBoolean("theme.KeepScreenOn", false));

        webView.getSettings().setBuiltInZoomControls(m_UseZoom);
        webView.getSettings().setSupportZoom(m_UseZoom);

        webView.addJavascriptInterface(this, "HTMLOUT");
        try {
            webView.setInitialScale(ExtPreferences.parseInt(prefs, "theme.ZoomLevel", 150));
        } catch (Exception ex) {
            Log.e(null, ex);
        }



        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            m_Data = intent.getData();


            return;
        }
        Bundle extras = intent.getExtras();

        m_ThemeUrl = extras.getString("ThemeUrl");
        if (extras.containsKey("Params"))
            m_Params = extras.getString("Params");

        s_ThemeId=m_ThemeUrl;
        s_Params=m_Params;
//        String url = "showtopic=" + m_ThemeUrl + (TextUtils.isEmpty(m_Params) ? "" : ("&" + m_Params));
//        showTheme(url);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            if (getIntent().getData() == null)
//                onBackPressed();
//            else
            {
                MyApp.showMainActivityWithoutBack(this);
            }

            return true;
        }

        return true;
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

    private Uri m_Data = null;

    @Override
    public void onResume() {
        super.onResume();
        webView.setWebViewClient(new MyWebViewClient());

        if (s_ThemeId != null) {
            String url = "showtopic=" + s_ThemeId + (TextUtils.isEmpty(s_Params) ? "" : ("&" + s_Params));
            s_ThemeId = null;
            s_Params = null;
            mHandler.post(new Runnable() {
                public void run() {
                    webView.loadUrl("javascript:clearPostBody();");
                }
            });
            showTheme(url);
        }

        if (m_Data != null) {
            String url = m_Data.toString();
            m_Data = null;
            if (checkIsTheme(url)) {
                return;
            }
            IntentActivity.tryShowUrl(this, mHandler, url, false, true);

        }
    }


    private int loadScreenOrientationPref(SharedPreferences prefs) {
        return ExtPreferences.parseInt(prefs, "theme.ScreenOrientation", -1);
    }

    public Handler getHandler() {
        return mHandler;
    }

    public Topic getTopic() {
        return m_Topic;
    }

    public String getLastUrl() {
        return m_LastUrl;
    }

    public boolean onSearchRequested() {
        pnlSearch.setVisibility(View.VISIBLE);
        return false;
    }

    public TopicAttaches getTopicAttaches(){
        return m_TopicAttaches;
    }

    public void rememberScrollX() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);

        }
    }

    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        webView.findAll(query);
        try {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(webView, true);
        } catch (Throwable ignored) {
        }
        onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(new Runnable() {
            public void run() {
                webView.findAll("");
                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(webView, false);
                } catch (Throwable ignored) {
                }

                pnlSearch.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
        });

    }

    private void loadPreferences(SharedPreferences prefs) {
        m_UseZoom = prefs.getBoolean("theme.ZoomUsing", true);
        m_SpoilFirstPost = prefs.getBoolean("theme.SpoilFirstPost", true);
        m_ConfirmSend = prefs.getBoolean("theme.ConfirmSend", true);
        m_HidePostForm = prefs.getBoolean("theme.HidePostForm", false);
        // m_UsePR = prefs.getBoolean("posts.UsePR", false);
        m_UseVolumesScroll = prefs.getBoolean("theme.UseVolumesScroll", false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    public void showPostLinkMenu(final String postId) {
        showLinkMenu(Post.getLink(m_Topic.getId(), postId));
    }

    public void showLinkMenu(final String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        new AlertDialog.Builder(ThemeActivity.this)
                .setTitle("Выберите действие для ссылки")
                .setMessage(link)
                .setPositiveButton("Открыть в браузере", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(link));
                        ThemeActivity.this.startActivity(Intent.createChooser(marketIntent, "Выберите"));
                    }
                })
                .setNegativeButton("Скопировать в буфер", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        StringUtils.copyLinkToClipboard(ThemeActivity.this, link);

                    }
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (pnlSearch.getVisibility() == View.VISIBLE) {
            closeSearch();
            return;
        }


        if (!m_History.isEmpty()) {
            m_FromHistory = true;
            History history = m_History.get(m_History.size() - 1);
            m_History.remove(m_History.size() - 1);
            m_ScrollX = history.scrollX;
            m_ScrollY = history.scrollY;
            showTheme(history.url);
        } else {
            getPostBody();
            if (!TextUtils.isEmpty(m_PostBody)) {
                new AlertDialog.Builder(ThemeActivity.this)
                        .setTitle("Подтвердите действие")
                        .setMessage("Имеется введенный текст сообщения! Закрыть тему?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                clear();
                                ThemeActivity.super.onBackPressed();
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            } else {
                clear();
                super.onBackPressed();
            }
        }
    }

    public void clear() {
        webView.clearCache(true);
        if (m_Topic != null)
            m_Topic.dispose();
        m_Topic = null;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!m_UseVolumesScroll)
            return super.dispatchKeyEvent(event);
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        WebView scrollView = webView;
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    scrollView.pageUp(false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    scrollView.pageDown(false);
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }


    public String getPostBody() {
        if (!Functions.isWebviewAllowJavascriptInterface(this))
            return m_PostBody;

        try {
            webView.loadUrl("javascript:getPostBody();");
            Thread.sleep(350);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return m_PostBody;
    }

    public void openActionMenu(final String postId, Boolean canEdit, Boolean canDelete) {
        final QuickAction mQuickAction = new QuickAction(this);
        ActionItem actionItem;
        int showProfilePosition = -1;
//        if (post.getCanEdit()) {
//            ActionItem actionItem = new ActionItem();
//            actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
//            actionItem.setTitle("Просмотр профиля...");
//
//            showProfilePosition = mQuickAction.addActionItem(actionItem);
//        }

        int quotePosition = -1;
//        if (Client.INSTANCE.getLogined()) {
//            actionItem = new ActionItem();
//            actionItem.setTitle("Цитата");
//            quotePosition = mQuickAction.addActionItem(actionItem);
//        }

        int claimPosition = -1;
        if (Client.INSTANCE.getLogined()) {
            actionItem = new ActionItem();
            actionItem.setIcon(getResources().getDrawable(R.drawable.alert));
            actionItem.setTitle("Жалоба");
            claimPosition = mQuickAction.addActionItem(actionItem);
        }

        int editPosition = -1;
        if (canEdit) {
            actionItem = new ActionItem();
            actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("Редактировать");

            editPosition = mQuickAction.addActionItem(actionItem);
        }


        int deletePosition = -1;
        if (canDelete) {
            actionItem = new ActionItem();
            actionItem.setTitle("Удалить");
            actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));
            deletePosition = mQuickAction.addActionItem(actionItem);
        }

        int plusOdinPosition = -1;
        int minusOdinPosition = -1;
        if (!canEdit && !canDelete && Client.INSTANCE.getLogined()) {

            actionItem = new ActionItem();
            actionItem.setTitle("+1");
            plusOdinPosition = mQuickAction.addActionItem(actionItem);

            actionItem = new ActionItem();
            actionItem.setTitle("-1");
            minusOdinPosition = mQuickAction.addActionItem(actionItem);
        }

        final int finalDeletePosition = deletePosition;
        final int finalEditPosition = editPosition;
        final int finalShowProfilePosition = showProfilePosition;
        final int finalClaimPosition = claimPosition;
        final int finalPlusOdinPosition = plusOdinPosition;
        final int finalMinusOdinPosition = minusOdinPosition;
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(int pos) {
                if (pos == finalDeletePosition) { //Add item selected
                    new AlertDialog.Builder(ThemeActivity.this)
                            .setTitle("Подтвердите действие")
                            .setMessage("Вы действительно хотите удалить это сообщение?")
                            .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteMessage(postId);
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                }
                            })
                            .create()
                            .show();
                } else if (pos == finalEditPosition) {
                    Intent intent = new Intent(ThemeActivity.this, EditPostPlusActivity.class);

                    intent.putExtra("forumId", m_Topic.getForumId());
                    intent.putExtra("themeId", m_Topic.getId());
                    intent.putExtra("postId", postId);
                    intent.putExtra("authKey", m_Topic.getAuthKey());
                    ThemeActivity.this.startActivity(intent);
                } else if (pos == finalShowProfilePosition) {
                    Intent intent = new Intent(ThemeActivity.this, ProfileActivity.class);

                    ThemeActivity.this.startActivity(intent);
                } else if (pos == finalClaimPosition) {
                    Post.claim(ThemeActivity.this, mHandler, m_Topic.getId(), postId);
                } else if (pos == finalPlusOdinPosition) {
                    Post.plusOne(ThemeActivity.this, mHandler, postId);
                } else if (pos == finalMinusOdinPosition) {
                    Post.minusOne(ThemeActivity.this, mHandler, postId);
                }


            }
        });

        mQuickAction.show(webView);


    }

    private void showThemeBody(String body) {
        try {
            ThemeActivity.this.setTitle(m_Topic.getCurrentPage() + "/" + m_Topic.getPagesCount() + " " + m_Topic.getTitle());

            webView.loadDataWithBaseURL("\"file:///android_asset/\"", body, "text/html", "UTF-8", null);


        } catch (Exception ex) {
            Log.e(ThemeActivity.this, ex);
        }
    }

    private class MyPictureListener implements WebView.PictureListener {
        Thread m_ScrollThread;
        public void onNewPicture(WebView view, Picture arg1) {
            if(TextUtils.isEmpty(m_ScrollElement)&&m_ScrollX==0){
                //webView.setPictureListener(null);
                return;
            }
            if(m_ScrollThread!=null)return;

            m_ScrollThread=new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    mHandler.post(new Runnable() {
                        public void run() {
                            tryScrollToElement();
                        }
                    });
                }
            });



            m_ScrollThread.start();
        }

        private void tryScrollToElement() {
            if (m_ScrollY != 0) {
                webView.scrollTo(0, Math.min(m_ScrollY, (int) Math.floor(webView.getContentHeight() * webView.getScale() - webView.getHeight())));
            } else if (!TextUtils.isEmpty(m_ScrollElement)) {
                webView.scrollTo(0, 100);
                webView.scrollTo(0, 0);
                webView.loadUrl("javascript: scrollToElement('entry" + m_ScrollElement + "');");
                m_ScrollElement = null;

            }
            webView.setPictureListener(null);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            setSupportProgressBarIndeterminateVisibility(true);
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            setSupportProgressBarIndeterminateVisibility(false);
            //ThemeActivity.this.setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
            // if (ThemeActivity.this.webView.GetJavascriptInterfaceBroken())
            {
                if (url.contains("HTMLOUT.ru")) {
                    Uri uri = Uri.parse(url);
                    try {
                        String function = uri.getPathSegments().get(0);
                        String query = uri.getQuery();
                        Class[] parameterTypes = null;
                        String[] parameterValues = null;
                        if (!TextUtils.isEmpty(query)) {
                            Matcher m = Pattern.compile("(.*?)=(.*?)(&|$)").matcher(query);
                            ArrayList<String> objs = new ArrayList<String>();

                            while (m.find()) {
                                objs.add(m.group(2));
                            }
                            parameterValues = new String[objs.size()];
                            parameterTypes = new Class[objs.size()];
                            for (int i = 0; i < objs.size(); i++) {
                                parameterTypes[i] = String.class;
                                parameterValues[i] = objs.get(i);
                            }
                        }
                        Method method = ThemeActivity.class.getMethod(function, parameterTypes);

                        method.invoke(ThemeActivity.this, parameterValues);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }

            }
            m_ScrollY = 0;
            m_ScrollX = 0;
            if (checkIsTheme(url))
                return true;

            IntentActivity.tryShowUrl(ThemeActivity.this, mHandler, url, true, false);

            return true;
        }
    }


    private boolean checkIsTheme(String url) {
        Pattern p = Pattern.compile("http://" + Client.SITE + "/forum/index.php\\?((.*)?showtopic=.*)");
        Pattern p1 = Pattern.compile("http://" + Client.SITE + "/forum/index.php\\?((.*)?act=findpost&pid=\\d+(.*)?)");
        Matcher m = p.matcher(url);
        Matcher m1 = p1.matcher(url);
        if (m.find()) {
            showTheme(m.group(1));

            return true;
        } else if (m1.find()) {
            showTheme(m1.group(1));
            return true;
        }
        return false;
    }


    private void setThemeParams(String url) {
        Pattern pattern = Pattern.compile("showtopic=(\\d+)(&(.*))?");
        Matcher m = pattern.matcher(url);
        if (m.find()) {

            m_ThemeUrl = m.group(1);

            m_Params = m.group(3);
        } else {
            m_ThemeUrl = null;

            m_Params = null;
        }
    }

    public void showTheme(String url) {
        closeSearch();
        webView.setPictureListener(new MyPictureListener());
        GetThemeTask getThemeTask = new GetThemeTask(this);
        getThemeTask.execute(url.replace("|", ""));
    }

    public void setAndSaveUseZoom(boolean useZoom) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        m_UseZoom = useZoom;
        webView.getSettings().setBuiltInZoomControls(m_UseZoom);
        webView.getSettings().setSupportZoom(m_UseZoom);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("theme.ZoomUsing", m_UseZoom);
        editor.commit();
    }

    public boolean getUseZoom() {
        return m_UseZoom;
    }

    public WebView getWebView() {
        return webView;
    }

    private void sendMessage(String body) {
        if (m_UsePR) {
            body += "\n\n[right][SIZE=1]Я использую [URL=http://4pda.ru/forum/index.php?showtopic=271502]4pda-клиент[/URL][/SIZE][/right]";
        }
        PostTask postTask = new PostTask(ThemeActivity.this);
        postTask.Post = body;
        postTask.execute();
    }

    private void deleteMessage(final String postId) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Удаление сообщения...");
        dialog.show();
        new Thread(new Runnable() {
            public void run() {
                Exception ex = null;
                try {
                    Post.delete(postId, m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey());
                } catch (Exception e) {
                    ex = e;
                }

                final Exception finalEx = ex;
                mHandler.post(new Runnable() {
                    public void run() {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (finalEx != null)
                            Log.e(ThemeActivity.this, finalEx);

                        m_ScrollY = webView.getScrollY();
                        m_ScrollX = webView.getScrollY();
                        showTheme(m_LastUrl);
                    }
                });
            }
        }).start();

    }

    public void showUserMenu(final String userId, final String userNick) {
        // не забыть менять в ForumUser
        final QuickAction mQuickAction = new QuickAction(ThemeActivity.this);

        int insertNickPosition = -1;
        if (Client.INSTANCE.getLogined()) {
            ActionItem actionItem = new ActionItem();
            //actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("Вставить ник");
            insertNickPosition = mQuickAction.addActionItem(actionItem);
        }

        int sendLSPosition = -1;
        if (Client.INSTANCE.getLogined()) {
            ActionItem actionItem = new ActionItem();
            // actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("Отправить сообщение");

            sendLSPosition = mQuickAction.addActionItem(actionItem);
        }

        int sendQmsPosition = -1;
        if (Client.INSTANCE.getLogined()) {
            ActionItem actionItem = new ActionItem();
            // actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("Связаться через QMS");

            sendQmsPosition = mQuickAction.addActionItem(actionItem);
        }

        if (mQuickAction.getItemsCount() == 0) return;

        final int finalInsertNickPosition = insertNickPosition;
        final int finalSendLSPosition = sendLSPosition;
        final int finalSendQmsPosition = sendQmsPosition;
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(int pos) {
                try {
                    if (pos == finalInsertNickPosition) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                webView.loadUrl("javascript:insertText('[b]" + userNick + ",[/b] ');");
                            }
                        });
                    } else if (pos == finalSendLSPosition) {
                        Intent intent = new Intent(ThemeActivity.this, EditMailActivity.class);
                        intent.putExtra(EditMailActivity.KEY_PARAMS, "CODE=04&act=Msg&MID=" + userId);
                        intent.putExtra(EditMailActivity.KEY_USER, userNick);
                        intent.putExtra(EditMailActivity.KEY_RETERN_BACK, true);
                        startActivity(intent);
                    } else if (pos == finalSendQmsPosition) {
                        QmsChatActivity.openChat(ThemeActivity.this, userId, userNick);
                    }
                } catch (Exception ex) {
                    Log.e(ThemeActivity.this, ex);
                }
            }
        });

        mQuickAction.show(webView);
    }

    // class JavaScriptInterface
    //{

    public void msgToClipboard(String msgId) {
        if (m_Topic != null)
            StringUtils.copyLinkToClipboard(ThemeActivity.this, "http://4pda.ru/forum/index.php?showtopic=" + m_Topic.getId() + "&view=findpost&p=" + msgId);

    }

//        public void showUserMenu(final String userId, final String userNick) {
//            ThemeActivity.this.showUserMenu(userId,userNick);
//        }
//
//        public void showLinkMenu(String link) {
//            ThemeActivity.this.showLinkMenu(link);
//        }


    public void setPostBody(String postBody) {
        m_PostBody = postBody;
    }

    public void enableemo() {
        m_EnableEmo = !m_EnableEmo;
    }

    public void enablesig() {
        m_Enablesig = !m_Enablesig;
    }

    public String getPostText(String postId, String date, String userNick, String innerText) {

        return Post.getQuote(postId, date, userNick, innerText);
    }

    public void showPostMenu(String postId, String canEdit, String canDelete) {

        ThemeActivity.this.openActionMenu(postId, "1".equals(canEdit), "1".equals(canDelete));

    }

    public void advPost(final String body) {
        Intent intent = new Intent(ThemeActivity.this, EditPostPlusActivity.class);

        intent.putExtra("forumId", m_Topic.getForumId());
        intent.putExtra("themeId", m_Topic.getId());
        intent.putExtra("postId", "-1");
        intent.putExtra("body", body);
        intent.putExtra("authKey", m_Topic.getAuthKey());
        ThemeActivity.this.startActivity(intent);


    }

    public void post(final String body) {
        if (TextUtils.isEmpty(body))
            return;

        if (m_ConfirmSend) {
            new AlertDialog.Builder(ThemeActivity.this)
                    .setTitle("Уверены?")
                    .setMessage("Подтвердите отправку")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendMessage(body);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .create().show();
        } else {
            sendMessage(body);
        }
    }

    public void nextPage() {
        m_ScrollY = 0;
        m_ScrollX = 0;


        showTheme("showtopic=" + m_ThemeUrl + "&st=" + m_Topic.getCurrentPage() * m_Topic.getPostsPerPageCount(m_LastUrl));
    }

    public void prevPage() {
        m_ScrollY = 0;
        m_ScrollX = 0;
        showTheme("showtopic=" + m_ThemeUrl + "&st=" + (m_Topic.getCurrentPage() - 2) * m_Topic.getPostsPerPageCount(m_LastUrl));
    }

    public void firstPage() {
        m_ScrollY = 0;
        m_ScrollX = 0;
        showTheme("showtopic=" + m_ThemeUrl);
    }

    public void lastPage() {
        m_ScrollY = 0;
        m_ScrollX = 0;
        showTheme("showtopic=" + m_ThemeUrl + "&st=" + (m_Topic.getPagesCount() - 1) * m_Topic.getPostsPerPageCount(m_LastUrl));
    }

    public void jumpToPage() {
        m_ScrollY = 0;
        m_ScrollX = 0;
        CharSequence[] pages = new CharSequence[m_Topic.getPagesCount()];

        final int postsPerPage = m_Topic.getPostsPerPageCount(m_LastUrl);

        for (int p = 0; p < m_Topic.getPagesCount(); p++) {
            pages[p] = "Стр. " + (p + 1) + " (" + ((p * postsPerPage + 1) + "-" + (p + 1) * postsPerPage) + ")";
        }

        new AlertDialog.Builder(ThemeActivity.this)
                .setTitle("Перейти к странице")
                .setSingleChoiceItems(pages, m_Topic.getCurrentPage() - 1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        showTheme("showtopic=" + m_ThemeUrl + "&st=" + i * postsPerPage);
                    }
                })
                .create()
                .show();
    }

    public void plusRep(String postId, String userId, String userNick) {
        showChangeRep(postId, userId, userNick, "add", "Поднять репутацию");
    }

    public void minusRep(String postId, String userId, String userNick) {
        showChangeRep(postId, userId, userNick, "minus", "Опустить репутацию");
    }

    public void claim(String postId) {
        Post.claim(ThemeActivity.this, mHandler, m_Topic.getId(), postId);

    }

    public void showRep(final String userId) {
        ReputationActivity.showRep(this,userId);
    }

    public void showRepMenu(final String postId, final String userId, final String userNick, String canPlus, String canMinus) {

        final QuickAction mQuickAction = new QuickAction(ThemeActivity.this);
        ActionItem actionItem;

        int minusRepPosition = -1;
        if ("1".equals(canMinus)) {
            actionItem = new ActionItem();
            //actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("Понизить");
            minusRepPosition = mQuickAction.addActionItem(actionItem);
        }

        int showRepPosition = -1;

        actionItem = new ActionItem();
        // actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
        actionItem.setTitle("Посмотреть");

        showRepPosition = mQuickAction.addActionItem(actionItem);


        int plusRepPosition = -1;
        if ("1".equals(canPlus)) {
            actionItem = new ActionItem();
            //actionItem.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
            actionItem.setTitle("Повысить");
            plusRepPosition = mQuickAction.addActionItem(actionItem);
        }


        if (mQuickAction.getItemsCount() == 0) return;


        final int finalMinusRepPosition = minusRepPosition;
        final int finalShowRepPosition = showRepPosition;
        final int finalPlusRepPosition = plusRepPosition;
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(int pos) {
                if (pos == finalMinusRepPosition) {
                    minusRep(postId, userId, userNick);
                } else if (pos == finalShowRepPosition) {
                    showRep(userId);
                } else if (pos == finalPlusRepPosition) {
                    plusRep(postId, userId, userNick);
                }
            }
        });

        mQuickAction.show(webView);


    }

    private void showChangeRep(final String postId, String userId, String userNick, final String type, String title) {
        ForumUser.startChangeRep(ThemeActivity.this, mHandler, userId, userNick, postId, type, title);

    }
    // }

    private void saveHistory(String nextUrl) {
        if (m_FromHistory) {
            m_FromHistory = false;
            return;
        }
//        URI redirectUrl = Client.INSTANCE.getRedirectUri();
//        if (redirectUrl != null)
//            m_History.add(redirectUrl.toString());
//        else
        if (m_LastUrl != null && !TextUtils.isEmpty(m_LastUrl) && !m_LastUrl.equals(nextUrl)) {
            History history = new History();
            history.url = m_LastUrl;
            history.scrollX = m_ScrollX;
            history.scrollY = m_ScrollY;
            m_History.add(history);
        }
    }

    private void setScrollElement() {
        m_ScrollElement = null;
        URI redirectUri = Client.INSTANCE.getRedirectUri();
        String url = redirectUri != null ? redirectUri.toString() : m_LastUrl;
        if (url != null) {
            Pattern p = Pattern.compile("#entry(\\d+)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                m_ScrollElement = m.group(1);
            }
        }
    }

    private class GetThemeTask extends AsyncTask<String, String, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;

        public GetThemeTask(Context context) {
            mContext = context;

            dialog = new ProgressDialog(mContext);


        }


        private String m_ThemeBody;

        @Override
        protected Boolean doInBackground(String... forums) {
            try {
                if (isCancelled()) return false;
                Client client = Client.INSTANCE;
                saveHistory(forums[0]);
                m_LastUrl = forums[0];
                mHandler.post(new Runnable() {
                    public void run() {
                        getPostBody();
                    }
                });


                TopicBodyBuilder topicBodyBuilder = client.loadTopic(mHandler, mContext, m_LastUrl, m_SpoilFirstPost, m_Enablesig, m_EnableEmo, m_PostBody, m_HidePostForm, null);
                setScrollElement();
                setThemeParams(Client.INSTANCE.getRedirectUri() != null ? Client.INSTANCE.getRedirectUri().toString() : m_LastUrl);
                m_Topic = topicBodyBuilder.getTopic();
                m_ThemeBody = topicBodyBuilder.getBody();
                m_TopicAttaches=topicBodyBuilder.getTopicAttaches();
                topicBodyBuilder.clear();
                return true;
            } catch (Exception e) {
                // Log.e(ThemeActivity.this, e);
                ex = e;
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(final String... progress) {
            mHandler.post(new Runnable() {
                public void run() {
                    if (dialog != null)
                        dialog.setMessage(progress[0]);
                }
            });
        }

        protected void onPreExecute() {
            try {
                this.dialog.setMessage("Загрузка темы...");
                this.dialog.show();
            } catch (Exception ex) {
                Log.e(null, ex);
                this.cancel(true);
            }
        }

        private Exception ex;

        protected void onPostExecute(final Boolean success) {
            try {
                if (this.dialog.isShowing()) {
                    this.dialog.dismiss();
                }
            } catch (Exception ex) {
                Log.e(null, ex);
            }

            if (isCancelled()) return;

            if (success) {
                showThemeBody(m_ThemeBody);

            } else {
                if (ex.getClass() != NotReportException.class) {


                    ThemeActivity.this.setTitle(ex.getMessage());


                    webView.loadDataWithBaseURL("\"file:///android_asset/forum/style_images/1/folder_editor_buttons_white/\"", m_ThemeBody, "text/html", "UTF-8", null);

                }
                Log.e(mContext, ex);
            }
        }
    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {

        Context mContext;
        private final ProgressDialog dialog;
        public String Post;
        private String mPostResult = null;

        public PostTask(Context context) {
            mContext = context;
            dialog = new ProgressDialog(mContext);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                mPostResult = Client.INSTANCE.reply(m_Topic.getForumId(), m_Topic.getId(), m_Topic.getAuthKey(),
                        Post, m_Enablesig, m_EnableEmo, true,null);

                return true;
            } catch (Exception e) {

                ex = e;
                return false;
            }
        }

        // can use UI thread here
        protected void onPreExecute() {
            this.dialog.setMessage("Отправка сообщения...");
            this.dialog.show();
        }

        private Exception ex;

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }

            if (success) {
                if (!TextUtils.isEmpty(mPostResult)) {
                    Toast.makeText(mContext, "Ошибка: " + mPostResult, Toast.LENGTH_LONG).show();
                    return;
                }
                webView.loadUrl("javascript:clearPostBody();");

                showTheme("showtopic=" + m_Topic.getId() + "&view=getnewpost");


            } else {
                if (ex != null)
                    Log.e(ThemeActivity.this, ex);
                else
                    Toast.makeText(mContext, "Неизвестная ошибка",
                            Toast.LENGTH_SHORT).show();

            }
        }

    }


    /**
     * A fragment that displays a menu.  This fragment happens to not
     * have a UI (it does not implement onCreateView), but it could also
     * have one if it wanted.
     */
    public static final class MenuFragment extends ProfileMenuFragment {

        private ThemeActivity getInterface() {
            if (getActivity() == null) return null;
            return (ThemeActivity) getActivity();
        }

        public MenuFragment() {
            super();

        }

        @Override
        public void onPrepareOptionsMenu(com.actionbarsherlock.view.Menu menu) {
            if (mTopicOptionsMenu != null)
                configureOptionsMenu(getActivity(), getInterface().getHandler(), mTopicOptionsMenu, getInterface().getTopic(),
                        true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());
            else if (getInterface() != null && getInterface().getTopic() != null)
                mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface().getTopic(),
                        true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());
        }

        private com.actionbarsherlock.view.SubMenu mTopicOptionsMenu;

        private static com.actionbarsherlock.view.SubMenu addOptionsMenu(final Context context, final Handler mHandler, com.actionbarsherlock.view.Menu menu, final Topic topic,
                                                                         Boolean addFavorites, final String shareItUrl) {
            com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Опции");
            optionsMenu.getItem().setIcon(android.R.drawable.ic_menu_more);
            configureOptionsMenu(context, mHandler, optionsMenu, topic, addFavorites, shareItUrl);
            return optionsMenu;
        }

        private static void configureOptionsMenu(final Context context, final Handler mHandler, com.actionbarsherlock.view.SubMenu optionsMenu, final Topic topic,
                                                 Boolean addFavorites, final String shareItUrl) {
            optionsMenu.clear();

            if (Client.INSTANCE.getLogined() && topic != null) {

                if (addFavorites) {
                    optionsMenu.add("Добавить в избранное").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                final HelpTask helpTask = new HelpTask(context, "Добавление в избранное");
                                helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                                    public Object onMethod(Object param) {
                                        if (helpTask.Success)
                                            Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                        else
                                            Log.e(context, helpTask.ex);
                                        return null;
                                    }
                                });
                                helpTask.execute(new HelpTask.OnMethodListener() {
                                    public Object onMethod(Object param) throws IOException {
                                        return topic.addToFavorites();
                                    }
                                }
                                );
                            } catch (Exception ex) {
                                Log.e(context, ex);
                            }

                            return true;
                        }
                    });

                    optionsMenu.add("Удалить из избранного").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                final HelpTask helpTask = new HelpTask(context, "Удаление из избранного");
                                helpTask.setOnPostMethod(new HelpTask.OnMethodListener() {
                                    public Object onMethod(Object param) {
                                        if (helpTask.Success)
                                            Toast.makeText(context, (String) param, Toast.LENGTH_SHORT).show();
                                        else
                                            Log.e(context, helpTask.ex);
                                        return null;
                                    }
                                });
                                helpTask.execute(new HelpTask.OnMethodListener() {
                                    public Object onMethod(Object param) throws IOException {
                                        return topic.removeFromFavorites();  //To change body of implemented methods use File | Settings | File Templates.
                                    }
                                }
                                );
                            } catch (Exception ex) {
                                Log.e(context, ex);
                            }
                            return true;
                        }
                    });


                    optionsMenu.add("Подписаться").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                topic.startSubscribe(context, mHandler);
                            } catch (Exception ex) {
                                Log.e(context, ex);
                            }


                            return true;
                        }
                    });

                    optionsMenu.add("Отписаться").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                topic.unSubscribe(context, mHandler);
                            } catch (Exception ex) {
                                Log.e(context, ex);
                            }
                            return true;
                        }
                    });

                    optionsMenu.add("Открыть форум темы").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                Intent intent = new Intent(context, QuickStartActivity.class);

                                intent.putExtra("template", Tabs.TAB_FORUMS);
                                intent.putExtra(ForumTreeTab.KEY_FORUM_ID, topic.getForumId());
                                intent.putExtra(ForumTreeTab.KEY_FORUM_TITLE, topic.getForumTitle());
                                intent.putExtra(ForumTreeTab.KEY_TOPIC_ID, topic.getId());
                                context.startActivity(intent);
                            } catch (Exception ex) {
                                Log.e(context, ex);
                            }
                            return true;
                        }
                    });
                }


            }
            optionsMenu.add("Поделиться ссылкой").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {

                    try {
                        Intent sendMailIntent = new Intent(Intent.ACTION_SEND);
                        sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, topic.getTitle());
                        sendMailIntent.putExtra(Intent.EXTRA_TEXT, TextUtils.isEmpty(shareItUrl) ? ("http://4pda.ru/forum/index.php?showtopic=" + topic.getId()) : shareItUrl);
                        sendMailIntent.setType("text/plain");

                        context.startActivity(Intent.createChooser(sendMailIntent, "Поделиться ссылкой"));
                    } catch (Exception ex) {
                        return false;
                    }
                    return true;
                }
            });
            //return optionsMenu;
        }

        @Override
        public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            try {

                com.actionbarsherlock.view.MenuItem item = menu.add("Вложения")
                        .setIcon(R.drawable.ic_menu_download)
                        .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                                final TopicAttaches topicAttaches=getInterface().getTopicAttaches();
                                if(topicAttaches==null||topicAttaches.size()==0){
                                    Toast.makeText(getActivity(),"Страница не имеет вложений",Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                final boolean[] selection=new boolean[topicAttaches.size()];
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Вложения")
                                        .setMultiChoiceItems(topicAttaches.getList(),selection,new DialogInterface.OnMultiChoiceClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                                selection[i]=b;
                                            }
                                        })
                                        .setPositiveButton("Скачать", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();

                                                for (int j=0;j<selection.length;j++){
                                                    if(!selection[j])continue;
                                                    DownloadsActivity.download(getActivity(), topicAttaches.get(j).getUri());
                                                }
                                            }
                                        })
                                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();
                                return true;
                            }
                        });
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                item = menu.add("Найти на странице")
                        .setIcon(android.R.drawable.ic_menu_search)
                        .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                                getInterface().onSearchRequested();

                                return true;
                            }
                        });
                item.setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_ALWAYS);

                menu.add("Обновить")
                        .setIcon(R.drawable.ic_menu_refresh)
                        .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                                getInterface().rememberScrollX();
//                            m_ScrollY = webView.getScrollY();
//                            m_ScrollX = webView.getScrollX();
                                getInterface().showTheme(getInterface().getLastUrl());


                                return true;
                            }
                        });
                menu.add("Браузер")
                        .setIcon(R.drawable.ic_menu_goto)
                        .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {

                            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                                try {
                                    Intent marketIntent = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://" + Client.SITE + "/forum/index.php?" + getInterface().getLastUrl()));
                                    startActivity(Intent.createChooser(marketIntent, "Выберите"));


                                } catch (ActivityNotFoundException e) {
                                    Log.e(getActivity(), e);
                                }


                                return true;
                            }
                        });
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                if (getInterface() != null)
                    mTopicOptionsMenu = addOptionsMenu(getActivity(), getInterface().getHandler(), menu, getInterface().getTopic(),
                            true, "http://4pda.ru/forum/index.php?" + getInterface().getLastUrl());

                com.actionbarsherlock.view.SubMenu optionsMenu = menu.addSubMenu("Настройки");
                optionsMenu.getItem().setIcon(android.R.drawable.ic_menu_preferences);
                optionsMenu.getItem().setTitle("Настройки");
                optionsMenu.add("Масштабировать").setIcon(android.R.drawable.ic_menu_preferences)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                try {
                                    prefs.getBoolean("theme.ZoomUsing", true);
                                    menuItem.setChecked(!menuItem.isChecked());
                                    getInterface().setAndSaveUseZoom(menuItem.isChecked());

                                } catch (Exception ex) {
                                    Log.e(getActivity(), ex);
                                }


                                return true;
                            }
                        }).setCheckable(true).setChecked(prefs.getBoolean("theme.ZoomUsing", true));

                //if (getInterface() != null && getInterface().getUseZoom())
                {
                    optionsMenu.add("Запомнить масштаб").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                            try {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("theme.ZoomLevel", Integer.toString((int) (getInterface().getWebView().getScale() * 100)));
                                editor.commit();
                                getInterface().getWebView().setInitialScale((int) (getInterface().getWebView().getScale() * 100));
                                Toast.makeText(getActivity(), "Масштаб запомнен", Toast.LENGTH_SHORT).show();
                            } catch (Exception ex) {
                                Log.e(getActivity(), ex);
                            }


                            return true;
                        }
                    });
                }

                optionsMenu.add("Загружать изображения").setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem menuItem) {
                        Boolean loadImagesAutomatically1 = getInterface().getWebView().getSettings().getLoadsImagesAutomatically();
                        getInterface().getWebView().getSettings().setLoadsImagesAutomatically(!loadImagesAutomatically1);
                        menuItem.setChecked(!loadImagesAutomatically1);
                        return true;
                    }
                }).setCheckable(true).setChecked(getInterface().getWebView().getSettings().getLoadsImagesAutomatically());

                menu.add("Закрыть")
                        .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                        .setOnMenuItemClickListener(new com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(com.actionbarsherlock.view.MenuItem item) {
                                getInterface().getPostBody();
                                if (!TextUtils.isEmpty(getInterface().getPostBody())) {
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("Подтвердите действие")
                                            .setMessage("Имеется введенный текст сообщения! Закрыть тему?")
                                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    getInterface().clear();
                                                    getInterface().finish();
                                                }
                                            })
                                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            })
                                            .create()
                                            .show();
                                } else {
                                    getInterface().clear();
                                    getInterface().finish();
                                }

                                return true;
                            }
                        });
            } catch (Exception ex) {
                Log.e(getActivity(), ex);
            }


        }
    }


    @Override
    public void onPause(){
        super.onPause();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


    @Override
    public void onStop(){
        super.onStop();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        webView.setWebViewClient(null);
        webView.setPictureListener(null);
    }
}

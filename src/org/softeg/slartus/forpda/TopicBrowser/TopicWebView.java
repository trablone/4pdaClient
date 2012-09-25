package org.softeg.slartus.forpda.TopicBrowser;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import org.softeg.slartus.forpda.*;

import java.lang.reflect.Method;

/**
 * User: slinkin
 * Date: 20.09.12
 * Time: 8:48
 */
public class TopicWebView extends WebView {
    private View pnlSearch;
    private EditText txtSearch;
    private ImageButton btnPrevSearch;
    private ImageButton btnNextSearch;
    private ImageButton btnCloseSearch;
    private Handler mHandler = new Handler();
    public TopicWebView(Context context) {
        super(context);
        init(context);
    }

    public TopicWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        // gd = new GestureDetector(context, sogl);
        WebSettings webSettings=getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode( WebSettings.LOAD_NO_CACHE);

        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setScrollbarFadingEnabled(false);
        // setWebChromeClient(new WebChromeClient());
        setBackgroundColor(MyApp.INSTANCE.getThemeStyleWebViewBackground());
        loadData("<html><head></head><body bgcolor="+MyApp.INSTANCE.getCurrentThemeName()+"></body></html>","text/html", "UTF-8");
    }

    public View getSearchPanel() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pnlSearch = inflater.inflate(R.layout.search_panel,null);

        txtSearch = (EditText) pnlSearch.findViewById(R.id.txtSearch);
        txtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                doSearch(txtSearch.getText().toString());
            }

            public void afterTextChanged(Editable editable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        btnPrevSearch = (ImageButton) pnlSearch.findViewById(R.id.btnPrevSearch);
        btnPrevSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                findNext(false);
            }
        });
        btnNextSearch = (ImageButton) pnlSearch.findViewById(R.id.btnNextSearch);
        btnNextSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                findNext(true);
            }
        });
        btnCloseSearch = (ImageButton) pnlSearch.findViewById(R.id.btnCloseSearch);
        btnCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                closeSearch();
            }
        });
        return pnlSearch;
    }

    public boolean onSearchRequested() {
        pnlSearch.setVisibility(View.VISIBLE);
        return false;
    }


    public void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    private void doSearch(String query) {
        if (TextUtils.isEmpty(query)) return;
        findAll(query);
        try {
            Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
            m.invoke(this, true);
        } catch (Throwable ignored) {
        }
        onSearchRequested();
    }

    private void closeSearch() {
        mHandler.post(new Runnable() {
            public void run() {
                findAll("");
                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.invoke(this, false);
                } catch (Throwable ignored) {
                }

                pnlSearch.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(pnlSearch.getWindowToken(), 0);
            }
        });
    }
}

package org.softeg.slartus.forpda.TopicBrowser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.view.Window;
import org.softeg.slartus.forpda.BaseFragmentActivity;
import org.softeg.slartus.forpda.classes.common.StringUtils;
import org.softeg.slartus.forpda.R;

/**
 * User: slinkin
 * Date: 20.09.12
 * Time: 8:47
 */
public class TopicActivity extends BaseFragmentActivity {
    private TopicWebView mBrowser;
    private String m_ScrollElement;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.topic);

        mBrowser = (TopicWebView) findViewById(R.id.browser);
        mBrowser.setWebViewClient(new MyWebViewClient());
        mBrowser.setPictureListener(new MyPictureListener());
        addContentView(mBrowser.getSearchPanel(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        registerForContextMenu(mBrowser);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mBrowser.handleIntent(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        final WebView.HitTestResult hitTestResult = mBrowser.getHitTestResult();
        switch (hitTestResult.getType()) {
            case WebView.HitTestResult.UNKNOWN_TYPE:
            case WebView.HitTestResult.EDIT_TEXT_TYPE:
                break;
            default: {
                showLinkMenu(hitTestResult.getExtra());
            }
        }
    }

    public void showLinkMenu(final String link) {
        if (TextUtils.isEmpty(link) || link.contains("HTMLOUT.ru")
                || link.equals("#")
                || link.startsWith("file:///")) return;
        new AlertDialog.Builder(this)
                .setTitle("Выберите действие для ссылки")
                .setMessage(link)
                .setPositiveButton("Открыть в браузере", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(link));
                        startActivity(Intent.createChooser(marketIntent, "Выберите"));
                    }
                })
                .setNegativeButton("Скопировать в буфер", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        StringUtils.copyLinkToClipboard(TopicActivity.this, link);

                    }
                })
                .create()
                .show();
    }

    private void tryScrollToElement() {
        if (!TextUtils.isEmpty(m_ScrollElement)) {
            mBrowser.scrollTo(0, 100);
            mBrowser.scrollTo(0, 0);
            mBrowser.loadUrl("javascript: scrollToElement('entry" + m_ScrollElement + "');");
            m_ScrollElement = null;
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

           setProgressBarIndeterminateVisibility(true);
        }

        @Override
        public void onScaleChanged(android.webkit.WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);


            setProgressBarIndeterminateVisibility(false);



        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, final String url) {
//            m_ScrollY = 0;
//            m_ScrollX = 0;
//            if (checkIsTheme(url))
//                return true;
//
//            IntentActivity.tryShowUrl(ThemeActivity.this, mHandler, url, true, false);

            return true;
        }



    }
    class MyPictureListener implements WebView.PictureListener {


        public void onNewPicture(WebView view, Picture arg1) {
            tryScrollToElement();
        }
    }


}

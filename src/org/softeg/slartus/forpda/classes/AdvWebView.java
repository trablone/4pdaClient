package org.softeg.slartus.forpda.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import org.softeg.slartus.forpda.MyApp;

/**
 * User: slinkin
 * Date: 25.01.12
 * Time: 10:00
 */
public class AdvWebView extends WebView {
    GestureDetector gd;

    public AdvWebView(Context context) {
        super(context);
        init(context);
    }

    public AdvWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        // gd = new GestureDetector(context, sogl);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        getSettings().setDomStorageEnabled(true);
        getSettings().setAllowFileAccess(true);
        getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setScrollbarFadingEnabled(false);
       // setWebChromeClient(new WebChromeClient());
        setBackgroundColor(MyApp.INSTANCE.getThemeStyleWebViewBackground());
        loadData("<html><head></head><body bgcolor="+MyApp.INSTANCE.getCurrentThemeName()+"></body></html>","text/html", "UTF-8");
    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
//        return gd.onTouchEvent(event);
//
//    }

    GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
        public boolean onDown(MotionEvent event) {
            return true;
        }

        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getRawX() > event2.getRawX()) {
                show_toast("swipe left");
            } else {
                show_toast("swipe right");
            }
            return true;
        }
    };

    void show_toast(final String text) {
        Toast t = Toast.makeText(this.getContext(), text, Toast.LENGTH_SHORT);
        t.show();
    }

//    private boolean is_gone = false;
//
//    @Override
//    public void onWindowVisibilityChanged(int visibility) {
//        super.onWindowVisibilityChanged(visibility);
//        if (visibility == View.GONE) {
//            try {
//                WebView.class.getMethod("onPause").invoke(this);//stop flash
//            } catch (Exception e) {
//            }
//            this.pauseTimers();
//            this.is_gone = true;
//        } else if (visibility == View.VISIBLE) {
//            try {
//                WebView.class.getMethod("onResume").invoke(this);//resume flash
//            } catch (Exception e) {
//            }
//            this.resumeTimers();
//            this.is_gone = false;
//        }
//    }
//
//    public void onDetachedFromWindow() {//this will be trigger when back key pressed, not when home key pressed
//        if (this.is_gone) {
//            try {
//                this.destroy();
//            } catch (Exception e) {
//            }
//        }
//    }
}

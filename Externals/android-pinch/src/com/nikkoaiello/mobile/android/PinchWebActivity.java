package com.nikkoaiello.mobile.android;

import android.app.Activity;
import android.os.Bundle;

public class PinchWebActivity extends Activity {
	
	PinchWebView view;
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        
        view = (PinchWebView) findViewById(R.id.web);
        //new SqueezeView(webView);
    }

    protected void onResume() {
    	super.onResume();
    	view.loadUrl("http://www.nytimes.com");
    }
    
}
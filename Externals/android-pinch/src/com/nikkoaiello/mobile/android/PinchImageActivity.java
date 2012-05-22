package com.nikkoaiello.mobile.android;

import android.app.Activity;
import android.os.Bundle;

public class PinchImageActivity extends Activity {
	
	PinchImageView view;
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageview);
        
        view = (PinchImageView) findViewById(R.id.image);
        //new SqueezeView(webView);
    }

    
}
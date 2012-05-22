package com.nikkoaiello.mobile.android;

import android.webkit.WebView;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;

public class PinchView {

	public static final int GROW = 0;
	public static final int SHRINK = 1;
	public static final int DURATION = 150;
	
	public static final float MIN_SCALE = 0.5f;
	public static final float MAX_SCALE = 2.5f;
	public static final float ZOOM = 0.1f;
	
	protected float x1, 
					x2, 
					y1, 
					y2, 
					x1_pre,
					y1_pre,
					x_scale = 1.0f,
					y_scale = 1.0f,
					dist_curr = -1, 
					dist_pre = -1,
					dist_delta;
	
	private WebView _view;
	private long mLastGestureTime;
	
	public PinchView(WebView view) {
		_view = view;
		_view.setBackgroundColor(Color.WHITE);
		_view.setWillNotDraw(false);
		_view.setOnTouchListener(touchListener);
		_view.setClipChildren(false);
		//if (_view instanceof WebView) {
			_view.setAnimationCacheEnabled(true);
		//}
		
	}
	
	protected OnTouchListener touchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			_view.onTouchEvent(event);
			int action = event.getAction() & MotionEvent.ACTION_MASK, 
				p_count = event.getPointerCount();
		
		    switch (action) {
		    case MotionEvent.ACTION_MOVE:
		    	int interpolator = android.R.anim.accelerate_interpolator;
		    	
		    	// point 1 coords
	    		x1 = event.getX(0);
	    		y1 = event.getY(0);
	    		
		    	if (p_count > 1) {
		    		// point 2 coords
		    		x2 = event.getX(1);
		    		y2 = event.getY(1);
		    		
		    		// distance between
		    		dist_curr = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
					dist_delta = dist_curr - dist_pre;
		    		
			    	long now = android.os.SystemClock.uptimeMillis();
			    	if (now - mLastGestureTime > 100/* && Math.abs(dist_delta) > 10*/) {
			    		mLastGestureTime = 0;
			    		
			    		ScaleAnimation scale = null;
		    			int mode = dist_delta > 0 ? GROW : (dist_curr == dist_pre ? 2 : SHRINK);
			    		switch (mode) {
			    		case GROW: // grow
			    			if (x_scale < MAX_SCALE) {
			    				scale = new ScaleAnimation(x_scale, 
			    						x_scale += ZOOM, 
			    						y_scale, 
			    						y_scale += ZOOM, 
			    						ScaleAnimation.RELATIVE_TO_SELF, 
			    						0.5f, 
			    						ScaleAnimation.RELATIVE_TO_SELF, 
			    						0.5f);
			    			}
			    		break;
			    		case SHRINK: // shrink
			    			if (x_scale > MIN_SCALE) {
			    				scale = new ScaleAnimation(x_scale, 
			    						x_scale -= ZOOM, 
			    						y_scale, 
			    						y_scale -= ZOOM, 
			    						ScaleAnimation.RELATIVE_TO_SELF, 
			    						0.5f, 
			    						ScaleAnimation.RELATIVE_TO_SELF, 
			    						0.5f);
			    			}
			    		break;
			    		}
			    		
			    		if (scale != null) {
				            scale.setDuration(DURATION);
				            scale.setFillAfter(true);
				            scale.setInterpolator(_view.getContext(), interpolator);
				            scale.setAnimationListener(new AnimationListener() {

								public void onAnimationEnd(Animation anim) {
									_view.setInitialScale((int) (x_scale * 100));
									_view.invalidate();
								}

								public void onAnimationRepeat(Animation arg0) {
									// TODO Auto-generated method stub
									
								}

								public void onAnimationStart(Animation arg0) {
									// TODO Auto-generated method stub
									
								}
				            	
				            });
				            _view.startAnimation(scale);
			    		}
			    		
			    		mLastGestureTime = now;
		    		}
			    	
			    	x1_pre = x1;
			    	y1_pre = y1;
					dist_pre = dist_curr;
		    	}
		    	// drag
		    	/*else {
		    		int mid_x = _view.getMeasuredWidth() >> 1,
		    			mid_y = _view.getMeasuredHeight() >> 1;
		    		
		    		TranslateAnimation translate = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 
												    					x1_pre - mid_x, 
												    					TranslateAnimation.ABSOLUTE, 
												    					x1 - mid_x, 
												    					TranslateAnimation.ABSOLUTE, 
												    					y1_pre - mid_y,
												    					TranslateAnimation.ABSOLUTE, 
												    					y1 - mid_y);
		    		translate.setDuration(10);
		    		translate.setFillEnabled(true);
		    		translate.setInterpolator(_view.getContext(), interpolator);
		    		_view.startAnimation(translate);
		    	}*/
		    break;
		    case MotionEvent.ACTION_POINTER_1_DOWN:
		    	// point 1 coords
	    		x1_pre = event.getX(0);
	    		y1_pre = event.getY(0);
	    		mLastGestureTime = android.os.SystemClock.uptimeMillis();
		    break;
		    /*case MotionEvent.ACTION_POINTER_1_UP:
		    	x1_pre = event.getX(0);
	    		y1_pre = event.getY(0);
		    break;*/
		    }
		    return true;
		}	
	};
}

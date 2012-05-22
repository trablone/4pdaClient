package com.nikkoaiello.mobile.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PinchWebView extends WebView {
	
	// actions
	public static final int GROW = 0;
	public static final int SHRINK = 1;
	
	// intervals
	public static final int DURATION = 150;
	
	public static int DEFAULT_SCALE = 100;
	public static float ZOOM_FACTOR = 0.075f;
	
	
	public int mDefaultWidth = 0, mDefaultHeight = 0,
		mWidth = 100, mOldWidth = -1, 
		mHeight = 100, mOldHeight = -1,
		mTouchSlop = 50;
	public float mScale = 1.0f, mOldScale = 1.0f, mMinScale = 0.5f, mMaxScale = 1.5f;
	
	protected static float x1, 
		x2, 
		y1, 
		y2, 
		x1_pre,
		y1_pre,
		dist_delta = 0,
		dist_curr = -1, 
		dist_pre = -1;
	
	private long mLastGestureTime;
	private boolean mDragging = false;
	private static final DrawFilter sZoomFilter = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.SUBPIXEL_TEXT_FLAG, Paint.LINEAR_TEXT_FLAG);
	private Picture mPicture = null;
	//private Paint mPaint;
	
	public PinchWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public PinchWebView(Context context) {
		super(context);
		init();
	}
	
	public void init() {
		setWillNotDraw(false);
		
		//mPaint = new Paint();
        //mPaint.setAntiAlias(true);
		
        setWebViewClient(new WebViewClientImpl());
        getSettings().setSupportZoom(true);
        getSettings().setUseWideViewPort(true);
        getSettings().setJavaScriptEnabled(true);
        setInitialScale(0);

        final float density = getContext().getResources().getDisplayMetrics().density;
        
        mDefaultWidth = mWidth = Math.round(getContext().getResources().getDisplayMetrics().widthPixels);
        mDefaultHeight = mHeight = Math.round(getContext().getResources().getDisplayMetrics().heightPixels);
        
        //Log.e("INIT VALUES", "Max X: " + MAX_X + ", Max Y: " + MAX_Y);

        mMinScale *= density;
        mMaxScale *= density;
        
        DEFAULT_SCALE *= density / 100;
        ZOOM_FACTOR *= density;
        
        Log.e("INIT VALUES", "Default Scale: " + DEFAULT_SCALE + ", Default Zoom Factor: " + ZOOM_FACTOR);
        
        mScale = 1;
	
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	public boolean onTouchEvent(MotionEvent event) {	
		if (!mDragging) {
			super.onTouchEvent(event);
		}
		
		int action = event.getAction() & MotionEvent.ACTION_MASK, 
			p_count = event.getPointerCount();
		
	    switch (action) {
	    case MotionEvent.ACTION_MOVE:
	    	//int interpolator = android.R.anim.accelerate_interpolator;
	    	
	    	// point 1 coords
    		x1 = event.getX(0);
    		y1 = event.getY(0);
    		
	    	if (p_count > 1) {
	    		if (getSettings().supportZoom() && !getSettings().getBuiltInZoomControls()) {
		    		mDragging = true;
		    		
		    		// point 2 coords
		    		x2 = event.getX(1);
		    		y2 = event.getY(1);
		    		
		    		// distance between
		    		dist_curr = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
					dist_delta = dist_curr - dist_pre;
		    		
			    	long now = android.os.SystemClock.uptimeMillis();
			    	if (now - mLastGestureTime > 100 && Math.abs(dist_delta) > 10) {
			    		mLastGestureTime = 0;
			    		
			    		ScaleAnimation scale = null;
		    			int mode = dist_delta > 0 ? GROW : (dist_curr == dist_pre ? 2 : SHRINK);
			    		switch (mode) {
			    		case GROW: // grow
			    			if (mScale < mMaxScale) {
			    				mOldScale = mScale;
			    				mScale += ZOOM_FACTOR;
			    			}
			    		break;
			    		case SHRINK: // shrink
			    			if (mScale > mMinScale) {
			    				mOldScale = mScale;
			    				mScale -= ZOOM_FACTOR;
			    			}
			    		break;
			    		}
			    		
			    		if (mode != 2) {
			    			mOldWidth = mWidth;
			    			mOldHeight = mHeight;
				            mWidth = Math.round(mDefaultWidth * mScale);
							mHeight = Math.round(mDefaultHeight * mScale);
							
							Log.e("NEW WIDTH", mWidth + "");
							
							//getLayoutParams().width = mWidth;
							//getLayoutParams().height = mHeight;
							
							//measure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.UNSPECIFIED), 
								//	MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.UNSPECIFIED));
							//layout(getLeft() + getScrollX(), getTop() + getScrollY(), mWidth + getLeft(), mHeight + getTop());
							
							//this.offsetTopAndBottom(mHeight - mOldHeight);
							
							//requestLayout();
							//stopLoading();
							//mPicture = capturePicture();
							invalidate();
							/*
			    			AnimationSet set = new AnimationSet(true);
			    			
			    			scale = new ScaleAnimation(
		    						mOldScale, mScale,
		    						mOldScale, mScale, 
		    						ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, 
		    						ScaleAnimation.RELATIVE_TO_PARENT, 0.5f);
			    			
			    			//scale.initialize(mWidth, mWidth, MAX_X, MAX_Y);
				            scale.setDuration(DURATION);
				            //scale.setFillAfter(true);
				            scale.setInterpolator(getContext(), android.R.anim.accelerate_interpolator);
				            scale.setAnimationListener(new AnimationListener() {
	
								public void onAnimationEnd(Animation anim) {
									//clearAnimation();
								}
	
								public void onAnimationRepeat(Animation arg0) {}
								public void onAnimationStart(Animation arg0) {}
				            });
				            
				            set.addAnimation(scale);
				            */
				            /*
				            TranslateAnimation translate = new TranslateAnimation(
				            		Animation.RELATIVE_TO_SELF, mOldWidth - mWidth, Animation.RELATIVE_TO_SELF, 0f,
				            		Animation.RELATIVE_TO_SELF, mOldHeight - mHeight, Animation.RELATIVE_TO_SELF, 0f);
				            
				            set.addAnimation(translate);
				            
				            LayoutAnimationController controller =
				                new LayoutAnimationController(set, 0.0f);
				            */
				            //setLayoutAnimation(controller);
				            //startLayoutAnimation();
				            
				            //layout(getLeft() - (int)(x_scale), getTop(), getRight(), getBottom() + (int)(y_scale));
				            //requestLayout();
				            //startAnimation(scale);
							
							//this.removeAllViews();
							
							//Log.e("NEW SCALE", "Scale: " + mScale);
						}
			    		
			    		mLastGestureTime = now;
		    		}
	    		}
		    	
		    	x1_pre = x1;
		    	y1_pre = y1;
				dist_pre = dist_curr;
	    	}
	    	else {
	    		//mGestureDetector.onTouchEvent(event);
	    		//Log.e("NUM CHILDREN", this.getM);
	    		//onScroll(event, event, x1_pre - x1, y1_pre - y1);
	    		
	    		// point 1 coords
	    		x1_pre = event.getX(0);
	    		y1_pre = event.getY(0);
	    		mLastGestureTime = android.os.SystemClock.uptimeMillis();
	    	}
	    break;
	    case MotionEvent.ACTION_DOWN:
	    	/*long now = android.os.SystemClock.uptimeMillis();
	    	// check for double-tap for zoom
	    	if (now - mLastGestureTime < 500) {
	    		zoomIn();
	    	}*/
	    	
	    	// point 1 coords
    		x1_pre = event.getX(0);
    		y1_pre = event.getY(0);
    		mLastGestureTime = android.os.SystemClock.uptimeMillis();
    	break;
	    case MotionEvent.ACTION_UP:
	    	if (mDragging) {
	    		mDragging = false;
	    	}
	    break;
	    }
	    
		return true;
	}
	
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		Log.e("LAYOUT CHANGED", "Left: " + l + ", Top: " + t + ", Right: " + r + ", Bottom: " + b);
		//computeScroll();
	}
	
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		Log.e("SIZE CHANGED", "Width: " + getWidth() + ", Height: " + getHeight());
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(measureWidth(widthMeasureSpec), 
				measureHeight(heightMeasureSpec));
		Log.e("ON MEASURE", "Width: " + MeasureSpec.getSize(widthMeasureSpec) + ", Height: " + MeasureSpec.getSize(heightMeasureSpec));
    }
	
	protected void onDraw(Canvas canvas) {
		//Log.e("ON DRAW", "DRAWING!");

		canvas.setDrawFilter(sZoomFilter);
		canvas.scale(mScale, mScale);
		canvas.translate(-getScrollX(), -getScrollY());
		//canvas.setViewport(mWidth, mHeight);
		//if (mPicture != null) {
		//	canvas.save();
		//	canvas.drawPicture(mPicture);
		//	canvas.restore();
		//	mPicture = null;
		//}
		
		super.onDraw(canvas);
		
		//canvas.setViewport(mWidth, mHeight);
		//canvas.restore();
	}
	
	/**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = mWidth;
        } else {
            // Measure the text
            result = (int) (mWidth) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = mHeight;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (mHeight) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

	
	private class WebViewClientImpl extends WebViewClient {
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
		
		public void onPageFinished(WebView view, String url) {
			
		}
		
		public void onScaleChanged(WebView view, float oldScale, float newScale) {
			mScale = newScale;
			Log.e("NEW SCALE", newScale + "");
		}
	}
	
}

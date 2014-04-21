package com.aspin.prototyper;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.provider.MediaStore;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class PrototypeActivity extends Activity {
	
	private GestureDetector mGestureDetector;

    @Override
    public void onAttachedToWindow() {
    	// do something when a view is attached to the window
        super.onAttachedToWindow();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prototype);
		mGestureDetector = createGestureDetector(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.prototype, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.start:
				startActivity(new Intent(PrototypeActivity.this, CameraActivity.class));
				// Intent intent = new Intent (MediaStore.ACTION_VIDEO_CAPTURE);
				// startActivityForResult(intent, TAKE_VIDEO_REQUEST);
				return true;
			case R.id.quit:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	private GestureDetector createGestureDetector(Context context) {
		    GestureDetector gestureDetector = new GestureDetector(context);
		        //Create a base listener for generic gestures
		        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
		            @Override
		            public boolean onGesture(Gesture gesture) {
		                if (gesture == Gesture.TAP) {
		                	openOptionsMenu();
		                    return true;
		                } else if (gesture == Gesture.TWO_TAP) {
		                	return true;
		                } else if (gesture == Gesture.SWIPE_RIGHT) {
		                    return true;
		                } else if (gesture == Gesture.SWIPE_LEFT) {
		                	return true;
		                }
		                
		                return false;
		            }
		        });
		        gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
		            @Override
		            public void onFingerCountChanged(int previousCount, int currentCount) {
		              // do something on finger count changes
		            }
		        });
		        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
		            @Override
		            public boolean onScroll(float displacement, float delta, float velocity) {
		              // do something on scrolling
		            	
		            	return true;
		            }
		        });
		        return gestureDetector;
	}

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }


	public void onOptionsMenuClosed(Menu menu) {
		// do something when menu closes
	}

}

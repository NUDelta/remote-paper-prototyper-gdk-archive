package com.aspin.prototyper;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.rtsp.RtspServer;
import net.majorkernelpanic.streaming.video.VideoQuality;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class CameraActivity extends Activity {
	
	private static final int TAKE_VIDEO_REQUEST = 2;

	private GestureDetector mGestureDetector;

	// streaming related
	private RelativeLayout mRelativeLayout; 
	private SurfaceView mSurfaceView;
	private Session mSession;
	private PowerManager.WakeLock mWakeLock;
	private RtspClient mClient;
	private Boolean recording = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		mGestureDetector = createGestureDetector(this);

		mRelativeLayout = (RelativeLayout) findViewById(R.id.camera_activity);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		
		// Configures the SessionBuilder
		SessionBuilder sBuilder = SessionBuilder.getInstance()
		.setSurfaceHolder(mSurfaceView.getHolder())
		.setContext(getApplicationContext())
		.setAudioEncoder(SessionBuilder.AUDIO_AAC)
		.setVideoEncoder(SessionBuilder.VIDEO_H264);
		
		// Starts the RTSP server
		mClient = new RtspClient();

//		mSession = sBuilder.build(); // this line throws an error
//		mClient.setSession(mSession);
		this.startService(new Intent(this,RtspServer.class));
		
		startRecord();	
	}
	
	// Experimental function to start camera
	public void startRecord() {
		Intent intent = new Intent (MediaStore.ACTION_VIDEO_CAPTURE);
		startActivityForResult(intent, TAKE_VIDEO_REQUEST);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.camera, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.record:
				startRecord();
				return true;
			case R.id.disconnect:
				// do more stuff
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

}

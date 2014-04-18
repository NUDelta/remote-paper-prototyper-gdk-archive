package com.aspin.prototyper;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class PrototypeActivity extends Activity {
	
	private static final int TAKE_PICTURE_REQUEST = 1;
	private static final int TAKE_VIDEO_REQUEST = 2;
	private GestureDetector mGestureDetector = null;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prototype);
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
				Intent intent = new Intent (getApplicationContext(), com.aspin.prototyper.CameraActivity.class);
				startActivityForResult(intent, 0);
				return true;
			case R.id.quit:
				// do more stuff
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	public void onOptionsMenuClosed(Menu menu) {
		finish();
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_prototype,
					container, false);
			return rootView;
		}
	}

}
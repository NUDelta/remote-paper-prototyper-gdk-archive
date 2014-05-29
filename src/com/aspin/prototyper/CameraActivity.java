package com.aspin.prototyper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class CameraActivity extends Activity {
	
	private static final int TAKE_VIDEO_REQUEST = 2;

	private final static VideoQuality QUALITY_GLASS = new VideoQuality(352, 288, 60, 384000);

	String user = "hello";
	String password = "goodbye";
	String url = "rtsp://glass.ci.northwestern.edu:4000/live/test.sdp";
//	String url = "rtsp://192.168.1.9:1935/live/test.sdp";

	// streaming stuff	
	private VideoQuality mQuality = QUALITY_GLASS;			
	private RelativeLayout mRelativeLayout; 
	private SurfaceView mSurfaceView;
	private Session mSession;
	private PowerManager.WakeLock mWakeLock;
	private RtspClient mClient;
	private Boolean recording = false;

	private GestureDetector mGestureDetector;
    private static final long DELAY_MILLIS = 3000;
    private String prevMessage = "";
    private String currentMessage = "Connecting..."; // change later to be connected the strings.xml file
    private TextView mTextView;
    private TextToSpeech tts;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "3dgBmw9ZzGVprNrdoNuQZ4TgmWzjkc8rc5HT3quP", "U7bbsU5rY5yUy8twRgSrvz46SUvL1O7OlrS2U8JP");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		mGestureDetector = createGestureDetector(this);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.camera_activity);
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
			}
		});
		tts.setLanguage(Locale.US);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
				
		// // Sets the port of the RTSP server to 1234
		// Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		// editor.putString(RtspServer.KEY_PORT, String.valueOf(1234));
		// editor.commit();

		mRelativeLayout = (RelativeLayout) findViewById(R.id.camera_activity);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		mTextView = (TextView) findViewById(R.id.comText);
		
		// Configures the SessionBuilder
		SessionBuilder sBuilder = SessionBuilder.getInstance()
		.setSurfaceHolder(mSurfaceView.getHolder())
		.setContext(getApplicationContext())
		.setAudioEncoder(SessionBuilder.AUDIO_AAC)
		.setVideoEncoder(SessionBuilder.VIDEO_H264)
		.setVideoQuality(QUALITY_GLASS);
		
		// creates the RTSP Client
		mClient = new RtspClient();

		try {
			mSession = sBuilder.build();
			mClient.setSession(mSession);
		} catch (Exception e) {
			logError(e.getMessage());
			e.printStackTrace();
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,"net.majorkernelpanic.example3.wakelock");

		mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				if (mSession != null) {
					try {
						if (mSession.getVideoTrack() != null) {
							mSession.getVideoTrack().setVideoQuality(mQuality);
							
							// Start streaming
							new ToggleStreamAsyncTask().execute();

						}
					} catch (RuntimeException e) {
						logError(e.getMessage());
						e.printStackTrace();
					}
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
				Log.i("CameraActivity", "surfaceChanged()");
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.i("CameraActivity", "surfaceDestroyed()");
			}

		});
		
		//Setting recording state to enabled
		recording = true;
		// startRecord();	
		new ChatUpdateAsync().execute();
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
			case R.id.disconnect:
				mClient.stopStream();
				recording = false;
				Log.i("MenuOption", "Attempting to stop?");
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	private void startStream() {
		// TODO Auto-generated method stub
		
	}

	// Connects/disconnects to the RTSP server and starts/stops the stream
	private class ToggleStreamAsyncTask extends AsyncTask<Void,Void,Integer> {

		private final int START_SUCCEEDED = 0x00;
		private final int START_FAILED = 0x01;
		private final int STOP = 0x02;

		@Override
		protected Integer doInBackground(Void... params) {
			if (!mClient.isStreaming()) {
				String ip,port,path;
				try {
					// We parse the URI written in the Editext
					Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
					Matcher m = uri.matcher(url); m.find();

					ip = m.group(1);
					port = m.group(2);
					path = m.group(3);
					Log.d("ip: ", ip);
					Log.d("port: ", port);
					Log.d("path: ", path);
					
					// Connection to the RTSP server
					if (mSession.getVideoTrack() != null) {
						mSession.getVideoTrack().setVideoQuality(mQuality);
					}
					mClient.setCredentials(user, password);
					mClient.setServerAddress(ip, Integer.parseInt(port));
					mClient.setStreamPath("/"+path);
					mClient.startStream(1);
					
					// Init recording flag
					recording = true;
					currentMessage = "You are now streaming.";
					return START_SUCCEEDED;
				} catch (Exception e) {
					Log.e("CameraActivity", "Error starting stream.", e);
					Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
					return START_FAILED;
				}
			} else {
				// Stops the stream and disconnects from the RTSP server
				mClient.stopStream();				
				// Setting recording state to disabled
				recording = false;
				Log.i("CameraActivity", "*** Recording stopStream()");
				finish();
			}
			return STOP;
		}

	}
	
	// Disconnects from the RTSP server and stops the stream
	private class StopStreamAsyncTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... params) {
				mClient.stopStream();
				return null;
		}
	}
	


	private void logError(String msg) {
		final String error = (msg == null) ? "Error unknown" : msg; 
		Log.e("CameraActivity",error);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(CameraActivity.this, error, Toast.LENGTH_SHORT).show();	
			}
		});
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

	@Override
	public void onStart() {
		super.onStart();
		mWakeLock.acquire();		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		if (mWakeLock.isHeld()) mWakeLock.release();
			// Setting recording state to disabled
			recording = false;
			mSession.flush();
			
			// Stops the stream and disconnects from the RTSP server
			mClient.stopStream();			
		
			setResult(PrototypeActivity.RESULT_OK);
			super.onStop();
	}	
	
	@Override
	protected void onPause() {

		//Stops the stream and disconnects from the RTSP server
		mClient.stopStream();
		
		// Unlock screen
		if (mWakeLock.isHeld()) mWakeLock.release();
		// Setting recording state to disabled
		recording = false;

		mSession.flush();

		setResult(PrototypeActivity.RESULT_OK);
		super.onPause();
	}
	
	private class ChatUpdateAsync extends AsyncTask<Void, Void, Void> {
      
		@Override
		protected Void doInBackground(Void... arg0) {
        	String text = "Connecting...";
        	try {
				Thread.sleep(DELAY_MILLIS);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			try {
//				text = Jsoup.parse(new URL("http://kevinjchen.com/message.html"), 20000).text();
//			}
//			catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//			catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        	ParseQuery<ParseObject> query = ParseQuery.getQuery("message");
        	query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> message, ParseException e) {
			        if (e == null) {
			            Log.d("Message", "Retrieved " + message.get(0).getString("msg"));
			            currentMessage = message.get(0).getString("msg");
			            
			        } else {
			            Log.d("Message", "Error: " + e.getMessage());
			        }
				}
        		
        	});
        	
        	// requires testing
        	final Location loc = getLastLocation(CameraActivity.this);
        	Log.d("Location", "Lat: " + loc.getLatitude());
        	Log.d("Location", "Long: " + loc.getLongitude());
        	
        	ParseQuery<ParseObject> query2 = ParseQuery.getQuery("location");
        	query2.findInBackground(new FindCallback<ParseObject>() {
        		
        		@Override
        		public void done(List<ParseObject> results, ParseException e) {
        			if (e == null) {
        				if (results.size() == 0) {
        					// make new object        					
        				}
        				else {
        					results.get(0).put("lat", loc.getLatitude());
        					results.get(0).put("lon", loc.getLongitude());
        					results.get(0).saveInBackground();
        				}
        			}
        			else {
        				Log.d("location", "Error: " + e.getMessage());
        			}
        		}
        	});
        	
        	
        	
        	

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Log.d("Getting text.", currentMessage);
			if (!currentMessage.equals(prevMessage)) {
				mTextView.setText(currentMessage);
				prevMessage = currentMessage;
				Log.d("Getting prev text.", prevMessage);
				
				AudioManager audio = (AudioManager) CameraActivity.this.getSystemService(Context.AUDIO_SERVICE);
				audio.playSoundEffect(Sounds.SUCCESS);
				
				tts.speak(currentMessage, TextToSpeech.QUEUE_ADD, null);
			}

			if (recording == true) {
		    	new ChatUpdateAsync().execute();				
			}

	    }

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onCancelled(Void result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
    }
	
    public static Location getLastLocation(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        List<String> providers = manager.getProviders(criteria, true);
        List<Location> locations = new ArrayList<Location>();
        for (String provider : providers) {
             Location location = manager.getLastKnownLocation(provider);
             if (location != null && location.getAccuracy() !=0.0) {
                 locations.add(location);
             }
        }
        Collections.sort(locations, new Comparator<Location>() {
            @Override
            public int compare(Location location, Location location2) {
                return (int) (location.getAccuracy() - location2.getAccuracy());
            }
        });
        if (locations.size() > 0) {
            return locations.get(0);
        }
        return null;
   }
	
}

package com.aspin.prototyper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

public class CameraActivity extends Activity implements Session.SessionListener, Publisher.PublisherListener, Subscriber.VideoListener {
	
	// OpenTok Members
    private static final String LOGTAG = "rppt";
    private static final String API_KEY = "45090422";
    private static final String SESSION_ID = "2_MX40NTA5MDQyMn5-MTQxNjU1MjI1MDQ5OH4wSW1xakI5cDFIYk9JOURyVXFQUFFjTGF-fg";
    private static final String TOKEN = "T1==cGFydG5lcl9pZD00NTA5MDQyMiZzaWc9NzkyYzgxMWM5ODNmMzQ5NjZlNWFjYTE5MmRiMjUwMzc1ZGUzNDIwZjpyb2xlPXB1Ymxpc2hlciZzZXNzaW9uX2lkPTJfTVg0ME5UQTVNRFF5TW41LU1UUXhOalUxTWpJMU1EUTVPSDR3U1cxeGFrSTVjREZJWWs5Sk9VUnlWWEZRVUZGalRHRi1mZyZjcmVhdGVfdGltZT0xNDE2NTUyMjgyJm5vbmNlPTAuMzEzNDg1NzE0MDQ5OTg4MjYmZXhwaXJlX3RpbWU9MTQxOTE0NDI0Mw==";
    
    private Session mSession;
    private Publisher mPublisher;
    private ArrayList<Stream> mStreams;
    protected Handler mHandler = new Handler();

    private RelativeLayout mPublisherViewContainer;
    
    private ProgressBar mLoadingSub;

    private boolean resumeHasRun = false;

	private boolean mIsBound = false;
	private NotificationCompat.Builder mNotifyBuilder;
	NotificationManager mNotificationManager;
	ServiceConnection mConnection;
	
    // Previous Version Members
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
		tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
			}
		});
		tts.setLanguage(Locale.US);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		// OpenTok methods
		mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mStreams = new ArrayList<Stream>();
        sessionConnect();
	
//		new ChatUpdateAsync().execute();
		
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	
	
	private void sessionConnect() {
        if (mSession == null) {
            mSession = new Session(CameraActivity.this,
                    API_KEY, SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(TOKEN);
        }
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
				Log.i("MenuOption", "Attempting to stop?");
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {

	}	
	
	@Override
	protected void onPause() {

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

	    	new ChatUpdateAsync().execute();				

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



    @Override
    public void onConnected(Session session) {
        Log.i(LOGTAG, "Connected to the session.");
        if (mPublisher == null) {
            mPublisher = new Publisher(CameraActivity.this, "publisher");
            mPublisher.setPublisherListener(this);
            attachPublisherView(mPublisher);
            mSession.publish(mPublisher);
        }
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOGTAG, "Disconnected from the session.");
        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
        }

        mPublisher = null;
        mStreams.clear();
        mSession = null;
    }

    private void subscribeToStream(Stream stream) {
    	// don't need
    }

    private void unsubscribeFromStream(Stream stream) {
    	// don't need
    }

    private void attachSubscriberView(Subscriber subscriber) {
        // don't need
    }

    private void attachPublisherView(Publisher publisher) {
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                480, 320);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,
                RelativeLayout.TRUE);
        layoutParams.bottomMargin = dpToPx(8);
        layoutParams.rightMargin = dpToPx(8);
        mPublisherViewContainer.addView(mPublisher.getView(), layoutParams);
    }

    @Override
    public void onError(Session session, OpentokError exception) {
        Log.i(LOGTAG, "Session exception: " + exception.getMessage());
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

//        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            mStreams.add(stream);
//            if (mSubscriber == null) {
//                subscribeToStream(stream);
//            }
//        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
//        if (!OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            if (mSubscriber != null) {
//                unsubscribeFromStream(stream);
//            }
//        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisher, Stream stream) {
//        if (OpenTokConfig.SUBSCRIBE_TO_SELF) {
//            mStreams.add(stream);
//            if (mSubscriber == null) {
//                subscribeToStream(stream);
//            }
//        }
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisher, Stream stream) {
//        if ((OpenTokConfig.SUBSCRIBE_TO_SELF && mSubscriber != null)) {
//            unsubscribeFromStream(stream);
//        }
    }

    @Override
    public void onError(PublisherKit publisher, OpentokError exception) {
        Log.i(LOGTAG, "Publisher exception: " + exception.getMessage());
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriber) {
        Log.i(LOGTAG, "First frame received");

        // stop loading spinning
//        mLoadingSub.setVisibility(View.GONE);
//        attachSubscriberView(mSubscriber);
    }

    /**
     * Converts dp to real pixels, according to the screen density.
     * 
     * @param dp
     *            A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

	@Override
	public void onVideoDisabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG,
                "Video disabled:" + reason);		
	}

	@Override
	public void onVideoEnabled(SubscriberKit subscriber, String reason) {
        Log.i(LOGTAG,"Video enabled:" + reason);		
	}

	@Override
	public void onVideoDisableWarning(SubscriberKit subscriber) {
		Log.i(LOGTAG, "Video may be disabled soon due to network quality degradation. Add UI handling here.");	
	}

	@Override
	public void onVideoDisableWarningLifted(SubscriberKit subscriber) {
		Log.i(LOGTAG, "Video may no longer be disabled as stream quality improved. Add UI handling here.");
	}

	
}

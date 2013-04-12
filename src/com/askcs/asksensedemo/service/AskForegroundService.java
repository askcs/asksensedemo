package com.askcs.asksensedemo.service;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;

import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.ISenseService;
import nl.sense_os.service.ISenseServiceCallback;
import nl.sense_os.service.commonsense.SenseApi;
import nl.sense_os.service.constants.SensePrefs;
import nl.sense_os.service.constants.SensePrefs.Main.Ambience;
import nl.sense_os.service.constants.SensePrefs.Main.Location;

import com.askcs.asksensedemo.MainActivity;
import com.askcs.asksensedemo.R;
import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class AskForegroundService extends Service implements ServiceConnection {

	private static final String TAG = AskForegroundService.class.getName();
	
	private SensePlatform sensePlatform = null;
	private SenseCallback callback = new SenseCallback();
	private NotificationManager notificationManager = null;
	private Messenger serviceMessenger = null;
	protected Set<Messenger> clients = null;
	private boolean isRunning = false;
	private final int SERVICE_ID = 452189;
	private DatabaseHelper databaseHelper = null;
	private RestTimerTask task = null;
		
	private class SenseCallback extends ISenseServiceCallback.Stub {
		@Override
		public void onChangeLoginResult(int result) throws RemoteException {
			switch (result) {
			case 0:
				Log.v(TAG, ">>> Change login OK");
                onLoggedIn();
				break;
			case -1:
				Log.v(TAG, ">>> Login failed! Connectivity problems?");
				break;
			case -2:
				Log.v(TAG, ">>> Login failed! Invalid username or password.");
				break;
			default:
				Log.w(TAG, ">>> Unexpected login result! Unexpected result: " + result);
			}
		}

		@Override
		public void onRegisterResult(int result) throws RemoteException {
            // not used
		}

		@Override
		public void statusReport(final int status) {
            // not used
		}
	}
	
	@Override
	public void onDestroy() {
	    
		super.onDestroy();
	    
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		
		if (serviceMessenger == null) {
			Log.w(TAG, "serviceMessenger == null");
			return null;
		}

		Log.d(TAG, "onBind(" + intent + ")");

		return serviceMessenger.getBinder();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		start();

		return START_NOT_STICKY;
	}
	
	public DatabaseHelper getHelper() {
		
	    if (databaseHelper == null) {
	        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    
	    return databaseHelper;
	}
	
	// TODO use support-lib classes to get rid of deprecation (use Builder pattern)
	@SuppressWarnings("deprecation")
	public void send(String message) {
		
		if(notificationManager == null) {
			Log.w(TAG, "notificationManager == null");
			return;
		}
		
		Resources res = getResources();
		
		Notification notification = new Notification(
				R.drawable.s_ask_icon_red, res.getString(R.string.new_notification), 
				System.currentTimeMillis());

		Intent gotoIntent = new Intent(this , MainActivity.class);

		gotoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, gotoIntent, 0);

		notification.setLatestEventInfo(this, res.getString(R.string.app_name), 
				message, pendingIntent);
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(Long.valueOf(System.currentTimeMillis()).hashCode(), notification);
	}

	protected void start() {
		
		if (!isRunning) {
			
			Log.d(TAG, "starting");
			
			try {
				sensePlatform = new SensePlatform(this, this);
				
				Setting user = this.getHelper().getSettingDao().queryForId(Setting.USER_KEY);
				Setting hash = this.getHelper().getSettingDao().queryForId(Setting.PASSWORD_KEY);
				
				if(user != null && hash != null) {
					sensePlatform.login(user.getValue(), hash.getValue(), callback);
				}
				else {
					Log.e(TAG, "user == null or hash == null");
				}
			} catch (Exception e) {
				Log.e(TAG, "something went wrong during starting SensePlatform: ", e);
			} 
			
			clients = new HashSet<Messenger>();
					
			try {
				notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			} catch (Exception e) {
				Log.e(TAG, "something went wrong during starting of service: ", e);
			}

			try {
				Notification notification =  updateNotification();
				
				super.startForeground(SERVICE_ID, notification);

				task = new RestTimerTask(this);
				
				task.start();
				
				isRunning = true;
				
			} catch (Exception e) {
				Log.w(TAG, "something went wrong while starting on foreground: ", e);
			}
		}
	}
	
	protected void stop() {
		
		try {
			Log.d(TAG, "stopping");
			
			super.stopForeground(true);
			
			if(isRunning && task != null) {
				task.stop();
				isRunning = false;
			}
			
		} catch(Exception e) {
			Log.e(TAG, "could not stop service: ", e);			
		}
	}
	
	// TODO use support-lib classes to get rid of deprecation (use Builder pattern)
	@SuppressWarnings("deprecation")
	protected Notification updateNotification() {
		
		if(notificationManager == null) {
			Log.w(TAG, "notificationManager == null");
			return null;
		}
		
		Resources res = getResources();
		
		Notification notification = new Notification(
				R.drawable.s_ask_icon_gray, res.getString(R.string.app_running), 
				System.currentTimeMillis());

		Intent gotoIntent = new Intent(this , MainActivity.class);

		gotoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, gotoIntent, 0);

		notification.setLatestEventInfo(this, res.getString(R.string.app_name), 
				res.getString(R.string.app_running), pendingIntent);
		
		notification.flags |= Notification.FLAG_NO_CLEAR;

		notificationManager.notify(SERVICE_ID, notification);
		
		return notification;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		
	}
	
	private void onLoggedIn() {
		try {
			Log.d(TAG, "onLoggedIn()");
			
            ISenseService service = sensePlatform.getService();
            
			service.toggleMain(true);
			service.toggleAmbience(true);
			service.toggleLocation(true);
			service.toggleMotion(true);
			
			// turn off some specific sensors
			//service.setPrefBool(Ambience.LIGHT, false);
			//service.setPrefBool(Ambience.CAMERA_LIGHT, false);
			//service.setPrefBool(Ambience.PRESSURE, false);

			// turn on specific sensors
			//service.setPrefBool(Ambience.MIC, true);
			//service.setPrefBool(Ambience.MIC, true);
			// NOTE: spectrum might be too heavy for the phone or consume too much energy
			//service.setPrefBool(Ambience.AUDIO_SPECTRUM, true);
						
			service.setPrefBool(Location.GPS, true);
			service.setPrefBool(Location.NETWORK, true);
			service.setPrefBool(Location.AUTO_GPS, true);
						
						
			// set how often to sample
			// 1 := rarely (~every 15 min)
			// 0 := normal (~every 5 min)
			// -1 := often (~every 10 sec)
			// -2 := real time (this setting affects power consumption considerably!)
			service.setPrefString(SensePrefs.Main.SAMPLE_RATE, "-1");

			// set how often to upload
			// 1 := eco mode (buffer data for 30 minutes before bulk uploading)
			// 0 := normal (buffer 5 min)
			// -1 := often (buffer 1 min)
			// -2 := real time (every new data point is uploaded immediately)
			service.setPrefString(SensePrefs.Main.SYNC_RATE, "-1");
			
			//sendData();
			//getData();
		} catch (Exception e) {
            Log.e(TAG, "Exception while starting sense library.", e);
		}
	}
	
	public JSONArray getData(String sensorName) {
		
		JSONArray data = new JSONArray();
		
		if(sensePlatform != null) {
			try {
				
				//String sensorName = "Activity";
				Log.v(TAG, ">>> starting getData(" + sensorName + ", ...)");
				data = sensePlatform.getData(sensorName, false, 10);
	            Log.v(TAG, ">>> data=" + data);
				
				//JSONArray data = sensePlatform.getData("position", true, 10);
	            //Log.v(TAG, "Received: '" + data + "'");
				
	            //sensePlatform.getService().getStatus(callback);
	            
			} catch (Exception e) {
	            Log.e(TAG, "Failed to get data!", e);
			}
		}
		
		return data;
	}
}

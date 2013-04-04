package com.askcs.asksensedemo.service;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.askcs.asksensedemo.MainActivity;
import com.askcs.asksensedemo.R;
import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class AskForegroundService extends Service {

	private static final String TAG = AskForegroundService.class.getName();
	
	private NotificationManager notificationManager = null;
	private Messenger serviceMessenger = null;
	protected Set<Messenger> clients = null;
	private boolean isRunning = false;
	private final int SERVICE_ID = 452189;
	private DatabaseHelper databaseHelper = null;
	private RestTimerTask task = null;
		
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
	
	private DatabaseHelper getHelper() {
		
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
			
			clients = new HashSet<Messenger>();
					
			try {
				notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			} catch (Exception e) {
				Log.w(TAG, "something went wrong during starting of service: ", e);
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
				R.drawable.s_ask_icon_gray, res.getString(R.string.app_name_started), 
				System.currentTimeMillis());

		Intent gotoIntent = new Intent(this , MainActivity.class);

		gotoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0, gotoIntent, 0);

		try {
			Setting user = this.getHelper().getSettingDao().queryForId(Setting.USER_KEY);
			
			notification.setLatestEventInfo(this, res.getString(R.string.app_name), 
					res.getString(R.string.app_name_started, user.getValue()), pendingIntent);
			
		} catch (SQLException e) {
			Log.e(TAG, "Something went wrong trying to get the user from the local DB:", e);
		}
		
		notification.flags |= Notification.FLAG_NO_CLEAR;

		notificationManager.notify(SERVICE_ID, notification);
		
		return notification;
	}
}

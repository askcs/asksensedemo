package com.askcs.asksensedemo.service;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.dao.Dao;

import android.util.Log;

public class RestTimerTask extends TimerTask {

	private static final String TAG = RestTimerTask.class.getName();
	
	private AskForegroundService service;
	protected Timer timer;
	protected long seconds;
	
	public RestTimerTask(AskForegroundService service) {
		this.service = service;
		this.timer = new Timer();
		this.seconds = 10;
	}
	
	private boolean findBoolean(Dao<Setting, String> dao, String id) {

		try {
			Setting setting = dao.queryForId(id);
			
			if(setting != null) {
				return setting.getValue().equals("true");
			}
			
		} catch (SQLException e) {
			Log.e(TAG, "could not retrieve settings from db: ", e);
		}
		
		return false;
	}
	
	@Override
	public void run() {
		Log.d(TAG, "ticking every " + seconds + " seconds");
		
		//service.send("Message@" + System.currentTimeMillis());
		
		boolean checkActivity = false;
		boolean checkLocation = false;
		boolean checkPresence = false;
		
		try {
			Dao<Setting, String> dao = service.getHelper().getSettingDao();
			
			checkActivity = findBoolean(dao, Setting.ACTIVITY_ENABLED_KEY);
			checkLocation = findBoolean(dao, Setting.LOCATION_ENABLED_KEY);
			checkPresence = findBoolean(dao, Setting.PRESENCE_ENABLED_KEY);
			
		} catch (SQLException e) {
			Log.e(TAG, "could not retrieve settings from db: ", e);
		}
		
		Log.d(TAG, "checkActivity:=" + checkActivity + ", checkLocation:=" + 
				checkLocation + ", checkPresence:=" + checkPresence);
	}

	public void start() {
		timer.scheduleAtFixedRate(this, 1000L, seconds * 1000L);
	}
	
	public void stop() {
		if(timer != null) {
			try {
				timer.cancel();
			} catch(Exception e) {
				Log.e(TAG, "something went wrong stopping RestTimerTask: ", e);
			}
		}
	}
}

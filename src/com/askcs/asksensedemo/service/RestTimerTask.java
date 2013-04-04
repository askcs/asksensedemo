package com.askcs.asksensedemo.service;

import java.util.Timer;
import java.util.TimerTask;

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
	
	@Override
	public void run() {
		Log.d(TAG, "ticking every " + seconds + " seconds");
		service.send("Message@" + System.currentTimeMillis());
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

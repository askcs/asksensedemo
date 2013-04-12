package com.askcs.asksensedemo;

import java.sql.SQLException;

import org.json.JSONArray;

import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.ISenseService;
import nl.sense_os.service.ISenseServiceCallback;
import nl.sense_os.service.commonsense.SenseApi;
import nl.sense_os.service.constants.SensePrefs;
import nl.sense_os.service.constants.SensePrefs.Main.Location;

import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.service.AskForegroundService;
import com.astuetz.viewpager.extensions.FixedTabsView;
import com.astuetz.viewpager.extensions.TabsAdapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class MainActivity extends Activity implements ServiceConnection {

	private static final String TAG = MainActivity.class.getName();
	
	private SensePlatform sensePlatform;
	private SenseCallback callback = new SenseCallback();
	
	private PagerAdapter pagerAdapter;
	private ViewPager pager;
	private FixedTabsView fixedTabs;
	private TabsAdapter fixedTabsAdapter;
	private DatabaseHelper databaseHelper = null;

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
	public void onCreate(Bundle savedInstanceState) {
				
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate(...)");
		
		setContentView(R.layout.activity_main);
						
		initViewPager(3, 0xFFFFFFFF, 0xFF000000);
		
		fixedTabs = (FixedTabsView) findViewById(R.id.fixed_tabs);
		fixedTabsAdapter = new FixedTabsAdapter(this);
		fixedTabs.setAdapter(fixedTabsAdapter);
		fixedTabs.setViewPager(pager);
		
		try {
			Setting user = getHelper().getSettingDao().queryForId(Setting.USER_KEY);
			
			if(user == null) {
				Intent intent = new Intent(this, LoginActivity.class);
			    startActivity(intent);				
			}
			//else {
			//	startService(new Intent(this, AskForegroundService.class));
			//}

		} catch (SQLException e) {
			Log.e(TAG, "Oops, SQLException:", e);
		}
	}
	
	private void initViewPager(int pageCount, int backgroundColor, int textColor) {

		pager = (ViewPager) findViewById(R.id.pager);
		pagerAdapter = new FixedTabsPagerAdapter(this, pageCount, backgroundColor, textColor);
		pager.setAdapter(pagerAdapter);
		pager.setCurrentItem(0);
		pager.setPageMargin(1);
	}
	
	
	public DatabaseHelper getHelper() {
		
	    if (databaseHelper == null) {
	        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }			
	    
	    return databaseHelper;
	}
	
	@Override
	protected void onDestroy() {
	    
		Log.d(TAG, "onDestroy");
		
		super.onDestroy();
	    
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	    
	    if(sensePlatform != null) {
	    	sensePlatform.close();
	    }
	}

	private void onLoggedIn() {
		try {
            // start sensingposition
			ISenseService service = sensePlatform.getService();
			service.toggleMain(true);
			service.toggleAmbience(true);
			service.toggleLocation(true);
			service.toggleMotion(true);
			
			//sendData();
			//getData();
			// TODO start tasks
			String sensorName = "Activity";
			Log.v(TAG, ">>> starting getData(" + sensorName + ", ...)");
			JSONArray data = sensePlatform.getData(sensorName, false, 10);
            Log.v(TAG, ">>> data=" + data);
			
		} catch (Exception e) {
            Log.e(TAG, "Exception while starting sense library.", e);
		}
	}
	
	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		setupSense();
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		Log.e(TAG, "disconnected!");
	}
	
	private void setupSense() {
		try {
            // log in (you only need to do this once, Sense will remember the login)
            sensePlatform.login("bkiers@ask-cs.com", SenseApi.hashPassword("!Amk220Bk284"), callback); // user=foo, pw=bar
            // this is an asynchronous call, we get a call to the callback object when the login is
            // complete

            // turn off some specific sensors
			ISenseService service = sensePlatform.getService();
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
			
		} catch (Exception e) {
            Log.e(TAG, "Exception while setting up Sense library.", e);
		}
	}
	
	@Override
	protected void onStart() {
		Log.v(TAG, "onStart");
		super.onStart();

        // create SensePlatform instance to do the complicated work
        // (when the service is ready, we get a call to onServiceConnected)
		sensePlatform = new SensePlatform(this, this);
	}
}

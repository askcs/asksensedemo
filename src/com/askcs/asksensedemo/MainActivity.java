package com.askcs.asksensedemo;

import java.sql.SQLException;

import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.astuetz.viewpager.extensions.FixedTabsView;
import com.astuetz.viewpager.extensions.TabsAdapter;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getName();
	
	private PagerAdapter pagerAdapter;
	private ViewPager pager;
	private FixedTabsView fixedTabs;
	private TabsAdapter fixedTabsAdapter;

	private DatabaseHelper databaseHelper = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		initViewPager(3, 0xFFFFFFFF, 0xFF000000);
		
		fixedTabs = (FixedTabsView) findViewById(R.id.fixed_tabs);
		fixedTabsAdapter = new FixedTabsAdapter(this);
		fixedTabs.setAdapter(fixedTabsAdapter);
		fixedTabs.setViewPager(pager);
		
		try {
			Dao<Setting, Integer> settingDao = getHelper().getSettingDao();
			Log.i(TAG, "all settings: " + settingDao.queryForAll());
		} catch (SQLException e) {
			Log.e(TAG, "could not get setting-DAO:", e);
		}
	}
	
	private void initViewPager(int pageCount, int backgroundColor, int textColor) {
		
		pager = (ViewPager) findViewById(R.id.pager);
		pagerAdapter = new ExamplePagerAdapter(this, pageCount, backgroundColor, textColor);
		pager.setAdapter(pagerAdapter);
		pager.setCurrentItem(0);
		pager.setPageMargin(1);
	}
	
	private DatabaseHelper getHelper() {
		
	    if (databaseHelper == null) {
	        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    
	    return databaseHelper;
	}
	
	@Override
	protected void onDestroy() {
	    
		super.onDestroy();
	    
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}
}

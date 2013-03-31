package com.askcs.asksensedemo;

import com.astuetz.viewpager.extensions.FixedTabsView;
import com.astuetz.viewpager.extensions.TabsAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends Activity {

	private PagerAdapter mPagerAdapter;
	private ViewPager mPager;
	private FixedTabsView mFixedTabs;
	private TabsAdapter mFixedTabsAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		initViewPager(3, 0xFFFFFFFF, 0xFF000000);
		mFixedTabs = (FixedTabsView) findViewById(R.id.fixed_tabs);
		mFixedTabsAdapter = new FixedTabsAdapter(this);
		mFixedTabs.setAdapter(mFixedTabsAdapter);
		mFixedTabs.setViewPager(mPager);
	}
	
	private void initViewPager(int pageCount, int backgroundColor, int textColor) {
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ExamplePagerAdapter(this, pageCount, backgroundColor, textColor);
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(1);
		mPager.setPageMargin(1);
	}
}

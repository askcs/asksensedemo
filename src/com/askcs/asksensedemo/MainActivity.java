package com.askcs.asksensedemo;

import com.astuetz.viewpager.extensions.FixedTabsView;
import com.astuetz.viewpager.extensions.TabsAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class MainActivity extends Activity {

	private PagerAdapter pagerAdapter;
	private ViewPager pager;
	private FixedTabsView fixedTabs;
	private TabsAdapter fixedTabsAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		initViewPager(3, 0xFFFFFFFF, 0xFF000000);
		fixedTabs = (FixedTabsView) findViewById(R.id.fixed_tabs);
		fixedTabsAdapter = new FixedTabsAdapter(this);
		fixedTabs.setAdapter(fixedTabsAdapter);
		fixedTabs.setViewPager(pager);
	}
	
	private void initViewPager(int pageCount, int backgroundColor, int textColor) {
		
		pager = (ViewPager) findViewById(R.id.pager);
		pagerAdapter = new ExamplePagerAdapter(this, pageCount, backgroundColor, textColor);
		pager.setAdapter(pagerAdapter);
		pager.setCurrentItem(0);
		pager.setPageMargin(1);
	}
}

package com.askcs.asksensedemo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;

public class FixedTabsAdapter implements TabsAdapter {
	
	private final Activity context;
	
	private final String[] tabTitles = {
	    "ACTIVITY", "LOCATION", "PRESENCE"
	};
	
	public FixedTabsAdapter(Activity context) {
		this.context = context;
	}
	
	@Override
	public View getView(int position) {
		
		LayoutInflater inflater = context.getLayoutInflater();
		
		ViewPagerTabButton tab = (ViewPagerTabButton) inflater.inflate(R.layout.tab, null);
		
		if (position >= 0 || position < tabTitles.length) {
			tab.setText(tabTitles[position]);
		}
		
		return tab;
	}
}

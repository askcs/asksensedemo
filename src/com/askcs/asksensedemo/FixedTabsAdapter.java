package com.askcs.asksensedemo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;

public class FixedTabsAdapter implements TabsAdapter {
	
	private Activity mContext;
	
	private String[] mTitles = {
	    "ACTIVITY", "LOCATION", "PRESENCE"
	};
	
	public FixedTabsAdapter(Activity ctx) {
		this.mContext = ctx;
	}
	
	@Override
	public View getView(int position) {
		
		LayoutInflater inflater = mContext.getLayoutInflater();
		
		ViewPagerTabButton tab = (ViewPagerTabButton) inflater.inflate(R.layout.tab, null);
		
		if (position < mTitles.length) {
			tab.setText(mTitles[position]);
		}
		
		return tab;
	}
	
}

package com.askcs.asksensedemo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import com.askcs.asksensedemo.model.Setting;
import com.astuetz.viewpager.extensions.TabsAdapter;
import com.astuetz.viewpager.extensions.ViewPagerTabButton;

public class FixedTabsAdapter implements TabsAdapter {
	
	private final Activity context;
	
	private static final Tab[] TABS = {
		new Tab("ACTIVITY", "Activity state sensor", Setting.ACTIVITY_ENABLED_KEY),
		new Tab("LOCATION", "Location state sensor", Setting.LOCATION_ENABLED_KEY),
		new Tab("PRESENCE", "Presence state sensor", Setting.PRESENCE_ENABLED_KEY)
	};
	
	public FixedTabsAdapter(Activity context) {
		this.context = context;
	}
	
	@Override
	public View getView(int position) {
		
		LayoutInflater inflater = context.getLayoutInflater();
		
		ViewPagerTabButton tab = (ViewPagerTabButton) inflater.inflate(R.layout.tab, null);
		
		Tab selected = getTab(position);

		tab.setText(selected.caption);
		
		return tab;
	}
	
	public static Tab getTab(int position) {
		return (position >= 0 || position < TABS.length) ? TABS[position] : new Tab("invalid tab", null, null);
	}
	
	static class Tab {
		
		public final String caption;
		public final String checkBoxText;
		public final String settingKey;
		
		Tab(String caption, String checkBoxText, String settingKey) {
			this.caption = caption;
			this.checkBoxText = checkBoxText;
			this.settingKey = settingKey;
		}
	}
}

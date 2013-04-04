package com.askcs.asksensedemo;

import java.sql.SQLException;

import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.dao.Dao;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class FixedTabsPagerAdapter extends PagerAdapter {
	
	private static final String TAG = FixedTabsPagerAdapter.class.getName();
	
	private MainActivity activity;
	private int length;
	private int backgroundColor;
	private int textColor;
	
	public FixedTabsPagerAdapter(MainActivity activity, int length, int backgroundColor, int textColor) {
		this.activity = activity;
		this.length = length;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
	}
	
	@Override
	public int getCount() {
		return length;
	}
	
	@Override
	public Object instantiateItem(View container, int position) {
		
		final int padding = 15;
		
		LinearLayout layout = new LinearLayout(activity);
		
		layout.setOrientation(LinearLayout.VERTICAL);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		TextView text = new TextView(activity);
		text.setPadding(padding, padding * 2, padding, padding);
		text.setLayoutParams(params);
		text.setTextSize(28);
		text.setGravity(Gravity.LEFT);
		text.setTextColor(textColor);
		text.setBackgroundColor(backgroundColor);
		
		final FixedTabsAdapter.Tab tab = FixedTabsAdapter.getTab(position);
		
		text.setText(tab.checkBoxText);
		
		layout.addView(text, 0);
		
		// radio buttons
		final RadioGroup group = new RadioGroup(activity);
		
		final RadioButton enableButton = new RadioButton(activity);
		enableButton.setText(" enabled");
		enableButton.setTextSize(24);
		
		final RadioButton disableButton = new RadioButton(activity);
		disableButton.setText(" disabled");
		disableButton.setTextSize(24);
		
		group.addView(enableButton);
		group.addView(disableButton);
		
		boolean enabled = false;
		
		Dao<Setting, String> tempDao = null;
		Setting tempSetting = null;
		
		try {
			tempDao = activity.getHelper().getSettingDao();
			tempSetting = tempDao.queryForId(tab.settingKey);
			
			if(tempSetting == null) {
				tempSetting = new Setting(tab.settingKey, "false");
				tempDao.create(tempSetting);
			}
			else if(tempSetting.getValue().equals("true")) {
				enabled = true;
			}
			
		} catch (SQLException e) {
			Log.e(TAG, "could not retrieve setting from db:", e);
		}
		
		final Dao<Setting, String> dao = tempDao;
		final Setting setting = tempSetting;

		// This needs to happen _after_ being added to the RadioGroup.
		if (enabled) {
			enableButton.setChecked(true);
		}
		else {
			disableButton.setChecked(true);
		}
				
		group.setPadding(padding, padding, padding, padding);
		
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	        	
	        	String value = enableButton.isChecked() ? "true" : "false";
	        	
	        	try {
	        		Log.d(TAG, "update key=" + tab.settingKey + ", value=" + value);
					setting.setValue(value);

					dao.update(setting);
					
				} catch (SQLException e) {
					Log.e(TAG, "could not update key=" + tab.settingKey + ", value=" + value, e);
				}
	        }
	    });
		
		layout.addView(group, 1);
		
		((ViewPager) container).addView(layout, 0);
		
		return layout;
	}
	
	
	@Override
	public void destroyItem(View container, int position, Object view) {
		((ViewPager) container).removeView((View) view);
	}
	
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}	
}

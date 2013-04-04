package com.askcs.asksensedemo;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FixedTabsPagerAdapter extends PagerAdapter {
	
	protected transient Activity context;
	
	private int length;
	private int backgroundColor;
	private int textColor;
	
	private String[] data = {"1", "2", "3"};
	
	public FixedTabsPagerAdapter(Activity context, int length, int backgroundColor, int textColor) {
		this.context = context;
		this.length = length;
		this.backgroundColor = backgroundColor;
		this.textColor = textColor;
	}
	
	@Override
	public int getCount() {
		return length;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Object instantiateItem(View container, int position) {
		
		RelativeLayout layout = new RelativeLayout(context);
		
		TextView t = new TextView(context);
		t.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		t.setText(data[position]);
		t.setTextSize(120);
		t.setGravity(Gravity.CENTER);
		t.setTextColor(textColor);
		t.setBackgroundColor(backgroundColor);
		
		layout.addView(t);
		
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

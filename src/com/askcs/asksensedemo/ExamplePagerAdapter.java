package com.askcs.asksensedemo;

import android.app.Activity;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExamplePagerAdapter extends PagerAdapter {
	
	protected transient Activity mContext;
	
	private int mLength = 0;
	private int mBackgroundColor = 0xFFFFFFFF;
	private int mTextColor = 0xFF000000;
	
	private String[] mData = {"1", "2", "3"};
	
	public ExamplePagerAdapter(Activity context, int length, int backgroundColor, int textColor) {
		mContext = context;
		mLength = length;
		mBackgroundColor = backgroundColor;
		mTextColor = textColor;
	}
	
	@Override
	public int getCount() {
		return mLength;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Object instantiateItem(View container, int position) {
		
		RelativeLayout v = new RelativeLayout(mContext);
		
		TextView t = new TextView(mContext);
		t.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		t.setText(mData[position]);
		t.setTextSize(120);
		t.setGravity(Gravity.CENTER);
		t.setTextColor(mTextColor);
		t.setBackgroundColor(mBackgroundColor);
		
		v.addView(t);
		
		((ViewPager) container).addView(v, 0);
		
		return v;
	}
	
	
	@Override
	public void destroyItem(View container, int position, Object view) {
		((ViewPager) container).removeView((View) view);
	}
	
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}
	
	
	@Override
	public void finishUpdate(View container) {}
	
	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {}
	
	@Override
	public Parcelable saveState() {
		return null;
	}
	
	@Override
	public void startUpdate(View container) {}
	
}

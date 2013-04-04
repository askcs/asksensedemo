package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="settings")
public class Setting {
	
	public static final String USER_KEY = "user";
	public static final String PASSWORD_KEY = "password";
	public static final String ACTIVITY_ENABLED_KEY = "activity_enabled";
	public static final String LOCATION_ENABLED_KEY = "location_enabled";
	public static final String PRESENCE_ENABLED_KEY = "presence_enabled";
	
	@DatabaseField(id=true)
	private String key;
	
	@DatabaseField
	private String value;
	
	public Setting() {
		// No-args constructor needed by ORMLite. 
	}
	
	public Setting(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null || this.getClass() != o.getClass()) {
			return false;
		}
		
		Setting that = (Setting)o;
		
		return this.key.equals(that.key);
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("{key=%s, value=%s}", key, value);
	}
}

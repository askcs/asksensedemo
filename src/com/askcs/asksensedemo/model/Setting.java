package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="settings")
public class Setting {
	
	public static final String USER_KEY = "user";
	public static final String PASSWORD_KEY = "password";
	
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
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return String.format("{key=%s, value=%s}", key, value);
	}
}

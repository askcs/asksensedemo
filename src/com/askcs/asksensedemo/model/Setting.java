package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="settings")
public class Setting {
	
	public static final String USER_KEY = "user";
	public static final String PASSWORD_KEY = "password";
    public static final String LOGGED_IN_KEY = "logged_in";
	public static final String ACTIVITY_ENABLED_KEY = "activity_enabled";
	public static final String LOCATION_ENABLED_KEY = "location_enabled";
	public static final String REACHABILITY_ENABLED_KEY = "reachability_enabled";
    public static final String POLL_SENSE_SECONDS_KEY = "poll_sense_seconds";

    // 1 := rarely (~every 15 min)
    // 0 := normal (~every 5 min)
    // -1 := often (~every 10 sec)
    // -2 := real time (this setting affects power consumption considerably!)
    public static final String SAMPLE_RATE_KEY = "sample_rate";

    // 1 := eco mode (buffer data for 30 minutes before bulk uploading)
    // 0 := normal (buffer 5 min)
    // -1 := often (buffer 1 min)
    // -2 := real time (every new data point is uploaded immediately)
    public static final String SYNC_RATE_KEY = "sync_rate";

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

	public String getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return String.format("{key=%s, value=%s}", key, value);
	}
}

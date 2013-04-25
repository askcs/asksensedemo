package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A class representing a setting, which is a key-value pair (both stored
 * as strings).
 */
@DatabaseTable(tableName = "settings")
public class Setting {

    /**
     * The key that is pointing to the username, or the empty string when
     * the user is not logged in.
     */
    public static final String USER_KEY = "user";

    /**
     * The key that is pointing to the MD5 hash of the user's password, or
     * the empty string when the user is not logged in.
     */
    public static final String PASSWORD_KEY = "password";

    /**
     * The key that is pointing to a boolean flag indicating if the user is
     * properly logged in (if the username and password were accepted).
     */
    public static final String LOGGED_IN_KEY = "logged_in";

    /**
     * The key that is pointing to a boolean flag indicating if the Activity
     * state sensor is to be monitored.
     */
    public static final String ACTIVITY_ENABLED_KEY = "activity_enabled";

    /**
     * The key that is pointing to a boolean flag indicating if the Location
     * state sensor is to be monitored.
     */
    public static final String LOCATION_ENABLED_KEY = "location_enabled";

    /**
     * The key that is pointing to a boolean flag indicating if the Reachability
     * state sensor is to be monitored.
     */
    public static final String REACHABILITY_ENABLED_KEY = "reachability_enabled";

    /**
     * The key that is pointing to the interval of seconds to poll Sense for new
     * sensor data.
     */
    public static final String POLL_SENSE_SECONDS_KEY = "poll_sense_seconds";

    /**
     * The key that is pointing to the sample rate setting.
     */
    public static final String SAMPLE_RATE_KEY = "sample_rate";

    /**
     * The key that is pointing to the sync rate setting.
     */
    public static final String SYNC_RATE_KEY = "sync_rate";

    // The unique key of the setting.
    @DatabaseField(id = true)
    private String key;

    // The value of this setting.
    @DatabaseField
    private String value;

    /**
     * Creates a new instance of a Setting: needed by ORMLite.
     */
    public Setting() {
    }

    /**
     * Creates a new instance of a Setting with a given `key` and `value`.
     *
     * @param key   the unique key of this setting.
     * @param value the value of this setting.
     */
    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the value of this setting.
     *
     * @return the value of this setting.
     */
    public String getValue() {
        return value;
    }

    /**
     * Changes the value of this setting.
     *
     * @param value the new value of this setting.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns a string representation of this setting.
     *
     * @return a string representation of this setting.
     */
    @Override
    public String toString() {
        return String.format("{key=%s, value=%s}", key, value);
    }
}

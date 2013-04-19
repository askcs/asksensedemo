package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;
import java.text.SimpleDateFormat;

/**
 *
 */
public class State {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    public static final String ACTIVITY_KEY = "Activity";
    public static final String LOCATION_KEY = "Location";
    public static final String REACHABILITY_KEY = "Reachability";

    @DatabaseField(id=true)
    private String state;

    @DatabaseField
    private String value;

    @DatabaseField
    private Long timestamp;

    public State() {
        // No-args constructor needed by ORMLite.
    }

    public State(String state, String value, Long timestamp) {
        this.state = state;
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        State that = (State) o;

        return state.equals(that.state) && value.equals(that.value);

    }

    @Override
    public int hashCode() {
        return (31 * state.hashCode()) + value.hashCode();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", FORMATTER.format(timestamp), value);
    }
}

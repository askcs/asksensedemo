package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;

public class State {

    public static final String ACTIVITY_KEY = "Activity";
    public static final String LOCATION_KEY = "Location";
    public static final String PRESENCE_KEY = "???";

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

    // TODO equals, hashCode, toString, getters, setters
}

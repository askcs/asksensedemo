package com.askcs.asksensedemo.model;

import com.j256.ormlite.field.DatabaseField;
import java.text.SimpleDateFormat;

/**
 * A class representing a state sensor, it's current value and
 * the time it was received from Sense.
 */
public class State {

    // The formatter used to display the date in the GUI.
    private static final SimpleDateFormat FORMATTER =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    // The various state sensors we're  monitoring:
    public static final String ACTIVITY_KEY = "Activity";
    public static final String LOCATION_KEY = "Location";
    public static final String REACHABILITY_KEY = "Reachability";

    // The (unique) key of the state (the static final ..._KEY's).
    @DatabaseField(id=true)
    private String state;

    // The value of this state ("sit", "reachable", ...)
    @DatabaseField
    private String value;

    // The epoch date on which the state was received from Sense.
    @DatabaseField
    private Long timestamp;

    /**
     * Creates a new instance of a State: needed by ORMLite.
     */
    public State() {
    }

    /**
     * Creates a new instance of a State.
     *
     * @param state the state (key).
     * @param value the value of the state ("sit", "reachable", ...).
     * @param timestamp the epoch date on which the state was received from Sense.
     */
    public State(String state, String value, Long timestamp) {
        this.state = state;
        this.value = value;
        this.timestamp = timestamp;
    }

    /**
     * Checks if `this` equals `that`, where `that` is `(State)o`.
     *
     * This will be `true` iff: `this.state` equals `o.state`
     * and `this.value` equals `o.value`. Note that the timestamp is
     * not included int this equality check.
     *
     * @param o the object to check for equality with `this`.
     * @return `true` iff: `this.state` equals `o.state`
     *          and `this.value` equals `o.value`. Note that the
     *          timestamp is not included int this equality check.
     */
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

    /**
     * Returns a hash code of this instance based on `this.state` and `this.value`.
     *
     * @return a hash code of this instance based on `this.state` and `this.value`.
     */
    @Override
    public int hashCode() {
        return (31 * state.hashCode()) + value.hashCode();
    }

    /**
     * Returns the `value` of this state ("sit", "reachable", ...).
     *
     * @return the `value` of this state ("sit", "reachable", ...).
     */
    public String getValue() {
        return value;
    }

    /**
     * Changes this state's `value`.
     *
     * @param value the new `value` of this state ("sit", "reachable", ...).
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the `timestamp` of this state.
     *
     * @return the `timestamp` of this state.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Changes this state's `timestamp`.
     *
     * @param timestamp the new `timestamp` of this state.
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a string representation of this state.
     *
     * @return a string representation of this state.
     */
    @Override
    public String toString() {
        return String.format("%s%n%s", FORMATTER.format(timestamp), value);
    }
}

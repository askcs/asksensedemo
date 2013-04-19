package com.askcs.asksensedemo;

/**
 * A final class to hold Message types.
 *
 * @see ForegroundService.ServiceHandler
 * @see MainActivity.ActivityHandler
 */
public final class MessageType {

    /**
     * Send from Activities to a Service in order to register
     * to be bound to that Service.
     */
    public static final int REGISTER = 1;

    /**
     * Send from Activities to a Service in order to register
     * to be unbound from that Service.
     */
    public static final int UNREGISTER = 2;

    /**
     * Send from Activities to a Service when the user changed
     * a setting, so that the Service can apply this new setting.
     */
    public static final int SETTING_CHANGED = 3;

    /**
     * Send from the Service to all bound Activities (just one in this
     * demo App) when one of the state-sensors changed so that the GUI
     * can be updated with this new state.
     */
    public static final int STATE_CHANGED = 4;

    // Private c-tor: no need to instantiate this class.
    private MessageType() {
    }
}

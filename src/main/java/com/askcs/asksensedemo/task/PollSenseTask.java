package com.askcs.asksensedemo.task;

import android.util.Log;
import com.askcs.asksensedemo.service.ForegroundService;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.dao.Dao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.TimerTask;

/**
 * A timer-task responsible for fetching state-sensor data from Sense.
 */
public final class PollSenseTask extends TimerTask {

    // The log-tag.
    private static final String TAG = PollSenseTask.class.getName();

    // The foreground service this timer task is executed from.
    private final ForegroundService service;

    // The next waiting time of this task: only used for debugging purposes.
    private final long pause;

    /**
     * Creates a new timer task that fetched state-sensor data from Sense.
     *
     * @param service the foreground service this timer task is executed from.
     * @param pause   the next waiting time of this task: only used for debugging purposes.
     */
    public PollSenseTask(ForegroundService service, long pause) {

        this.service = service;
        this.pause = pause;
    }

    /**
     * Fetches all state-sensor settings and delegates the work to the `check()` method.
     *
     * @see this#check(boolean, String)
     */
    @Override
    public void run() {

        Log.d(TAG, "ticking every " + (pause / 1000) + " seconds");

        Dao<Setting, String> settingDao = service.getHelper().getDao(Setting.class, String.class);

        // Let's assume none of the state sensors need to be polled.
        boolean checkActivity = false;
        boolean checkLocation = false;
        boolean checkReachability = false;

        try {
            // Get all sensor enabled-settings from the local DB.
            Setting activitySetting = settingDao.queryForId(Setting.ACTIVITY_ENABLED_KEY);
            Setting locationSetting = settingDao.queryForId(Setting.LOCATION_ENABLED_KEY);
            Setting presenceSetting = settingDao.queryForId(Setting.REACHABILITY_ENABLED_KEY);

            // Parse their string values into boolean values.
            checkActivity = activitySetting.getValue().equals(String.valueOf(Boolean.TRUE));
            checkLocation = locationSetting.getValue().equals(String.valueOf(Boolean.TRUE));
            checkReachability = presenceSetting.getValue().equals(String.valueOf(Boolean.TRUE));
        }
        catch (SQLException e) {
            Log.e(TAG, "Could not get settings from local DB: ", e);
        }

        // Delegate the actual work.
        check(checkActivity, State.ACTIVITY_KEY);
        check(checkLocation, State.LOCATION_KEY);
        check(checkReachability, State.REACHABILITY_KEY);
    }

    // Does the actual work: if `doCheck` is `true`, the most recent entry
    // for `stateKey` from Sense is retrieved and compared to the most recent
    // entry from the local DB.
    private void check(boolean doCheck, String stateKey) {

        if (!doCheck) {
            return;
        }

        try {
            // Get the most recent (single!) entry from Sense.
            JSONArray data = service.getSensePlatform().getData(stateKey, false, 1);

            // Check if Sense returned at least 1 value.
            if (data.length() > 0) {

                Dao<State, String> stateDao = service.getHelper().getDao(State.class, String.class);

                // Get the most recent entry from the local DB.
                State mostRecentLocal = stateDao.queryForId(stateKey);

                // Get the most recent entry from Sense.
                JSONObject obj = (JSONObject) data.get(0);
                State mostRecentSense = new State(stateKey, obj.getString("value"), obj.getLong("timestamp"));

                // Check if there are any changes.
                if (!mostRecentSense.equals(mostRecentLocal)) {

                    String message = String.format("%s -> %s", mostRecentLocal.getValue(), mostRecentSense.getValue());
                    Log.d(TAG, message);

                    // Update the old entry and update the local DB.
                    mostRecentLocal.setValue(mostRecentSense.getValue());
                    mostRecentLocal.setTimestamp(mostRecentSense.getTimestamp());
                    stateDao.update(mostRecentLocal);

                    // Send a notification to the user via the OS status-bar.
                    service.sendNotification(message);
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "something went wrong while fetching data from Sense: ", e);
        }
    }
}

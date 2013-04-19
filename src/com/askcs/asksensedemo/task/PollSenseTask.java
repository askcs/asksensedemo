package com.askcs.asksensedemo.task;

import android.util.Log;
import com.askcs.asksensedemo.ForegroundService;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.dao.Dao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.TimerTask;

public class PollSenseTask extends TimerTask {

    private static final String TAG = PollSenseTask.class.getName();

    private final ForegroundService service;

    public PollSenseTask(ForegroundService service) {

        this.service = service;
    }

    private void check(boolean doCheck, String stateKey) {

        if(doCheck) {

            Dao<State, String> stateDao = service.getHelper().getStateDao();

            try {
                int limit = 1;
                JSONArray data = service.getSensePlatform().getData(stateKey, false, limit);

                Log.d(TAG, "--> limit=" + limit + ", data.length=" + data.length());

                State last = stateDao.queryForId(stateKey);

                if(data.length() > 0) {

                    JSONObject obj = (JSONObject)data.get(0);
                    State state = new State(stateKey, obj.getString("value"), obj.getLong("timestamp"));

                    Log.d(TAG, stateKey + " :: last state -> " + state);

                    if(state.equals(last)) {
                        Log.d(TAG, "no state change between last=[" + last + "] and state=[" + state + "]");
                    }
                    else {

                        String message = String.format("%s -> %s", last.getValue(), state.getValue());

                        Log.i(TAG, message);

                        last.setValue(state.getValue());
                        last.setTimestamp(state.getTimestamp());
                        stateDao.update(last);

                        service.sendNotification(message);
                    }
                }
            } catch(Exception e) {
                Log.e(TAG, "Oops: ", e);
            }
        }
        else {
            Log.d(TAG, "skipping " + stateKey);
        }
    }

    @Override
    public void run() {
        try {

            Dao<Setting, String> settingDao = service.getHelper().getSettingDao();

            boolean checkActivity = false;
            boolean checkLocation = false;
            boolean checkPresence = false;

            try {
                Setting activitySetting = settingDao.queryForId(Setting.ACTIVITY_ENABLED_KEY);
                Setting locationSetting = settingDao.queryForId(Setting.LOCATION_ENABLED_KEY);
                Setting presenceSetting = settingDao.queryForId(Setting.REACHABILITY_ENABLED_KEY);

                checkActivity = activitySetting.getValue().equals(String.valueOf(Boolean.TRUE));
                checkLocation = locationSetting.getValue().equals(String.valueOf(Boolean.TRUE));
                checkPresence = presenceSetting.getValue().equals(String.valueOf(Boolean.TRUE));
            }
            catch (SQLException e) {
                Log.e(TAG, "Oops: ", e);
            }

            check(checkActivity, State.ACTIVITY_KEY);
            check(checkLocation, State.LOCATION_KEY);
            check(checkPresence, State.REACHABILITY_KEY);
        }
        catch (Exception e) {
            Log.e(TAG, "Oops: ", e);
        }
    }
}

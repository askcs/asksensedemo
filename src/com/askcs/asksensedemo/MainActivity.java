package com.askcs.asksensedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    private DatabaseHelper databaseHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        final Dao<Setting, String> dao = this.getHelper().getSettingDao();

        try {
            final Setting userSetting = dao.queryForId(Setting.USER_KEY);
            final Setting passwordSetting = dao.queryForId(Setting.PASSWORD_KEY);

            Log.d(TAG, "userSetting=" + userSetting);

            if(userSetting.getValue().isEmpty()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            else {
                startService(new Intent(this, ForegroundService.class));
            }
        }
        catch (SQLException e) {
            Log.e(TAG, "Oops: ", e);
        }

        super.setContentView(R.layout.main);

        init(R.id.id_radio_group_activity, R.id.id_radio_activity_enabled,
                R.id.id_radio_activity_disabled, Setting.ACTIVITY_ENABLED_KEY);

        init(R.id.id_radio_group_location, R.id.id_radio_location_enabled,
                R.id.id_radio_location_disabled, Setting.LOCATION_ENABLED_KEY);

        init(R.id.id_radio_group_presence, R.id.id_radio_presence_enabled,
                R.id.id_radio_presence_disabled, Setting.PRESENCE_ENABLED_KEY);
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");

        super.onDestroy();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private void init(int groupId, int enabledId, int disabledId, final String key) {

        final Dao<Setting, String> dao = this.getHelper().getSettingDao();

        final RadioGroup group = (RadioGroup)super.findViewById(groupId);
        final RadioButton enabled = (RadioButton)super.findViewById(enabledId);
        final RadioButton disabled = (RadioButton)super.findViewById(disabledId);

        try {

            final Setting enabledSetting = dao.queryForId(key);

            // Initialize the radio buttons.
            enabled.setChecked(Boolean.valueOf(enabledSetting.getValue()));
            disabled.setChecked(!Boolean.valueOf(enabledSetting.getValue()));

            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    try {
                        // Upon a radio button change, update the setting in the local DB.
                        String newValue = enabled.isChecked() ? String.valueOf(Boolean.TRUE) : String.valueOf(Boolean.FALSE);
                        enabledSetting.setValue(newValue);
                        Log.d(TAG, "update setting: " + enabledSetting);
                        dao.update(enabledSetting);
                    } catch (SQLException e) {
                        Log.e(TAG, "Oops: ", e);
                    }
                }
            });

        } catch (SQLException e) {
            Log.e(TAG, "Oops: ", e);
        }
    }

    public DatabaseHelper getHelper() {

        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }
}

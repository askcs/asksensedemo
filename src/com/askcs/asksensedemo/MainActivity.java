package com.askcs.asksensedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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

        final Intent serviceIntent = new Intent(this, ForegroundService.class);

        try {
            final Setting userSetting = dao.queryForId(Setting.USER_KEY);

            Log.d(TAG, "userSetting=" + userSetting);

            if(userSetting.getValue().isEmpty()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            else {
                startService(serviceIntent);
            }
        }
        catch (SQLException e) {
            Log.e(TAG, "Oops: ", e);
        }

        super.setContentView(R.layout.main);

        init(R.id.id_checkbox_activity, Setting.ACTIVITY_ENABLED_KEY);

        init(R.id.id_checkbox_location, Setting.LOCATION_ENABLED_KEY);

        init(R.id.id_checkbox_presence, Setting.PRESENCE_ENABLED_KEY);

        Button logout = (Button) findViewById(R.id.id_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.confirm_logout)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    Setting userSetting = dao.queryForId(Setting.USER_KEY);
                                    Setting passwordSetting = dao.queryForId(Setting.PASSWORD_KEY);

                                    userSetting.setValue("");
                                    passwordSetting.setValue("");

                                    dao.update(userSetting);
                                    dao.update(passwordSetting);

                                    MainActivity.this.stopService(serviceIntent);
                                    MainActivity.this.finish();
                                }
                                catch (SQLException e) {
                                    Log.e(TAG, "Oops: ", e);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Cancelled, do nothing.
                            }
                        })
                        .create()
                        .show();
            }
        });
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

    private void init(int checkboxId, final String key) {

        final Dao<Setting, String> dao = this.getHelper().getSettingDao();

        final CheckBox checkBox = (CheckBox)super.findViewById(checkboxId);

        try {

            final Setting enabledSetting = dao.queryForId(key);

            checkBox.setChecked(Boolean.valueOf(enabledSetting.getValue()));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Upon change, update the setting in the local DB.
                        String newValue = checkBox.isChecked() ? String.valueOf(Boolean.TRUE) : String.valueOf(Boolean.FALSE);
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

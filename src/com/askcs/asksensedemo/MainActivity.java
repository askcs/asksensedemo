package com.askcs.asksensedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

import static com.askcs.asksensedemo.MessageType.*;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    // TODO
    private Messenger serviceMessenger = null;

    // The Messenger of this Activity that handles incoming messages.
    private Messenger activityMessenger = null;

    // TODO
    private DatabaseHelper databaseHelper = null;

    // A handler receiving Messages from the fore ground service it is bound to.
    private static final class ActivityHandler extends Handler {

        private final WeakReference<MainActivity> reference;

        ActivityHandler(MainActivity activity) {
            reference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {

            MainActivity activity = reference.get();

            if(activity == null) {
                Log.w(TAG, "activity was already destroyed, ignoring message: " + message);
                return;
            }

            switch (message.what) {

                case STATE_CHANGED:
                    Log.d(TAG, "handling STATE_CHANGED");
                    activity.readStates();
                    break;
                default:
                    Log.w(TAG, "unknown message type: " + message);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        checkLoggedIn();

        final Dao<Setting, String> settingDao = this.getHelper().getSettingDao();

        final Intent serviceIntent = new Intent(this, ForegroundService.class);

        super.setContentView(R.layout.main);

        init(R.id.id_checkbox_activity, Setting.ACTIVITY_ENABLED_KEY);
        init(R.id.id_checkbox_location, Setting.LOCATION_ENABLED_KEY);
        init(R.id.id_checkbox_presence, Setting.REACHABILITY_ENABLED_KEY);

        findViewById(R.id.id_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.confirm_logout)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    Setting userSetting = settingDao.queryForId(Setting.USER_KEY);
                                    Setting passwordSetting = settingDao.queryForId(Setting.PASSWORD_KEY);
                                    Setting loggedInSetting = settingDao.queryForId(Setting.LOGGED_IN_KEY);

                                    userSetting.setValue("");
                                    passwordSetting.setValue("");
                                    loggedInSetting.setValue(String.valueOf(Boolean.FALSE));

                                    settingDao.update(userSetting);
                                    settingDao.update(passwordSetting);
                                    settingDao.update(loggedInSetting);

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

        this.activityMessenger = new Messenger(new ActivityHandler(this));

        bindToService();

        readStates();
    }

    private void bindToService() {

        final ServiceConnection connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {

                serviceMessenger = new Messenger(service);

                try {
                    Message message = Message.obtain(null, REGISTER);
                    message.replyTo = MainActivity.this.activityMessenger;
                    serviceMessenger.send(message);

                } catch (RemoteException e) {
                    Log.e(TAG, "crashed service: ", e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName className) {
                serviceMessenger = null;
            }
        };

        bindService(new Intent(this, ForegroundService.class), connection, Context.BIND_AUTO_CREATE);
    }

    private void checkLoggedIn() {

        final Dao<Setting, String> settingDao = this.getHelper().getSettingDao();

        try {
            final Setting loggedInSetting = settingDao.queryForId(Setting.LOGGED_IN_KEY);

            Log.d(TAG, "loggedInSetting=" + loggedInSetting);

            if(!loggedInSetting.getValue().equals(String.valueOf(Boolean.TRUE))) {
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
    }

    private void readStates() {

        try {
            // Get the most recent states and update the GUI text views.

            final Dao<State, String> stateDao = this.getHelper().getStateDao();

            State activityState = stateDao.queryForId(State.ACTIVITY_KEY);
            State locationState = stateDao.queryForId(State.LOCATION_KEY);
            State presenceState = stateDao.queryForId(State.REACHABILITY_KEY);

            TextView txtActivity = (TextView) super.findViewById(R.id.id_txt_status_activity);
            txtActivity.setText(activityState.toString());

            TextView txtLocation = (TextView) super.findViewById(R.id.id_txt_status_location);
            txtLocation.setText(locationState.toString());

            TextView txtPresence = (TextView) super.findViewById(R.id.id_txt_status_presence);
            txtPresence.setText(presenceState.toString());

        }
        catch (SQLException e) {
            Log.e(TAG, "Oops: ", e);
        }
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");

        super.onDestroy();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }

        if(this.serviceMessenger != null) {

            Message unregisterMessage = Message.obtain(null, UNREGISTER);
            unregisterMessage.replyTo = this.activityMessenger;

            try {
                this.serviceMessenger.send(unregisterMessage);
            } catch (RemoteException e) {
                Log.w(TAG, "could not unregister from service");
            }
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

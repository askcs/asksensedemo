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
import com.askcs.asksensedemo.service.ForegroundService;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.askcs.asksensedemo.service.MessageType.*;

/**
 * The main Activity of this App. If the username and hashed password are
 * still in the local DB, this Activity is created, and if not, the
 * #LoginActivity is started.
 *
 * This Activity also connects to the #service.ForegroundService when it is
 * created (and disconnects when destroyed!) so that the foreground service
 * can notify this Activity when a state change has happened (and this
 * Activity can update the GUI components holding this information).
 *
 * It also works the other way around: when the user change a setting, this
 * Activity notifies the foreground service about this.
 */
public class MainActivity extends Activity {

    // The log tag.
    private static final String TAG = MainActivity.class.getName();

    // The messenger that can be used to send Messages to the foreground service.
    private Messenger serviceMessenger = null;

    // The Messenger of this Activity that handles incoming messages.
    private Messenger activityMessenger = null;

    // The local SQLite database helper to retrieve DAO instances from.
    private DatabaseHelper databaseHelper = null;

    // A handler receiving Messages from the fore ground service it is bound to.
    static final class ActivityHandler extends Handler {

        // Use a weak reference: the enclosing Activity might be destroyed,
        // resulting in leaking service connections.
        private final WeakReference<MainActivity> reference;

        /**
         * Creates a new ActivityHandler instance.
         *
         * @param activity the Activity it was created from.
         */
        ActivityHandler(MainActivity activity) {
            reference = new WeakReference<MainActivity>(activity);
        }

        /**
         * Handles incoming messages.
         *
         * @param message the incoming Message.
         */
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

    /**
     * Called when this Activity is created.
     *
     * @param savedInstanceState the saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        if(!isLoggedIn()) {
            // If the user is not logged in, start the login Activity.
            startActivity(new Intent(this, LoginActivity.class));

            // Call finish() so that the back button cannot take the user back to this activity.
            finish();
        }
        else {
            startService(new Intent(this, ForegroundService.class));

            final Dao<Setting, String> settingDao = this.getHelper().getSettingDao();

            super.setContentView(R.layout.main);

            initStateCheckBox(R.id.id_checkbox_activity, Setting.ACTIVITY_ENABLED_KEY);
            initStateCheckBox(R.id.id_checkbox_location, Setting.LOCATION_ENABLED_KEY);
            initStateCheckBox(R.id.id_checkbox_presence, Setting.REACHABILITY_ENABLED_KEY);

            final Activity activity = MainActivity.this;

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

                                        activity.stopService(new Intent(activity, ForegroundService.class));
                                        activity.finish();
                                    }
                                    catch (SQLException e) {
                                        Log.e(TAG, "there was a problem retrieving data from the local DB: ", e);
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
            readSettings();
        }
    }

    /**
     * Binds this Activity to the foreground service.
     */
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

    /**
     * Returns true iff the user is logged in (if the Setting.LOGGED_IN_KEY
     * in the local DB is set to TRUE).
     *
     * @return true iff the user is logged in.
     */
    private boolean isLoggedIn() {

        final Dao<Setting, String> settingDao = this.getHelper().getSettingDao();

        try {
            final Setting loggedInSetting = settingDao.queryForId(Setting.LOGGED_IN_KEY);

            Log.d(TAG, "loggedInSetting=" + loggedInSetting);

            return loggedInSetting.getValue().equals(String.valueOf(Boolean.TRUE));
        }
        catch (SQLException e) {
            Log.e(TAG, "Oops: ", e);
            return false;
        }
    }

    /**
     * Updates all TextViews to show the most recent values of the state sensors.
     */
    private void readStates() {

        try {
            // Get the most recent states and update the GUI components with said values.

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

    /**
     * Reads all settings from the local DB and updates the button's that
     * show these values.
     */
    private void readSettings() {

        try {

            final Dao<Setting, String> settingDao = this.getHelper().getSettingDao();

            // Display the presently selected poll-Sense setting.
            setButton(settingDao.queryForId(Setting.POLL_SENSE_SECONDS_KEY),
                    R.array.poll_sense_labels,
                    R.array.poll_sense_values,
                    R.id.poll_sense);

            // Display the presently selected sample-rate setting.
            setButton(settingDao.queryForId(Setting.SAMPLE_RATE_KEY),
                    R.array.sample_rate_labels,
                    R.array.sample_rate_values,
                    R.id.sample_rate);

            // Display the presently selected sync-rate setting.
            setButton(settingDao.queryForId(Setting.SYNC_RATE_KEY),
                    R.array.sync_rate_labels,
                    R.array.sync_rate_values,
                    R.id.sync_rate);

        } catch (SQLException e) {
            Log.e(TAG, "Could not read setting from local DB: ", e);
        }
    }

    /**
     * Set text on a specific setting-button and also associates an array
     * of options that is to be presented to the user when the button is
     * pressed.
     *
     * @param setting the Setting representing the current value.
     * @param labelsId the id pointing to the array of labels (human
     *                 readable). See values/constants.xml for these
     *                 values.
     * @param valuesId the id pointing to the array of values. See
     *                 values/constants.xml for these values.
     * @param buttonId the id of the button.
     */
    private void setButton(final Setting setting, int labelsId, int valuesId, int buttonId) {

        String selectedValue = setting.getValue();

        final String[] labelsArray = getResources().getStringArray(labelsId);
        final String[] valuesArray = getResources().getStringArray(valuesId);
        final List<String> labels = Arrays.asList(labelsArray);
        final List<String> values = Arrays.asList(valuesArray);

        final int indexSelectedValue = values.indexOf(selectedValue);

        if(indexSelectedValue < 0) {
            Log.w(TAG, "this should not have happened: `indexSelectedValue < 0`, setting=" + setting);
            return;
        }

        final Button button = (Button) super.findViewById(buttonId);

        button.setText(labels.get(indexSelectedValue));

        // Listen for clicks on the settings-button.
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final AlertDialog.Builder optionsDialog = new AlertDialog.Builder(MainActivity.this);

                // Listen for clicks on one of the popped up options.
                optionsDialog.setSingleChoiceItems(labelsArray,
                        values.indexOf(setting.getValue()), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int index) {

                        final Dao<Setting, String> settingDao = MainActivity.this.getHelper().getSettingDao();

                        button.setText(labelsArray[index]);

                        try {
                            // Update the DB with this new value.
                            setting.setValue(valuesArray[index]);
                            settingDao.update(setting);

                            // Inform the service of this setting change.
                            MainActivity.this.serviceMessenger.send(Message.obtain(null, SETTING_CHANGED));

                        } catch (Exception e) {
                            Log.e(TAG, "could not change setting: " + setting, e);
                        }

                        dialog.dismiss();
                    }
                });

                // Create and show the options selection dialog.
                optionsDialog.create().show();
            }
        });
    }

    /**
     * Called when this Activity is destroyed.
     */
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

    /**
     * Attaches a listener to a checkbox that will dis- or enable
     * updates to state-sensor changes.
     *
     * @param checkboxId the id of the check-box.
     * @param key        the key associated with the state-sensor.
     *
     * @see State#ACTIVITY_KEY
     * @see State#LOCATION_KEY
     * @see State#REACHABILITY_KEY
     */
    private void initStateCheckBox(int checkboxId, final String key) {

        final Dao<Setting, String> dao = this.getHelper().getSettingDao();

        final CheckBox checkBox = (CheckBox)super.findViewById(checkboxId);

        try {

            final Setting enabledSetting = dao.queryForId(key);

            checkBox.setChecked(Boolean.valueOf(enabledSetting.getValue()));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Upon a state change, update the setting in the local DB.
                        String newValue = checkBox.isChecked() ? String.valueOf(Boolean.TRUE) : String.valueOf(Boolean.FALSE);
                        enabledSetting.setValue(newValue);

                        // Update the local DB.
                        Log.d(TAG, "update setting: " + enabledSetting);
                        dao.update(enabledSetting);

                    } catch (SQLException e) {
                        Log.e(TAG, "could not update setting: " + enabledSetting, e);
                    }
                }
            });

        } catch (SQLException e) {
            Log.e(TAG, "there was a problem retrieving data from the local DB: ", e);
        }
    }

    /**
     * Returns a DatabaseHelper.
     *
     * @return a DatabaseHelper.
     */
    public DatabaseHelper getHelper() {

        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }
}

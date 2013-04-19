package com.askcs.asksensedemo.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.askcs.asksensedemo.MainActivity;
import com.askcs.asksensedemo.R;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.dao.Dao;
import nl.sense_os.service.commonsense.SenseApi;

import java.sql.SQLException;

/**
 * A task performing a login to Sense.
 */
public class LoginTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = LoginTask.class.getName();

    private final Activity activity;
    private final ProgressDialog progressDialog;
    private final Dao<Setting, String> settingDao;
    private final String username;
    private final String passwordHash;
    private String message = null;

    /**
     * Creates a new login-task.
     *
     * @param activity     the Activity from which this task was started.
     * @param username     the username.
     * @param passwordHash a MD5 hash of the password.
     * @param settingDao   a DAO for the local setting-table.
     */
    public LoginTask(Activity activity, String username, String passwordHash, Dao<Setting, String> settingDao) {

        this.progressDialog = new ProgressDialog(activity);
        this.settingDao = settingDao;
        this.activity = activity;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Makes a synchronous login call the the Sense backend.
     *
     * @param params nothing.
     * @return a <code>boolean</code> value indicating whether the login was successful.
     */
    @Override
    protected Boolean doInBackground(Void... params) {

        boolean success = false;

        try {
            int result = SenseApi.login(activity, username, passwordHash);

            switch (result) {
                case 0:
                    success = true;
                    message = activity.getString(R.string.welcome_user, username);
                    break;
                case -1:
                    message = activity.getString(R.string.no_login_connection);
                    break;
                case -2:
                    message = activity.getString(R.string.no_login_credentials);
                    break;
                default:
                    message = activity.getString(R.string.no_login_other, result);
            }
        } catch (Exception e) {
            message = e.getMessage();
        }

        return success;
    }

    /**
     * Executed before `doInBackground(...)`.
     */
    @Override
    protected void onPreExecute() {

        // Give the dialog a message and pop it up on the screen.
        this.progressDialog.setMessage(activity.getResources().getString(R.string.logging_in));
        this.progressDialog.show();
    }

    /**
     * Executed after `doInBackground(...)` and is executed on the GUI thread.
     *
     * @param success if the login was successful.
     */
    @Override
    protected void onPostExecute(Boolean success) {

        // Close the dialog, if still visible.
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

        try {
            // Fetch all credential Setting object from the local DB.
            Setting usernameSetting = settingDao.queryForId(Setting.USER_KEY);
            Setting passwordSetting = settingDao.queryForId(Setting.PASSWORD_KEY);
            Setting loggedInSetting = settingDao.queryForId(Setting.LOGGED_IN_KEY);

            // Change all settings depending on successful login.
            usernameSetting.setValue(success ? username : "");
            passwordSetting.setValue(success ? passwordHash : "");
            loggedInSetting.setValue(String.valueOf(success ? Boolean.TRUE : Boolean.FALSE));

            // Update/commit the settings to the local DB.
            settingDao.update(usernameSetting);
            settingDao.update(passwordSetting);
            settingDao.update(loggedInSetting);
        } catch (SQLException e) {
            Log.e(TAG, "Something went wrong updating login credentials: ", e);
        }

        if (success) {
            // Close the LoginActivity upon successful login and start the main Activity.
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        }
    }
}

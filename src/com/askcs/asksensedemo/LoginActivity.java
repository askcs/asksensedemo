package com.askcs.asksensedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.util.Utils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getName();

    private DatabaseHelper databaseHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        super.setContentView(R.layout.login);

        final EditText username = (EditText) super.findViewById(R.id.id_username);
        final EditText password = (EditText) super.findViewById(R.id.id_password);
        final Button signin = (Button) super.findViewById(R.id.id_signin);
        final TextView error = (TextView) super.findViewById(R.id.id_error);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null && bundle.containsKey("message")) {
            error.setText((String)bundle.get("message"));
        }

        // TODO remove after testing:
        //username.setText(getString(R.string.sense_username));
        //password.setText(getString(R.string.sense_password));

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usernameValue = username.getText().toString().trim();
                String passwordValue = password.getText().toString().trim();

                if(usernameValue.isEmpty()) {
                    error.setText(getString(R.string.enter_username));
                    username.requestFocus();
                }
                else if(passwordValue.isEmpty()) {
                    error.setText(getString(R.string.enter_password));
                    password.requestFocus();
                }
                else {

                    Dao<Setting, String> dao = LoginActivity.this.getHelper().getSettingDao();

                    try {
                        Setting usernameSetting = dao.queryForId(Setting.USER_KEY);
                        Setting passwordSetting = dao.queryForId(Setting.PASSWORD_KEY);
                        Setting loggedInSetting = dao.queryForId(Setting.LOGGED_IN_KEY);

                        usernameSetting.setValue(usernameValue);
                        passwordSetting.setValue(Utils.md5(passwordValue));
                        loggedInSetting.setValue(String.valueOf(Boolean.TRUE));

                        dao.update(usernameSetting);
                        dao.update(passwordSetting);
                        dao.update(loggedInSetting);

                        Log.d(TAG, "usernameSetting=" + usernameSetting + ", passwordSetting=" + passwordSetting);

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    } catch (SQLException e) {
                        Log.e(TAG, "Oops: ", e);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "onDestroy");

        super.onDestroy();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
        }
    }

    public DatabaseHelper getHelper() {

        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }
}

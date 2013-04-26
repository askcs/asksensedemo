package com.askcs.asksensedemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.task.LoginTask;
import com.askcs.asksensedemo.task.RegisterTask;
import com.askcs.asksensedemo.util.Utils;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

public class RegisterActivity extends Activity {

    private static final String TAG = RegisterActivity.class.getName();

    private DatabaseHelper databaseHelper = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        super.setContentView(R.layout.register);

        final EditText username = (EditText) super.findViewById(R.id.reg_username);
        final EditText password1 = (EditText) super.findViewById(R.id.reg_password1);
        final EditText password2 = (EditText) super.findViewById(R.id.reg_password2);
        final Button register = (Button) super.findViewById(R.id.reg_register);
        final TextView error = (TextView) super.findViewById(R.id.reg_error);

        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String usernameValue = username.getText().toString().trim();
                String passwordValue1 = password1.getText().toString().trim();
                String passwordValue2 = password2.getText().toString().trim();

                if(usernameValue.isEmpty()) {
                    error.setText(getString(R.string.enter_username));
                    username.requestFocus();
                }
                else if(passwordValue1.isEmpty()) {
                    error.setText(getString(R.string.enter_password));
                    password1.requestFocus();
                }
                else if(passwordValue2.isEmpty()) {
                    error.setText(getString(R.string.enter_password));
                    password2.requestFocus();
                }
                else if(!passwordValue1.equals(passwordValue2)) {
                    error.setText(getString(R.string.unequal_password));
                    password1.setText("");
                    password2.setText("");
                    password1.requestFocus();
                }
                else {

                    Dao<Setting, String> dao = RegisterActivity.this.getHelper().getDao(Setting.class, String.class);

                    RegisterTask task = new RegisterTask(RegisterActivity.this, usernameValue, Utils.md5(passwordValue1), dao);
                    task.execute();
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

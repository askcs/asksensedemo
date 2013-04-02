package com.askcs.asksensedemo;

import java.sql.SQLException;

import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private static final String TAG = LoginActivity.class.getName();
	
	private Button loginButton;
	private EditText usernameText;
	private EditText password1Text;
	private EditText password2Text;
	
	private DatabaseHelper databaseHelper = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		loginButton = (Button) super.findViewById(R.id.btn_login);
		usernameText = (EditText) super.findViewById(R.id.txt_username);
		password1Text = (EditText) super.findViewById(R.id.txt_password1);
		password2Text = (EditText) super.findViewById(R.id.txt_password2);
		
		loginButton.setEnabled(false);
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loginOrRegister();
			}
		});
		
		TextWatcher watcher = new SimpleTextWatcher();
		
		usernameText.addTextChangedListener(watcher);
		password1Text.addTextChangedListener(watcher);
		password2Text.addTextChangedListener(watcher);
	}

	private void loginOrRegister() {
		
		String username = usernameText.getText().toString().trim();
		String password1 = password1Text.getText().toString().trim();
		String password2 = password2Text.getText().toString().trim();
		
		if(password2.length() == 0) {
			
			// TODO make an (async) task instead
			try {
				getHelper().getSettingDao().create(new Setting(Setting.USER_KEY, username));
				getHelper().getSettingDao().create(new Setting(Setting.PASSWORD_KEY, password1));				
				Toast.makeText(this, "logged in as " + username, Toast.LENGTH_LONG).show();
			} catch (SQLException e) {
				Log.e(TAG, "Oops, SQLException:", e);
			}
			
			super.finish();
		}
		else {
			Toast.makeText(this, "TODO register " + username + ":" + password1, Toast.LENGTH_LONG).show();
		}
	}
	
	private DatabaseHelper getHelper() {
		
	    if (databaseHelper == null) {
	        databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
	    }
	    
	    return databaseHelper;
	}
	
	@Override
	protected void onDestroy() {
	    
		super.onDestroy();
	    
	    if (databaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        databaseHelper = null;
	    }
	}
	
	private class SimpleTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
			// nothing to do
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// nothing to do
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
			loginButton.setEnabled(false);
			loginButton.setText(LoginActivity.this.getString(R.string.login));
			
			String username = usernameText.getText().toString().trim();
			String password1 = password1Text.getText().toString().trim();
			String password2 = password2Text.getText().toString().trim();
			
			// Make sure a username and password is provided.
			if(username.length() > 0 && password1.length() > 0) {
				
				if(password2.length() > 0) {
					loginButton.setText(LoginActivity.this.getString(R.string.register));
					loginButton.setEnabled(password1.equals(password2));
				}
				else {
					// Only enable the button if no 2nd password is provided!
					loginButton.setEnabled(password2.length() == 0);
				}
			}
		}
	}
}

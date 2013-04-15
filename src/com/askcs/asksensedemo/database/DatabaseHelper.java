package com.askcs.asksensedemo.database;

import com.askcs.asksensedemo.model.State;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.askcs.asksensedemo.R;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.util.Date;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String TAG = DatabaseHelper.class.getName();
	
	private static final String DATABASE_NAME = "ask_sense_demo.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<Setting, String> settingDao = null;
    private Dao<State, String> stateDao = null;

	public DatabaseHelper(Context context) {
		
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		
		Log.d(TAG, "onCreate");
		
		try {
			for(Class<?> tableClass : DatabaseConfigUtil.classes) {
				Log.d(TAG, "creating a table for class: " + tableClass.getName());
				TableUtils.createTable(connectionSource, tableClass);				
			}

            // Add some default settings and states.

            this.getSettingDao().create(new Setting(Setting.ACTIVITY_ENABLED_KEY, String.valueOf(Boolean.FALSE)));
            this.getSettingDao().create(new Setting(Setting.LOCATION_ENABLED_KEY, String.valueOf(Boolean.FALSE)));
            this.getSettingDao().create(new Setting(Setting.PRESENCE_ENABLED_KEY, String.valueOf(Boolean.FALSE)));
            this.getSettingDao().create(new Setting(Setting.USER_KEY, ""));
            this.getSettingDao().create(new Setting(Setting.PASSWORD_KEY, ""));
            this.getSettingDao().create(new Setting(Setting.LOGGED_IN_KEY, String.valueOf(Boolean.FALSE)));

            this.getStateDao().create(new State(State.ACTIVITY_KEY, "unknown", new Date().getTime()));
            this.getStateDao().create(new State(State.LOCATION_KEY, "unknown", new Date().getTime()));
            this.getStateDao().create(new State(State.PRESENCE_KEY, "unknown", new Date().getTime()));

		} catch (SQLException e) {
			Log.e(TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		
		// TODO
	}

	public Dao<Setting, String> getSettingDao() {
		
		if (settingDao == null) {
            try {
                settingDao = getDao(Setting.class);
            } catch (SQLException e) {
                Log.e(TAG, "Oops: ", e);
                settingDao = null;
            }
        }
		
		return settingDao;
	}

    public Dao<State, String> getStateDao() {

        if (stateDao == null) {
            try {
                stateDao = getDao(State.class);
            } catch (SQLException e) {
                Log.e(TAG, "Oops: ", e);
                stateDao = null;
            }
        }

        return stateDao;
    }

	@Override
	public void close() {
		super.close();
		settingDao = null;
	}
}

package com.askcs.asksensedemo.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.askcs.asksensedemo.R;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	
	private static final String TAG = DatabaseHelper.class.getName();
	
	private static final String DATABASE_NAME = "ask_sense_demo.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<Setting, String> settingDao = null;

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

	public Dao<Setting, String> getSettingDao() throws SQLException {
		
		if (settingDao == null) {
			settingDao = getDao(Setting.class);
		}
		
		return settingDao;
	}

	@Override
	public void close() {
		super.close();
		settingDao = null;
	}
}

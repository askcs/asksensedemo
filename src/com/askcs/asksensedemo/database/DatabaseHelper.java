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

	private Dao<Setting, Integer> settingDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		
		try {
			Log.i(TAG, "onCreate");
			
			for(Class<?> tableClass : DatabaseConfigUtil.classes) {
				Log.i(TAG, "creating a table for class: " + tableClass.getName());
				TableUtils.createTable(connectionSource, tableClass);				
			}
			
		} catch (SQLException e) {
			Log.e(TAG, "Can't create database", e);
			throw new RuntimeException(e);
		}

		try {
			Dao<Setting, Integer> dao = getSettingDao();
			dao.create(new Setting("user", "bart"));
			dao.create(new Setting("password", "s3cr3t"));
		} catch (SQLException e) {
			Log.e(TAG, "could not insert data:", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		// Not needed.
	}

	/**
	 * Returns the Database Access Object (DAO) for our Setting class. It
	 * will create it or just give the cached value.
	 */
	public Dao<Setting, Integer> getSettingDao() throws SQLException {
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

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
import java.util.HashMap;
import java.util.Map;

/**
 * A database helper extending `OrmLiteSqliteOpenHelper` used to get
 * DAO object from to query and update the Setting and State tables.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // The log tag.
    private static final String TAG = DatabaseHelper.class.getName();

    // The database name.
    private static final String DATABASE_NAME = "ask_sense_demo.db";

    // The database version.
    private static final int DATABASE_VERSION = 1;

    // A map acting as a cache of DAO instances.
    private Map<Class, Dao> daoMap = null;

    /**
     * Creates a new instance of this `OrmLiteSqliteOpenHelper`.
     *
     * @param context the context from which this class was instantiated.
     */
    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);

        daoMap = new HashMap<Class, Dao>();
    }

    /**
     * Creates a new database and adding some default values to it.
     *
     * @param db               the database.
     * @param connectionSource the connection source.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {

        Log.d(TAG, "onCreate");

        try {
            for (Class<?> tableClass : DatabaseConfigUtil.CLASSES) {
                Log.d(TAG, "creating a table for class: " + tableClass.getName());
                TableUtils.createTable(connectionSource, tableClass);
            }

            final long time = new Date().getTime();

            // Add default settings.
            this.getDao(Setting.class, String.class).create(new Setting(Setting.ACTIVITY_ENABLED_KEY, String.valueOf(Boolean.FALSE)));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.LOCATION_ENABLED_KEY, String.valueOf(Boolean.FALSE)));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.REACHABILITY_ENABLED_KEY, String.valueOf(Boolean.FALSE)));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.USER_KEY, ""));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.PASSWORD_KEY, ""));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.LOGGED_IN_KEY, String.valueOf(Boolean.FALSE)));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.SAMPLE_RATE_KEY, "-1"));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.SYNC_RATE_KEY, "-2"));
            this.getDao(Setting.class, String.class).create(new Setting(Setting.POLL_SENSE_SECONDS_KEY, "10"));

            // Add default states.
            this.getDao(State.class, String.class).create(new State(State.ACTIVITY_KEY, "-", time));
            this.getDao(State.class, String.class).create(new State(State.LOCATION_KEY, "-", time));
            this.getDao(State.class, String.class).create(new State(State.REACHABILITY_KEY, "-", time));

        } catch (SQLException e) {
            Log.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Called when {@link this#DATABASE_VERSION} is changed.
     *
     * @param db               the database to upgrade.
     * @param connectionSource the connection source to the database.
     * @param oldVersion       the previous database version.
     * @param newVersion       the new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {

        throw new UnsupportedOperationException("onUpgrade(...) not implemented!");
    }

    /**
     * Returns a DAO instance for a certain model class.
     *
     * @param modelClass the model class to create the DAO for.
     * @param idClass    the unique ID of the model.
     * @param <M>        the generic type of the model.
     * @param <I>        the generic type of the id.
     * @return a DAO instance for a certain model class.
     */
    @SuppressWarnings("unchecked")
    public <M, I> Dao<M, I> getDao(Class<M> modelClass, Class<I> idClass) {

        Log.d(TAG, "retrieving dao: Dao<" + modelClass + ", " + idClass + ">");

        Dao<M, I> dao = daoMap.get(modelClass);

        if (dao == null) {

            try {
                // Create a new DAO.
                dao = getDao(modelClass);

                // Put the DAO in the cached map.
                daoMap.put(modelClass, dao);

            } catch (SQLException e) {
                Log.e(TAG, "Could not create a DAO for: " + modelClass, e);
            }
        }

        return dao;
    }

    /**
     * Closes the connection to the database.
     */
    @Override
    public void close() {
        super.close();
    }
}

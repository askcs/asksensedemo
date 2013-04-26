package com.askcs.asksensedemo.database;

import com.askcs.asksensedemo.LoginActivity;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.dao.Dao;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DatabaseHelperTest {

    @Test
    public void readSettingsPresentTest() throws Exception {

        DatabaseHelper helper = new DatabaseHelper(new LoginActivity());

        assertThat(helper, not(equalTo(null)));

        Dao<Setting, String> dao = helper.getDao(Setting.class, String.class);

        // Check if all default settings are present in the database.
        assertThat(dao.queryForId(Setting.LOGGED_IN_KEY).getValue(), is(String.valueOf(Boolean.FALSE)));
        assertThat(dao.queryForId(Setting.USER_KEY).getValue(), is(""));
        assertThat(dao.queryForId(Setting.PASSWORD_KEY).getValue(), is(""));
        assertThat(dao.queryForId(Setting.ACTIVITY_ENABLED_KEY).getValue(), is(String.valueOf(Boolean.FALSE)));
        assertThat(dao.queryForId(Setting.LOCATION_ENABLED_KEY).getValue(), is(String.valueOf(Boolean.FALSE)));
        assertThat(dao.queryForId(Setting.REACHABILITY_ENABLED_KEY).getValue(), is(String.valueOf(Boolean.FALSE)));
        assertThat(dao.queryForId(Setting.POLL_SENSE_SECONDS_KEY).getValue(), is("10"));
        assertThat(dao.queryForId(Setting.SYNC_RATE_KEY).getValue(), is("-2"));
        assertThat(dao.queryForId(Setting.SAMPLE_RATE_KEY).getValue(), is("-1"));

        assertThat(dao.queryForId("non-existing-key"), equalTo(null));
    }

    @Test
    public void readStatePresentTest() throws Exception {

        DatabaseHelper helper = new DatabaseHelper(new LoginActivity());

        assertThat(helper, not(equalTo(null)));

        Dao<State, String> dao = helper.getDao(State.class, String.class);

        // Check if all default settings are present in the database.
        assertThat(dao.queryForId(State.ACTIVITY_KEY).getValue(), is("-"));
        assertThat(dao.queryForId(State.LOCATION_KEY).getValue(), is("-"));
        assertThat(dao.queryForId(State.REACHABILITY_KEY).getValue(), is("-"));
    }

    @Test
    public void createTest() throws Exception {

        DatabaseHelper helper = new DatabaseHelper(new LoginActivity());

        assertThat(helper, not(equalTo(null)));

        Dao<Setting, String> dao = helper.getDao(Setting.class, String.class);

        final String key = "MU";
        final String value = "42";

        dao.create(new Setting(key, value));

        Setting created = dao.queryForId(key);

        assertThat(created, not(equalTo(null)));
        assertThat(created.getValue(), is(value));
    }

    @Test
    public void updateTest() throws Exception {

        DatabaseHelper helper = new DatabaseHelper(new LoginActivity());

        assertThat(helper, not(equalTo(null)));

        Dao<Setting, String> dao = helper.getDao(Setting.class, String.class);

        final String key = "MU";
        final String value = "42";
        final String otherValue = "something else";

        dao.create(new Setting(key, value));

        Setting retrieved = dao.queryForId(key);

        assertThat(retrieved, not(equalTo(null)));
        assertThat(retrieved.getValue(), is(value));

        retrieved.setValue(otherValue);
        dao.update(retrieved);

        Setting otherRetrieved = dao.queryForId(key);

        assertThat(otherRetrieved, not(equalTo(null)));
        assertThat(otherRetrieved.getValue(), is(otherValue));
    }

    @Test
    public void deleteTest() throws Exception {

        DatabaseHelper helper = new DatabaseHelper(new LoginActivity());

        assertThat(helper, not(equalTo(null)));

        Dao<Setting, String> dao = helper.getDao(Setting.class, String.class);

        final String key = "MU";
        final String value = "42";

        dao.create(new Setting(key, value));

        Setting created = dao.queryForId(key);

        assertThat(created, not(equalTo(null)));
        assertThat(created.getValue(), is(value));

        dao.delete(created);

        Setting retrieved = dao.queryForId(key);

        // There is no such Setting anymore, hence equals to 'null'.
        assertThat(retrieved, equalTo(null));
    }
}

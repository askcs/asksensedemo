package com.askcs.asksensedemo;

import android.app.*;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.model.State;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;

import java.util.Timer;
import java.util.TimerTask;
import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.ISenseService;
import nl.sense_os.service.ISenseServiceCallback;
import nl.sense_os.service.constants.SensePrefs;
import org.json.JSONArray;
import org.json.JSONObject;

public class ForegroundService extends Service implements ServiceConnection {

    private static final String TAG = ForegroundService.class.getName();

    private static final int SERVICE_ID = 45167812;
    private NotificationManager notificationManager = null;
    private Timer timer = null;
    private boolean isRunning = false;
    private DatabaseHelper databaseHelper = null;

    private SenseCallback callback = new SenseCallback();
    private SensePlatform sensePlatform = null;

    private class SenseCallback extends ISenseServiceCallback.Stub {
        @Override
        public void onChangeLoginResult(int result) throws RemoteException {

            boolean loginOK = false;
            String message = null;

            switch (result) {
                case 0:
                    Log.v(TAG, "Change login OK");
                    loginOK = true;
                    onLoggedIn();
                    break;
                case -1:
                    message = getString(R.string.no_login_connection);
                    break;
                case -2:
                    message = getString(R.string.no_login_credentials);
                    break;
                default:
                    message = getString(R.string.no_login_other, result);
            }

            if(!loginOK) {

                Log.w(TAG, "could not login: " + message);

                final Dao<Setting, String> settingDao = ForegroundService.this.getHelper().getSettingDao();

                try {
                    Setting loggedInSetting = settingDao.queryForId(Setting.LOGGED_IN_KEY);
                    loggedInSetting.setValue(String.valueOf(Boolean.FALSE));
                    settingDao.update(loggedInSetting);
                }
                catch (Exception e) {
                    Log.e(TAG, "Oops: ", e);
                }

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("message", message);
                getApplication().startActivity(intent);

                stopService(new Intent(ForegroundService.this, ForegroundService.class));
            }
        }

        @Override
        public void onRegisterResult(int result) throws RemoteException {
            // not used
        }

        @Override
        public void statusReport(final int status) {
            // not used
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // There's nothing binding to this service, just return null.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!isRunning) {

            Log.d(TAG, "starting service");

            timer = new Timer();

            sensePlatform = new SensePlatform(this, this);

            try {
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            } catch (Exception e) {
                Log.w(TAG, "something went wrong during initialization of member vars: ", e);
            }

            try {
                Notification notification =  updateNotification();

                super.startForeground(SERVICE_ID, notification);

                isRunning = true;

            } catch (Exception e) {
                Log.w(TAG, "something went wrong while starting on foreground: ", e);
            }
        }

        return START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {

        super.onDestroy();

        isRunning = false;

        if(timer != null) {
            timer.cancel();
        }

        synchronized (this.sensePlatform) {

            if(sensePlatform != null) {

                try {
                    sensePlatform.logout();
                }
                catch (RemoteException e) {
                    Log.e(TAG, "could not logout of Sense: ", e);
                }

                sensePlatform.close();
            }
        }

        Log.d(TAG, "stopping service");
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        try {

            Dao<Setting, String> dao = this.getHelper().getSettingDao();
            Setting userSetting =  dao.queryForId(Setting.USER_KEY);
            Setting passwordSetting =  dao.queryForId(Setting.PASSWORD_KEY);

            // Log in (you only need to do this once, Sense will remember the login).
            // This is an asynchronous call, we get a call to the callback object when the login is
            // complete.
            getSensePlatform().login(userSetting.getValue(), passwordSetting.getValue(), callback);

        } catch (Exception e) {
            Log.e(TAG, "Exception while setting up Sense library.", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        // not used
    }

    private void onLoggedIn() {
        try {
            // start sensing
            ISenseService service = getSensePlatform().getService();
            service.toggleMain(true);
            //service.toggleAmbience(true);
            service.toggleLocation(true);
            service.toggleMotion(true);
            service.togglePhoneState(true);
            //service.toggleDeviceProx(true);
            //service.toggleExternalSensors(true);

            // turn off some specific sensors
            //service.setPrefBool(SensePrefs.Main.Ambience.LIGHT, false);
            //service.setPrefBool(SensePrefs.Main.Ambience.CAMERA_LIGHT, false);
            //service.setPrefBool(SensePrefs.Main.Ambience.PRESSURE, false);

            // turn on specific sensors
            //service.setPrefBool(SensePrefs.Main.Ambience.MIC, true);
            // NOTE: spectrum might be too heavy for the phone or consume too much energy
            //service.setPrefBool(SensePrefs.Main.Ambience.AUDIO_SPECTRUM, true);
            //service.setPrefBool(SensePrefs.Main.Location.GPS, true);
            //service.setPrefBool(SensePrefs.Main.Location.NETWORK, true);
            //service.setPrefBool(SensePrefs.Main.Location.AUTO_GPS, true);

            // set how often to sample
            // 1 := rarely (~every 15 min)
            // 0 := normal (~every 5 min)
            // -1 := often (~every 10 sec)
            // -2 := real time (this setting affects power consumption considerably!)
            service.setPrefString(SensePrefs.Main.SAMPLE_RATE, "-1");

            // set how often to upload
            // 1 := eco mode (buffer data for 30 minutes before bulk uploading)
            // 0 := normal (buffer 5 min)
            // -1 := often (buffer 1 min)
            // -2 := real time (every new data point is uploaded immediately)
            service.setPrefString(SensePrefs.Main.SYNC_RATE, "-2");

            timer.schedule(new TimerTask() {

                    private void check(boolean doCheck, String stateKey) {

                        if(doCheck) {

                            Dao<State, String> stateDao = ForegroundService.this.getHelper().getStateDao();

                            try {
                                int limit = 1;
                                JSONArray data = getSensePlatform().getData(stateKey, false, limit);

                                Log.d(TAG, "--> limit=" + limit + ", data.length=" + data.length());

                                State last = stateDao.queryForId(stateKey);

                                if(data.length() > 0) {

                                    JSONObject obj = (JSONObject)data.get(0);
                                    State state = new State(stateKey, obj.getString("value"), obj.getLong("timestamp"));

                                    Log.d(TAG, stateKey + " :: last state -> " + state);

                                    if(state.equals(last)) {
                                        Log.d(TAG, "no state change between last=[" + last + "] and state=[" + state + "]");
                                    }
                                    else {

                                        String message = String.format("%s -> %s", last.getValue(), state.getValue());

                                        Log.i(TAG, message);

                                        last.setValue(state.getValue());
                                        last.setTimestamp(state.getTimestamp());
                                        stateDao.update(last);

                                        sendNotification(message);
                                    }
                                }
                            } catch(Exception e) {
                                Log.e(TAG, "Oops: ", e);
                            }
                        }
                        else {
                            Log.d(TAG, "skipping " + stateKey);
                        }
                    }

                    @Override
                    public void run() {
                        try {

                            Dao<Setting, String> settingDao = ForegroundService.this.getHelper().getSettingDao();

                            boolean checkActivity = false;
                            boolean checkLocation = false;
                            boolean checkPresence = false;

                            try {
                                Setting activitySetting = settingDao.queryForId(Setting.ACTIVITY_ENABLED_KEY);
                                Setting locationSetting = settingDao.queryForId(Setting.LOCATION_ENABLED_KEY);
                                Setting presenceSetting = settingDao.queryForId(Setting.PRESENCE_ENABLED_KEY);

                                checkActivity = activitySetting.getValue().equals(String.valueOf(Boolean.TRUE));
                                checkLocation = locationSetting.getValue().equals(String.valueOf(Boolean.TRUE));
                                checkPresence = presenceSetting.getValue().equals(String.valueOf(Boolean.TRUE));
                            }
                            catch (SQLException e) {
                                Log.e(TAG, "Oops: ", e);
                            }

                            check(checkActivity, State.ACTIVITY_KEY);
                            check(checkLocation, State.LOCATION_KEY);
                            check(checkPresence, State.PRESENCE_KEY);
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Oops: ", e);
                        }
                    }
                },
                2000L, // delay
                getResources().getInteger(R.integer.poll_sense_seconds) * 1000L // pause
            );

        } catch (Exception e) {
            Log.e(TAG, "there's a problem while starting sense library: ", e);
        }
    }

    protected synchronized SensePlatform getSensePlatform() {
        return this.sensePlatform;
    }

    @SuppressWarnings("deprecation")
    protected Notification updateNotification() {

        if(notificationManager == null) {
            Log.w(TAG, "notificationManager == null");
            return null;
        }

        Resources res = getResources();

        Notification notification = new Notification(
                R.drawable.ask_icon_gray, getString(R.string.app_started),
                System.currentTimeMillis());

        Intent gotoIntent = new Intent(this , MainActivity.class);

        gotoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, gotoIntent, 0);

        String user = "unknown";

        try {
            Dao<Setting, String> dao = getHelper().getSettingDao();
            Setting userSetting = dao.queryForId(Setting.USER_KEY);
            user = userSetting.getValue();
        }
        catch (SQLException e) {
            Log.e(TAG, "Oops: ", e);
        }

        notification.setLatestEventInfo(this, res.getString(R.string.app_name),
                res.getString(R.string.logged_in_as, user), pendingIntent);


        notification.flags |= Notification.FLAG_NO_CLEAR;

        notificationManager.notify(SERVICE_ID, notification);

        return notification;
    }

    @SuppressWarnings("deprecation")
    public void sendNotification(String message) {

        if(notificationManager == null) {
            Log.w(TAG, "notificationManager == null");
        }

        Resources res = getResources();

        Notification notification = new Notification(
                R.drawable.ask_icon_red, "state change",
                System.currentTimeMillis());

        Intent gotoIntent = new Intent(this , MainActivity.class);

        gotoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, gotoIntent, 0);

        notification.setLatestEventInfo(this, res.getString(R.string.app_name),
                message, pendingIntent);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(Long.valueOf(System.currentTimeMillis()).hashCode(), notification);
    }

    public DatabaseHelper getHelper() {

        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }
}

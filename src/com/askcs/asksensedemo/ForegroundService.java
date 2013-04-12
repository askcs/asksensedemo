package com.askcs.asksensedemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import java.sql.SQLException;
import java.util.HashSet;

public class ForegroundService extends Service {

    private static final String TAG = ForegroundService.class.getName();

    private static final int SERVICE_ID = 45167812;
    private NotificationManager notificationManager = null;
    private boolean isRunning = false;
    private DatabaseHelper databaseHelper = null;

    @Override
    public IBinder onBind(Intent intent) {
        // There's nothing binding to this service, just return null.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!isRunning) {

            Log.d(TAG, "starting service");

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

        Log.d(TAG, "stopping service");
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

    public DatabaseHelper getHelper() {

        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }
}

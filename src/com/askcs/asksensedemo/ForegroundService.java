package com.askcs.asksensedemo;

import android.app.*;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.askcs.asksensedemo.database.DatabaseHelper;
import com.askcs.asksensedemo.model.Setting;
import com.askcs.asksensedemo.task.PollSenseTask;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.lang.ref.WeakReference;
import java.sql.SQLException;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import nl.sense_os.platform.SensePlatform;
import nl.sense_os.service.ISenseService;
import nl.sense_os.service.ISenseServiceCallback;
import nl.sense_os.service.constants.SensePrefs;

import static com.askcs.asksensedemo.MessageType.*;

/**
 * A service running in the foreground responsible for handling tasks that
 * poll data from Sense.
 */
public class ForegroundService extends Service implements ServiceConnection {

    // The logger tag.
    private static final String TAG = ForegroundService.class.getName();

    // The ID of this service.
    private static final int SERVICE_ID = 45167812;

    // The notification manager used to send OS-notifications.
    private NotificationManager notificationManager = null;

    // The timer that schedules the Sense poll-task.
    private Timer timer = null;

    // A flag indicating this service is running.
    private boolean isRunning = false;

    // A reference to the ORM-lite DB helper.
    private DatabaseHelper databaseHelper = null;

    // A callback used to receive async updates on.
    private SenseCallback callback = new SenseCallback();

    // The sense platform.
    private SensePlatform sensePlatform = null;

    // The messenger used to bind activities upon itself.
    private Messenger serviceMessenger = null;

    // A set of activities that are bound to this service.
    private Set<Messenger> clients = null;

    // A handler receiving Messages from Activities that are bound to this service.
    private static class ServiceHandler extends Handler {

        private WeakReference<ForegroundService> reference;

        ServiceHandler(ForegroundService service) {
            reference = new WeakReference<ForegroundService>(service);
        }

        /**
         * Handles incoming messages from bound activities.
         *
         * @param message the incoming message.
         */
        @Override
        public void handleMessage(Message message) {

            ForegroundService service = reference.get();

            if (service == null) {
                Log.w(TAG, "service was already destroyed, ignoring message: " + message);
                return;
            }

            switch (message.what) {

                case REGISTER:
                    boolean added = service.clients.add(message.replyTo);
                    Log.d(TAG, "handling REGISTER, added=" + added);
                    break;
                case UNREGISTER:
                    boolean removed = service.clients.remove(message.replyTo);
                    Log.d(TAG, "handling UNREGISTER, removed=" + removed);
                    break;
                case SETTING_CHANGED:
                    Log.d(TAG, "handling SETTING_CHANGED");
                    service.resetSensePollTask();
                    break;
                default:
                    Log.w(TAG, "unknown message type: " + message);
            }
        }
    }

    /**
     * A callback through which Sense sends updates.
     */
    private class SenseCallback extends ISenseServiceCallback.Stub {

        @Override
        public void onChangeLoginResult(int result) throws RemoteException {

            // The LoginActivity assured that username and password are
            // correct, no need to inspect `result`.
            ISenseService service = getSensePlatform().getService();

            service.toggleMain(true);
            service.toggleLocation(true);
            service.toggleMotion(true);
            service.togglePhoneState(true);

            resetSensePollTask();
        }

        @Override
        public void onRegisterResult(int result) throws RemoteException {
            // not used
        }

        @Override
        public void statusReport(int status) {
            // not used
        }
    }

    /**
     * Binds intents to this service.
     *
     * @see super#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind(" + intent + ")");

        if (serviceMessenger == null) {
            Log.w(TAG, "serviceMessenger == null");
            return null;
        }

        return serviceMessenger.getBinder();
    }

    /**
     * Starts the service and creates an instance of a `SensePlatform`.
     *
     * @see super#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Only do this if we're not yet running.
        if (!isRunning) {

            Log.d(TAG, "starting service");

            sensePlatform = new SensePlatform(this, this);
            serviceMessenger = new Messenger(new ServiceHandler(this));
            clients = new HashSet<Messenger>();

            try {
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = initNotification();
                super.startForeground(SERVICE_ID, notification);

                isRunning = true;

            } catch (Exception e) {
                Log.w(TAG, "something went wrong while starting on foreground: ", e);
            }
        }

        return START_STICKY;
    }

    /**
     * Called when this service is destroyed.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        isRunning = false;

        // Cancel the timer holding the task that is polling Sense.
        if (timer != null) {
            timer.cancel();
        }

        SensePlatform platform = this.getSensePlatform();

        // Logout and close the Sense platform, if possible.
        if (platform != null) {
            try {
                platform.logout();
                platform.close();
            } catch (RemoteException e) {
                Log.e(TAG, "could not stop SensePlatform: ", e);
            }
        }

        Log.d(TAG, "stopping service");
    }

    /**
     * Executed when a connection to Sense is available.
     *
     * @param componentName the ComponentName.
     * @param binder        the IBinder.
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        try {

            Dao<Setting, String> dao = this.getHelper().getSettingDao();
            Setting userSetting = dao.queryForId(Setting.USER_KEY);
            Setting passwordSetting = dao.queryForId(Setting.PASSWORD_KEY);

            getSensePlatform().login(userSetting.getValue(), passwordSetting.getValue(), callback);

        } catch (Exception e) {
            Log.e(TAG, "Exception while setting up Sense library.", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        // not used
    }

    // Starts/resets the task that polls Sense for sensor data. Note that this method
    // is called at startup and every time the user changes a setting. Therefore
    // the `Timer` will need to be cancelled every time this method is called.
    private void resetSensePollTask() {

        try {
            ISenseService service = getSensePlatform().getService();
            Dao<Setting, String> dao = this.getHelper().getSettingDao();

            Setting sampleRateSetting = dao.queryForId(Setting.SAMPLE_RATE_KEY);
            Setting syncRateSetting = dao.queryForId(Setting.SYNC_RATE_KEY);
            Setting pollSecondsSetting = dao.queryForId(Setting.POLL_SENSE_SECONDS_KEY);

            service.setPrefString(SensePrefs.Main.SAMPLE_RATE, sampleRateSetting.getValue());
            service.setPrefString(SensePrefs.Main.SYNC_RATE, syncRateSetting.getValue());

            // Check if the timer is already running, if so, cancel it.
            if (timer != null) {
                timer.cancel();
            }

            timer = new Timer();

            final long delay = 2000L;
            final long pause = Integer.valueOf(pollSecondsSetting.getValue()) * 1000L;

            timer.schedule(new PollSenseTask(this), delay, pause);

        } catch (Exception e) {
            Log.e(TAG, "there's a problem while starting sense library: ", e);
        }
    }

    /**
     * Lazily get a ORM-lite flavoured <code>DatabaseHelper</code>. This can
     * be called from the GUI thread, and from the Timer-thread.
     *
     * @return a ORM-lite flavoured <code>DatabaseHelper</code>.
     */
    public synchronized DatabaseHelper getHelper() {

        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
    }

    /**
     * Returns a reference to the <code>SensePlatform</code>. This can
     * be called from the GUI thread, and from the Timer-thread.
     *
     * @return a reference to the <code>SensePlatform</code>.
     */
    public synchronized SensePlatform getSensePlatform() {

        return this.sensePlatform;
    }

    /**
     * Initializes the notification of the running foreground service.
     *
     * @return the Notification for this running foreground service.
     */
    public Notification initNotification() {

        if (notificationManager == null) {
            Log.w(TAG, "notificationManager == null");
            return null;
        }

        String user = "unknown";

        try {
            Dao<Setting, String> dao = getHelper().getSettingDao();
            Setting userSetting = dao.queryForId(Setting.USER_KEY);
            user = userSetting.getValue();
        } catch (SQLException e) {
            Log.e(TAG, "Could not retrieve USER_KEY from local DB: : ", e);
        }

        Notification notification = this.createNotification(
                R.drawable.ask_icon_gray,
                getString(R.string.app_started),
                getString(R.string.logged_in_as, user),
                false);

        notificationManager.notify(SERVICE_ID, notification);

        return notification;
    }

    /**
     * Sends a new a new Notification with a given <code>message</code> and
     * notifying all bound activities of the state change.
     *
     * @param message a new a new Notification
     */
    public void sendNotification(String message) {

        if (notificationManager == null) {
            Log.w(TAG, "notificationManager == null");
        }

        Notification notification = this.createNotification(
                R.drawable.ask_icon_red,
                getString(R.string.state_change),
                message,
                true);

        notificationManager.notify(Long.valueOf(System.currentTimeMillis()).hashCode(), notification);

        // Notify clients of the state change.
        for (Messenger client : this.clients) {

            try {
                client.send(Message.obtain(null, STATE_CHANGED));

            } catch (RemoteException e) {
                Log.w(TAG, "could not send STATE_CHANGED, removing from clients");
                clients.remove(client);
            }
        }
    }

    /**
     * Creates a new Notification.
     *
     * @param icon       the icon the Notification should display.
     * @param title      the title of the Notification.
     * @param message    the message of the Notification.
     * @param autoCancel indicates if the Notification is to be cancelled when pressed.
     * @return a new Notification.
     */
    private Notification createNotification(int icon, String title, String message, boolean autoCancel) {

        if (notificationManager == null) {
            Log.w(TAG, "notificationManager == null");
            return null;
        }

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(autoCancel)
                .setContentIntent(pendingIntent)
                .build();
    }
}

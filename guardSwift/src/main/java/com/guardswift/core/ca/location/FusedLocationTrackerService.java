package com.guardswift.core.ca.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.dagger.InjectingService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.TaskQueryBuilder;
import com.guardswift.ui.notification.LocationNotification;
import com.guardswift.ui.notification.NotificationID;
import com.guardswift.util.datastructure.CircularArrayList;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider;

import static com.guardswift.util.rx.UnsubscribeIfPresent.dispose;


public class FusedLocationTrackerService extends InjectingService {

    private static final String TAG = FusedLocationTrackerService.class.getSimpleName();

    private static int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    public static final int ACTIVITY_HISTORY_SIZE = 5;

    PowerManager.WakeLock wl;

    public FusedLocationTrackerService() {
    }

    public static void start(Context context) {
        Log.e(TAG, "startGPSTrackerService");
        // always rebuild geofence on startup
        context.startService(new Intent(context, FusedLocationTrackerService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, FusedLocationTrackerService.class));

    }

    private Queue<DetectedActivity> fiveLastDetectedActivities = EvictingQueue.create(ACTIVITY_HISTORY_SIZE);
    private Location mLastTaskRadiusLocation;
    private Disposable locationDisposable;
    private Tracker tracker;

    @Inject
    ParseTasksCache tasksCache;
    @Inject
    GuardCache guardCache;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "GPSTrackerService onCreate");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            wl.acquire();
        }

        this.startForeground(NotificationID.LOCATION, LocationNotification.create(this, ""));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.i(TAG, "Starting GPSTrackerService! " + LOCATION_PRIORITY);
        if (hasGooglePlayServices()) {

            tracker = new Tracker();
            requestLocationUpdates();

        }
        return Service.START_STICKY;
    }


    /**
     * Manual restart of service
     * http://stackoverflow.com/questions/24077901/how-to-create-an-always-running-background-service
     */
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Intent restartService = new Intent(getApplicationContext(),
//                this.getClass());
//        restartService.setPackage(getPackageName());
//        PendingIntent restartServicePI = PendingIntent.getService(
//                getApplicationContext(), 1, restartService,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);
//
//    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping GPSTrackerService");

        dispose(locationDisposable);

        wl.release();

        super.onDestroy();
    }


    private boolean hasGooglePlayServices() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }


    private void requestLocationUpdates() {

        dispose(locationDisposable); // in case we are already listening

        LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LOCATION_PRIORITY)
                .setInterval(10000)
                .setFastestInterval(5000);

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new HandleException(TAG, "Missing location permission", new SecurityException("Missing permission"));
            return;
        }

        locationDisposable = locationProvider.getUpdatedLocation(request)
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .onErrorResumeNext(throwable -> {
                    Location location = new android.location.Location("");
                    location.setAccuracy(100); // do not let past filter
                    return Observable.just(location);
                })
                .filter(location -> location.getAccuracy() < 30)
                .subscribe(location -> {
                    if (guardCache.isLoggedIn()) {

                        DetectedActivity currentActivity = ActivityDetectionModule.Recent.getDetectedActivity();
                        fiveLastDetectedActivities.add(currentActivity);

                        Log.d(TAG, "currentActivity: " + currentActivity.getType());
                        Log.d(TAG, " - activityHistory: " + fiveLastDetectedActivities);
                        Log.d(TAG, " - activityHistorySize: " + fiveLastDetectedActivities.size());

                        Location previousLocation = LocationModule.Recent.getLastKnownLocation();

                        inspectContextOfTasksWithinRadius(
                                location,
                                previousLocation != null ? previousLocation : location,
                                currentActivity
                        );

                        LocationModule.Recent.setLastKnownLocation(location);

                        LocationNotification.update(FusedLocationTrackerService.this, location);

                        tracker.appendLocation(getApplicationContext(), location);
                    } else {
                        stopSelf();
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on Fused Location observable", throwable);
                    Crashlytics.logException(throwable);
                    new HandleException(TAG, "Error on Fused Location observable", throwable);
                });
    }

    private List<ParseTask> radiusTasks = Lists.newArrayList();

    private void inspectContextOfTasksWithinRadius(Location currentLocation, Location previousLocation, DetectedActivity currentActivity) {

        boolean updateLocalDataStore = mLastTaskRadiusLocation == null || currentLocation.distanceTo(mLastTaskRadiusLocation) >= 500;

        Log.d(TAG, "Radius tasks: " + radiusTasks.size());

        boolean triggerUIUpdate = false;
        for (ParseTask task : radiusTasks) {
            boolean trigger = task.getContextUpdateStrategy().updateContext(
                    currentLocation,
                    previousLocation,
                    currentActivity,
                    fiveLastDetectedActivities
            );

            // if one of the context updates were truthy then trigger ui update
            triggerUIUpdate = triggerUIUpdate || trigger;
        }

        if (triggerUIUpdate) {
            Log.d(TAG, "postUIUpdate");
            EventBusController.postUIUpdate();
        }

        if (updateLocalDataStore) {
            new TaskQueryBuilder(false)
                    .matchingTaskTypes(Lists.newArrayList(ParseTask.TASK_TYPE_STRING.REGULAR, ParseTask.TASK_TYPE_STRING.RAID, ParseTask.TASK_TYPE_STRING.ALARM))
                    .notMarkedFinished()
                    .within(1, currentLocation).build()
                    .findInBackground().onSuccess(boltsTask -> {

                List<ParseTask> tasks = boltsTask.getResult();

                Log.d(TAG, "Updated tasks: " + tasks.size());

                radiusTasks = tasks;
                mLastTaskRadiusLocation = currentLocation;

                return null;
            });
        }



    }


}

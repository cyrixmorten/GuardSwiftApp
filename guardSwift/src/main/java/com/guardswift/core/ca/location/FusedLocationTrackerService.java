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
import com.google.android.gms.location.LocationRequest;
import com.google.common.collect.Lists;
import com.guardswift.core.ca.geofence.GeofencingModule;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.jobs.oneoff.RebuildGeofencesJob;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.documentation.gps.TrackerData;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.notification.LocationNotification;
import com.guardswift.ui.notification.NotificationID;
import com.guardswift.util.Util;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.List;
import java.util.Set;
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

    private static final int DISTANCE_METERS_FOR_GEOFENCEREBUILD = 1500;
    private static final int DISTANCE_METERS_FOR_UIUPDATE = 20;

    private Disposable locationDisposable;
    private Location mLastUIUpdateLocation;
    private Tracker tracker;

    @Inject
    ParseTasksCache tasksCache;
    @Inject
    GeofencingModule geofencingModule;
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
                .filter(location -> (mLastUIUpdateLocation == null || location.getAccuracy() < 30))
                .subscribe(location -> {
                    if (guardCache.isLoggedIn()) {

                        LocationModule.Recent.setLastKnownLocation(location);

                        rebuildGeofencesIfDistanceThresholdReached(location);
                        inspectDistanceToGeofencedTasks(location);
                        updateUIIfDistanceThresholdReached(location);

                        LocationNotification.update(FusedLocationTrackerService.this, location);

                        uploadLocation(location);
                    } else {
                        stopSelf();
                    }
                }, throwable -> {
                    Log.e(TAG, "Error on Fused Location observable", throwable);
                    Crashlytics.logException(throwable);
                    new HandleException(TAG, "Error on Fused Location observable", throwable);
                });
    }

    private void uploadLocation(Location location) {
        tracker.appendLocation(getApplicationContext(), location);

//        Guard guard = guardCache.getLoggedIn();
//        if (guard != null) {
//
//            TrackerData trackerData = TrackerData.create(location, guardCache.getLoggedIn());
//            guard.setPosition(location);
//
//            ParseObject.saveAllInBackground(Lists.newArrayList(guard, trackerData));
//        }
    }


    private void rebuildGeofencesIfDistanceThresholdReached(Location location) {

        Location lastGeofenceRebuildLocation = RegisterGeofencesIntentService.getLastRebuildLocation();

        if (lastGeofenceRebuildLocation == null || RegisterGeofencesIntentService.isRebuildingGeofence()) {
            return;
        }

        float distance = Util.distanceMeters(lastGeofenceRebuildLocation, location);
        boolean triggerByDistance = distance >= DISTANCE_METERS_FOR_GEOFENCEREBUILD;


        if (triggerByDistance) {
            RebuildGeofencesJob.scheduleJob(false);
        }
    }


    private void updateUIIfDistanceThresholdReached(Location location) {
        float distance = Util.distanceMeters(mLastUIUpdateLocation, location);
        if (mLastUIUpdateLocation == null || distance >= DISTANCE_METERS_FOR_UIUPDATE) {
            mLastUIUpdateLocation = location;

            EventBusController.postUIUpdate(location);

        }
    }

    private void inspectDistanceToGeofencedTasks(Location location) {

        if (RegisterGeofencesIntentService.isRebuildingGeofence()) {
            return;
        }

        Set<ParseTask> geofencedTasks = tasksCache.getAllGeofencedTasks();


        List<String> tasksWithinGeofence = Lists.newArrayList();
        List<String> tasksMovedWithinGeofence = Lists.newArrayList();
        List<String> tasksMovedOutsideGeofence = Lists.newArrayList();

        for (ParseTask task : geofencedTasks) {

            ParseGeoPoint clientPosition = task.getPosition();
            float distance = ParseModule.distanceBetweenMeters(location, clientPosition);
            int radius = task.getGeofenceStrategy().getGeofenceRadiusMeters();

            if (distance <= radius) {
                tasksWithinGeofence.add(task.getObjectId());
            }

            if (tasksCache.isWithinGeofence(task)) {
                if (distance > radius) {
                    tasksMovedOutsideGeofence.add(task.getObjectId());
                }
            } else {
                if (distance < radius) {
                    tasksMovedWithinGeofence.add(task.getObjectId());
                }
            }

        }

        geofencingModule.onWithinGeofences(tasksWithinGeofence.toArray(new String[tasksWithinGeofence.size()]));
        geofencingModule.onEnteredGeofences(tasksMovedWithinGeofence.toArray(new String[tasksMovedWithinGeofence.size()]), true);
        geofencingModule.onExitedGeofences(tasksMovedOutsideGeofence.toArray(new String[tasksMovedOutsideGeofence.size()]), true);


    }


}

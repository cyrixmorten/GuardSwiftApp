package com.guardswift.core.ca.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.common.collect.Lists;
import com.guardswift.core.ca.GeofencingModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.util.Util;
import com.parse.ParseGeoPoint;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FusedLocationTrackerService extends InjectingService {

    private static final String TAG = FusedLocationTrackerService.class.getSimpleName();

    private static int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;


    PowerManager.WakeLock wl;

    public FusedLocationTrackerService() {
    }

    public static void start(Context context) {
        Log.e(TAG, "startGPSTrackerService");
        context.startService(new Intent(context, FusedLocationTrackerService.class));
    }

    public static void start(Context context, int priority) {
        Log.e(TAG, "startGPSTrackerService");
        LOCATION_PRIORITY = priority;
        context.startService(new Intent(context, FusedLocationTrackerService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, FusedLocationTrackerService.class));

    }

    private static final int DISTANCE_METERS_FOR_GEOFENCEREBUILD = 1500;
    private static final int DISTANCE_METERS_FOR_UIUPDATE = 20;

    private Subscription locationSubscription;


    private Location mLastGeofenceRebuildLocation;
    private Location mLastUIUpdateLocation;

    private Tracker tracker;


    @Inject
    GSTasksCache tasksCache;
    @Inject
    GeofencingModule geofencingModule;
    @Inject
    GuardCache guardCache;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "GPSTrackerService onCreate");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        wl.acquire();

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
        mLastGeofenceRebuildLocation = null;
        if (hasGooglePlayServices()) {
            unsubscribeLocationUpdates();
        }
        wl.release();
        super.onDestroy();
    }


    public void unsubscribeLocationUpdates() {
        if (locationSubscription != null && !locationSubscription.isUnsubscribed()) {
            Log.i(TAG, "Unsubscribe location updates");
            try {
                locationSubscription.unsubscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
            locationSubscription = null;
        }
    }


    private boolean hasGooglePlayServices() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }


    private void requestLocationUpdates() {

        Log.i(TAG, "requestLocationUpdates");

        unsubscribeLocationUpdates(); // in case we are already listening

        LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LOCATION_PRIORITY)
                .setInterval(10000)
                .setFastestInterval(5000);

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());

        locationSubscription = locationProvider.getUpdatedLocation(request)
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
//                .doOnError(new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        new HandleException(TAG, "Location error", throwable);
//                        Crashlytics.logException(throwable);
//                    }
//                })
                .onErrorReturn(new Func1<Throwable, Location>() {
                    @Override
                    public Location call(Throwable throwable) {
                        Location location = new android.location.Location("");
                        location.setAccuracy(100); // do not let past filter

                        new HandleException(TAG, "Location error", throwable);

                        return location;
                    }
                })
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        return (mLastUIUpdateLocation == null || location.getAccuracy() < 30);
                    }
                })
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {

                        if (guardCache.isLoggedIn()) {
                            LocationModule.Recent.setLastKnownLocation(location);

                            if (!rebuildGeofencesIfDistanceThresholdReached(location)) {
                                updateUIIfDistanceThresholdReached(location);
                                inspectDistanceToGeofencedTasks(location);
                            }


                            tracker.appendLocation(getApplicationContext(), location);


                        } else {
                            stopSelf();
                        }
                    }
                });
    }


    private boolean rebuildGeofencesIfDistanceThresholdReached(Location location) {

        float distance = Util.distanceMeters(mLastGeofenceRebuildLocation, location);
        boolean triggerByDistance = distance >= DISTANCE_METERS_FOR_GEOFENCEREBUILD;

        if (triggerByDistance) {
            mLastGeofenceRebuildLocation = location;

            RegisterGeofencesIntentService.start(getApplicationContext());

            return true;
        }

        return false;
    }


    private void updateUIIfDistanceThresholdReached(Location location) {
        float distance = Util.distanceMeters(mLastUIUpdateLocation, location);
        if (mLastUIUpdateLocation == null || distance >= DISTANCE_METERS_FOR_UIUPDATE) {
            mLastUIUpdateLocation = location;

            EventBusController.postUIUpdate(location);

        }
    }

    private void inspectDistanceToGeofencedTasks(Location location) {

        Set<GSTask> geofencedTasks = tasksCache.getAllGeofencedTasks();


        List<String> tasksWithinGeofence = Lists.newArrayList();
        List<String> tasksMovedWithinGeofence = Lists.newArrayList();
        List<String> tasksMovedOutsideGeofence = Lists.newArrayList();

        for (GSTask task : geofencedTasks) {

            ParseGeoPoint clientPosition = task.getPosition();
            float distance = ParseModule.distanceBetweenMeters(location, clientPosition);
            int radius = task.getGeofenceStrategy().getGeofenceRadius();

            if (distance <= radius) {
                tasksWithinGeofence.add(task.getGeofenceId());
            }

            if (tasksCache.isWithinGeofence(task)) {
                if (distance > radius) {
                    tasksMovedOutsideGeofence.add(task.getGeofenceId());
                }
            } else {
                if (distance < radius) {
                    tasksMovedWithinGeofence.add(task.getGeofenceId());
                }
            }

        }

        geofencingModule.onWithinGeofences(tasksWithinGeofence.toArray(new String[tasksWithinGeofence.size()]));
        geofencingModule.onEnteredGeofences(tasksMovedWithinGeofence.toArray(new String[tasksMovedWithinGeofence.size()]), true);
        geofencingModule.onExitedGeofences(tasksMovedOutsideGeofence.toArray(new String[tasksMovedOutsideGeofence.size()]), true);


    }


}

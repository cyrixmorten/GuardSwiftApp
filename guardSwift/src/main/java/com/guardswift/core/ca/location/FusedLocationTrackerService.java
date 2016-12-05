package com.guardswift.core.ca.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.common.collect.Lists;
import com.guardswift.dagger.InjectingService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.ca.GeofencingModule;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.GSTasksCache;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.documentation.gps.LocationTracker;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
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

    private LocationTracker locationTracker;


    @Inject
    GSTasksCache tasksCache;
    @Inject
    GeofencingModule geofencingModule;
    @Inject
    GuardCache guardCache;

    @Override
    public void onCreate() {
        Log.d(TAG, "GPSTrackerService onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        Log.i(TAG, "Starting GPSTrackerService! " + LOCATION_PRIORITY);
        if (hasGooglePlayServices()) {

            locationTracker = new LocationTracker();
            requestLocationUpdates();

        }
        return Service.START_STICKY;
    }

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
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (result != ConnectionResult.SUCCESS) {
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
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        String message = "Error on location init: " + throwable.getMessage();
                        Log.e(TAG, message, throwable);
                        Crashlytics.logException(throwable);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Location>() {
                    @Override
                    public Location call(Throwable throwable) {
                        Location location = new android.location.Location("");
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

                            Guard guard = guardCache.getLoggedIn();
                            locationTracker.appendLocation(getApplicationContext(), guard, location);


                        } else {
                            stopSelf();
                        }
                    }
                });
    }


    private boolean rebuildGeofencesIfDistanceThresholdReached(Location location) {
        boolean newTasks = GuardSwiftApplication.getInstance().shouldTriggerNewGeofence();

        float distance = Util.distanceMeters(mLastGeofenceRebuildLocation, location);
        if (mLastGeofenceRebuildLocation == null || distance >= DISTANCE_METERS_FOR_GEOFENCEREBUILD || newTasks) {
            mLastGeofenceRebuildLocation = location;

            RegisterGeofencesIntentService.start(getApplicationContext());

            GuardSwiftApplication.getInstance().triggerNewGeofence(false);

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

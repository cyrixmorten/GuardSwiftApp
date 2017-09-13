package com.guardswift.core.ca.location;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.guardswift.R;
import com.guardswift.core.Constants;
import com.guardswift.core.ca.GeofencingModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.ca.geofence.RegisterGeofencesIntentService;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.dagger.InjectingService;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.cache.data.GuardCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.documentation.gps.Tracker;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.activity.MainActivity;
import com.guardswift.util.Util;
import com.parse.ParseGeoPoint;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
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
        // always rebuild geofence on startup
        context.startService(new Intent(context, FusedLocationTrackerService.class));
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, FusedLocationTrackerService.class));

    }

    private static final int DISTANCE_METERS_FOR_GEOFENCEREBUILD = 1500;
    private static final int DISTANCE_METERS_FOR_UIUPDATE = 20;

    private Subscription locationSubscription;


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
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        wl.acquire();

        this.startForeground(Constants.ACTIVITY_RECOGNITION_NOTIFICATION_ID, createForegroundNotification(""));
    }

    private Notification createForegroundNotification(String contentText) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        return new Notification.Builder(this)
                .setOngoing(true)
                .setContentTitle(getText(R.string.gps_position))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateForegroundNotification(Location location) {
        Notification notification = createForegroundNotification(getString(R.string.latlng, location.getLatitude(), location.getLongitude()));

        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.notify(Constants.FUSED_LOCATION_NOTIFICATION_ID, notification);
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
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }


    private void requestLocationUpdates() {

        Log.i(TAG, "requestLocationUpdates");

        unsubscribeLocationUpdates(); // in case we are already listening

        LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LOCATION_PRIORITY)
                .setInterval(10000)
                .setFastestInterval(5000);

        ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new HandleException(TAG, "Missing location permission", new SecurityException("Missing permission"));
            return;
        }

        locationSubscription = locationProvider.getUpdatedLocation(request)
                .observeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .onErrorResumeNext(new Func1<Throwable, Observable<Location>>() {
                    @Override
                    public Observable<Location> call(Throwable throwable) {
                        Location location = new android.location.Location("");
                        location.setAccuracy(100); // do not let past filter
                        return Observable.just(location);
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

                            rebuildGeofencesIfDistanceThresholdReached(location);
                            inspectDistanceToGeofencedTasks(location);
                            updateUIIfDistanceThresholdReached(location);

                            tracker.appendLocation(getApplicationContext());

                            updateForegroundNotification(location);
                        } else {
                            stopSelf();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Error on Fused Location observable", throwable);
                        Crashlytics.logException(throwable);
                        new HandleException(TAG, "Error on Fused Location observable", throwable);
                    }
                });
    }


    private void rebuildGeofencesIfDistanceThresholdReached(Location location) {

        Location lastGeofenceRebuildLocation = RegisterGeofencesIntentService.getLastRebuildLocation();

        if (lastGeofenceRebuildLocation == null || RegisterGeofencesIntentService.isRebuildingGeofence()) {
            return;
        }

        float distance = Util.distanceMeters(lastGeofenceRebuildLocation, location);
        boolean triggerByDistance = distance >= DISTANCE_METERS_FOR_GEOFENCEREBUILD;


        if (triggerByDistance) {
            RegisterGeofencesIntentService.start(getApplicationContext(), false);
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
            int radius = task.getGeofenceStrategy().getGeofenceRadius();

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

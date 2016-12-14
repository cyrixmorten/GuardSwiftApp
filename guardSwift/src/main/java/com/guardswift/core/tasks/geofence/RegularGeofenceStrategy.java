package com.guardswift.core.tasks.geofence;

import android.content.Context;
import android.location.Location;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;
import rx.functions.Func1;

public class RegularGeofenceStrategy extends BaseGeofenceStrategy {

    private static final String TAG = RegularGeofenceStrategy.class.getSimpleName();


    public RegularGeofenceStrategy(GSTask task) {
        super(task);
    }

    @Override
    public void exitGeofence() {
        super.exitGeofence();

        DetectedActivity activity = ActivityDetectionModule.Recent.getDetectedActivity();
        if (activity.getType() == DetectedActivity.IN_VEHICLE) {
//            Location locationWithSpeed = LocationModule.Recent.getLastKnownLocationWithSpeed();
//            if (locationWithSpeed != null && locationWithSpeed.getSpeed() > 1.4f) {
            task.getAutomationStrategy().automaticDeparture();
//            }
        }

//        Log.e(TAG, "exitGeofence: " + ActivityDetectionModule.getNameFromType(activity.getType()) + " speed: " + locationWithSpeed.getSpeed());
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public int getGeofenceRadius() {
        return 300;
    }

    @Override
    public void queryGeofencedTasks(final int withinKm, final FindCallback<ParseObject> callback) {

        if (LocationModule.Recent.getLastKnownLocation() != null) {
            geofenceQuery(withinKm, LocationModule.Recent.getLastKnownLocation()).findInBackground(callback);
            return;
        }

        final Context context = GuardSwiftApplication.getInstance();

        final ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(context);
        locationProvider.getLastKnownLocation()
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Crashlytics.logException(throwable);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Location>() {
                    @Override
                    public Location call(Throwable throwable) {
                        return LocationModule.Recent.getLastKnownLocation();
                    }
                })
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        if (location == null) {
                            callback.done(new ArrayList<ParseObject>(), null);
                            return;
                        }
                        geofenceQuery(withinKm, location)
                                .findInBackground(callback);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        new HandleException(context, TAG, "getLastKnownLocation", throwable);
                    }
                });
    }

    private ParseQuery<ParseObject> geofenceQuery(int withinKm, Location fromLocation) {
        return new CircuitUnit.QueryBuilder(true)
                .isRunToday()
                .within(withinKm, fromLocation)
                .buildAsParseObject();
//                                .whereNear(CircuitUnit.clientPosition, ParseModule.geoPointFromLocation(location))
    }
}

package com.guardswift.core.tasks.geofence;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.FindCallback;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.functions.Action1;
import rx.functions.Func1;

public class RegularGeofenceStrategy extends BaseGeofenceStrategy {

    private static final String TAG = RegularGeofenceStrategy.class.getSimpleName();

    public static TaskGeofenceStrategy getInstance(GSTask task) {
        return new RegularGeofenceStrategy(task);
    }

    private RegularGeofenceStrategy(GSTask task) {
        super(task);
    }

    @Override
    public void withinGeofence() {
        super.withinGeofence();

        if (!task.isArrived()) {

            Location guardLocation = LocationModule.Recent.getLastKnownLocation();
            ParseGeoPoint clientLocation = task.getPosition();

            float distanceMeters = ParseModule.distanceBetweenMeters(guardLocation, clientLocation);

            if (distanceMeters < task.getRadius()) {
                DetectedActivity lastActivity = ActivityDetectionModule.Recent.getDetectedActivity();
                task.getActivityStrategy().handleActivityInsideGeofence(lastActivity);
            }

        }
    }

    @Override
    public void exitGeofence() {
        super.exitGeofence();

        DetectedActivity activity = ActivityDetectionModule.Recent.getDetectedActivity();
        if (activity.getType() == DetectedActivity.IN_VEHICLE) {
            task.getAutomationStrategy().automaticDeparture();
        }
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

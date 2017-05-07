package com.guardswift.core.tasks.geofence;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
    public void queryGeofencedTasks(final int withinKm, Location fromLocation, final FindCallback<ParseObject> callback) {

        if (fromLocation != null) {
            geofenceQuery(withinKm, fromLocation).findInBackground(callback);
        } else {
            callback.done(null, new ParseException(ParseException.OTHER_CAUSE, "Missing location for geofencing regular tasks"));
        }
    }

    private ParseQuery<ParseObject> geofenceQuery(int withinKm, Location fromLocation) {
        return new CircuitUnit.QueryBuilder(true)
                .isRunToday()
                .within(withinKm, fromLocation)
                .buildAsParseObject();
//                                .whereNear(CircuitUnit.clientPosition, ParseModule.geoPointFromLocation(location))
    }
}

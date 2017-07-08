package com.guardswift.core.tasks.geofence;

import android.location.Location;
import android.util.Log;

import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class AlarmGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = AlarmGeofenceStrategy.class.getSimpleName();

    public static TaskGeofenceStrategy getInstance(GSTask task) {
        return new AlarmGeofenceStrategy(task);
    }

    private AlarmGeofenceStrategy(GSTask task) {
        super(task);
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
    public void withinGeofence() {
        super.withinGeofence();

        float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getClient().getPosition());
        if (distanceToClient < task.getRadius()) {
            task.getAutomationStrategy().automaticArrival();
        }
    }

    @Override
    public void exitGeofence() {
        super.exitGeofence();

        if (!task.isArrived()) {
            return;
        }

        if (task.getAutomationStrategy() != null) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }

    @Override
    public void queryGeofencedTasks(final int withinKm, Location fromLocation, final FindCallback<ParseObject> callback) {

        Log.d(TAG, "queryGeofencedTasks");

        if (fromLocation != null) {
            geofenceQuery(withinKm, fromLocation).findInBackground(callback);
        } else {
            callback.done(null, new ParseException(ParseException.OTHER_CAUSE, "Missing location for geofencing alarms"));
        }

    }

    private ParseQuery<ParseObject> geofenceQuery(int withinKm, Location fromLocation) {
        return new ParseTask().getQueryBuilder(true)
                .whereStatus(ParseTask.STATUS.PENDING, ParseTask.STATUS.ACCEPTED, ParseTask.STATUS.ARRIVED)
                .within(withinKm, fromLocation)
                .buildAsParseObject();
    }
}

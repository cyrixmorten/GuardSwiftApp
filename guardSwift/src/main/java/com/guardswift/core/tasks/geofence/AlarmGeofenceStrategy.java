package com.guardswift.core.tasks.geofence;

import android.location.Location;

import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.query.AlarmTaskQueryBuilder;
import com.parse.FindCallback;
import com.parse.ParseException;

public class AlarmGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = AlarmGeofenceStrategy.class.getSimpleName();

    public static TaskGeofenceStrategy getInstance(ParseTask task) {
        return new AlarmGeofenceStrategy(task);
    }

    private AlarmGeofenceStrategy(ParseTask task) {
        super(task);
    }

    @Override
    public String getName() {
        return TAG;
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
    public void queryGeofencedTasks(final int withinKm, Location fromLocation, final FindCallback<ParseTask> callback) {
        if (fromLocation != null) {
            new AlarmTaskQueryBuilder(false)
                    .whereStatus(ParseTask.STATUS.PENDING, ParseTask.STATUS.ACCEPTED, ParseTask.STATUS.ARRIVED)
                    .within(withinKm, fromLocation)
                    .build()
                    .findInBackground(callback);
        } else {
            callback.done(null, new ParseException(ParseException.OTHER_CAUSE, "Missing location for geofencing alarms"));
        }

    }

}

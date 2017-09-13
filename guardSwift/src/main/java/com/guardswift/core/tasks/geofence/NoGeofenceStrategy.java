package com.guardswift.core.tasks.geofence;


import com.guardswift.persistence.parse.execution.task.ParseTask;

public class NoGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = NoGeofenceStrategy.class.getSimpleName();

    public static TaskGeofenceStrategy getInstance(ParseTask task) {return new NoGeofenceStrategy(task); }

    private NoGeofenceStrategy(ParseTask task) {
        super(task);
    }

    @Override
    public String getName() {
        return TAG;
    }

}

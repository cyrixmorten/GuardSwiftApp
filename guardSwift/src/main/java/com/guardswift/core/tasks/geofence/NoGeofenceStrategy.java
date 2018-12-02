package com.guardswift.core.tasks.geofence;


import android.location.Location;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.FindCallback;

import java.util.ArrayList;

public class NoGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = NoGeofenceStrategy.class.getSimpleName();

    public static TaskGeofenceStrategy getInstance(ParseTask task) {return new NoGeofenceStrategy(task); }

    private NoGeofenceStrategy(ParseTask task) {
        super(task);
    }

    @Override
    public void queryGeofencedTasks(int withinKm, Location fromLocation, FindCallback<ParseTask> callback) {
        callback.done(new ArrayList<>(), null);
    }

    @Override
    public String getName() {
        return TAG;
    }

}

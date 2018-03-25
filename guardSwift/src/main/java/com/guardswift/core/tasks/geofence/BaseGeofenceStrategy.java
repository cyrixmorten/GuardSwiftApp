package com.guardswift.core.tasks.geofence;

import android.location.Location;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

abstract class BaseGeofenceStrategy implements TaskGeofenceStrategy {

    protected final ParseTask task;

    BaseGeofenceStrategy(ParseTask task) {
        this.task = task;
    }

    @Override
    public abstract void queryGeofencedTasks(int withinKm, Location fromLocation, FindCallback<ParseTask> callback);

    @Override
    public int getGeofenceRadiusMeters() {
        return 300;
    }

    @Override
    public void withinGeofence() {}

    @Override
    public void enterGeofence() {}

    @Override
    public void exitGeofence() {}

    /**
     * Wraps the {@link TaskGeofenceStrategy} queryGeofencedTasks implementation into a Bolts.ParseTask
     *
     * @param radiusKm
     * @return
     */
    @Override
    public Task<List<ParseTask>> queryGeofencedTasks(int radiusKm, Location fromLocation) {
        final TaskCompletionSource<List<ParseTask>> successful = new TaskCompletionSource<>();
        queryGeofencedTasks(radiusKm, fromLocation, new FindCallback<ParseTask>() {
            @Override
            public void done(List<ParseTask> objects, ParseException e) {
                if (e != null) {
                    successful.setError(e);
                    return;
                }
                successful.setResult(objects);
            }
        });
        return successful.getTask();
    }
}

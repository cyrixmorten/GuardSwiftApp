package com.guardswift.core.tasks.geofence;

import android.location.Location;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;
import bolts.TaskCompletionSource;

abstract class BaseGeofenceStrategy implements TaskGeofenceStrategy {

    protected final ParseTask task;

    protected BaseGeofenceStrategy(ParseTask task) {
        this.task = task;
    }

    @Override
    public int getGeofenceRadius() {
        return 0;
    }

    /**
     * Unless implemented by concrete class, no tasks are scheduled for geofencing
     *
     * @param withinKm
     * @param callback
     */
    @Override
    public void queryGeofencedTasks(int withinKm, Location fromLocation, FindCallback<ParseTask> callback) {
        callback.done(new ArrayList<ParseTask>(), null);
    }
    /*
     * Called when at the border of the geofence
     * client_radius > dist_client < (client_radius + 200)
     */
    @Override
    public void withinGeofence() {

    }

    @Override
    public void enterGeofence() {
//        boolean moved = tasksCache.moveWithinGeofence(task);

//        ParseTask.EVENT_TYPE event_type = (usingGPS) ? ParseTask.EVENT_TYPE.GEOFENCE_ENTER_GPS : ParseTask.EVENT_TYPE.GEOFENCE_ENTER;
//        new EventLog.Builder(context)
//                .taskPointer(task, event_type)
//                .event(context.getString(R.string.event_geofence_enter))
//                .automatic(true)
//                .eventCode(EventLog.EventCodes.AUTOMATIC_GEOFENCE_ENTER).saveAsync();
    }

    @Override
    public void exitGeofence() {
//        boolean moved = tasksCache.moveOutsideGeofence(task);
//        ParseTask.EVENT_TYPE event_type = (usingGPS) ? ParseTask.EVENT_TYPE.GEOFENCE_EXIT_GPS : ParseTask.EVENT_TYPE.GEOFENCE_EXIT;
//        new EventLog.Builder(context)
//                .taskPointer(task, event_type)
//                .event(context.getString(R.string.event_geofence_exit))
//                .automatic(true)
//                .eventCode(EventLog.EventCodes.AUTOMATIC_GEOFENCE_EXIT).saveAsync();
    }

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

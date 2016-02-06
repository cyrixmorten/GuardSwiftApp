package com.guardswift.core.tasks.geofence;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import bolts.Task;

/**
 * Created by cyrix on 6/28/15.
 */
abstract class BaseGeofenceStrategy<T extends BaseTask> implements TaskGeofenceStrategy<T> {

    protected final GSTask task;

    public BaseGeofenceStrategy(GSTask task) {
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
    public void queryGeofencedTasks(int withinKm, FindCallback<ParseObject> callback) {
        callback.done(new ArrayList<ParseObject>(), null);
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

//        GSTask.EVENT_TYPE event_type = (usingGPS) ? GSTask.EVENT_TYPE.GEOFENCE_ENTER_GPS : GSTask.EVENT_TYPE.GEOFENCE_ENTER;
//        new EventLog.Builder(context)
//                .taskPointer(task, event_type)
//                .event(context.getString(R.string.event_geofence_enter))
//                .automatic(true)
//                .eventCode(EventLog.EventCodes.AUTOMATIC_GEOFENCE_ENTER).saveAsync();
    }

    @Override
    public void exitGeofence() {
//        boolean moved = tasksCache.moveOutsideGeofence(task);
//        GSTask.EVENT_TYPE event_type = (usingGPS) ? GSTask.EVENT_TYPE.GEOFENCE_EXIT_GPS : GSTask.EVENT_TYPE.GEOFENCE_EXIT;
//        new EventLog.Builder(context)
//                .taskPointer(task, event_type)
//                .event(context.getString(R.string.event_geofence_exit))
//                .automatic(true)
//                .eventCode(EventLog.EventCodes.AUTOMATIC_GEOFENCE_EXIT).saveAsync();
    }

    /**
     * Wraps the {@link TaskGeofenceStrategy} queryGeofencedTasks implementation into a Bolts.Task
     *
     * @param radiusKm
     * @return
     */
    @Override
    public Task<List<ParseObject>> queryGeofencedTasks(int radiusKm) {
        final Task<List<ParseObject>>.TaskCompletionSource successful = Task.create();
        queryGeofencedTasks(radiusKm, new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
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

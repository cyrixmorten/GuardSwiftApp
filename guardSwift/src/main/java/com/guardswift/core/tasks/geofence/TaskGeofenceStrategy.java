package com.guardswift.core.tasks.geofence;

import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.FindCallback;
import com.parse.ParseObject;

import java.util.List;

import bolts.Task;

/**
 * Created by cyrix on 3/12/15.
 */
public interface TaskGeofenceStrategy {

    String getName();

    void withinGeofence();
    void enterGeofence();
    void exitGeofence();

    int getGeofenceRadius();

    Task<List<ParseObject>> queryGeofencedTasks(int withinKm);
    void queryGeofencedTasks(int withinKm, FindCallback<ParseObject> callback);

}

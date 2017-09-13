package com.guardswift.core.tasks.geofence;

import android.location.Location;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.FindCallback;

import java.util.List;

import bolts.Task;

public interface TaskGeofenceStrategy {

    String getName();

    void withinGeofence();
    void enterGeofence();
    void exitGeofence();

    int getGeofenceRadius();

    // TODO inline query in GeofencingModule
    Task<List<ParseTask>> queryGeofencedTasks(int withinKm, Location fromLocation);
    void queryGeofencedTasks(int withinKm, Location fromLocation, FindCallback<ParseTask> callback);

}

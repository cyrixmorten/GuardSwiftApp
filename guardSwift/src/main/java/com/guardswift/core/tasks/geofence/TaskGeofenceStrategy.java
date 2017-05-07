package com.guardswift.core.tasks.geofence;

import android.location.Location;

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

    Task<List<ParseObject>> queryGeofencedTasks(int withinKm, Location fromLocation);
    void queryGeofencedTasks(int withinKm, Location fromLocation, FindCallback<ParseObject> callback);

}

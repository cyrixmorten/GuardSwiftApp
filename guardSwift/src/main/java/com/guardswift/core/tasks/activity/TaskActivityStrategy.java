package com.guardswift.core.tasks.activity;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.persistence.parse.execution.BaseTask;

/**
 * Created by cyrix on 6/7/15.
 */
public interface TaskActivityStrategy<T extends BaseTask> {


    /*
     * Called on any task where the device is registered to be inside the geofence
     */
    void handleActivityInsideGeofence(DetectedActivity activity);

    /*
     * Called on any task where the device is registered to be outside the geofence
     *
     * Note: only tasks where device has been inside geofence is taken into account
     */
    void handleActivityOutsideGeofence(DetectedActivity activity);

}

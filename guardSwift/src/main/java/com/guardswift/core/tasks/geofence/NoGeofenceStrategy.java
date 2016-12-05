package com.guardswift.core.tasks.geofence;

import com.guardswift.persistence.parse.execution.GSTask;

/**
 * Created by cyrix on 6/7/15.
 */
public class NoGeofenceStrategy extends BaseGeofenceStrategy {


    private static final String TAG = NoGeofenceStrategy.class.getSimpleName();

    public NoGeofenceStrategy(GSTask task) {
        super(task);
    }

    @Override
    public String getName() {
        return TAG;
    }

}

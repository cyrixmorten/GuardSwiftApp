package com.guardswift.core.tasks.activity;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.persistence.parse.execution.BaseTask;

/**
 * Created by cyrix on 6/7/15.
 */
public class DoNothingStrategy<T extends BaseTask> implements TaskActivityStrategy<T> {


    public DoNothingStrategy() {
    }

    @Override
    public void handleActivityInsideGeofence(DetectedActivity activity) {
    }

    @Override
    public void handleActivityOutsideGeofence(DetectedActivity activity) {
    }
}

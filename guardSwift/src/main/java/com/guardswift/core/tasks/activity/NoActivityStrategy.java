package com.guardswift.core.tasks.activity;

import com.google.android.gms.location.DetectedActivity;

/**
 * Created by cyrix on 6/7/15.
 */
public class NoActivityStrategy implements TaskActivityStrategy {


    public static NoActivityStrategy getInstance() {
        return  new NoActivityStrategy();
    }

    private NoActivityStrategy() {}

    @Override
    public void handleActivityInsideGeofence(DetectedActivity activity) {
    }

    @Override
    public void handleActivityOutsideGeofence(DetectedActivity activity) {
    }
}

package com.guardswift.core.tasks.context;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;

import java.util.Queue;

public interface ContextUpdateStrategy {
    boolean updateContext(Location currentLocation, Location previousLocation,
                          DetectedActivity currentActivity,
                          Queue<DetectedActivity> activityHistory);
}

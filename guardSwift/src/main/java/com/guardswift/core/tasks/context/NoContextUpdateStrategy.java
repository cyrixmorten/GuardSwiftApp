package com.guardswift.core.tasks.context;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Queue;

public class NoContextUpdateStrategy implements ContextUpdateStrategy {


    private static final String TAG = NoContextUpdateStrategy.class.getSimpleName();

    public static ContextUpdateStrategy getInstance(ParseTask task) {return new NoContextUpdateStrategy(); }

    private NoContextUpdateStrategy() { }


    @Override
    public boolean updateContext(Location currentLocation, Location previousLocation, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory) {
        return false;
    }
}

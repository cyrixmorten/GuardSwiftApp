package com.guardswift.core.tasks.context;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Queue;

abstract class BaseContextStrategy implements ContextUpdateStrategy {

    protected final ParseTask task;
    protected final TaskController controller;

    BaseContextStrategy(ParseTask task) {
        this.task = task;
        this.controller = task.getController();
    }


    @Override
    public boolean updateContext(Location currentLocation, Location previousLocation, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory) {
        if (!task.isFinished()) {
            float distanceToClient = ParseModule.distanceBetweenMeters(currentLocation, task.getClient().getPosition());

            if (task.isPending()) {
                return pendingTaskUpdate(currentLocation, previousLocation, currentActivity, activityHistory, distanceToClient);
            }

            if (task.isArrived()) {
                return arrivedTaskUpdate(currentLocation, previousLocation, currentActivity, activityHistory, distanceToClient);
            }
        }

        return false;
    }


    abstract boolean pendingTaskUpdate(Location current, Location previous, DetectedActivity activity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters);
    abstract boolean arrivedTaskUpdate(Location current, Location previous, DetectedActivity activity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters);
}

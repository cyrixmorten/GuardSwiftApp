package com.guardswift.core.tasks.context;

import android.location.Location;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Queue;

public class AlarmContextStrategy extends BaseContextStrategy {


    private static final String TAG = AlarmContextStrategy.class.getSimpleName();

    public static ContextUpdateStrategy getInstance(ParseTask task) {
        return new AlarmContextStrategy(task);
    }

    private AlarmContextStrategy(ParseTask task) {
        super(task);
    }

    @Override
    boolean pendingTaskUpdate(Location current, Location previous, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters) {
        boolean triggerArrival = distanceToClientMeters < task.getRadius();

        if (triggerArrival) {
            controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
        }

        return triggerArrival;
    }

    @Override
    boolean arrivedTaskUpdate(Location current, Location previous,  DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters) {

        boolean isWellOutsideRadius = distanceToClientMeters > task.getRadius() * 2;

        if (isWellOutsideRadius) {
            controller.performAutomaticAction(TaskController.ACTION.FINISH, task);
        }

        return isWellOutsideRadius;
    }

}

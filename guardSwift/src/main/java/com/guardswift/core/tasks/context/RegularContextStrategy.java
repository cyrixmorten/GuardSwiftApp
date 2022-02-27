package com.guardswift.core.tasks.context;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.core.ca.location.FusedLocationTrackerService;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Queue;

public class RegularContextStrategy extends BaseContextStrategy {

    private static final String TAG = RegularContextStrategy.class.getSimpleName();

    public static ContextUpdateStrategy getInstance(ParseTask task) {
        return new RegularContextStrategy(task);
    }

    private RegularContextStrategy(ParseTask task) {
        super(task);
    }

    @Override
    boolean pendingTaskUpdate(Location current, Location previous, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters) {
        Log.d(TAG, "pendingTaskUpdate " + task.getClientName());
        
        if (task.disableAutomaticArrival()) {
            Log.d(TAG, "task.disableAutomaticArrival()" + task.disableAutomaticArrival());
            return false;
        }

        if (distanceToClientMeters < task.getRadius()) {

            int inactiveCount = 0;
            for (DetectedActivity activity: activityHistory) {
                int type = activity.getType();

                if (ActivityDetectionModule.isInactive(type) ||
                    ActivityDetectionModule.isUnknown(type)) {

                    inactiveCount++;

                }
            }
            
            boolean onFootOrInactive = ActivityDetectionModule.isOnFoot(currentActivity.getType()) ||
                            inactiveCount == FusedLocationTrackerService.ACTIVITY_HISTORY_SIZE;

            boolean triggerArrival = onFootOrInactive &&
                    task.isWithinScheduledTimeRelaxed() &&
                    task.matchesSelectedTaskGroupStarted();

            Log.d(TAG, task.getClientName() + ": " + triggerArrival + " inactiveCount: " + inactiveCount);

            if (triggerArrival) {
                controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
            }

            return triggerArrival;
        }

        return false;
    }

    @Override
    boolean arrivedTaskUpdate(Location current, Location previous, DetectedActivity currentActivity, Queue<DetectedActivity> activityHistory, float distanceToClientMeters) {

        boolean isWellOutsideRadius = distanceToClientMeters > (task.getRadius() * 4);
        boolean inVehicle = currentActivity.getType() == DetectedActivity.IN_VEHICLE;
        boolean triggerDeparture = isWellOutsideRadius || inVehicle;

        if (triggerDeparture) {
            task.getController().performAutomaticAction(TaskController.ACTION.PENDING, task);
        }

        return triggerDeparture;
    }


}

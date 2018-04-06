package com.guardswift.core.tasks.activity;

import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Maps;
import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Map;

public class ArriveWhenNotInVehicleStrategy implements TaskActivityStrategy {

    interface TriggerArrival {
        void trigger();
    }

    private static final String TAG = ArriveWhenNotInVehicleStrategy.class.getSimpleName();

    private final ParseTask task;


    // Saved in a static map because there can be multiple instances of this strategy per task
    private static Map<String, ArriveOnStillTimer> arriveOnStillTimerMap = Maps.newConcurrentMap();

    private ArriveOnStillTimer arriveOnStillTimer;


    public static TaskActivityStrategy getInstance(ParseTask task) {
        return new ArriveWhenNotInVehicleStrategy(task);
    }

    private ArriveWhenNotInVehicleStrategy(final ParseTask task) {
        this.task = task;

        if (task.getObjectId() != null) {
            ArriveOnStillTimer arriveOnStillTimer = arriveOnStillTimerMap.get(task.getObjectId());

            // Create on timer per task
            if (arriveOnStillTimer == null) {
                arriveOnStillTimer = new ArriveOnStillTimer(task, this::arriveIfNear);

                arriveOnStillTimerMap.put(task.getObjectId(), arriveOnStillTimer);
            }

            this.arriveOnStillTimer = arriveOnStillTimer;
        }
    }

    private void arriveIfNear() {
        float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getPosition());

        Log.d(TAG, "distanceToClient: " + distanceToClient);

        if (distanceToClient < task.getRadius()) {
            task.getAutomationStrategy().automaticArrival();
        }
    }

    @Override
    public void handleActivityInsideGeofence(DetectedActivity activity) {
        if (activity == null || task.isArrived()) {
            return;
        }

        int activityType = activity.getType();

        if (activityType == DetectedActivity.STILL || activityType == DetectedActivity.TILTING) {
            // Might have arrived and got out of vehicle, lets wait a minute and see
            if (!arriveOnStillTimer.running()) {
                arriveOnStillTimer.start();
            }
        } else if (activityType == DetectedActivity.ON_FOOT) {
            // Pretty sure the guard is out of the vehicle, no need to wait to check
            arriveOnStillTimer.stop();
            arriveIfNear();
        } else if (activityType == DetectedActivity.IN_VEHICLE) {
            // Never mind, driving by
            arriveOnStillTimer.stop();

        }

    }


    @Override
    public void handleActivityOutsideGeofence(DetectedActivity activity) {
        if (task.isAborted()) {
            return;
        }

        if (activity.getType() == DetectedActivity.IN_VEHICLE && activity.getConfidence() == 100) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }
}

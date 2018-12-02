package com.guardswift.core.tasks.activity;

import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Maps;
import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.TriggerTimer;
import com.guardswift.persistence.parse.execution.task.ParseTask;

import java.util.Map;

public class ArriveWhenNotInVehicleStrategy implements TaskActivityStrategy {


    private static final String TAG = ArriveWhenNotInVehicleStrategy.class.getSimpleName();

    private final ParseTask task;


    // Saved in a static map because there can be multiple instances of this strategy per task
    private static Map<String, TriggerTimer> arriveOnStillTimerMap = Maps.newConcurrentMap();

    private TriggerTimer triggerTimer;


    public static TaskActivityStrategy getInstance(ParseTask task) {
        return new ArriveWhenNotInVehicleStrategy(task);
    }

    private ArriveWhenNotInVehicleStrategy(final ParseTask task) {
        this.task = task;

        if (task.getObjectId() != null) {
            TriggerTimer triggerTimer = arriveOnStillTimerMap.get(task.getObjectId());

            // Create one timer per task
            if (triggerTimer == null) {
                triggerTimer = new TriggerTimer(this::arriveIfNear, 60);

                arriveOnStillTimerMap.put(task.getObjectId(), triggerTimer);
            }

            this.triggerTimer = triggerTimer;
        }
    }

    private void arriveIfNear() {
        float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getPosition());

        Log.d(TAG, "arriveIfNear: " + distanceToClient + " < " + task.getRadius() + " - " + task.getClientName());

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

        Log.d(TAG, "handleActivityInsideGeofence: " + activityType + " - " + task.getClientName());

        if (activityType == DetectedActivity.STILL || activityType == DetectedActivity.TILTING) {

            Log.d(TAG, "triggerTimer.running: " + triggerTimer.running());

            // Might have arrived and got out of vehicle, lets wait a minute and see
            if (!triggerTimer.running()) {
                triggerTimer.start();
            }
        } else if (activityType == DetectedActivity.ON_FOOT) {
            // Pretty sure the guard is out of the vehicle, no need to wait to check
            triggerTimer.stop();
            arriveIfNear();
        } else if (activityType == DetectedActivity.IN_VEHICLE) {
            // Never mind, driving by
            triggerTimer.stop();

        }

    }


    @Override
    public void handleActivityOutsideGeofence(DetectedActivity activity) {
        if (task.isAborted()) {
            return;
        }

        if (activity.getType() == DetectedActivity.IN_VEHICLE && activity.getConfidence() > 80) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }
}

package com.guardswift.core.tasks.activity;

import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Maps;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
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


    private static Map<String, ArriveOnStillTimer> arriveOnStillTimerMap = Maps.newConcurrentMap();
    private ArriveOnStillTimer arriveOnStillTimer;



    public static TaskActivityStrategy getInstance(ParseTask task) {
        return new ArriveWhenNotInVehicleStrategy(task);
    }

    private ArriveWhenNotInVehicleStrategy(final ParseTask task) {
        this.task = task;

        if (task.getObjectId() != null) {
            ArriveOnStillTimer arriveOnStillTimer = arriveOnStillTimerMap.get(task.getObjectId());
            if (arriveOnStillTimer == null) {
                arriveOnStillTimer = new ArriveOnStillTimer(task, new TriggerArrival() {

                    @Override
                    public void trigger() {
                        arriveIfNear();
                    }
                });

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

        Log.d(TAG, "Activity: " + ActivityDetectionModule.getNameFromType(activity.getType()));

        if (activity.getType() != DetectedActivity.IN_VEHICLE) {
            if (activity.getType() == DetectedActivity.STILL) {
                // We want to ensure that the guard is not just waiting for a red light or crossroads inside vehicle, delaying arrival
                if (!arriveOnStillTimer.running()) {
                    arriveOnStillTimer.start();
                }
                return;
            } else if (activity.getType() == DetectedActivity.ON_FOOT) {
                arriveOnStillTimer.stop();
            }

            arriveIfNear();
        }
    }


    @Override
    public void handleActivityOutsideGeofence(DetectedActivity activity) {
        if (task.isAborted()) {
            return;
        }

        if (activity.getType() == DetectedActivity.IN_VEHICLE) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }
}

package com.guardswift.core.tasks.activity;

import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;

public class ArriveWhenNotInVehicleStrategy implements TaskActivityStrategy {

    interface TriggerArrival {
        void trigger();
    }

    private static final String TAG = ArriveWhenNotInVehicleStrategy.class.getSimpleName();

    private final GSTask task;
    private ArriveOnStillTimer arriveOnStillTimer;

    public ArriveWhenNotInVehicleStrategy(final GSTask task) {
        this.task = task;
        this.arriveOnStillTimer = new ArriveOnStillTimer(task, new TriggerArrival() {

            @Override
            public void trigger() {
                // if it is aborted it is because
                if (!task.isAborted()) {
                    arriveIfNear();
                }
            }
        });
    }

    private void arriveIfNear() {
        float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getClient().getPosition());

        Log.d(TAG, "distanceToClient: " + distanceToClient);

        if (distanceToClient < 75) {
            task.getAutomationStrategy().automaticArrival();
        }
    }

    @Override
    public void handleActivityInsideGeofence(DetectedActivity activity) {
        Log.d(TAG, "handleActivityInsideGeofence");
        Log.d(TAG, "task.isArrived(): " + task.isArrived());
        if (activity == null || task.isArrived()) {
            return;
        }

        Log.d(TAG, "Activity: " + ActivityDetectionModule.getNameFromType(activity.getType()));

        if (activity.getType() != DetectedActivity.IN_VEHICLE) {
            if (activity.getType() == DetectedActivity.STILL || activity.getType() == DetectedActivity.TILTING) {
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

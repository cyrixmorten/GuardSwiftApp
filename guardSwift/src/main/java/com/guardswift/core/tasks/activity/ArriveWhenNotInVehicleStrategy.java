package com.guardswift.core.tasks.activity;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;

public class ArriveWhenNotInVehicleStrategy implements TaskActivityStrategy {

    private static final String TAG = ArriveWhenNotInVehicleStrategy.class.getSimpleName();

    private final GSTask task;
    private ArriveOnStillTimer arriveOnStillTimer;

    public ArriveWhenNotInVehicleStrategy(GSTask task) {
        this.task = task;
        this.arriveOnStillTimer = new ArriveOnStillTimer(task);
    }

    @Override
    public void handleActivityInsideGeofence(DetectedActivity activity) {
        if (task.isArrived()) {
            return;
        }

        arriveOnStillTimer.stop();

        if (activity.getType() != DetectedActivity.IN_VEHICLE) {
            if (activity.getType() == DetectedActivity.STILL || activity.getType() == DetectedActivity.TILTING) {
                // We want to ensure that the guard is not just waiting for a red light or crossroads inside vehicle, delaying arrival
                arriveOnStillTimer.start();
                return;
            }

            float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getClient().getPosition());
            if (distanceToClient < 75) {
                task.getAutomationStrategy().automaticArrival();
            }
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

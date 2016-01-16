package com.guardswift.core.tasks.activity;

import com.google.android.gms.location.DetectedActivity;
import com.guardswift.persistence.parse.execution.BaseTask;

/**
 * Created by cyrix on 6/7/15.
 */
public class ArriveWhenNotInVehicleStrategy<T extends BaseTask> implements TaskActivityStrategy<T> {

    private static final String TAG = ArriveWhenNotInVehicleStrategy.class.getSimpleName();

    private final T task;
    private ArriveOnStillTimer arriveOnStillTimer;

    public ArriveWhenNotInVehicleStrategy(T task) {
        this.task = task;
        this.arriveOnStillTimer = new ArriveOnStillTimer(task);
    }

    @Override
    public void handleActivityInsideGeofence(DetectedActivity activity) {
        arriveOnStillTimer.stop();

        if (activity.getType() != DetectedActivity.IN_VEHICLE) {
            if (activity.getType() == DetectedActivity.STILL) {
                // We want to ensure that the guard is not just waiting for a red light or crossroads inside vehicle, delaying arrival
                arriveOnStillTimer.start();
                return;
            }

            task.getAutomationStrategy().automaticArrival();
        }
    }


    @Override
    public void handleActivityOutsideGeofence(DetectedActivity activity) {
        if (activity.getType() == DetectedActivity.IN_VEHICLE) {
            task.getAutomationStrategy().automaticDeparture();
        }
    }
}

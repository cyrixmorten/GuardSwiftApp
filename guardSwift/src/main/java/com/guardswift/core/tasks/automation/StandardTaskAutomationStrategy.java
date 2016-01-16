package com.guardswift.core.tasks.automation;

import android.content.Context;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Sounds;

/**
 * Created by cyrix on 6/7/15.
 */
public class StandardTaskAutomationStrategy<T extends BaseTask> implements TaskAutomationStrategy<T> {

    private static final String TAG = StandardTaskAutomationStrategy.class.getSimpleName();

    private final T task;
    private final Context context;

    public StandardTaskAutomationStrategy(T task) {
        this.task = task;
        this.context = GuardSwiftApplication.getInstance();
    }


    @Override
    public void automaticArrival() {
        TaskController<T> controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.ARRIVE, task)) {
            Log.w(TAG, "automaticArrival " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.arrived);
            controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
        }
    }

    @Override
    public void automaticDeparture() {
        TaskController<T> controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.ABORT, task)) {
//            DetectedActivity activity = ActivityDetectionModule.Recent.getDetectedActivity();
//            if (activity.getType() == DetectedActivity.IN_VEHICLE) {
//                Location locationWithSpeed = LocationModule.Recent.getLastKnownLocationWithSpeed();
//                if (locationWithSpeed != null && locationWithSpeed.getSpeed() > 1.4f) {
//                    // TODO Test rules
//                }
//            }
            Log.w(TAG, "automaticDeparture " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.departure);
            controller.performAutomaticAction(TaskController.ACTION.ABORT, task);
        }
    }


}

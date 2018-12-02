package com.guardswift.core.tasks.automation;

import android.content.Context;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Sounds;

public class StandardTaskAutomationStrategy implements TaskAutomationStrategy {

    private static final String TAG = StandardTaskAutomationStrategy.class.getSimpleName();

    private final ParseTask task;

    public static TaskAutomationStrategy getInstance(ParseTask task) {
        return new StandardTaskAutomationStrategy(task);
    }


    private StandardTaskAutomationStrategy(ParseTask task) {
        this.task = task;
    }


    @Override
    public void automaticArrival() {
        Context context = GuardSwiftApplication.getInstance();

        Log.d(TAG, "automaticArrival: " + task.getClientName());

        Log.d(TAG, " - isWithinScheduledTime: " + task.isWithinScheduledTime());
        Log.d(TAG, " - matchesSelectedTaskGroupStarted: " + task.matchesSelectedTaskGroupStarted());

        TaskController controller = task.getController();
        if (task.isWithinScheduledTime() && task.matchesSelectedTaskGroupStarted()) {
            if (controller.canPerformAutomaticAction(TaskController.ACTION.ARRIVE, task)) {
                Log.w(TAG, "automaticArrival " + task.getTaskType() + " " + task.getClientName());
                Sounds.getInstance(context).playNotification(R.raw.arrived);
                controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
            }
        }
    }

    @Override
    public void automaticDeparture() {
        Context context = GuardSwiftApplication.getInstance();
        TaskController controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.RESET, task)) {
            Log.w(TAG, "automaticDeparture " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.departure);
            controller.performAutomaticAction(TaskController.ACTION.RESET, task);
        }
    }


}

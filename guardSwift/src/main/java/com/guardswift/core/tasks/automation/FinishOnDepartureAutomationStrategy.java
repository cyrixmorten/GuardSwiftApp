package com.guardswift.core.tasks.automation;

import android.content.Context;
import android.util.Log;

import com.guardswift.R;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Sounds;

public class FinishOnDepartureAutomationStrategy implements TaskAutomationStrategy {

    private static final String TAG = FinishOnDepartureAutomationStrategy.class.getSimpleName();

    private final GSTask task;
    private final Context context;

    public FinishOnDepartureAutomationStrategy(GSTask task) {
        this.task = task;
        this.context = GuardSwiftApplication.getInstance();
    }


    @Override
    public void automaticArrival() {
        TaskController controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.ARRIVE, task)) {
            Log.w(TAG, "automaticArrival " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.arrived);
            controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
        }
    }

    @Override
    public void automaticDeparture() {
        TaskController controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.FINISH, task)) {
            Log.w(TAG, "automaticDeparture " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.departure);
            controller.performAutomaticAction(TaskController.ACTION.FINISH, task);
        }
    }


}

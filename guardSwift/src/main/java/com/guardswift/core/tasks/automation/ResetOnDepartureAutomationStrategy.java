package com.guardswift.core.tasks.automation;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Maps;
import com.guardswift.R;
import com.guardswift.core.ca.location.LocationModule;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.TriggerTimer;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Sounds;

import java.util.Map;

public class ResetOnDepartureAutomationStrategy implements TaskAutomationStrategy {

    private static final String TAG = ResetOnDepartureAutomationStrategy.class.getSimpleName();

    private final ParseTask task;

    // Saved in a static map because there can be multiple instances of this strategy per task
    private static Map<String, TriggerTimer> departTimerMap = Maps.newConcurrentMap();

    private TriggerTimer triggerTimer;

    public static TaskAutomationStrategy getInstance(ParseTask task) {
        return new ResetOnDepartureAutomationStrategy(task);
    }

    private ResetOnDepartureAutomationStrategy(ParseTask task) {
        this.task = task;

        if (task.getObjectId() != null) {
            TriggerTimer triggerTimer = departTimerMap.get(task.getObjectId());

            // Create one timer per task
            if (triggerTimer == null) {
                triggerTimer = new TriggerTimer(this::resetIfOutside, 30);

                departTimerMap.put(task.getObjectId(), triggerTimer);
            }

            this.triggerTimer = triggerTimer;
        }
    }

    private void resetIfOutside() {
        float distanceToClient = ParseModule.distanceBetweenMeters(LocationModule.Recent.getLastKnownLocation(), task.getPosition());

        if (distanceToClient > task.getRadius()) {
            this.automaticDeparture();
        } else {
            // reset the timer to check again in a short moment
            triggerTimer.start();
        }
    }

    @Override
    public void automaticArrival() {
        if (task.isWithinScheduledTime() && task.matchesSelectedTaskGroupStarted()) {
            Context context = GuardSwiftApplication.getInstance();
            TaskController controller = task.getController();
            if (controller.canPerformAutomaticAction(TaskController.ACTION.ARRIVE, task)) {
                Log.w(TAG, "automaticArrival " + task.getTaskType() + " " + task.getClientName());
                Sounds.getInstance(context).playNotification(R.raw.arrived);
                controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);

                if (!triggerTimer.running()) {
                    triggerTimer.start();
                }
            }
        }

    }

    @Override
    public void automaticDeparture() {
        Log.w(TAG, "automaticDeparture " + task.getTaskType() + " " + task.getClientName());
        TaskController controller = task.getController();
        controller.performAutomaticAction(TaskController.ACTION.RESET, task);

        triggerTimer.stop();
    }



}

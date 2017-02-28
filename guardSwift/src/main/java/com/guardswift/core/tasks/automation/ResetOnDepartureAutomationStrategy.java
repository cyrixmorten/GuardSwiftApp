package com.guardswift.core.tasks.automation;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.guardswift.R;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.guardswift.util.Sounds;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by cyrix on 6/7/15.
 */
public class ResetOnDepartureAutomationStrategy implements TaskAutomationStrategy {

    private static final String TAG = ResetOnDepartureAutomationStrategy.class.getSimpleName();

    private final GSTask task;

    private Timer timer;

    private static final Set<String> lockedTasks = Sets.newConcurrentHashSet();
    private static final Map<String, TaskAutomationStrategy> instances = Maps.newConcurrentMap();

    public static TaskAutomationStrategy getInstance(GSTask task) {
        return new ResetOnDepartureAutomationStrategy(task);
    }

    private ResetOnDepartureAutomationStrategy(GSTask task) {
        this.task = task;
    }

    @Override
    public void automaticArrival() {
        if (lockedTasks.contains(task.getObjectId())) {
            // task is locked for arrivals
            return;
        }
        Context context = GuardSwiftApplication.getInstance();
        TaskController controller = task.getController();
        if (controller.canPerformAutomaticAction(TaskController.ACTION.ARRIVE, task)) {
            Log.w(TAG, "automaticArrival " + task.getTaskType() + " " + task.getClientName());
            Sounds.getInstance(context).playNotification(R.raw.arrived);
            controller.performAutomaticAction(TaskController.ACTION.ARRIVE, task);
            startLockTimer();
        }

    }

    @Override
    public void automaticDeparture() {
        Log.w(TAG, "automaticDeparture " + task.getTaskType() + " " + task.getClientName());
        TaskController controller = task.getController();
        controller.performAutomaticAction(TaskController.ACTION.RESET, task);
    }


    private void startLockTimer() {
        final String objectId = task.getObjectId();

        lockedTasks.add(objectId);

        //set a new TriggerTask
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lockedTasks.remove(objectId);

                ResetOnDepartureAutomationStrategy.this.clearLockTimer();
            }
        }, TimeUnit.MINUTES.toMillis(10));
    }
    private void clearLockTimer() {

        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}

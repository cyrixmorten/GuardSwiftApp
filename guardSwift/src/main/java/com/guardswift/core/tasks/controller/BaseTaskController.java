package com.guardswift.core.tasks.controller;

import android.util.Log;

import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class BaseTaskController implements TaskController {

    private static final String TAG = BaseTaskController.class.getSimpleName();

    abstract ParseTask performAction(ACTION action, ParseTask task, boolean automatic);

    @Override
    public ParseTask performAction(ACTION action, ParseTask task) {
        return performAction(action, task, false);
    }

    private boolean actionAllowedByTaskState(ACTION action, ParseTask task) {
        switch (task.getTaskState()) {
            case PENDING:

                if (action == ACTION.ARRIVE) {
                    Date lastArrivalDate = task.getLastArrivalDate();
                    long timeSinceLastArrivalMs = lastArrivalDate != null ? Math.abs(new Date().getTime() - lastArrivalDate.getTime()) : Long.MAX_VALUE;
                    long timeSinceLastArrivalMinutes = TimeUnit.MINUTES.convert(timeSinceLastArrivalMs, TimeUnit.MILLISECONDS);
                    boolean enoughTimeSinceLastArrival = timeSinceLastArrivalMinutes > task.getMinutesBetweenArrivals();

                    Log.d(TAG, "timeSinceLastArrivalMinutes: " + timeSinceLastArrivalMinutes);

                    return enoughTimeSinceLastArrival;
                }

                return action != ACTION.PENDING;
            case ACCEPTED:
                return action != ACTION.ACCEPT;
            case ARRIVED:
                return action != ACTION.ARRIVE;
            case ABORTED:
                return action != ACTION.ABORT && action != ACTION.PENDING;
            case FINISHED:
                return action != ACTION.FINISH;
        }

        return false;
    }

    @Override
    public boolean canPerformAction(ACTION action, ParseTask task) {

        boolean guardLoggedIn = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().isLoggedIn();

        return guardLoggedIn && actionAllowedByTaskState(action, task);
    }

    
    @Override
    public boolean canPerformAutomaticAction(ACTION action, ParseTask task) {
        return canPerformAction(action, task) && !task.isFinished();
    }

    @Override
    public ParseTask performAutomaticAction(ACTION action, ParseTask task) {
        if (canPerformAutomaticAction(action, task)) {
            ParseTask executedTask = performAction(action, task, true);

            EventBusController.postUIUpdate(task);

            return executedTask;
        }
        return task;
    }

    @Override
    public ParseTask.TASK_STATE translatesToState(ACTION action) {
        switch (action) {
            case ACCEPT:
                return ParseTask.TASK_STATE.ACCEPTED;
            case ARRIVE:
                return ParseTask.TASK_STATE.ARRIVED;
            case ABORT:
                return ParseTask.TASK_STATE.ABORTED;
            case FINISH:
                return ParseTask.TASK_STATE.FINISHED;
        }
        return ParseTask.TASK_STATE.PENDING;
    }
}

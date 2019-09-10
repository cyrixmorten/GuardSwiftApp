package com.guardswift.core.tasks.controller;

import android.util.Log;

import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class BaseTaskController implements TaskController {

    private static final String TAG = BaseTaskController.class.getSimpleName();

    protected abstract ParseTask applyAction(ACTION action, ParseTask task, boolean automatic);

    @Override
    public ParseTask performManualAction(ACTION action, ParseTask task) {
        if (canPerformAction(action, task)) {
            return applyAction(action, task, false);
        }
        return task;
    }

    @Override
    public ParseTask performAutomaticAction(ACTION action, ParseTask task) {
        if (canPerformAction(action, task) && !task.isFinished()) {
            return applyAction(action, task, true);
        }
        return task;
    }

    private boolean actionAllowedByTaskState(ACTION action, ParseTask task) {
        switch (task.getTaskState()) {
            case PENDING:
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

    private boolean canPerformAction(ACTION action, ParseTask task) {

        boolean guardLoggedIn = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().isLoggedIn();

        boolean canAddArrival = true;

        Log.d(TAG, "canPerformAction: " + action);

        if (action == ACTION.ARRIVE) {
            canAddArrival = task.getMinutesSinceLastArrival() > task.getMinutesBetweenArrivals();
            Log.d(TAG, "canAddArrival: " + canAddArrival + " - " + task.getMinutesSinceLastArrival());
        }

        return guardLoggedIn && canAddArrival && actionAllowedByTaskState(action, task);
    }


}

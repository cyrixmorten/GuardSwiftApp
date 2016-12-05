package com.guardswift.core.tasks.controller;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.ui.GuardSwiftApplication;

/**
 * Created by cyrix on 10/28/15.
 */
public abstract class BaseTaskController implements TaskController {

    abstract GSTask performAction(ACTION action, GSTask task, boolean automatic);

    @Override
    public GSTask performAction(ACTION action, GSTask task) {
        return performAction(action, task, false);
    }

    private boolean actionAllowedByTaskState(ACTION action, GSTask task) {
        switch (task.getTaskState()) {
            case PENDING:
                return action != ACTION.RESET && action != ACTION.ABORT;
            case ACCEPTED:
                return action != ACTION.ACCEPT;
            case ARRIVED:
                return action != ACTION.ARRIVE;
            case ABORTED:
                return action != ACTION.ABORT;
            case FINISHED:
                return action != ACTION.FINISH;
        }

        return false;
    }

    @Override
    public boolean canPerformAction(ACTION action, GSTask task) {

        boolean guardLoggedIn = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().isLoggedIn();

        return guardLoggedIn && actionAllowedByTaskState(action, task);
    }

    
    @Override
    public boolean canPerformAutomaticAction(ACTION action, GSTask task) {
        return canPerformAction(action, task) && !task.isFinished();
    }

    @Override
    public GSTask performAutomaticAction(ACTION action, GSTask task) {
        if (canPerformAutomaticAction(action, task)) {
            return performAction(action, task, true);
        }
        return task;
    }

    @Override
    public GSTask.TASK_STATE translatesToState(ACTION action) {
        switch (action) {
            case ACCEPT:
                return GSTask.TASK_STATE.ACCEPTED;
            case ARRIVE:
                return GSTask.TASK_STATE.ARRIVED;
            case ABORT:
                return GSTask.TASK_STATE.ABORTED;
            case FINISH:
                return GSTask.TASK_STATE.FINISHED;
        }
        return GSTask.TASK_STATE.PENDING;
    }
}

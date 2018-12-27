package com.guardswift.core.tasks.controller;

import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;

public abstract class BaseTaskController implements TaskController {

    abstract ParseTask performAction(ACTION action, ParseTask task, boolean automatic);

    @Override
    public ParseTask performAction(ACTION action, ParseTask task) {
        return performAction(action, task, false);
    }

    private boolean actionAllowedByTaskState(ACTION action, ParseTask task) {
        switch (task.getTaskState()) {
            case PENDING:
                return action != ACTION.ABORT && action != ACTION.PENDING;
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

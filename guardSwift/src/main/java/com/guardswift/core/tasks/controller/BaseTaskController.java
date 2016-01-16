package com.guardswift.core.tasks.controller;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;

/**
 * Created by cyrix on 10/28/15.
 */
public abstract class BaseTaskController<T extends BaseTask> implements TaskController<T> {

    abstract T performAction(ACTION action, T task, boolean automatic);

    @Override
    public T performAction(ACTION action, T task) {
        return performAction(action, task, false);
    }

    @Override
    public boolean canPerformAction(ACTION action, T task) {
        switch (task.getTaskState()) {
            case PENDING:
                return action != ACTION.RESET && action != ACTION.ABORT;
            case ACCEPTED:
                return action != ACTION.ACCEPT;
            case ARRIVED:
                return action != ACTION.ARRIVE;
            case ABORTED:
                return action != ACTION.ABORT;
            case FINSIHED:
                return action != ACTION.FINISH;
        }
        return true;
    }

    
    @Override
    public boolean canPerformAutomaticAction(ACTION action, T task) {
        return canPerformAction(action, task) && !task.isFinished();
    }

    @Override
    public T performAutomaticAction(ACTION action, T task) {
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
                return GSTask.TASK_STATE.FINSIHED;
        }
        return GSTask.TASK_STATE.PENDING;
    }
}

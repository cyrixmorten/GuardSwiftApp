package com.guardswift.core.tasks.controller;

import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;

/**
 * Created by cyrix on 2/27/15.
 */
public interface TaskController<T extends BaseTask> {


    enum ACTION {OPEN, ACCEPT, IGNORE, ARRIVE, ABORT, FINISH, OPEN_WRITE_REPORT, OPEN_CHECKPOINTS, FORWARD, RESET}

    GSTask.TASK_STATE translatesToState(ACTION action);

    T performAction(TaskController.ACTION action, T task);
    boolean canPerformAction(TaskController.ACTION action, T task);

    T performAutomaticAction(TaskController.ACTION action, T task);
    boolean canPerformAutomaticAction(TaskController.ACTION action, T task);

}

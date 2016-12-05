package com.guardswift.core.tasks.controller;

import com.guardswift.persistence.parse.execution.GSTask;

public interface TaskController {


    enum ACTION {OPEN, ACCEPT, IGNORE, ARRIVE, ABORT, FINISH, OPEN_WRITE_REPORT, OPEN_CHECKPOINTS, FORWARD, RESET}

    GSTask.TASK_STATE translatesToState(ACTION action);

    GSTask performAction(TaskController.ACTION action, GSTask task);
    boolean canPerformAction(TaskController.ACTION action, GSTask task);

    GSTask performAutomaticAction(TaskController.ACTION action, GSTask task);
    boolean canPerformAutomaticAction(TaskController.ACTION action, GSTask task);

}

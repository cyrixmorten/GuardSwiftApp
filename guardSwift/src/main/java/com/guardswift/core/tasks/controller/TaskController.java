package com.guardswift.core.tasks.controller;


import com.guardswift.persistence.parse.execution.task.ParseTask;

public interface TaskController {


    enum ACTION {OPEN, ACCEPT, IGNORE, ARRIVE, ABORT, FINISH, OPEN_WRITE_REPORT, OPEN_CHECKPOINTS, FORWARD, PENDING}

    ParseTask.TASK_STATE translatesToState(ACTION action);

    ParseTask performAction(TaskController.ACTION action, ParseTask task);
    boolean canPerformAction(TaskController.ACTION action, ParseTask task);

    ParseTask performAutomaticAction(TaskController.ACTION action, ParseTask task);
    boolean canPerformAutomaticAction(TaskController.ACTION action, ParseTask task);

}

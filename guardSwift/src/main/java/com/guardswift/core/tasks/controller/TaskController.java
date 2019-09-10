package com.guardswift.core.tasks.controller;


import com.guardswift.persistence.parse.execution.task.ParseTask;

public interface TaskController {


    enum ACTION {OPEN, ACCEPT, IGNORE, ARRIVE, ABORT, FINISH, OPEN_WRITE_REPORT, FORWARD, PENDING}

    ParseTask performManualAction(TaskController.ACTION action, ParseTask task);
    ParseTask performAutomaticAction(TaskController.ACTION action, ParseTask task);

}

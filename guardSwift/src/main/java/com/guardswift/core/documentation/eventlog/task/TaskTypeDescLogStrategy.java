package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

/**
 * Adds a small description of the type of task to the log
 *
 * The task type is added during planning of the task
 *
 * Created by cyrix on 6/7/15.
 */
public class TaskTypeDescLogStrategy implements LogTaskStrategy {

    public static final String type = "type";

    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        String type = task.getType();
        toParseObject.put(TaskTypeDescLogStrategy.type, (type != null) ? type : "");

    }
}

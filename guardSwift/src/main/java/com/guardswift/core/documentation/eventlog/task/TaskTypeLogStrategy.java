package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

/**
 *
 * Created by cyrix on 6/7/15.
 */
public class TaskTypeLogStrategy implements LogTaskStrategy {

    public static final String taskTypeName = "taskTypeName";
    public static final String taskTypeCode = "taskTypeCode";

    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        if (task != null && toParseObject != null) {
            toParseObject.put(TaskTypeLogStrategy.taskTypeName, task.getTaskType().toString());
            toParseObject.put(TaskTypeLogStrategy.taskTypeCode, task.getTaskType().ordinal());
        }
    }
}

package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

/**
 *
 * Saves the unique parseObject id of the task as a String
 *
 * Note: cannot be used directly as a pointer
 *
 * Created by cyrix on 6/7/15.
 */
public class TaskIdLogStrategy implements LogTaskStrategy {


    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        toParseObject.put(EventLog.taskId, task.getObjectId());
    }
}

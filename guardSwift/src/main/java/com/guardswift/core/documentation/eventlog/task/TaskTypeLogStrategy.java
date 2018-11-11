package com.guardswift.core.documentation.eventlog.task;

import android.util.Log;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;

class TaskTypeLogStrategy implements LogTaskStrategy {

    @Override
    public void log(ParseTask task, ParseObject toParseObject) {
        if (task != null && toParseObject != null) {
            toParseObject.put(EventLog.taskType, task.getTaskTypeString());
            toParseObject.put(EventLog.taskTypeName, task.getTaskType().toString());
            toParseObject.put(EventLog.taskTypeCode, task.getTaskType().ordinal());
        }
    }
}

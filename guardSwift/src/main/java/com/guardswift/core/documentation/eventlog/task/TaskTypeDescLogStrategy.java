package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;


public class TaskTypeDescLogStrategy implements LogTaskStrategy {

    @Override
    public void log(ParseTask task, ParseObject toParseObject) {
        String type = task.getType();
        toParseObject.put(EventLog.type, (type != null) ? type : "");

    }
}

package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;


class TaskEventCodeLogStrategy implements LogTaskStrategy {

    @Override
    public void log(ParseTask task, ParseObject toParseObject) {
        toParseObject.put(EventLog.eventCode, task.getEventCode());
    }

}

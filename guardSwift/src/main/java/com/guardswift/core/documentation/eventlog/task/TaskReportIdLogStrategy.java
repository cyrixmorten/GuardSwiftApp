package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.parse.ParseObject;


public class TaskReportIdLogStrategy implements LogTaskStrategy {

    @Override
    public void log(ParseTask task, ParseObject toParseObject) {
        toParseObject.put(EventLog.reportId, task.getReportId());
    }
}

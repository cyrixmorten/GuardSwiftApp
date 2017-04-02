package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;


public class TaskWithinScheduledTimeLogStrategy implements LogTaskStrategy {



    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        toParseObject.put(EventLog.withinSchedule, task.isWithinScheduledTime());
    }
}

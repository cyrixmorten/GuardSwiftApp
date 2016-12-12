package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;


public class TaskReportIdLogStrategy implements LogTaskStrategy {

    public static final String reportId = "reportId"; // an id generated to uniquely identify relevant report for this log entry


    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        toParseObject.put(TaskReportIdLogStrategy.reportId, task.getReportId());
    }
}

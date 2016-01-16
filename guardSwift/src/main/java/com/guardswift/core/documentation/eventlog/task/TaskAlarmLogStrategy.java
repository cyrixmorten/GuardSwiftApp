package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.parse.execution.GSTask;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/7/15.
 */
public class TaskAlarmLogStrategy implements LogTaskStrategy {

    public static final String reportId = "reportId"; // an id generated to uniquely identify relevant report for this log entry
    public static final String alarm = "alarm";


    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        if (task instanceof Alarm) {

            Alarm alarm = (Alarm)task;

            toParseObject.put(TaskAlarmLogStrategy.alarm, alarm);

            toParseObject.put(TaskAlarmLogStrategy.reportId, alarm.getReportId());
        }

    }
}

package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.parse.ParseObject;

public class TaskLogStrategy implements LogTaskStrategy {

    private static final String TAG = TaskLogStrategy.class.getSimpleName();

    @Override
    public void log(ParseTask task, ParseObject toParseObject) {
        toParseObject.put(EventLog.task, ParseObject.createWithoutData(ParseTask.class, task.getObjectId()));

        if (task.getTimeStart() != null) {
            toParseObject.put(EventLog.timeStart, task.getTimeStart());
            toParseObject.put(EventLog.timeStartString, task.getTimeStartString());
        }

        if (task.getTimeEnd() != null) {
            toParseObject.put(EventLog.timeEnd, task.getTimeEnd());
            toParseObject.put(EventLog.timeEndString, task.getTimeEndString());
        }

        if (task.getTaskGroupStarted() != null) {
            toParseObject.put(EventLog.taskGroupStarted, ParseObject.createWithoutData(TaskGroupStarted.class, task.getTaskGroupStarted().getObjectId()));
        }

    }

}

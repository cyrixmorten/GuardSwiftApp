package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.parse.ParseObject;

// TODO Will replace Circuit, District and Static tasks
public class TaskLogStrategy implements LogTaskStrategy {


    public static final String task = "task";
    public static final String timeStart = "timeStart";
    public static final String timeEnd = "timeEnd";


    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        if (task instanceof ParseTask) {

            ParseTask parseTask = (ParseTask)task;

            toParseObject.put(TaskLogStrategy.task, parseTask);
        }
    }

}

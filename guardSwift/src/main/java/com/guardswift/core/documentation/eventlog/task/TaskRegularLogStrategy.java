package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/7/15.
 */
public class TaskRegularLogStrategy implements LogTaskStrategy {


    public static final String circuitStarted = "circuitStarted";
    public static final String circuitUnit = "circuitUnit";
    public static final String isExtra = "isExtra";
    public static final String timeStart = "timeStart";
    public static final String timeStartString = "timeStartString";
    public static final String timeEnd = "timeEnd";
    public static final String timeEndString = "timeEndString";


    @Override
    public void log(GSTask task, ParseObject toParseObject) {
        if (task instanceof CircuitUnit) {

            CircuitUnit circuitUnit = (CircuitUnit) task;

            toParseObject.put(TaskRegularLogStrategy.circuitUnit, ParseObject.createWithoutData(CircuitUnit.class, circuitUnit.getObjectId()));
            toParseObject.put(TaskRegularLogStrategy.timeStart, circuitUnit.getTimeStart());
            toParseObject.put(TaskRegularLogStrategy.timeStartString, circuitUnit.getTimeStartString());
            toParseObject.put(TaskRegularLogStrategy.timeEnd, circuitUnit.getTimeEnd());
            toParseObject.put(TaskRegularLogStrategy.timeEndString, circuitUnit.getTimeEndString());
            toParseObject.put(TaskRegularLogStrategy.circuitStarted, circuitUnit.getCircuitStarted());
        }
    }

}

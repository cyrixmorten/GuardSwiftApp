package com.guardswift.core.documentation.eventlog.task;

import com.guardswift.core.exceptions.LogError;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.parse.ParseObject;

/**
 * Created by cyrix on 6/7/15.
 */
public class TaskRegularLogStrategy implements LogTaskStrategy {

    private static final String TAG = TaskRegularLogStrategy.class.getSimpleName();

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

            String objectId = circuitUnit.getObjectId();

            toParseObject.put(TaskRegularLogStrategy.circuitUnit, ParseObject.createWithoutData(CircuitUnit.class, objectId));

            if (circuitUnit.getTimeStart() != null) {
                toParseObject.put(TaskRegularLogStrategy.timeStart, circuitUnit.getTimeStart());
                toParseObject.put(TaskRegularLogStrategy.timeStartString, circuitUnit.getTimeStartString());
            } else {
                LogError.log(TAG, "timeStartDate was null for CircuitUnit: " + objectId);
            }

            if (circuitUnit.getTimeEnd() != null) {
                toParseObject.put(TaskRegularLogStrategy.timeEnd, circuitUnit.getTimeEnd());
                toParseObject.put(TaskRegularLogStrategy.timeEndString, circuitUnit.getTimeEndString());
            } else {
                LogError.log(TAG, "timeEndDate was null for CircuitUnit: " + objectId);
            }

            if (circuitUnit.getCircuitStarted() != null) {
                toParseObject.put(TaskRegularLogStrategy.circuitStarted, circuitUnit.getCircuitStarted());
            } else {
                LogError.log(TAG, "circuitStarted was null for CircuitUnit: " + objectId);
            }
        }
    }

}

package com.guardswift.persistence.parse.execution.task;

import android.support.annotation.NonNull;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.query.TaskGroupStartedQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

@ParseClassName("TaskGroupStarted")
public class TaskGroupStarted extends ExtendedParseObject{

    @Override
    public int compareTo(@NonNull ExtendedParseObject object) {
        if (object instanceof TaskGroupStarted) {
            return getName().compareTo(((TaskGroupStarted)object).getName());
        }

        return 0;
    }


//    public static class Query {
//
//        private static String TAG = "CircuitStarted.Query";
//
//        /*
//         * Get newest circuitStarted based on taskGroup
//         */
//        public static Task<TaskGroupStarted> findInBackground(Circuit circuit) {
//            ParseQuery<TaskGroupStarted> query = new TaskGroupStarted.QueryBuilder(true)
//                    .matching(circuit).whereActive().build();
//            return query.getFirstInBackground();
//        }
//
//        /*
//         * Get newest circuitStarted based on taskGroup
//         */
//        public static TaskGroupStarted findFrom(Circuit circuit)
//                throws ParseException {
//            ParseQuery<TaskGroupStarted> query = new TaskGroupStarted.QueryBuilder(true)
//                    .matching(circuit).whereActive().build();
//            return query.getFirst();
//        }
//
//        public static TaskGroupStarted get(String objectId)
//                throws ParseException {
//            Log.e(TAG, "getCircuitStarted: " + objectId);
//            ParseQuery<TaskGroupStarted> query = new TaskGroupStarted.QueryBuilder(true)
//                    .withObjectId(objectId).build();
//            return query.getFirst();
//        }
//
//        public static boolean isCircuitsAvailable() {
//            try {
//                int count = TaskGroupStarted.getQueryBuilder(true).build().count();
//                Log.d(TAG, "isCircuitsAvailable count: " + count);
//                return count != 0;
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            return false;
//        }
//    }


    public static final String name = "name";
    public static final String taskGroup = "taskGroup";
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";
    public static final String hasExtras = "hasExtras"; // has extra

    public static final String extrasCount = "extrasCount"; // number of extras
    public static final String eventCount = "eventCount";

    @Override
    public String getParseClassName() {
        return TaskGroupStarted.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<TaskGroupStarted> getAllNetworkQuery() {
        return new TaskGroupStartedQueryBuilder(false).build();
    }

    public static TaskGroupStartedQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new TaskGroupStartedQueryBuilder(fromLocalDatastore);
    }


    public void incrementExtras() {
        put(hasExtras, true);
        increment(extrasCount);
        saveEventually();
    }

    // public List<DistrictWatch> getDistrictWatches() {
    // return getSet(districtWatches);
    // }

    // public boolean isChecklistStartConfirmed() {
    // return has(checklistStartConfirmed);
    // }
    //
    // public boolean isChecklistEndConfirmed() {
    // return has(checklistEndConfirmed);
    // }
    //
    // public void confirmChecklistStart(List<String> checklist) {
    // put(checklistStartConfirmed, checklist);
    // }
    //
    // public void confirmChecklistEnd(List<String> checklist) {
    // put(checklistEndConfirmed, checklist);
    // }
    //
    // public void setName(String name) {
    // put(CircuitStarted.name, name);
    // }

    public ParseTask.TASK_TYPE getTaskType() {
        return ParseTask.TASK_TYPE.REGULAR;
    }

    public String getName() {
        return getString(name);
    }

    // public void setOwner(ParseObject owner) {
    // put(CircuitStarted.owner, owner);
    // }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    // public void setGuard(Guard guard) {
    // put(CircuitStarted.guard, guard);
    // }

    //
    // public Guard getGuard() {
    // return (Guard) getParseObject(guard);
    // }

    // public void setCircuit(Circuit taskGroup) {
    // put(CircuitStarted.taskGroup, taskGroup);
    // put(CircuitStarted.districtWatches, taskGroup.getDistrictWatches());
    // }

    public TaskGroup getTaskGroup() {
        return (TaskGroup) getParseObject(taskGroup);
    }

    // public void setTimeStartedNow() {
    // put(CircuitStarted.timeStarted, new Date());
    // }

    // public void setTimeStarted(Date date) {
    // put(CircuitStarted.timeStarted, date);
    // }

    public Date getTimeStarted() {
        return getDate(timeStarted);
    }

    // public void setTimeEndedNow() {
    // put(CircuitStarted.timeEnded, new Date());
    // }

    public Date getTimeEnded() {
        return getDate(timeEnded);
    }

    public void incrementEventCount() {
        if (!has(eventCount)) {
            put(TaskGroupStarted.eventCount, 1);
            return;
        }
        increment(TaskGroupStarted.eventCount);
    }

    // public void setTimeStart(int hour, int minute) {
    // put(CircuitStarted.timeStartHour, hour);
    // put(CircuitStarted.timeStartMinute, minute);
    // }
    //
    // public void setTimeEnd(int hour, int minute) {
    // put(CircuitStarted.timeEndHour, hour);
    // put(CircuitStarted.timeEndMinute, minute);
    // }
    //
    // public int getTimeStartHour() {
    // return getInt(CircuitStarted.timeEndHour);
    // }
    //
    // public void setTimeStartString(String timeStartString) {
    // put(CircuitStarted.timeStartString, timeStartString);
    // }
    //
    // public String getTimeStartString() {
    // return getString(CircuitStarted.timeStartString);
    // }
    //
    // public void setTimeEndString(String timeEndString) {
    // put(CircuitStarted.timeEndString, timeEndString);
    // }
    //
    // public String getTimeEndString() {
    // return getString(CircuitStarted.timeEndString);
    // }
    //
    // public void setTimeStartSortable(int timeStartSortable) {
    // put(CircuitStarted.timeStartSortable, timeStartSortable);
    // }
    //
    // public int getTimeStartSortable() {
    // return getInt(timeStartSortable);
    // }
    //
    // public void setTimeEndSortable(int timeEndSortable) {
    // put(CircuitStarted.timeEndSortable, timeEndSortable);
    // }
    //
    // public int getTimeEndSortable() {
    // return getInt(timeEndSortable);
    // }

}

package com.guardswift.persistence.parse.execution.task;

import androidx.annotation.NonNull;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.query.TaskGroupStartedQueryBuilder;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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



    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<TaskGroupStarted> getAllNetworkQuery() {
        return new TaskGroupStartedQueryBuilder(false).build();
    }

    public static TaskGroupStartedQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new TaskGroupStartedQueryBuilder(fromLocalDatastore);
    }


    public ParseTask.TASK_TYPE getTaskType() {
        return ParseTask.TASK_TYPE.REGULAR;
    }

    public String getName() {
        return getString(name);
    }


    public ParseObject getOwner() {
        return getParseObject(owner);
    }


    public TaskGroup getTaskGroup() {
        return (TaskGroup) getParseObject(taskGroup);
    }



}

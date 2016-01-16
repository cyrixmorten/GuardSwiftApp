package com.guardswift.persistence.parse.execution.regular;

import android.content.Context;
import android.util.Log;

import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.GSTaskList;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.Date;

import bolts.Task;

@ParseClassName("CircuitStarted")
public class CircuitStarted extends ExtendedParseObject implements GSTaskList {

//    public static class Recent {
//
//        private static String TAG = "CircuitStarted.Recent";
//
//        private static CircuitStarted selected;
//
//
//        public static void setSelected(ParseModulePreferences preferences, CircuitStarted circuitStarted) {
//
//            selected = circuitStarted;
//
//            if (preferences == null)
//                return;
//
//            String objectId = circuitStarted.getObjectId();
//            if (objectId != null) {
//                Log.d(TAG, "setCircuitStarted " + objectId);
//                preferences.setCircuitStartedObjectId(objectId);
//            } else {
//                throw new NullPointerException("circuitStarted has no objectId");
//            }
//
//            //
////            Circuit.Recent.setSelected(preferences, circuitStarted.getCircuit());
//        }
//
//        public static CircuitStarted getSelected() {
//            return selected;
//        }
//
//        public static CircuitStarted getSelected(ParseModulePreferences preferences) {
//
//            if (selected != null)
//                return selected;
//
//
//            if (preferences.isGuardLoggedIn()
//                    && preferences.isCircuitSelected()) {
//                String objectId = preferences.getCircuitStartedObjectId();
//                try {
//                    selected = Query.get(objectId);
//                } catch (ParseException e) {
//                    Log.e(TAG, "getCurrentCircuit not found");
//                }
//
//            } else {
//                // No CircuitStarted Selected
//            }
//
//            return selected;
//        }
//
//    }

    public static class Query {

        private static String TAG = "CircuitStarted.Query";

        /*
         * Get newest circuitStarted based on circuit
         */
        public static Task<CircuitStarted> findInBackground(Circuit circuit) {
            ParseQuery<CircuitStarted> query = new CircuitStarted.QueryBuilder(true)
                    .matching(circuit).whereActive().build();
            return query.getFirstInBackground();
        }

        /*
         * Get newest circuitStarted based on circuit
         */
        public static CircuitStarted findFrom(Circuit circuit)
                throws ParseException {
            ParseQuery<CircuitStarted> query = new CircuitStarted.QueryBuilder(true)
                    .matching(circuit).whereActive().build();
            return query.getFirst();
        }

        public static CircuitStarted get(String objectId)
                throws ParseException {
            Log.e(TAG, "getCircuitStarted: " + objectId);
            ParseQuery<CircuitStarted> query = new CircuitStarted.QueryBuilder(true)
                    .withObjectId(objectId).build();
            return query.getFirst();
        }

        public static boolean isCircuitsAvailable() {
            try {
                int count = CircuitStarted.getQueryBuilder(true).build().count();
                Log.d(TAG, "isCircuitsAvailable count: " + count);
                return count != 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static final String PIN = "CircuitStarted";

    public static final String name = "name";
    // public static final String guard = "guard";
    public static final String circuit = "circuit";
    // public static final String districtWatches = "districtWatches";
    // public static final String checklistStartConfirmed =
    // "checklistStartConfirmed";
    // public static final String checklistEndConfirmed =
    // "checklistEndConfirmed";
    // public static final String timeStartString = "timeStartString";
    // public static final String timeEndString = "timeEndString";
    // public static final String timeStartHour = "timeStartHour";
    // public static final String timeStartMinute = "timeStartMinute";
    // public static final String timeEndHour = "timeEndHour";
    // public static final String timeEndMinute = "timeEndMinute";
    // public static final String timeStartSortable = "timeStartSortable";
    // public static final String timeEndSortable = "timeEndSortable";
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";
    public static final String hasExtras = "hasExtras"; // has extra
    // circuitUnits added
    public static final String extrasCount = "extrasCount"; // number of extras
    // added
    public static final String eventCount = "eventCount";

    @Override
    public String getPin() {
        return PIN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<CircuitStarted> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public static class QueryBuilder extends ParseQueryBuilder<CircuitStarted> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(PIN, fromLocalDatastore, ParseQuery
                    .getQuery(CircuitStarted.class));
        }

        @Override
        public ParseQuery<CircuitStarted> build() {
            query.include(circuit);
            whereActive();
            return super.build();
        }

        public QueryBuilder sortByName() {
            query.addAscendingOrder(name);
            return this;
        }

        public QueryBuilder withObjectId(String objectId) {
            query.whereEqualTo("objectId", objectId);
            return this;
        }

        public QueryBuilder whereActive() {
            query.whereDoesNotExist(CircuitStarted.timeEnded);
            return this;
        }

        public QueryBuilder matching(Circuit circuit) {
            query.whereEqualTo(CircuitStarted.circuit, circuit);
            return this;
        }

    }

    public void incrementExtras() {
        put(hasExtras, true);
        increment(extrasCount);
        pinThenSaveEventually();
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

    @Override
    public GSTask.TASK_TYPE getTaskType() {
        return GSTask.TASK_TYPE.REGULAR;
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

    // public void setCircuit(Circuit circuit) {
    // put(CircuitStarted.circuit, circuit);
    // put(CircuitStarted.districtWatches, circuit.getDistrictWatches());
    // }

    public Circuit getCircuit() {
        return (Circuit) getParseObject(circuit);
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
            put(CircuitStarted.eventCount, 1);
            return;
        }
        increment(CircuitStarted.eventCount);
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

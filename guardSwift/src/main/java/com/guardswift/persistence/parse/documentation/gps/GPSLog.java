//package com.guardswift.persistence.parse.documentation.gps;
//
//import android.content.Context;
//import android.location.Location;
//import android.util.Log;
//
//import com.guardswift.core.ca.LocationModule;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.ParseQueryBuilder;
//import com.guardswift.persistence.parse.execution.task.alarm.Alarm;
//import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchClient;
//import com.guardswift.persistence.parse.execution.GSTask;
//import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
//import com.guardswift.persistence.parse.execution.task.districtwatch.DistrictWatchStarted;
//import com.parse.ParseClassName;
//import com.parse.ParseQuery;
//
//import org.json.JSONObject;
//
//import bolts.Continuation;
//
///*
// * Keeps a log of all the GPS positions logged while being inside the geofence of a Task
// */
//// TODO deprecated
//@ParseClassName("GPSLog")
//public class GPSLog extends ExtendedParseObject {
//
//    public static final String PIN = "GPSLog";
//
//    public static final String installation = "installation";
//
//    public static final String alarm = "alarm";
//
//    public static final String circuitStarted = "circuitStarted";
//    public static final String circuitUnit = "circuitUnit";
//
//    public static final String districtWatchClient = "districtWatchClient";
//    public static final String districtWatchStarted = "districtWatchStarted";
//
//    public static final String guard = "guard";
//    public static final String client = "client";
//
//    public static final String deviceTimestamp = "deviceTimestamp";
//
//    public static final String log = "log";
//    public static final String logsCount = "logsCount";
//
//
//    @Override
//    public String getPin() {
//        return PIN;
//    }
//
//    /*
//     * Only creates a new GPSLog if not found in LDS
//     */
////    public static Task<GPSLog> createIfNeeded(GSTask task) {
////
////        final Task<GPSLog>.TaskCompletionSource promise = Task.create();
////
////        final GPSLog newGpsLog = create(task);
////
////
////        new QueryBuilder(true).matching(newGpsLog).build().getFirstInBackground(new GetCallback<GPSLog>() {
////            @Override
////            public void done(GPSLog gpsLog, ParseException e) {
////                Log.d("GPSLog", "createIfNeeded " + gpsLog);
////                if (gpsLog != null) {
////                    promise.setResult(gpsLog);
////                } else {
////                    promise.setResult(newGpsLog);
////                }
////            }
////        });
////
////        return promise.getTask();
////
////    }
//
//    /*
//     * Always creates a new instance of GPSLog
//     */
//    public static GPSLog create(GSTask task, Guard guard) {
//
//
////        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
//
//        GPSLog gpsLog = new GPSLog();
//
//        if (task instanceof Alarm) {
//            gpsLog.setAlarm((Alarm) task);
//        }
//        if (task instanceof CircuitUnit) {
//            gpsLog.setCircuitUnit((CircuitUnit) task);
//        }
//        if (task instanceof DistrictWatchClient) {
//            gpsLog.setDistrictWatchClient((DistrictWatchClient) task);
//        }
//
//        gpsLog.setClient(task.getClient());
//
//        gpsLog.put(GPSLog.guard, guard);
//        gpsLog.setDefaultOwner();
//        return gpsLog;
//    }
//
//    ;
//
//    public void addLocation(Location location) {
//        JSONObject jsonLocation = LocationModule.locationToJSONObject(location);
//        add(GPSLog.log, jsonLocation);
//        increment(GPSLog.logsCount);
//    }
//
//    public int getLogsCount() {
//        return getInt(GPSLog.logsCount);
//    }
//
////    @Override
////    public void appendLocation(Location location) {
////        if (waitingForSaveToComplete)
////            return;
////
////        // ignore if distance moved since last is less than 3 meters
////        if (mLastLocation != null && !BuildConfig.DEBUG) {
////            float distance = mLastLocation.distanceTo(location);
////            if (distance < 3) {
//////                Log.d(TAG, "ignoring due to distance: " + distance);
////                return;
////            }
////        }
////
////        mLastLocation = location;
////
//////        Log.d(TAG, "appendLocation");
////        final JSONObject jsonLocation = LocationModule.locationToJSONObject(location);
////
////        addUnique(GPSLog.log, jsonLocation);
////        increment(GPSLog.logsCount);
////
////        int gpsCount = getInt(GPSLog.logsCount);
////        if (gpsCount >= 360) {
//////            Log.d(TAG, "saveAsync gpsHolder " + gpsCount + task.getParseObject().getObjectId());
////            waitingForSaveToComplete = true;
////            saveEventually(new SaveCallback() {
////                @Override
////                public void done(ParseException e) {
////                    if (e != null) {
////                        Crashlytics.logException(e);
////                        return;
////                    }
////                    gpsHolder.unpin();
////                    waitingForSaveToComplete = false;
////                }
////            });
////
////        } else {
//////            Log.d(TAG, "gpsHolder count " + gpsCount + " " + task.getParseObject().getObjectId());
////            if ((gpsCount % 12) == 0) {
//////                Log.d(TAG, "Pin gpsHolder with count: " + gpsCount);
////                pinInBackground(PIN)
////            }
////        }
////    }
//
//    private void setClient(Client client) {
//        put(GPSLog.client, client);
//    }
//
//    private void setAlarm(Alarm alarm) {
//        put(GPSLog.alarm, alarm);
//    }
//
//    private void setCircuitUnit(CircuitUnit circuitUnit) {
//        put(GPSLog.circuitUnit, circuitUnit);
//        CircuitStarted.Query.findInBackground(circuitUnit.getCircuit()).onSuccess(new Continuation<CircuitStarted, Object>() {
//            @Override
//            public Object then(bolts.Task<CircuitStarted> task) throws Exception {
//                Log.d(PIN, "Setting CircuitStarted: " + task.getResult());
//                put(GPSLog.circuitStarted, task.getResult());
//                return null;
//            }
//        });
//    }
//
//    private void setDistrictWatchClient(DistrictWatchClient districtWatchClient) {
//        put(GPSLog.districtWatchClient, circuitUnit);
//        DistrictWatchStarted.Query.findInBackground(districtWatchClient.getDistrictWatch()).onSuccess(new Continuation<DistrictWatchStarted, Object>() {
//            @Override
//            public Object then(bolts.Task<DistrictWatchStarted> task) throws Exception {
//                Log.d(PIN, "Setting DistrictWatchStarted: " + task.getResult());
//                put(GPSLog.districtWatchStarted, task.getResult());
//                return null;
//            }
//        });
//    }
//
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public ParseQuery<GPSLog> getAllNetworkQuery() {
//        return new QueryBuilder(false).build();
//    }
//
//    @Override
//    public void updateFromJSON(final Context context,
//                               final JSONObject jsonObject) {
//        // TODO Auto-generated method stub
//    }
//
//    public static class QueryBuilder extends ParseQueryBuilder<GPSLog> {
//
//        public QueryBuilder(boolean fromLocalDatastore) {
//            super(PIN, fromLocalDatastore, ParseQuery.getQuery(GPSLog.class));
//        }
//
//        public QueryBuilder matching(GPSLog gpsLog) {
//            for (String key : gpsLog.keySet()) {
//
//                switch (key) {
//                    case GPSLog.client:
//                        matching((Client) gpsLog.getParseObject(key));
//                        break;
//                    case GPSLog.circuitStarted:
//                        matching((CircuitStarted) gpsLog.getParseObject(key));
//                        break;
//                    case GPSLog.circuitUnit:
//                        matching((CircuitUnit) gpsLog.getParseObject(key));
//                        break;
//
//                }
//
//            }
//            return this;
//        }
//
//        private QueryBuilder matching(CircuitStarted circuitStarted) {
//            query.whereEqualTo(GPSLog.circuitStarted, circuitStarted);
//            return this;
//        }
//
//        public QueryBuilder matching(Client client) {
//            query.whereEqualTo(GPSLog.client, client);
//            return this;
//        }
//
//        public QueryBuilder matching(CircuitUnit circuitUnit) {
//            query.whereEqualTo(GPSLog.circuitUnit, circuitUnit);
//            return this;
//        }
//
//        ;
//
//    }
//
//
//}

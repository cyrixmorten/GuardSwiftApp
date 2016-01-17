package com.guardswift.persistence.parse.documentation.event;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.guardswift.R;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.FingerprintingModule;
import com.guardswift.core.ca.LocationModule;
import com.guardswift.core.documentation.eventlog.context.LogContextStrategy;
import com.guardswift.core.documentation.eventlog.context.LogStrategyFactory;
import com.guardswift.core.documentation.eventlog.context.TaskLogStrategyFactory;
import com.guardswift.core.documentation.eventlog.task.LogTaskStrategy;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.alarm.Alarm;
import com.guardswift.persistence.parse.execution.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.regular.CircuitUnit;
import com.guardswift.util.GeocodedAddress;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;
import dk.alexandra.positioning.wifi.AccessPoint;

@ParseClassName("EventLog")
public class EventLog extends ExtendedParseObject {


    public static class Recent {
        private static EventLog selected;

        public static EventLog getSelected() {
            return selected;
        }

        public static void setSelected(EventLog selected) {
            Recent.selected = selected;
        }

    }

    public static final String PIN = "EventLog";


    public static class EventCodes {
        public static final int CIRCUITUNIT_ARRIVED = 101;
        public static final int CIRCUITUNIT_FINISHED = 102;
        public static final int CIRCUITUNIT_ABORT = 103;
        public static final int CIRCUITUNIT_OTHER = 104;
        public static final int CIRCUITUNIT_EXTRA_TIME = 105;
        public static final int CIRCUITUNIT_CHECKPOINT_ARRIVED = 106;
        public static final int CIRCUITUNIT_CHECKPOINT_ABORTED = 107;
        public static final int AUTOMATIC_CIRCUITUNIT_CHECKPOINT_ARRIVED = 1006;
        public static final int AUTOMATIC_GEOFENCE_ENTER = 1101;
        public static final int AUTOMATIC_GEOFENCE_EXIT = 1102;
        public static final int AUTOMATIC_ARRIVED_ON_FOOT = 1201;
        public static final int AUTOMATIC_ARRIVED_STILL = 1202;
        public static final int AUTOMATIC_ARRIVED = 1301;
        public static final int AUTOMATIC_DEPARTURE = 1302;

        public static final int DISTRICTWATCH_ARRIVED = 111;
        public static final int DISTRICTWATCH_FINISHED = 112;
        public static final int DISTRICTWATCH_ABORT = 113;
        public static final int DISTRICTWATCH_OTHER = 114;
        public static final int ALARM_ACCEPTED = 120;
        public static final int ALARM_ARRIVED = 121;
        public static final int ALARM_FINISHED = 122;
        public static final int ALARM_ABORT = 123;
        public static final int ALARM_OTHER = 124;
        public static final int ALARM_OTHER_REPORT = 125;
        public static final int ALARM_FORWARDED = 126;
        public static final int GUARD_LOGIN = 200;
        public static final int GUARD_LOGOUT = 201;
        public static final int TASK_GPS_SUMMARY = 180;
        public static final int TASK_EVENT_SUMMARY = 181;

    }


    public static class Builder {

        private String TAG = "EventLog.Builder";

        private final Context context;
        private final EventLog eventLog;

        private GSTask gsTask;
//        private TaskSummary.EVENT_TYPE event_type;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
            this.eventLog = new EventLog();

            applyDefaultLogValues();
            clientProximity();

            eventLog.setOwner(ParseUser.getCurrentUser());
        }

        /*
         * Used to copy an existing event
         */
        public Builder from(EventLog eventLog, GSTask taskPointer) {

            for (String key : eventLog.keySet()) {
                if (eventLog.get(key) != null) {
                    this.eventLog.put(key, eventLog.get(key));
                }
            }

            applyDefaultLogValues();

            taskPointer(taskPointer, GSTask.EVENT_TYPE.OTHER);

            return this;
        }

        private void applyDefaultLogValues() {
            List<LogContextStrategy> logStrategies = new LogStrategyFactory(context).getStrategies();

            for (LogContextStrategy logStrategy : logStrategies) {
                logStrategy.log(this.eventLog);
            }
        }


        public Builder eventType(EventType eventType) {
            if (eventType != null) {
                this.eventLog.put(EventLog.eventType, eventType);
                event(eventType.getName());
            }
            return this;
        }

        public Builder event(String event) {
            this.eventLog.put(EventLog.event, (event != null) ? event : "");
            return this;
        }

        public Builder remarks(String remarks) {
            this.eventLog.put(EventLog.remarks, (remarks != null) ? remarks : "");
            return this;
        }

        public Builder amount(int amount) {
            this.eventLog.put(EventLog.amount, amount);
            return this;
        }

        public Builder amount(CharSequence text) {
            try {
                int amount = Integer.parseInt(String.valueOf(text));
                this.eventLog.put(EventLog.amount, amount);
            } catch (Exception e) {
                this.eventLog.put(EventLog.amount, 0);
            }

            return this;
        }

        public Builder eventCode(int eventCode) {
            this.eventLog.put(EventLog.eventCode, eventCode);
            return this;
        }

        public Builder people(String people) {
            this.eventLog.put(EventLog.people, (people != null) ? people : "");
            return this;
        }

        public Builder location(String clientLocation) {
            this.eventLog.put(EventLog.clientLocation, (clientLocation != null) ? clientLocation : "");
            return this;
        }


        public Builder taskPointer(GSTask task, GSTask.EVENT_TYPE event_type) {

            this.gsTask = task;
//            this.event_type = event_type;

            TaskLogStrategyFactory logStrategyFactory = new TaskLogStrategyFactory();

            List<LogTaskStrategy> logStrategies = logStrategyFactory.getStrategies();

            for (LogTaskStrategy logStrategy : logStrategies) {
                logStrategy.log(task, eventLog);
            }


//            eventLog.put(EventLog.task_type, task.getTaskType().toString());
            eventLog.put(EventLog.task_event, event_type.toString());
            eventCode(task.getEventCode());


            return this;
        }

        public Builder automatic(boolean automatic) {
            eventLog.setAutomatic(automatic);
            return this;
        }

        public Builder wifiSample(Set<AccessPoint> sample) {
            eventLog.put(EventLog.checkpoint_wifi_sample, FingerprintingModule.convertToJsonArray(sample));
            return this;
        }


        public Builder checkpoint(ClientLocation checkpoint, boolean automatic) {
            double probability = checkpoint.getLastProbability();
            double distance = checkpoint.getLastDistance();

//            if (!automatic) {
            eventLog.put(EventLog.checkpoint, checkpoint);
            eventLog.put(EventLog.checkpoint_name, checkpoint.getLocation());
            eventLog.put(EventLog.checkpoint_probability, probability);
            eventLog.put(EventLog.checkpoint_distance, distance);
//            }

//            ClientLocation checkpointGuess = ClientLocation.Recent.getNearCheckpoint();
//            if (checkpointGuess != null) {
//                eventLog.put(EventLog.checkpoint_guess, checkpointGuess);
//                eventLog.put(EventLog.checkpoint_guess_name, checkpointGuess.getLocations());
//                eventLog.put(EventLog.checkpoint_guess_probability, checkpointGuess.getLastProbability());
//                eventLog.put(EventLog.checkpoint_guess_distance, checkpointGuess.getLastDistance());
//            }


//            String remarks = (automatic) ? context.getString(R.string.automatic_event).toUpperCase() : "";
//            remarks += "\nEst. Afst: " + String.format("%.2f", distance) + "\nSands: " + String.format("%.2f", probability);

            event(context.getString(R.string.event_checkpoint));
            location(checkpoint.getLocation());
//            remarks(remarks);
            eventCode(EventLog.EventCodes.CIRCUITUNIT_CHECKPOINT_ARRIVED);
            automatic(automatic);


//            Map<String, String> dimensions = new HashMap<>();
//            if (eventLog.getClient() != null)
//                dimensions.put("client", eventLog.getClient().getName());
//            dimensions.put("symbolic", checkpoint.getLocations());
//            dimensions.put("estimatedDistance", String.valueOf(distance));
//            dimensions.put("probability", String.valueOf(probability));
//            dimensions.put("automatic", String.valueOf(automatic));
//            ParseAnalytics.trackEventInBackground("checkpoint", dimensions);

            return this;
        }


        private Builder clientProximity() {
            Location location = LocationModule.Recent.getLastKnownLocation();
            if (eventLog.getClient() != null && location != null) {
                ParseGeoPoint clientPosition = eventLog.getClient().getPosition();
//                double distanceKm = ParseModule.distanceBetweenKilomiters(location, clientPosition);
                float distanceMeters = ParseModule.distanceBetweenMeters(location, clientPosition);
                eventLog.addClientProximity(distanceMeters);
            }
            return this;
        }

//        private Builder detectedActivity() {
//            DetectedActivity activity = ActivityDetectionModule.Recent.getDetectedActivity();
//            if (activity != null) {
//                eventLog.setDetectedActivity(activity);
//            }
//            return this;
//        }

//        public Builder guard(Guard guard) {
//            if (guard != null)
//                eventLog.setGuard(guard);
//            return this;
//        }

//        private Builder location(Location location) {
//            if (location != null)
//                eventLog.setLocation(location);
//            return this;
//        }

//        private Builder districtWatchClient(DistrictWatchClient districtWatchClient) {
//            eventLog.setDistrictWatchClient(districtWatchClient);
//            try {
//                // Query locally for the newest DistrictWatchStarted
//                eventLog.setDistrictWatchStarted(DistrictWatchStarted.Query.findFrom(districtWatchClient.getDistrictWatch()));
//            } catch (ParseException e) {
//                // fallback to Recent
//                eventLog.setDistrictWatchStarted(DistrictWatchStarted.Recent.getSelected());
//            }
//
//            return this;
//        }

//        private Builder districtWatchStarted(DistrictWatchStarted districtWatchStarted) {
//            eventLog.setDistrictWatchStarted(districtWatchStarted);
//            return this;
//        }

//        private Builder circuitUnit(CircuitUnit circuitUnit) {
//            eventLog.setCircuitUnit(circuitUnit);
//            if (circuitUnit.isArrived()) {
//                try {
//                    // Query locally for the newest circuitStarted
//                    circuitStarted(CircuitStarted.Query.findFrom(circuitUnit.getCircuit()));
//                } catch (ParseException e) {
//                    // In case of error fallback to pointer kept on circuitUnit
//                    // A final fallback to Recents is done for getCircuitStarted on failure
//                    circuitStarted(circuitUnit.getCircuitStarted(false));
//                }
//            } else {
//                circuitStarted(CircuitStarted.Recent.getSelected());
//            }
//            return this;
//        }

//        public Builder circuitStarted(CircuitStarted circuitStarted) {
//            if (circuitStarted != null)
//                eventLog.setCircuitstarted(circuitStarted);
//            return this;
//        }

        /**
         * Automatically adds current CircuitStarted, Guard, Location, client proximity and DeviceTimestamp
         * before calling saveeventually and broadcast a UI init
         */
        public void saveAsync() {
            saveAsync(null);
        }

        public void saveAsync(final GetCallback<EventLog> pinnedCallback) {

            Log.e(TAG, "Save event " + eventLog.getEvent());


            Log.w(TAG, "1) Geocode start");
            LocationModule.reverseGeocodedAddress(context).continueWith(new Continuation<GeocodedAddress, Object>() {
                @Override
                public Object then(Task<GeocodedAddress> task) throws Exception {
                    Log.w(TAG, "2) Geocode done");
                    final Capture<GeocodedAddress> geocodedAddress = new Capture<>();
                    if (!task.isFaulted()) {
                        geocodedAddress.set(task.getResult());
                        eventLog.put(EventLog.geocodedAddress, task.getResult().toJSON());
                    } else {
                        // error reverse geocoding address
                        // fall-thru as reverse geocoding is not strictly needed
                        new HandleException(context, TAG, "reverseGeocodeAddress", task.getError());
                    }


                    Log.w(TAG, "3) Save event");
                    eventLog.pinThenSaveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (pinnedCallback != null) {
                                pinnedCallback.done(eventLog, e);
                            }
                            EventBusController.postUIUpdate(eventLog);
                            Log.w(TAG, "4) Save event - pinned");
                        }
                    }, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.w(TAG, "5) Save event - saved");

                            if (gsTask != null) {
                                Log.w(TAG, "Report strategy: " + gsTask.getTaskReportingStrategy());
                                gsTask.getTaskReportingStrategy().addUnique(context, eventLog, new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            new HandleException(context, TAG, "Save report online", e);
                                        }
                                        updateGuardInfo(geocodedAddress.get());
                                    }
                                });
                            } else {
                                Log.w(TAG, "Not for task");
                                updateGuardInfo(geocodedAddress.get());
                            }
                        }
                    });

                    return null;
                }
            });

        }

        // Assert: eventlog is saved online
        private void updateGuardInfo(GeocodedAddress reverseGeocodedAddress) {
            Log.w(TAG, "6) Update guard");
            Guard guard = eventLog.getGuard();
            if (guard == null) {
                return;
            }

            guard.setPosition(eventLog.getPosition());
            guard.setLastGeocodedAddress(reverseGeocodedAddress);
            guard.setLastEvent(eventLog);
            guard.pinThenSaveEventually();
        }


        public Builder correctGuess(boolean b) {
            eventLog.put(correctGuess, b);
            return this;
        }

        public Builder summary(String columnName, JSONArray summary, Date timeStarted, Date timeEnded) {
            eventLog.setSummary(columnName, summary, timeStarted, timeEnded);
            return this;
        }

//        public Builder eventSummary(JSONArray summaryEntries) {
//            eventLog.put(eventSummary, summaryEntries);
//            return this;
//        }
//
//        public Builder taskSummary(JSONArray summaryEntries) {
//            eventLog.put(taskSummary, summaryEntries);
//            return this;
//        }

//        public Builder publicReadable() {
//            ParseACL acl = new ParseACL();
//            acl.setWriteAccess(ParseUser.getCurrentUser(), true);
//            acl.setPublicWriteAccess(false);
//            acl.setPublicReadAccess(true);
////            acl.setWriteAccess(ParseUser.getCurrentUser(), false);
//            eventLog.setACL(acl);
//            return this;
//        }

        public EventLog build() {
            return eventLog;
        }


        public Builder activity(ActivityRecognitionResult activityRecognitionResult) {
            eventLog.put("activity", activityResultAsJSONArray(activityRecognitionResult));
            return this;
        }

        private JSONArray activityResultAsJSONArray(ActivityRecognitionResult activityResult) {
            JSONArray jsonArray = new JSONArray();
            List<DetectedActivity> activityOptions = activityResult.getProbableActivities();
            for (DetectedActivity activity : activityOptions) {
                jsonArray.put(detectedActivityResultAsJSON(activity));
            }
            return jsonArray;
        }

        private JSONObject detectedActivityResultAsJSON(DetectedActivity detectedActivity) {

            int type = detectedActivity.getType();
            int confidence = detectedActivity.getConfidence();
            String name = ActivityDetectionModule.getNameFromType(detectedActivity.getType());

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("activityType", type);
            map.put("activityConfidence", confidence);
            map.put("activityName", name);

//        Log.d(TAG, "Activity -> JSON");
//        Log.d(TAG, "Type: " + type);
//        Log.d(TAG, "Confidence: " + confidence);
//        Log.d(TAG, "Name: " + name);

            JSONObject jsonObject = new JSONObject(map);

            return jsonObject;
        }


        public Builder locationTrackerUrl(String result) {
            eventLog.put("locationTrackerUrl", result);
            return this;
        }
    }


    // pointers

    public static final String client = "client";
    public static final String districtWatchStarted = "districtWatchStarted";
    public static final String districtWatchClient = "districtWatchClient";
    public static final String contactClient = "contactClient";
    public static final String circuitStarted = "circuitStarted";
    public static final String circuitUnit = "circuitUnit";
    public static final String districtWatchUnit = "districtWatchUnit";
    public static final String alarm = "alarm";
    // task
    public static final String taskId = "taskId";
    public static final String reportId = "reportId";
    // strings
    public static final String clientName = "clientName";
    public static final String clientCity = "clientCity";
    public static final String clientZipcode = "clientZipcode";
    public static final String clientAddress = "clientAddress";
    public static final String clientAddressNumber = "clientAddressNumber";
    public static final String clientFullAddress = "clientFullAddress";
    //
    public static final String guard = "guard";
    public static final String guardId = "guardId";
    public static final String guardName = "guardName";
    // planned times
    public static final String timeStart = "timeStart";
    public static final String timeStartString = "timeStartString";
    public static final String timeEnd = "timeEnd";
    public static final String timeEndString = "timeEndString";
    // event description
    public static final String task_type = "task_type";
    public static final String task_event = "task_event";
    public static final String type = "type";
    public static final String eventType = "eventType";
    public static final String event = "event";
    public static final String amount = "amount";
    public static final String people = "people";
    public static final String clientLocation = "clientLocation";
    public static final String remarks = "remarks";
    public static final String isExtra = "isExtra";
    // gps
    public static final String clientDistanceMeters = "clientDistanceMeters";
    public static final String provider = "provider";
    public static final String position = "position";
    public static final String accuracy = "accuracy";
    public static final String altitude = "altitude";
    public static final String bearing = "bearing";
    public static final String speed = "speed";
    public static final String geocodedAddress = "geocodedAddress";
    // activity
    public static final String activityType = "activityType";
    public static final String activityConfidence = "activityConfidence";
    public static final String activityName = "activityName";
    // eventCode
    public static final String eventCode = "eventCode";
    // locations
//    public static final String locations = "locations";
    // checkpoint
    public static final String checkpoint = "checkpoint";
    public static final String checkpoint_name = "checkpoint_name";
    public static final String checkpoint_distance = "checkpoint_distance";
    public static final String checkpoint_probability = "checkpoint_probability";

    public static final String checkpoint_guess = "checkpoint_guess";
    public static final String checkpoint_guess_name = "checkpoint_guess_name";
    public static final String checkpoint_guess_distance = "checkpoint_guess_distance";
    public static final String checkpoint_guess_probability = "checkpoint_guess_probability";

    public static final String checkpoint_wifi_sample = "checkpoint_wifi_sample";
    // misc
    public static final String deviceTimestamp = "deviceTimestamp";
    public static final String clientTimestamp = "clientTimestamp"; // TODO deprecate in favor of deviceTimeStamp
    public static final String automatic = "automatic";
    public static final String correctGuess = "correctGuess";

    // summary
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";


    @Override
    public String getPin() {
        return PIN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<EventLog> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public static QueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public static class QueryBuilder extends ParseQueryBuilder<EventLog> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(PIN, fromLocalDatastore, ParseQuery.getQuery(EventLog.class));
        }

        @Override
        public ParseQuery<EventLog> build() {
            query.orderByDescending(EventLog.deviceTimestamp);
            return super.build();
        }

        public QueryBuilder matching(Guard guard) {
            query.whereEqualTo(EventLog.guard, guard);
            return this;
        }

//        public QueryBuilder matching(DistrictWatchClient districtWatchClient) {
//
//            query.whereEqualTo(EventLog.districtWatchClient,
//                    districtWatchClient);
//
//            return this;
//        }

//         public QueryBuilder matchingIncludes(CircuitUnit circuitUnit) {
//
//         query.whereEqualTo(EventLog.circuitUnit, circuitUnit);
//
//         return this;
//         }

        public QueryBuilder matching(Client client) {
            query.whereEqualTo(EventLog.client, client);

            return this;
        }

        public QueryBuilder matching(Alarm alarm) {
            query.whereEqualTo(EventLog.alarm, alarm);
            return this;
        }

        public QueryBuilder eventCode(int eventCode) {
            query.whereEqualTo(EventLog.eventCode, eventCode);
            return this;
        }

        public QueryBuilder eventCodes(int... eventCodes) {
            query.whereContainedIn(EventLog.eventCode, Arrays.asList(eventCodes));
            return this;
        }

        public QueryBuilder matchingEvents(List<String> events) {
            if (events != null && !events.isEmpty())
                query.whereContainedIn(EventLog.event, events);
            return this;
        }


        public QueryBuilder matchingEvent(String event) {
            if (event != null && !event.isEmpty())
                query.whereEqualTo(EventLog.event, event);
            return this;
        }

        public QueryBuilder matchingEventCode(int eventCode) {
            query.whereEqualTo(EventLog.eventCode, eventCode);
            return this;
        }

        /*
         * Exclude checkpoint and other automatic events
         */
        public QueryBuilder excludeAutomatic() {

            query.whereNotEqualTo(automatic, true);

            return this;
        }

        /*
         * Exclude onActionArrive, onActionAbort, onActionFinish events
         */
        public QueryBuilder whereIsReportEntry() {

            query.whereContainedIn(eventCode, Arrays.asList(EventCodes.ALARM_OTHER, EventCodes.CIRCUITUNIT_OTHER, EventCodes.DISTRICTWATCH_OTHER));

//            ParseQuery<EventLog> alarmEvents = ParseQuery.getQuery(EventLog.class);
//            alarmEvents.whereEqualTo(eventCode, EventCodes.ALARM_OTHER);
//            ParseQuery<EventLog> circuitUnitEvents = ParseQuery.getQuery(EventLog.class);
//            circuitUnitEvents.whereEqualTo(eventCode, EventCodes.CIRCUITUNIT_OTHER);
//            ParseQuery<EventLog> districtWatchEvents = ParseQuery.getQuery(EventLog.class);
//            districtWatchEvents.whereEqualTo(eventCode, EventCodes.DISTRICTWATCH_OTHER);
//
//            appendQueries(alarmEvents, circuitUnitEvents, districtWatchEvents);

            return this;
        }


        public QueryBuilder matchingReportId(String reportId) {
            query.whereEqualTo(EventLog.reportId, reportId);
            return this;
        }

        public QueryBuilder notMatchingReportId(String reportId) {
            query.whereNotEqualTo(EventLog.reportId, reportId);
            return this;
        }
    }


    public CircuitUnit getCircuitUnit() {
        return (CircuitUnit) getParseObject(circuitUnit);
    }


    public int getEventCode() {
        return getInt(eventCode);
    }

    public void addClientProximity(float distanceMeters) {
        put(clientDistanceMeters, distanceMeters);
    }


//    private void setContactClient(Client contactClient) {
//        put(EventLog.contactClient, contactClient);
//    }
//    public void setClients(Client contactClient, Client client) {
//        put(EventLog.contactClient, contactClient);
//        setClient(client);
//    }

//    public void setClients(Client contactClient, DistrictWatchClient client) {
//        put(EventLog.contactClient, contactClient);
//        setClient(client);
//    }

//    public void setData(CircuitStarted circuitStarted, Guard guard,
//                        Location location, DetectedActivity detectedActivity, Event event) {
//        if (circuitStarted != null)
//            setCircuitstarted(circuitStarted);
//        if (guard != null)
//            setGuard(guard);
//        if (location != null)
//            setLocation(location);
//        if (detectedActivity != null)
//            setDetectedActivity(detectedActivity);
//        setEventType(event);
//        setOwner(ParseUser.getCurrentUser());
//    }

    private void setAutomatic(boolean automatic) {
        put(EventLog.automatic, automatic);
    }

//    public void setClientTimestampNow() {
//        put(clientTimestamp, new Date());
//        put(deviceTimestamp, new Date());
//    }

//    private void setCheckpointDistance(double distance) {
//        put(checkpoint_distance, distance);
//    }

//    private void setCheckpointProbability(double probability) {
//        put(checkpoint_probability, probability);
//    }

//    private void setEventCode(int eventCode) {
//        put(EventLog.eventCode, eventCode);
//    }


//    private void setDetectedActivity(DetectedActivity detectedActivity) {
//        put(activityType, detectedActivity.getType());
//        put(activityConfidence, detectedActivity.getConfidence());
//        put(activityName, ActivityDetectionModule.getNameFromType(detectedActivity.getType()));
//    }

//    private void setAlarm(Alarm alarm) {
//        put(EventLog.alarm, alarm);
//
//        Client client = alarm.getClient();
//        String type = alarm.getType();
//
//        put(EventLog.type, (type != null) ? type : "");
//
//        if (client != null) {
//            put(EventLog.client, client);
//            put(EventLog.contactClient, client);
//            if (client.has(Client.name))
//                put(EventLog.clientName, client.getName());
//            put(EventLog.clientAddress, client.getAddressName());
//            put(EventLog.clientAddressNumber, client.getAddressNumber());
//            put(EventLog.clientCity, client.getCityName());
//            put(EventLog.clientZipcode, client.getZipcode());
//            String clientFullAddress = client.getAddressName() + " "
//                    + client.getAddressNumber() + " " + client.getZipcode() + " "
//                    + client.getCityName();
//            put(EventLog.clientFullAddress, clientFullAddress);
//        }
//    }

//    private void setCircuitUnit(CircuitUnit circuitUnit) {
//        put(EventLog.circuitUnit, circuitUnit);
//
//        setTaskType(circuitUnit.getName());
//        put(isExtra, circuitUnit.isExtra());
//
//        put(timeStart, circuitUnit.getTimeStart());
//        put(timeStartString, circuitUnit.getTimeStartString());
//        put(timeEnd, circuitUnit.getTimeEnd());
//        put(timeEndString, circuitUnit.getTimeEndString());
//
//        Client client = circuitUnit.getClient();
//
//        setClient(client);
//    }

//    private void setDistrictWatchClient(DistrictWatchClient districtWatchClient) {
//        put(EventLog.districtWatchClient, districtWatchClient);
//        if (districtWatchClient.getDistrictWatchUnit() != null) {
//            put(EventLog.districtWatchUnit, districtWatchClient.getDistrictWatchUnit());
//        }
//
//
//        setClient(districtWatchClient);
//    }


//    private void setDistrictWatchStarted(
//            DistrictWatchStarted districtWatchStarted) {
//
//        if (districtWatchStarted != null) {
//            put(EventLog.districtWatchStarted, districtWatchStarted);
//
//            DistrictWatch districtWatch = districtWatchStarted.getDistrictWatch();
//            if (districtWatch != null) {
//                put(timeStart, districtWatch.getTimeStart());
//                put(timeStartString, districtWatch.getTimeStartString());
//                put(timeEnd, districtWatch.getTimeEnd());
//                put(timeEndString, districtWatch.getTimeEndString());
//            }
//
//        }
//
//    }
    // public void setDistrictWatchClient(Client districtWatchClient) {
    // put(EventLog.districtWatchClient, districtWatchClient);
    // }

//    public static class Event {
//        // public String type;
//        public final String event;
//        public final int amount;
//        public final String location;
//        public final String remarks;
//        public final int eventCode;
//
//        public Event(String event, int amount, String location,
//                     String remarks, int eventCode) {
//            super();
//            // this.type = type;
//            this.event = event;
//            this.amount = amount;
//            this.location = location;
//            this.remarks = remarks;
//            this.eventCode = eventCode;
//        }
//
//    }
//
//    private void setEventType(Event event) {
//        setEventCode(event.eventCode);
//        setEventType(event.event);
//        if (event.amount != 0)
//            setAmount(event.amount);
//        setClientLocation(event.location);
//        setRemarks(event.remarks);
//    }

//    private void setLocation(Location location) {
//        if (location != null) {
//            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(
//                    location.getLatitude(), location.getLongitude());
//            setPosition(parseGeoPoint);
//
//            put(EventLog.accuracy, location.getAccuracy());
//            put(EventLog.altitude, location.getAltitude());
//            put(EventLog.bearing, location.getBearing());
//            put(EventLog.provider, location.getProvider());
//            put(EventLog.speed, location.getSpeed());
//        }
//    }


    private void setOwner(ParseObject owner) {
        put(EventLog.owner, owner);
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

//    private void setClient(DistrictWatchClient client) {
//        put(EventLog.districtWatchClient, client);
//        put(EventLog.client, client.getClient());
//        put(EventLog.contactClient, client.getClient());
//        put(EventLog.clientName, client.getClientName());
//        put(EventLog.clientAddress, client.getAddressName());
//        put(EventLog.clientAddressNumber, client.getAddressNumber());
//        put(EventLog.clientCity, client.getCityName());
//        put(EventLog.clientZipcode, client.getZipcode());
//        String clientFullAddress = client.getAddressName() + " "
//                + client.getAddressNumber() + " " + client.getZipcode() + " "
//                + client.getCityName();
//        put(EventLog.clientFullAddress, clientFullAddress);
//    }
//
//    private void setClient(Client client) {
//        put(EventLog.client, client);
//        put(EventLog.contactClient, client);
//        if (client.has(Client.name))
//            put(EventLog.clientName, client.getName());
//        put(EventLog.clientAddress, client.getAddressName());
//        put(EventLog.clientAddressNumber, client.getAddressNumber());
//        put(EventLog.clientCity, client.getCityName());
//        put(EventLog.clientZipcode, client.getZipcode());
//        String clientFullAddress = client.getAddressName() + " "
//                + client.getAddressNumber() + " " + client.getZipcode() + " "
//                + client.getCityName();
//        put(EventLog.clientFullAddress, clientFullAddress);
//    }

    public Client getClient() {
        if (!has(EventLog.client))
            return null;

        Client client = (Client) getParseObject(EventLog.client);
        if (client != null && !client.isDataAvailable()) {
            try {
                client.fetchFromLocalDatastore();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return client;
    }

//    private void setGuard(Guard guard) {
//        put(EventLog.guard, guard);
//        put(EventLog.guardId, guard.getGuardId());
//        put(EventLog.guardName, guard.getName());
//    }

    public Guard getGuard() {
        return (Guard) getParseObject(guard);
    }

    public String getGuardName() {
        return getString(EventLog.guardName);
    }

//    private void setCircuitstarted(CircuitStarted circuitStarted) {
//        if (circuitStarted != null) {
//            put(EventLog.circuitStarted, circuitStarted);
//        }
//    }

    public CircuitStarted getCircuitstarted() {
        return (CircuitStarted) getParseObject(circuitStarted);
    }

    // public void setCircuitUnitStarted(CircuitUnitStarted circuitUnitStarted)
    // {
    // put(CircuitEventLog.circuitUnitStarted, circuitUnitStarted);
    // }
    //
    // public CircuitUnitStarted getCircuitunitstarted() {
    // return (CircuitUnitStarted) getParseObject(circuitUnitStarted);
    // }

//    private void setEventType(String event) {
//        if (event != null) {
//            put(EventLog.event, event);
//        } else {
//            put(EventLog.event, "");
//        }
//    }

    public String getEvent() {
        return has(event) ? getString(event) : "";
    }

    public EventType getEventType() {
        return (EventType) getParseObject(eventType);
    }

//    public void setTaskType(String type) {
//        if (type != null) {
//            put(EventLog.type, type);
//        } else {
//            put(EventLog.type, "");
//        }
//    }

    public String getType() {
        return getString(type);
    }

//    private void setAmount(int amount) {
//        put(EventLog.amount, amount);
//    }

    public int getAmount() {
        if (has(amount)) {
            return getNumber(amount).intValue();
        }
        return 0;
    }

//    private void setClientLocation(String location) {
//        if (location != null) {
//            put(EventLog.location, location);
//        } else {
//            put(EventLog.location, "");
//        }
//    }

    public String getLocations() {
        return getString(clientLocation);
    }

    public String getPeople() {
        return getString(people);
    }

//    private void setRemarks(String remarks) {
//        if (remarks != null) {
//            put(EventLog.remarks, remarks);
//        } else {
//            put(EventLog.remarks, "");
//        }
//    }

    public String getRemarks() {
        return (has(remarks)) ? getString(remarks) : "";
    }

//    private void setPosition(ParseGeoPoint position) {
//        put(EventLog.position, position);
//    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(position);
    }

    public void setSummary(String columnName, JSONArray summary, Date timeStarted, Date timeEnded) {
        put(columnName, summary);
        put(EventLog.timeStarted, timeStarted);
        put(EventLog.timeEnded, timeEnded);

//        String startString = Util.dateFormatHourMinutes().format(timeStarted);
//        String endString = Util.dateFormatHourMinutes().format(timeEnded);
//
//        put(EventLog.remarks, "Log for " + startString + " til " + endString)
    }

    public Date getDeviceTimestamp() {
        return getDate(deviceTimestamp);
    }

    public void setDeviceTimestamp(Date date) {
        put(deviceTimestamp, date);
    }

    public void setAmount(int amount) {
        put(EventLog.amount, amount);
    }

    /**
     * Fetches EventLog matching the given task and pin them to the datastore
     *
     * @param task
     */
    public void updateDatastore(GSTask task) {
        EventLog.QueryBuilder builder = EventLog.getQueryBuilder(false);
        builder.matching(task.getClient());
        builder.excludeAutomatic();
        builder.whereIsReportEntry();
        updateAll(builder.build(), new ExtendedParseObject.DataStoreCallback<EventLog>() {
            @Override
            public void success(List<EventLog> objects) {
                // yay
                EventBusController.postUIUpdate(objects);
            }

            @Override
            public void failed(ParseException e) {
                if (e != null) {
                    new HandleException(TAG, "updateDatastore", e);
                }
//                Crashlytics.log(ParseUser.getCurrentUser().getUsername() + " failed to update EventLog data for client " + getClient().getName());
            }
        });
    }


}

package com.guardswift.persistence.parse.documentation.event;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.common.collect.Lists;
import com.guardswift.R;
import com.guardswift.core.ca.ActivityDetectionModule;
import com.guardswift.core.ca.FingerprintingModule;
import com.guardswift.core.documentation.eventlog.context.LogContextStrategy;
import com.guardswift.core.documentation.eventlog.context.LogCurrentActivityStrategy;
import com.guardswift.core.documentation.eventlog.context.LogCurrentGuardStrategy;
import com.guardswift.core.documentation.eventlog.context.LogStrategyFactory;
import com.guardswift.core.documentation.eventlog.context.LogTimestampStrategy;
import com.guardswift.core.documentation.eventlog.task.LogTaskStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskClientLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskDistrictWatchLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskIdLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskLogStrategyFactory;
import com.guardswift.core.documentation.eventlog.task.TaskRegularLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskReportIdLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskStaticLogStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskTypeLogStrategy;
import com.guardswift.eventbus.EventBusController;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitStarted;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
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

import bolts.Task;
import dk.alexandra.positioning.wifi.AccessPoint;

//import com.guardswift.persistence.parse.execution.task.alarm.Alarm;

@ParseClassName("EventLog")
public class EventLog extends ExtendedParseObject {


    private String withSpace(String str) {
        if (str.isEmpty()) {
            return "";
        }

        return str + " ";
    }

    public String getSummaryString() {

        String timestamp = DateFormat.getTimeFormat(GuardSwiftApplication.getInstance()).format(getDeviceTimestamp());

        return withSpace(timestamp) +
                withSpace(getEvent()) +
                withSpace(getAmountString()) +
                withSpace(getPeople()) +
                withSpace(getLocations()) +
                withSpace(getRemarks());
    }

    public static class Recent {
        private static EventLog selected;

        public static EventLog getSelected() {
            return selected;
        }

        public static void setSelected(EventLog selected) {
            Recent.selected = selected;
        }

    }


    public static class EventCodes {
        public static final int CIRCUITUNIT_ARRIVED = 101;
        public static final int CIRCUITUNIT_FINISHED = 102;
        public static final int CIRCUITUNIT_ABORT = 103;
        public static final int CIRCUITUNIT_LEFT = 108;
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
        public static final int STATIC_ARRIVED = 131;
        public static final int STATIC_FINISHED = 132;
        public static final int STATIC_ABORT = 133;
        public static final int STATIC_OTHER = 134;
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
            List<LogContextStrategy> logStrategies = new LogStrategyFactory().getStrategies();

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


            return this;
        }

        public Builder automatic(boolean automatic) {
            eventLog.setAutomatic(automatic);
            return this;
        }

        public Builder deviceTimeStamp(Date time) {
            eventLog.setDeviceTimestamp(time);
            return this;
        }

        public Builder wifiSample(Set<AccessPoint> sample) {
            eventLog.put(EventLog.checkpoint_wifi_sample, FingerprintingModule.convertToJsonArray(sample));
            return this;
        }


        public Builder checkpoint(ClientLocation checkpoint, boolean automatic) {
            double probability = checkpoint.getLastProbability();
            double distance = checkpoint.getLastDistance();

            eventLog.put(EventLog.checkpoint, checkpoint);
            eventLog.put(EventLog.checkpoint_name, checkpoint.getLocation());
            eventLog.put(EventLog.checkpoint_probability, probability);
            eventLog.put(EventLog.checkpoint_distance, distance);


            event(context.getString(R.string.event_checkpoint));
            location(checkpoint.getLocation());
            automatic(automatic);


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
            saveAsync(null, null);
        }

        public void saveAsync(final GetCallback<EventLog> pinnedCallback) {
            saveAsync(pinnedCallback, null);
        }

        public void saveAsync(final GetCallback<EventLog> pinnedCallback, final GetCallback<EventLog> savedCallback) {

            Log.e(TAG, "Save event " + eventLog.getEvent());

//            Log.w(TAG, "1) Geocode start");
//            LocationModule.reverseGeocodedAddress(context).continueWith(new Continuation<GeocodedAddress, Object>() {
//                @Override
//                public Object then(Task<GeocodedAddress> task) throws Exception {
//                    Log.w(TAG, "2) Geocode done");
//                    final Capture<GeocodedAddress> geocodedAddress = new Capture<>();
//                    if (!task.isFaulted()) {
//                        geocodedAddress.set(task.getResult());
//                        eventLog.put(EventLog.geocodedAddress, task.getResult().toJSON());
//                    } else {
//                        // error reverse geocoding address
//                        // fall-thru as reverse geocoding is not strictly needed
//                        new HandleException(context, TAG, "reverseGeocodeAddress", task.getError());
//                    }


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

//                            updateGuardInfo(geocodedAddress.get());
                    updateGuardInfo();

                    if (savedCallback != null) {
                        savedCallback.done(eventLog, e);
                    }
                }
            });

//                    return null;
//                }
//            });

        }

        // Assert: eventlog is saved online
        private void updateGuardInfo() {
            Log.w(TAG, "6) Update guard");
            Guard guard = eventLog.getGuard();
            if (guard == null) {
                return;
            }

            guard.setPosition(eventLog.getPosition());
//            guard.setLastGeocodedAddress(reverseGeocodedAddress);
            guard.setLastEvent(eventLog);
            guard.pinThenSaveEventually();
        }


        public Builder correctGuess(boolean b) {
            eventLog.put(correctGuess, b);
            return this;
        }

//        public Builder summary(String columnName, JSONArray summary, Date timeStarted, Date timeEnded) {
//            eventLog.setSummary(columnName, summary, timeStarted, timeEnded);
//            return this;
//        }

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


//        public Builder locationTrackerUrl(String result) {
//            eventLog.put("locationTrackerUrl", result);
//            return this;
//        }
    }


    // pointers
    public static final String client = TaskClientLogStrategy.client;
    public static final String districtWatchStarted = TaskDistrictWatchLogStrategy.districtWatchStarted;
    public static final String districtWatchClient = TaskDistrictWatchLogStrategy.districtWatchClient;
    public static final String circuitStarted = TaskRegularLogStrategy.circuitStarted;
    public static final String circuitUnit = TaskRegularLogStrategy.circuitUnit;

    public static final String staticTask = TaskStaticLogStrategy.staticTask;
    // task
    public static final String taskId = TaskIdLogStrategy.taskId;
    public static final String reportId = TaskReportIdLogStrategy.reportId;
    // strings
    public static final String clientName = TaskClientLogStrategy.clientName;
    public static final String clientCity = TaskClientLogStrategy.clientCity;
    public static final String clientZipcode = TaskClientLogStrategy.clientZipcode;
    public static final String clientAddress = TaskClientLogStrategy.clientAddress;
    public static final String clientAddressNumber = TaskClientLogStrategy.clientAddressNumber;
    public static final String clientFullAddress = TaskClientLogStrategy.clientFullAddress;
    //
    public static final String guard = LogCurrentGuardStrategy.guard;
    public static final String guardId = LogCurrentGuardStrategy.guardId;
    public static final String guardName = LogCurrentGuardStrategy.guardName;
    // planned times
    public static final String timeStart = TaskRegularLogStrategy.timeStart;
    public static final String timeStartString = TaskRegularLogStrategy.timeStartString;
    public static final String timeEnd = TaskRegularLogStrategy.timeEnd;
    public static final String timeEndString = TaskRegularLogStrategy.timeEndString;
    // event description
//    public static final String task_type = "task_type"; // not task specific , e.g. ARRIVE, OTHER, etc.
    public static final String task_event = "task_event";
    public static final String type = "type";
    public static final String eventType = "eventType";
    public static final String event = "event";
    public static final String amount = "amount";
    public static final String people = "people";
    public static final String clientLocation = "clientLocation";
    public static final String remarks = "remarks";
    public static final String isExtra = "isExtra";
    public static final String withinSchedule = "withinSchedule";
    // gps
    public static final String clientDistanceMeters = TaskClientLogStrategy.clientDistanceMeters;
    public static final String provider = "provider";
    public static final String position = "position";
    public static final String accuracy = "accuracy";
    public static final String altitude = "altitude";
    public static final String bearing = "bearing";
    public static final String speed = "speed";
    public static final String geocodedAddress = "geocodedAddress";
    // activity
    public static final String activityType = LogCurrentActivityStrategy.activityType;
    public static final String activityConfidence = LogCurrentActivityStrategy.activityConfidence;
    public static final String activityName = LogCurrentActivityStrategy.activityName;
    // eventCode
    public static final String eventCode = "eventCode";
    // locations
//    public static final String locations = "locations";
    // checkpoint
    public static final String checkpoint = "checkpoint";
    public static final String checkpoint_name = "checkpoint_name";
    public static final String checkpoint_distance = "checkpoint_distance";
    public static final String checkpoint_probability = "checkpoint_probability";

//    public static final String checkpoint_guess = "checkpoint_guess";
//    public static final String checkpoint_guess_name = "checkpoint_guess_name";
//    public static final String checkpoint_guess_distance = "checkpoint_guess_distance";
//    public static final String checkpoint_guess_probability = "checkpoint_guess_probability";

    public static final String checkpoint_wifi_sample = "checkpoint_wifi_sample";
    // misc
    public static final String deviceTimestamp = LogTimestampStrategy.deviceTimestamp;
    //    public static final String clientTimestamp = "clientTimestamp"; // TODO deprecate in favor of deviceTimeStamp
    public static final String automatic = "automatic";
    public static final String correctGuess = "correctGuess";

    // summary
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";


    @Override
    public String getParseClassName() {
        return EventLog.class.getSimpleName();
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
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(EventLog.class));
        }

        @Override
        public ParseQuery<EventLog> build() {
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

//        public QueryBuilder matching(Alarm alarm) {
//            query.whereEqualTo(EventLog.alarm, alarm);
//            return this;
//        }

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

        public QueryBuilder matchingEventTypes(String... eventTypes) {
            List<String> events = Lists.newArrayList(eventTypes);
            if (!events.isEmpty())
                query.whereContainedIn(EventLog.eventType, events);
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


            List<Integer> arrived = Arrays.asList(EventCodes.CIRCUITUNIT_ARRIVED, EventCodes.ALARM_ARRIVED, EventCodes.DISTRICTWATCH_ARRIVED, EventCodes.STATIC_ARRIVED);
            List<Integer> written = Arrays.asList(EventCodes.STATIC_OTHER, EventCodes.ALARM_OTHER, EventCodes.CIRCUITUNIT_OTHER, EventCodes.DISTRICTWATCH_OTHER, EventCodes.CIRCUITUNIT_EXTRA_TIME);

            List<Integer> reportEntryCodes = Lists.newArrayList();
            reportEntryCodes.addAll(arrived);
            reportEntryCodes.addAll(written);

            query.whereContainedIn(eventCode, reportEntryCodes);

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

        public QueryBuilder orderByDescendingTimestamp() {
            query.orderByDescending(EventLog.deviceTimestamp);
            return this;
        }

        public QueryBuilder orderByAscendingTimestamp() {
            query.orderByAscending(EventLog.deviceTimestamp);
            return this;
        }
    }


    public CircuitUnit getCircuitUnit() {
        return (CircuitUnit) getParseObject(circuitUnit);
    }


    public boolean isReportEvent() {
        int eventCode = getEventCode();
        switch (eventCode) {
            case EventCodes.ALARM_OTHER:
                return true;
            case EventCodes.CIRCUITUNIT_OTHER:
                return true;
            case EventCodes.DISTRICTWATCH_OTHER:
                return true;
            case EventCodes.STATIC_OTHER:
                return true;
            case EventCodes.CIRCUITUNIT_EXTRA_TIME:
                return true;
            default:
                return false;
        }
    }

    public int getEventCode() {
        return getInt(eventCode);
    }

//    public void addClientProximity(float distanceMeters) {
//        put(clientDistanceMeters, distanceMeters);
//    }


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


    public String getTaskTypeName() {
        return getString(TaskTypeLogStrategy.taskTypeName);
    }

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

    public Task<Client> getClientInBackground() {
        Client client = getClient();
        if (client != null) {
            return client.fetchIfNeededInBackground();
        }
        return Task.forError(new ParseException(ParseException.OBJECT_NOT_FOUND, "Client not found"));
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
        return getStringSafe(event);
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

    public String getAmountString() {
        if (getAmount() > 0) {
            return String.valueOf(getAmount());
        }

        return "";
    }

//    private void setClientLocation(String location) {
//        if (location != null) {
//            put(EventLog.location, location);
//        } else {
//            put(EventLog.location, "");
//        }
//    }

    public String getLocations() {
        return getStringSafe(clientLocation);
    }

    public String getPeople() {
        return getStringSafe(people);
    }

//    private void setRemarks(String remarks) {
//        if (remarks != null) {
//            put(EventLog.remarks, remarks);
//        } else {
//            put(EventLog.remarks, "");
//        }
//    }

    public String getRemarks() {
        return getStringSafe(remarks);
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(position);
    }

    public Date getDeviceTimestamp() {
        Date date = getDate(deviceTimestamp);
        if (date == null) {
            date = getCreatedAt();
        }
        return (date != null) ? date : new Date();
    }

    public void setDeviceTimestamp(Date date) {
        put(deviceTimestamp, date);
    }

    public void setAmount(int amount) {
        put(EventLog.amount, amount);
    }

    public GSTask.TASK_TYPE getTaskType() {
        if (getTaskTypeName().equals(GSTask.TASK_TYPE.ALARM.toString())) {
            return GSTask.TASK_TYPE.ALARM;
        }
        if (has(EventLog.circuitUnit)) {
            return GSTask.TASK_TYPE.REGULAR;
        }
        if (has(EventLog.districtWatchClient)) {
            return GSTask.TASK_TYPE.DISTRICTWATCH;
        }
        if (has(EventLog.staticTask)) {
            return GSTask.TASK_TYPE.STATIC;
        }
        return null;
    }

    /**
     * Fetches EventLog matching the given task and pin them to the datastore
     *
     * @param task
     */
//    public void updateDatastore(GSTask task) {
//        EventLog.QueryBuilder builder = EventLog.getQueryBuilder(false);
//        builder.matching(task.getClient());
//        builder.excludeAutomatic();
//        builder.whereIsReportEntry();
//        updateAll(builder.build(), new ExtendedParseObject.DataStoreCallback<EventLog>() {
//            @Override
//            public void success(List<EventLog> objects) {
//                // yay
//                EventBusController.postUIUpdate(objects);
//            }
//
//            @Override
//            public void failed(ParseException e) {
//                if (e != null) {
//                    new HandleException(TAG, "updateDatastore", e);
//                }
////                Crashlytics.log(ParseUser.getCurrentUser().getUsername() + " failed to update EventLog data for client " + getClient().getName());
//            }
//        });
//    }


}

package com.guardswift.persistence.parse.documentation.event;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.guardswift.core.ca.activity.ActivityDetectionModule;
import com.guardswift.core.documentation.eventlog.context.LogContextStrategy;
import com.guardswift.core.documentation.eventlog.context.LogCurrentActivityStrategy;
import com.guardswift.core.documentation.eventlog.context.LogStrategyFactory;
import com.guardswift.core.documentation.eventlog.context.LogTimestampStrategy;
import com.guardswift.core.documentation.eventlog.task.LogTaskStrategy;
import com.guardswift.core.documentation.eventlog.task.TaskLogStrategyFactory;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.guardswift.persistence.parse.query.EventLogQueryBuilder;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Task;


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
        public static final int REGULAR_ARRIVED = 101;
        public static final int REGULAR_FINISHED = 102;
        public static final int REGULAR_ABORT = 103;
        public static final int REGULAR_LEFT = 108;
        public static final int REGULAR_OTHER = 104;
        public static final int REGULAR_EXTRA_TIME = 105;
        public static final int CIRCUITUNIT_CHECKPOINT_ARRIVED = 106;
        public static final int CIRCUITUNIT_CHECKPOINT_ABORTED = 107;
        public static final int AUTOMATIC_CIRCUITUNIT_CHECKPOINT_ARRIVED = 1006;
        public static final int AUTOMATIC_GEOFENCE_ENTER = 1101;
        public static final int AUTOMATIC_GEOFENCE_EXIT = 1102;
        public static final int AUTOMATIC_ARRIVED_ON_FOOT = 1201;
        public static final int AUTOMATIC_ARRIVED_STILL = 1202;
        public static final int AUTOMATIC_ARRIVED = 1301;
        public static final int AUTOMATIC_DEPARTURE = 1302;

        public static final int RAID_ARRIVED = 111;
        public static final int DISTRICTWATCH_FINISHED = 112;
        public static final int DISTRICTWATCH_ABORT = 113;
        public static final int RAID_OTHER = 114;
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

        private ParseTask parseTask;
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
        public Builder from(EventLog eventLog, ParseTask taskPointer) {

            for (String key : eventLog.keySet()) {
                if (eventLog.get(key) != null) {
                    this.eventLog.put(key, eventLog.get(key));
                }
            }

            applyDefaultLogValues();

            taskPointer(taskPointer, ParseTask.EVENT_TYPE.OTHER);

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


        public Builder taskPointer(ParseTask task, ParseTask.EVENT_TYPE event_type) {

            this.parseTask = task;
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
            eventLog.pinThenSaveEventually(PIN_NEW, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        new HandleException(TAG, "Pinning new EventLog", e);
                    }

                    if (pinnedCallback != null) {
                        pinnedCallback.done(eventLog, e);
                    }
                    Log.w(TAG, "4) Save event - pinned");
                }
            }, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        new HandleException(TAG, "Saving new EventLog", e);
                    }

                    Log.w(TAG, "5) Save event - saved");

//                            updateGuardInfo(geocodedAddress.get());
                    updateGuardInfo();

                    if (savedCallback != null) {
                        savedCallback.done(eventLog, e);
                    }
                }
            }, true);

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

            // makes it impossible to unpin eventlog
//            guard.setLastEvent(ParseObject.createWithoutData(EventLog.class, eventLog.getObjectId()));
            guard.saveEventually();
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
    public static final String client = "client";
    public static final String taskGroupStarted = "taskGroupStarted";
    public static final String taskGroup = "taskGroup";
    public static final String task = "task";

    // task
    public static final String taskId = "taskId";
    public static final String reportId = "reportId";
    public static final String taskType = "taskType";
    public static final String taskTypeName = "taskTypeName";
    public static final String taskTypeCode = "taskTypeCode";
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
    public static final String clientDistanceMeters = "clientDistanceMeters";
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

    public static final String checkpoint_wifi_sample = "checkpoint_wifi_sample";
    // misc
    public static final String deviceTimestamp = LogTimestampStrategy.deviceTimestamp;

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
        return new EventLogQueryBuilder(false).build();
    }


    public static EventLogQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new EventLogQueryBuilder(fromLocalDatastore);
    }


    public ParseTask getTask() {
        return (ParseTask) getParseObject(EventLog.task);
    }


    public boolean isReportEvent() {
        int eventCode = getEventCode();
        switch (eventCode) {
            case EventCodes.ALARM_OTHER:
                return true;
            case EventCodes.REGULAR_OTHER:
                return true;
            case EventCodes.RAID_OTHER:
                return true;
            case EventCodes.STATIC_OTHER:
                return true;
            case EventCodes.REGULAR_EXTRA_TIME:
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
        return getString(EventLog.taskTypeName);
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


    public Guard getGuard() {
        return (Guard) getParseObject(guard);
    }

    public String getGuardName() {
        return getString(EventLog.guardName);
    }



    public TaskGroupStarted getTaskGroupStarted() {
        return (TaskGroupStarted) getParseObject(EventLog.taskGroupStarted);
    }



    public String getEvent() {
        return getStringSafe(event);
    }

    public EventType getEventType() {
        return (EventType) getParseObject(eventType);
    }


    public String getType() {
        return getString(type);
    }


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


    public String getLocations() {
        return getStringSafe(clientLocation);
    }

    public String getPeople() {
        return getStringSafe(people);
    }


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

    public ParseTask.TASK_TYPE getTaskType() {
        String taskTypeName = getTaskTypeName();
        if (taskTypeName.equalsIgnoreCase(ParseTask.TASK_TYPE_STRING.ALARM)) {
            return ParseTask.TASK_TYPE.ALARM;
        }
        if (taskTypeName.equalsIgnoreCase(ParseTask.TASK_TYPE_STRING.REGULAR)) {
            return ParseTask.TASK_TYPE.REGULAR;
        }
        if (taskTypeName.equalsIgnoreCase(ParseTask.TASK_TYPE_STRING.RAID)) {
            return ParseTask.TASK_TYPE.RAID;
        }
        if (taskTypeName.equalsIgnoreCase(ParseTask.TASK_TYPE_STRING.STAITC)) {
            return ParseTask.TASK_TYPE.STATIC;
        }
        return null;
    }




}

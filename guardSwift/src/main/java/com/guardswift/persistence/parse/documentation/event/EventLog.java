package com.guardswift.persistence.parse.documentation.event;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.guardswift.eventbus.events.UpdateUIEvent;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.EventType;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
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


        public static final int RAID_ARRIVED = 111;
        public static final int RAID_OTHER = 114;
        public static final int ALARM_ACCEPTED = 120;
        public static final int ALARM_ARRIVED = 121;
        public static final int ALARM_FINISHED = 122;
        public static final int ALARM_ABORT = 123;
        public static final int ALARM_OTHER = 124;
        public static final int STATIC_ARRIVED = 131;
        public static final int STATIC_FINISHED = 132;
        public static final int STATIC_OTHER = 134;
        public static final int GUARD_LOGIN = 200;
        public static final int GUARD_LOGOUT = 201;

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

            TaskLogStrategyFactory logStrategyFactory = new TaskLogStrategyFactory();

            List<LogTaskStrategy> logStrategies = logStrategyFactory.getStrategies();

            for (LogTaskStrategy logStrategy : logStrategies) {
                logStrategy.log(task, eventLog);
            }


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

        /**
         * Automatically adds current CircuitStarted, Guard, Location, client proximity and DeviceTimestamp
         * before calling saveeventually and broadcast a UI update
         */
        public void saveAsync() {
            saveAsync(null, null);
        }

        public void saveAsync(final GetCallback<EventLog> pinnedCallback) {
            saveAsync(pinnedCallback, null);
        }

        public void saveAsync(final GetCallback<EventLog> pinnedCallback, final GetCallback<EventLog> savedCallback) {

            Log.e(TAG, "Save event " + eventLog.getEvent());

            // Todo: remove pinnedCallback
            if (pinnedCallback != null) {
                pinnedCallback.done(eventLog, null);
            }

            Log.w(TAG, "3) Save event");
            eventLog.saveEventuallyAndNotify(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        new HandleException(TAG, "Saving new EventLog", e);
                    }

//                    EventBusController.postUIUpdate(eventLog, UpdateUIEvent.ACTION.CREATE);

                    Log.w(TAG, "5) Save event - saved");

                    updateGuardInfo();

                    if (savedCallback != null) {
                        savedCallback.done(eventLog, e);
                    }
                }
            }, UpdateUIEvent.ACTION.CREATE);

        }

        // Assert: eventlog is saved online
        private void updateGuardInfo() {
            Log.w(TAG, "6) Update guard");
            Guard guard = eventLog.getGuard();
            if (guard == null) {
                return;
            }

            guard.setPosition(eventLog.getPosition());
            guard.saveInBackground();
        }

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


            JSONObject jsonObject = new JSONObject(map);

            return jsonObject;
        }


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

    // misc
    public static final String deviceTimestamp = LogTimestampStrategy.deviceTimestamp;

    public static final String automatic = "automatic";
    public static final String correctGuess = "correctGuess";



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

    public boolean isArrivalEvent() {
        int eventCode = getEventCode();

        return eventCode == EventCodes.ALARM_ARRIVED || eventCode == EventCodes.RAID_ARRIVED || eventCode == EventCodes.REGULAR_ARRIVED;
    }


    private String getTaskTypeName() {
        return getString(EventLog.taskTypeName);
    }

    private void setAutomatic(boolean automatic) {
        put(EventLog.automatic, automatic);
    }


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


    public Guard getGuard() {
        return (Guard) getParseObject(guard);
    }

    public String getGuardName() {
        return getString(EventLog.guardName);
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

    private String getAmountString() {
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
        if (taskTypeName.equalsIgnoreCase(ParseTask.TASK_TYPE_STRING.STATIC)) {
            return ParseTask.TASK_TYPE.STATIC;
        }
        return null;
    }

    public String getReportId() {
        return getStringSafe(EventLog.reportId);
    }

    @Override
    public int compareTo(@NonNull ExtendedParseObject another) {
        if (another instanceof EventLog) {
            return ((EventLog) another).getDeviceTimestamp().compareTo(getDeviceTimestamp());
        }
        return super.compareTo(another);
    }
}

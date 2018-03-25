package com.guardswift.persistence.parse.execution.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.guardswift.BuildConfig;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.tasks.activity.ArriveWhenNotInVehicleStrategy;
import com.guardswift.core.tasks.activity.NoActivityStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
import com.guardswift.core.tasks.automation.FinishOnDepartureAutomationStrategy;
import com.guardswift.core.tasks.automation.NoAutomationStrategy;
import com.guardswift.core.tasks.automation.ResetOnDepartureAutomationStrategy;
import com.guardswift.core.tasks.automation.StandardTaskAutomationStrategy;
import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
import com.guardswift.core.tasks.controller.AlarmController;
import com.guardswift.core.tasks.controller.RaidController;
import com.guardswift.core.tasks.controller.RegularController;
import com.guardswift.core.tasks.controller.StaticTaskController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.geofence.AlarmGeofenceStrategy;
import com.guardswift.core.tasks.geofence.NoGeofenceStrategy;
import com.guardswift.core.tasks.geofence.RaidGeofenceStrategy;
import com.guardswift.core.tasks.geofence.RegularGeofenceStrategy;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

import bolts.Continuation;
import bolts.Task;


@ParseClassName("Task")
public class ParseTask extends ExtendedParseObject implements Positioned {

    public static final int DEFAULT_RADIUS_RAID = 50;
    public static final int DEFAULT_RADIUS_REGULAR = 100;
    public static final int DEFAULT_RADIUS_ALARM = 100;


    public enum TASK_TYPE {REGULAR, RAID, STATIC, ALARM}

    public enum TASK_STATE {PENDING, ACCEPTED, ARRIVED, ABORTED, FINISHED}

    public enum EVENT_TYPE {BEGIN, ARRIVE, ABORT, CHECKPOINT, FINISH, DEPARTURE, ACCEPT, GEOFENCE_ENTER, GEOFENCE_EXIT, GEOFENCE_ENTER_GPS, GEOFENCE_EXIT_GPS, OTHER, LEAVE}

    public static final String taskGroup = "taskGroup";
    public static final String taskGroupStarted = "taskGroupStarted";

    public static final String taskType = "taskType"; // Regular, District, Alarm, ..
    public static final String status = "status"; // Pending, Accepted, Arrived, Finished

    public static final String type = "type"; // short desc of task provided by owner
    public static final String guard = "guard";
    public static final String client = "client";
    public static final String position = "position";

    public static final String clientId = "clientId";
    public static final String clientName = "clientName";
    public static final String clientAddress = "clientAddress";

    public static final String street = "street";
    public static final String streetNumber = "streetNumber";
    public static final String postalCode = "postalCode";
    public static final String city = "city";
    public static final String formattedAddress = "formattedAddress";
    public static final String fullAddress = "fullAddress";

//    public static final String timeStarted = "timeStarted";
//    public static final String timeEnded = "timeEnded";

    public static final String geofenceRadius = "geofenceRadius";

    // Alarm
    public static final String central = "central";
    public static final String centralName = "centralName";
    public static final String signalStatus = "signalStatus";
    public static final String keybox = "keybox";
    public static final String remarks = "remarks";
    public static final String priority = "priority";

    public static final String knownStatus = "knownStatus";
    public static final String original = "original";

    // Regular/Raid
    public static final String name = "name";
    public static final String days = "days";
    public static final String isRunToday = "isRunToday";
    public static final String timeStartDate = "timeStartDate";
    public static final String timeEndDate = "timeEndDate";
    public static final String supervisions = "supervisions";
    public static final String timesArrived = "timesArrived";


    public static class STATUS {
        public static String PENDING = "pending";
        public static String ACCEPTED = "accepted";
        public static String ARRIVED = "arrived";
        public static String ABORTED = "aborted";
        public static String FINISHED = "finished";
    }

    public static class TASK_TYPE_STRING {
        public static String ALARM = "Alarm";
        public static String REGULAR = "Regular";
        public static String RAID = "Raid";
        public static String STATIC = "Static";
    }


    private ParseTasksCache tasksCache;

    public ParseTask() {
        tasksCache = GuardSwiftApplication.getInstance().getCacheFactory().getTasksCache();
    }

    public BaseTaskCache<ParseTask> getCache() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getTaskCache();
    }

    public TaskGeofenceStrategy getGeofenceStrategy() {
        switch (this.getTaskType()) {
            case ALARM:
                return AlarmGeofenceStrategy.getInstance(this);
            case REGULAR:
                return RegularGeofenceStrategy.getInstance(this);
            case RAID:
                return RaidGeofenceStrategy.getInstance(this);
            case STATIC:
                return NoGeofenceStrategy.getInstance(this);
        }
        return null;
    }

    public TaskActivityStrategy getActivityStrategy() {
        switch (this.getTaskType()) {
            case ALARM:
                return NoActivityStrategy.getInstance();
            case REGULAR:
                return ArriveWhenNotInVehicleStrategy.getInstance(this);
            case RAID:
                return NoActivityStrategy.getInstance();
            case STATIC:
                return NoActivityStrategy.getInstance();
        }
        return null;
    }

    public TaskAutomationStrategy getAutomationStrategy() {
        switch (this.getTaskType()) {
            case ALARM:
                return FinishOnDepartureAutomationStrategy.getInstance(this);
            case REGULAR:
                return StandardTaskAutomationStrategy.getInstance(this);
            case RAID:
                return ResetOnDepartureAutomationStrategy.getInstance(this);
            case STATIC:
                return NoAutomationStrategy.getInstance();
        }
        return null;
    }

    public TaskController getController() {
        switch (this.getTaskType()) {
            case ALARM:
                return AlarmController.getInstance();
            case REGULAR:
                return RegularController.getInstance();
            case STATIC:
                return StaticTaskController.getInstance();
            case RAID:
                return RaidController.getInstance();
        }
        return null;
    }


    public int getEventCode() {
        switch (this.getTaskType()) {
            case ALARM:
                return EventLog.EventCodes.ALARM_OTHER;
            case REGULAR:
                return EventLog.EventCodes.REGULAR_OTHER;
            case STATIC:
                return EventLog.EventCodes.STATIC_OTHER;
            case RAID:
                return EventLog.EventCodes.RAID_OTHER;
        }
        return 0;
    }

    public void setTaskType(TASK_TYPE taskType) {
        String taskTypeString = "";
        switch (taskType) {
            case ALARM:
                taskTypeString = TASK_TYPE_STRING.ALARM;
                break;
            case REGULAR:
                taskTypeString = TASK_TYPE_STRING.REGULAR;
                break;
            case STATIC:
                taskTypeString = TASK_TYPE_STRING.STATIC;
                break;
            case RAID:
                taskTypeString = TASK_TYPE_STRING.RAID;
                break;
        }

        put(ParseTask.taskType, taskTypeString);
    }

    public String getTaskTypeString() {
        return getString(ParseTask.taskType);
    }

    public TASK_TYPE getTaskType() {
        switch (getTaskTypeString()) {
            case "Alarm":
                return TASK_TYPE.ALARM;
            case "Regular":
                return TASK_TYPE.REGULAR;
            case "Static":
                return TASK_TYPE.STATIC;
            case "Raid":
                return TASK_TYPE.RAID;
        }

        new HandleException(TAG, "Task missing taskType", null);

        return null;
    }

    public boolean isAlarmTask() {
        return getTaskType().equals(TASK_TYPE.ALARM);
    }

    public boolean isRegularTask() {
        return getTaskType().equals(TASK_TYPE.REGULAR);
    }

    public boolean isRaidTask() {
        return getTaskType().equals(TASK_TYPE.RAID);
    }

    public boolean isStaticTask() {
        return getTaskType().equals(TASK_TYPE.STATIC);
    }


    public TaskGroupStarted getTaskGroupStarted() {
        return (TaskGroupStarted) getParseObject(ParseTask.taskGroupStarted);
    }

    private void setStatus(String status) {

        // prevent abort notification to be broadcast by server
        if (status.equals(STATUS.ABORTED)) {
            addKnownStatus(status);
        }

        put(ParseTask.status, status);
    }

    public String getStatus() {
        return getStringSafe(ParseTask.status, STATUS.PENDING);
    }

    public TASK_STATE getTaskState() {
        if (isFinished()) {
            return TASK_STATE.FINISHED;
        }
        if (isAborted()) {
            return TASK_STATE.ABORTED;
        }
        if (isArrived()) {
            return TASK_STATE.ARRIVED;
        }
        if (isAccepted()) {
            return TASK_STATE.ACCEPTED;
        }
        return TASK_STATE.PENDING;
    }

    public String getType() {
        return has(ParseTask.type) ? getString(ParseTask.type) : "";
    }

//    public String getReportId() {
//        String groupId = this.getObjectId();
//        if (has(ParseTask.client) && has(ParseTask.taskGroupStarted)) {
//            groupId = getTaskGroupStarted().getObjectId() + "-" + getClient().getObjectId();
//        }
//        return groupId;
//    }


    public ExtendedParseObject getParseObject() {
        return this;
    }


    @Override
    public <T extends ParseObject> ParseQuery<T> getAllNetworkQuery() {
        return null;
    }


    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    public void setClient(Client client) {
        put(ParseTask.client, client);
    }

    public Client getClient() {
        return (Client) getLDSFallbackParseObject(ParseTask.client);
    }

    public String getClientId() {
        return getStringSafe(ParseTask.clientId);
    }

    public String getClientName() {
        return getStringSafe(ParseTask.clientName);
    }

    public String getClientAddress() {
        return getStringSafe(ParseTask.clientAddress);
    }

    private void setGuardCurrent() {
        put(ParseTask.guard, GuardSwiftApplication.getLastActiveGuard());
    }

    public void setGuard(Guard guard) {
        if (guard == null) {
            remove(ParseTask.guard);
            return;
        }
        put(ParseTask.guard, guard);
    }

    public boolean isRunToday() {
        if (isRegularTask() || isRaidTask()) {
            return getBoolean(ParseTask.isRunToday);
        }

        return true;
    }

    public Guard getGuard() {
        return (Guard) getLDSFallbackParseObject(ParseTask.guard);
    }

    public String getStreet() {
        return getStringSafe(ParseTask.street);
    }

    public String getStreetNumber() {
        return getStringSafe(ParseTask.streetNumber);
    }

    public String getPostalCode() {
        return getStringSafe(ParseTask.postalCode);
    }

    public String getCity() {
        return getStringSafe(ParseTask.city);
    }

    public String getFormattedAddress() {
        return getStringSafe(ParseTask.formattedAddress);
    }

    public String getFullAddress() {
        return getStringSafe(ParseTask.fullAddress);
    }

    public String getCentralName() {
        return getStringSafe(ParseTask.centralName);
    }

    public String getSignalStatus() {
        return getStringSafe(ParseTask.signalStatus);
    }

    public String getKeybox() {
        return getStringSafe(ParseTask.keybox);
    }

    public String getRemarks() {
        return getStringSafe(ParseTask.remarks);
    }

    public String getPriority() {
        return getStringSafe(ParseTask.priority);
    }

    public void setPending() {
        setStatus(STATUS.PENDING);
    }

    public void setAccepted() {
//        setTimeStartedNow();
        setStatus(STATUS.ACCEPTED);
        setGuardCurrent();
    }

    public void setArrived() {
        setStatus(STATUS.ARRIVED);
        setGuardCurrent();
        increment(ParseTask.timesArrived);
    }

    public void deleteArrival() {
        put(ParseTask.timesArrived, getTimesArrived() - 1);
    }

    public void reset() {
        setGuard(null);
        setPending();
    }

    public void setAborted() {
//        setTimeEndedNow();
        setStatus(STATUS.ABORTED);
        setGuardCurrent();

        tasksCache.removeGeofence(this);
    }

    public void setFinished() {
//        setTimeEndedNow();
        setStatus(STATUS.FINISHED);
        setGuardCurrent();

        tasksCache.removeGeofence(this);
    }

    public void addKnownStatus(String status) {
        addUnique(ParseTask.knownStatus, status);
    }

    public boolean isPending() {
        return getStatus().equals(STATUS.PENDING);
    }

    public boolean isAccepted() {
        return getStatus().equals(STATUS.ACCEPTED);
    }

    public boolean isArrived() {
        return getStatus().equals(STATUS.ARRIVED);
    }

    public boolean isAborted() {
        return getStatus().equals(STATUS.ABORTED);
    }

    public boolean isFinished() {
        return getStatus().equals(STATUS.FINISHED);
    }

    public boolean isAfterScheduledStartTime() {
        if (isRegularTask() || isRaidTask()) {
            try {
                DateTimeZone dtz = DateTimeZone.getDefault();

                LocalDateTime plannedTimeStart = new LocalDateTime(getTimeStart(), dtz);
                // use task date hour and minute components
                LocalDateTime timeStart = new LocalDateTime(dtz)
                        .withHourOfDay(plannedTimeStart.getHourOfDay())
                        .withMinuteOfHour(plannedTimeStart.getMinuteOfHour());

                // In case of time savings, add +1 hour
                // https://stackoverflow.com/questions/5451152/how-to-handle-jodatime-illegal-instant-due-to-time-zone-offset-transition
                if (dtz.isLocalDateTimeGap(timeStart)) {
                    timeStart.withHourOfDay(plannedTimeStart.getHourOfDay() + 1);
                }

                DateTime now = DateTime.now(dtz);

                return now.isAfter(timeStart.toDateTime());
            } catch (Exception e) {
                new HandleException(TAG, "isAfterScheduledStartTime", e);
            }
        }

        return true;
    }

    public boolean isBeforeScheduledEndTime() {
        if (isRegularTask() || isRaidTask()) {
            try {
                DateTimeZone dtz = DateTimeZone.getDefault();

                LocalDateTime plannedTimeEnd = new LocalDateTime(getTimeEnd(), dtz);
                // use task date hour and minute components
                LocalDateTime timeEnd = new LocalDateTime(dtz)
                        .withHourOfDay(plannedTimeEnd.getHourOfDay())
                        .withMinuteOfHour(plannedTimeEnd.getMinuteOfHour());

                // In case of time savings, add +1 hour
                // https://stackoverflow.com/questions/5451152/how-to-handle-jodatime-illegal-instant-due-to-time-zone-offset-transition
                if (dtz.isLocalDateTimeGap(timeEnd)) {
                    timeEnd.withHourOfDay(plannedTimeEnd.getHourOfDay() + 1);
                }

                DateTime now = DateTime.now(dtz);

                return now.isBefore(timeEnd.toDateTime());
            } catch (Exception e) {
                new HandleException(TAG, "isBeforeScheduledEndTime", e);
            }

        }

        return true;
    }

    public boolean isWithinScheduledTime() {
        // always return true if debugging
        if (BuildConfig.DEBUG) {
            return true;
        }

        return isAfterScheduledStartTime() && isBeforeScheduledEndTime();
    }

    public int getRadius() {
        if (has(ParseTask.geofenceRadius)) {
            return getInt(geofenceRadius);
        }
        switch (this.getTaskType()) {
            case ALARM:
                return DEFAULT_RADIUS_ALARM;
            case REGULAR:
                return DEFAULT_RADIUS_REGULAR;
            case STATIC:
                return 0;
            case RAID:
                return DEFAULT_RADIUS_RAID;
        }

        return 0;
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(ParseTask.position);
    }

//    public Date getTimeStarted() {
//        return getDate(timeStarted);
//    }
//
//
//    private void setTimeStartedNow() {
//        put(ParseTask.timeStarted, new Date());
//    }
//
//
//    private void setTimeEndedNow() {
//        put(ParseTask.timeEnded, new Date());
//    }

    public String getOriginal() {
        return getStringSafe(original, "");
    }


    // Used for sorting
    @Override
    public int compareTo(@NonNull ExtendedParseObject another) {
        // TODO introduce swappable compare strategies
        if (another instanceof ParseTask && (isRegularTask() || isRaidTask())) {
            ParseTask otherTask = (ParseTask) another;

            // another has clientId and this does not
            if (this.getClientId().isEmpty() && !otherTask.getClientId().isEmpty()) {
                return -1;
            }
            // this has clientId and another does not
            if (!this.getClientId().isEmpty() && otherTask.getClientId().isEmpty()) {
                return 1;
            }
            // both has clientId
            if (!this.getClientId().isEmpty() && !otherTask.getClientId().isEmpty()) {
                return this.getClientId().compareTo(otherTask.getClientId());
            }
            // neither contains clientId
            return this.getClientName().compareTo(otherTask.getClientName());
        } else {
            return 0;
        }
    }

    /**
     * REGULAR
     */

    public String getName() {
        return getStringSafe(ParseTask.name);
    }

    public Date getTimeStart() {
        return getDate(ParseTask.timeStartDate);
    }

    public String getTimeStartString() {
        Date date = getTimeStart();
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

    public Date getTimeEnd() {
        return getDate(ParseTask.timeEndDate);
    }

    public String getTimeEndString() {
        Date date = getDate(ParseTask.timeEndDate);
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

    public int getPlannedSuperVisions() {
        return getInt(ParseTask.supervisions);
    }


    public int getTimesArrived() {
        return getInt(ParseTask.timesArrived);
    }

    public boolean isCompletedButNotMarkedFinished() {
        return isPending() && getTimesArrived() >= getPlannedSuperVisions();
    }

    /**
     * STATIC
     */

    public static void createStaticTask(Client client, final GetCallback<ParseTask> getCallback) {
        final ParseTask task = new ParseTask();
        task.setTaskType(TASK_TYPE.STATIC);
        task.setDefaultOwner();
        task.setClient(client);
        task.saveEventuallyAndNotify(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                getCallback.done(task, e);
            }
        });
    }

    public void addReportEntry(Context context, String remarks, GetCallback<EventLog> pinned) {
        addReportEntry(context, remarks, pinned, null);
    }

    public void addReportEntry(Context context, String remarks, GetCallback<EventLog> pinned, GetCallback<EventLog> saved) {
        new EventLog.Builder(context)
                .taskPointer(this, ParseTask.EVENT_TYPE.OTHER)
                .remarks(remarks)
                .eventCode(this.getEventCode())
                .saveAsync(pinned, saved);
    }

    public void setStartedBy(Guard guard) {
        setGuard(guard);
        setArrived();
//        setTimeStartedNow();
    }

    public Task<Report> findReport(final boolean fromLocalDataStore) {
        return Report.getQueryBuilder(fromLocalDataStore).matching(this).build().getFirstInBackground().continueWithTask(new Continuation<Report, Task<Report>>() {
            @Override
            public Task<Report> then(Task<Report> reportTask) throws Exception {
                if (reportTask.isFaulted()) {
                    Exception error = reportTask.getError();
                    if (error instanceof ParseException && ((ParseException) error).getCode() != ParseException.OBJECT_NOT_FOUND) {
                        // it is expected that new reports are not found, any other errors, however, are reported
                        new HandleException(TAG, "FindReport", error);

                        return reportTask;
                    } else {
                        if (fromLocalDataStore) {
                            Log.w(TAG, "Report not found locally - attempt online");
                            return findReport(false);
                        } else {

                            new HandleException(TAG, "Not able to find report", error);

                            throw error;
                        }
                    }
                }
                Report report = reportTask.getResult();

                // successfully located report
                // store in LDS if found online
                if (!fromLocalDataStore) {
                    report.pinInBackground();
                }

                return Task.forResult(report);
            }
        });
    }

    public boolean matchesSelectedTaskGroupStarted() {
        TaskGroupStartedCache taskGroupStartedCache = GuardSwiftApplication.getInstance().getCacheFactory().getTaskGroupStartedCache();

        if (taskGroupStartedCache.getSelected() != null) {
            if (getTaskGroupStarted() != null) {
                boolean isMatch = getTaskGroupStarted().equals(taskGroupStartedCache.getSelected());
                //Log.d(TAG, "isMatch: " + isMatch);
                return isMatch;
            }
        }

        // if guard forget to select a taskgroup, always return true
        return taskGroupStartedCache.getSelected() == null;
    }
}

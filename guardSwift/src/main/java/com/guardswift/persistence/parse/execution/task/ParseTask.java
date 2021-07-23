package com.guardswift.persistence.parse.execution.task;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.guardswift.BuildConfig;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.tasks.controller.AlarmController;
import com.guardswift.core.tasks.controller.RaidController;
import com.guardswift.core.tasks.controller.RegularController;
import com.guardswift.core.tasks.controller.StaticTaskController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.context.AlarmContextStrategy;
import com.guardswift.core.tasks.context.NoContextUpdateStrategy;
import com.guardswift.core.tasks.context.RaidContextStrategy;
import com.guardswift.core.tasks.context.RegularContextStrategy;
import com.guardswift.core.tasks.context.ContextUpdateStrategy;
import com.guardswift.persistence.cache.planning.TaskGroupStartedCache;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.cache.task.ParseTasksCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.documentation.report.Report;
import com.guardswift.persistence.parse.query.EventLogQueryBuilder;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import bolts.Task;


@ParseClassName("Task")
public class ParseTask extends ExtendedParseObject implements Positioned {

    public static final int DEFAULT_RADIUS_RAID = 75;
    public static final int DEFAULT_RADIUS_REGULAR = 150;
    public static final int DEFAULT_RADIUS_ALARM = 100;

    public enum TASK_TYPE {REGULAR, RAID, STATIC, ALARM}

    public enum TASK_STATE {PENDING, ACCEPTED, ARRIVED, ABORTED, FINISHED}

    public enum EVENT_TYPE {BEGIN, ARRIVE, ABORT, CHECKPOINT, FINISH, DEPARTURE, ACCEPT, GEOFENCE_ENTER, GEOFENCE_EXIT, GEOFENCE_ENTER_GPS, GEOFENCE_EXIT_GPS, OTHER, PENDING}

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
    public static final String isWeekly = "isWeekly";

    public static final String minutesBetweenArrivals = "minutesBetweenArrivals";
    public static final String lastArrivalDate = "lastArrivalDate";

    public static final String expireDate = "expireDate";


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

    public ContextUpdateStrategy getContextUpdateStrategy() {
        switch (this.getTaskType()) {
            case ALARM:
                return AlarmContextStrategy.getInstance(this);
            case REGULAR:
                return RegularContextStrategy.getInstance(this);
            case RAID:
                return RaidContextStrategy.getInstance(this);
            case STATIC:
                return NoContextUpdateStrategy.getInstance(this);
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
        return getStringSafe(ParseTask.taskType);
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

    public TaskGroup getTaskGroup() {
        return (TaskGroup) getParseObject(ParseTask.taskGroup);
    }

    public void setTaskGroup(TaskGroup taskGroup) {
        put(ParseTask.taskGroup, taskGroup);
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

    public void addDays(Set<Integer> days) {
        addAllUnique(ParseTask.days, days);
    }

    public List<Integer> getDays() {
        return getList(ParseTask.days);
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

    public Date getArrivalDate(int hourOfDay, int minuteOfHour) {
        DateTimeZone dtz = DateTimeZone.getDefault();

        LocalDateTime taskGroupResetTime = new LocalDateTime(getTaskGroupResetDate(), dtz);

        LocalDateTime now = new LocalDateTime(dtz);
        LocalDateTime hourAndMinuteTime = new LocalDateTime(dtz)
                .withHourOfDay(hourOfDay)
                .withMinuteOfHour(minuteOfHour);

        boolean nowHasPastMidnight = now.getHourOfDay() <= taskGroupResetTime.getHourOfDay();
        boolean inputTimeHasPastMidnight = hourAndMinuteTime.getHourOfDay() <= taskGroupResetTime.getHourOfDay();

        if (nowHasPastMidnight && !inputTimeHasPastMidnight) {
            hourAndMinuteTime = hourAndMinuteTime.minusDays(1);
        }

        if (inputTimeHasPastMidnight && !nowHasPastMidnight) {
            hourAndMinuteTime = hourAndMinuteTime.plusDays(1);
        }

        return hourAndMinuteTime.toDate();
    }

    public void setArrived() {
        setStatus(STATUS.ARRIVED);
        setGuardCurrent();
    }

    public void incrementArrivedCount() {
        setLastArrivalDate(new Date());
        increment(ParseTask.timesArrived);
    }

    public void deleteArrival() {
        // disable time between arrivals restriction
        // TODO: here we should query eventlogs and pick the most recent arrival date
        setLastArrivalDate(new Date(1970));

        put(ParseTask.timesArrived, getTimesArrived() - 1);
    }

    public void setAborted() {
//        setTimeEndedNow();
        setStatus(STATUS.ABORTED);
        setGuardCurrent();
    }

    public void setFinished() {
//        setTimeEndedNow();
        setStatus(STATUS.FINISHED);
        setGuardCurrent();
    }

    private void addKnownStatus(String status) {
        addUnique(ParseTask.knownStatus, status);
    }

    public boolean isPending() {
        return getStatus().equals(STATUS.PENDING);
    }

    private boolean isAccepted() {
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

    private Date getTaskGroupResetDate() {
        TaskGroup taskGroup = getTaskGroup();

        return taskGroup != null && taskGroup.isDataAvailable() ? taskGroup.getAdjustedResetDate() : new Date();
    }

    private LocalDateTime getAdjustedTime(Date hourAndMinute, DateTimeZone dtz) {


        LocalDateTime taskGroupResetTime = new LocalDateTime(getTaskGroupResetDate(), dtz);
        LocalDateTime hourAndMinuteDateTime = new LocalDateTime(hourAndMinute, dtz);

        // use task date hour and minute components
        LocalDateTime time = taskGroupResetTime
                .withHourOfDay(hourAndMinuteDateTime.getHourOfDay())
                .withMinuteOfHour(hourAndMinuteDateTime.getMinuteOfHour());

        // In case of time savings, add +1 hour
        // https://stackoverflow.com/questions/5451152/how-to-handle-jodatime-illegal-instant-due-to-time-zone-offset-transition
        if (dtz.isLocalDateTimeGap(time)) {
            time = time.plusHours(1);
        }

        if (hourAndMinuteDateTime.getHourOfDay() <= taskGroupResetTime.getHourOfDay()) {
            time = time.plusDays(1);
        }

        return time;
    }

    public boolean isAfterScheduledStartTime() {
        if (isRegularTask() || isRaidTask()) {
            try {
                DateTimeZone dtz = DateTimeZone.getDefault();
                LocalDateTime timeStart = getAdjustedTime(getTimeStart(), dtz);

                return DateTime.now(dtz).isAfter(timeStart.toDateTime());
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
                LocalDateTime timeEnd = getAdjustedTime(getTimeEnd(), dtz);

                return DateTime.now(dtz).isBefore(timeEnd.toDateTime());
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

    public boolean isWithinScheduledTimeRelaxed() {
        int relaxMinutes = 10;

        // always return true if debugging
        if (BuildConfig.DEBUG) {
            return true;
        }

        if (isRegularTask() || isRaidTask()) {
            try {
                DateTimeZone dtz = DateTimeZone.getDefault();

                DateTime now = DateTime.now(dtz);

                LocalDateTime timeEnd = getAdjustedTime(getTimeEnd(), dtz);
                long diffMsAfterEnd = Math.abs(now.toDate().getTime() - timeEnd.toDate().getTime());
                long diffMinutesAfterEnd = TimeUnit.MINUTES.convert(diffMsAfterEnd, TimeUnit.MILLISECONDS);
                boolean isBeforeEnd = now.isBefore(timeEnd.toDateTime());
                boolean isBeforeEndRelaxed = isBeforeEnd || diffMinutesAfterEnd < relaxMinutes;

                LocalDateTime timeStart = getAdjustedTime(getTimeStart(), dtz);
                long diffMsBeforeStart = Math.abs(now.toDate().getTime() - timeStart.toDate().getTime());
                long diffMinutesBeforeStart = TimeUnit.MINUTES.convert(diffMsBeforeStart, TimeUnit.MILLISECONDS);
                boolean isAfterStart = now.isAfter(timeStart.toDateTime());
                boolean isAfterStartRelaxed = isAfterStart || diffMinutesBeforeStart < relaxMinutes;

                return isBeforeEndRelaxed && isAfterStartRelaxed;
            } catch (Exception e) {
                new HandleException(TAG, "isBeforeScheduledEndTime", e);
            }

        }

        return true;
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

    public void setTimeStart(Date timeStart) {
        put(ParseTask.timeStartDate, timeStart);
    }

    public Date getTimeStart() {
        return getDate(ParseTask.timeStartDate);
    }

    public String getTimeStartString() {
        Date date = getTimeStart();
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

    public void setTimeEnd(Date timeEnd) {
        put(ParseTask.timeEndDate, timeEnd);
    }

    public Date getTimeEnd() {
        return getDate(ParseTask.timeEndDate);
    }

    public String getTimeEndString() {
        Date date = getDate(ParseTask.timeEndDate);
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

    public void setExpireDate(Date date) {
        put(ParseTask.expireDate, date);
    }

    public Date getExpireDate() {
        return getDate(ParseTask.expireDate);
    }

    public long getMinutesSinceLastArrival() {
        Date lastArrivalDate = getLastArrivalDate();
        long timeSinceLastArrivalMs = lastArrivalDate != null ? Math.abs(new Date().getTime() - lastArrivalDate.getTime()) : Long.MAX_VALUE;

        Log.d(TAG, "timeSinceLastArrivalMs: " + timeSinceLastArrivalMs);

        return TimeUnit.MINUTES.convert(timeSinceLastArrivalMs, TimeUnit.MILLISECONDS);
    }

    public int getMinutesBetweenArrivals() {
        return getIntSafe(ParseTask.minutesBetweenArrivals, 10);
    }

    public void setLastArrivalDate(Date date) {
        put(ParseTask.lastArrivalDate, date);
    }

    private Date getLastArrivalDate() {
        Date lastArrivalDate = getDate(ParseTask.lastArrivalDate);
        return lastArrivalDate != null ? lastArrivalDate : new Date(1970);
    }

    public void setPlannedSupervisions(int supervisions) {
        put(ParseTask.supervisions, supervisions);
    }

    public int getPlannedSupervisions() {
        return getInt(ParseTask.supervisions);
    }


    public int getTimesArrived() {
        return getInt(ParseTask.timesArrived);
    }

    public boolean isCompletedButNotMarkedFinished() {
        return isPending() && getTimesArrived() >= getPlannedSupervisions();
    }

    public static void createStaticTask(Client client, final GetCallback<ParseTask> getCallback) {
        final ParseTask task = new ParseTask();
        task.setTaskType(TASK_TYPE.STATIC);
        task.setDefaultOwner();
        task.setClient(client);
        task.saveEventuallyAndNotify(e -> getCallback.done(task, e));
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

    public Task<Integer> getExtraMinutes() {
        return new EventLogQueryBuilder(false)
                .matching(getTaskGroupStarted())
                .matching(getClient())
                .matchingEventCode(EventLog.EventCodes.REGULAR_EXTRA_TIME)
                .build()
                .findInBackground().onSuccess(boltsTask -> {
                    int minutes = 0;
                    for (EventLog eventLog: boltsTask.getResult()) {
                        minutes += eventLog.getAmount();
                    }

                    return minutes;
                });
    }

    public Task<Report> findReport(final boolean fromLocalDataStore) {
        return Report.getQueryBuilder(fromLocalDataStore).matching(this).build().getFirstInBackground().continueWithTask(reportTask -> {
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
        });
    }

    public boolean matchesSelectedTaskGroupStarted() {
        TaskGroupStartedCache taskGroupStartedCache = GuardSwiftApplication.getInstance().getCacheFactory().getTaskGroupStartedCache();

        if (taskGroupStartedCache.getSelected() != null) {
            if (getTaskGroupStarted() != null) {
                Log.d(TAG, "matchesSelectedTaskGroupStarted: " + getTaskGroupStarted().getObjectId() + " == " + taskGroupStartedCache.getSelected().getObjectId());
                return getTaskGroupStarted().equals(taskGroupStartedCache.getSelected());
            }
        }

        // if guard forget to select a taskgroup, always return true
        return taskGroupStartedCache.getSelected() == null;
    }

    public boolean isWeekly() {
        return getBooleanSafe(ParseTask.isWeekly, false);
    }
}

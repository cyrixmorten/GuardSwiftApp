package com.guardswift.persistence.parse.execution;

import android.content.Context;

import com.guardswift.core.tasks.activity.NoActivityStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
import com.guardswift.core.tasks.automation.FinishOnDepartureAutomationStrategy;
import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
import com.guardswift.core.tasks.controller.AlarmController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.geofence.AlarmGeofenceStrategy;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.query.AlarmQueryBuilder;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.util.Date;


@ParseClassName("Task")
public class ParseTask extends BaseTask {


    public static final String taskGroup = "taskGroup";
    public static final String taskGroupStarted = "taskGroupStarted";

    public static final String taskType = "taskType"; // Regular, District, Alarm, ..
    public static final String status = "status"; // Pending, Accepted, Arrived, Finished

    public static final String type = "type"; // short desc of task provided by owner
    public static final String guard = "guard";
    public static final String client = "client";
    public static final String position = "position";

    public static final String street = "street";
    public static final String streetNumber = "streetNumber";
    public static final String postalCode = "postalCode";
    public static final String city = "city";
    public static final String formattedAddress = "formattedAddress";
    public static final String fullAddress = "fullAddress";

    // Alarm
    public static final String central = "central";
    public static final String centralName = "centralName";
    public static final String signalStatus = "signalStatus";
    public static final String keybox = "keybox";
    public static final String remarks = "remarks";
    public static final String priority = "priority";

    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";


    public static class STATUS {
        public static String PENDING = "pending";
        public static String ACCEPTED = "accepted";
        public static String ARRIVED = "arrived";
        public static String ABORTED = "aborted";
        public static String FINISHED = "finished";
    }

    public ParseTask() {

    }

    @Override
    public BaseTaskCache<ParseTask> getCache() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getTaskCache();
    }

    @Override
    public TaskGeofenceStrategy getGeofenceStrategy() {
        if (this.getTaskType() == TASK_TYPE.ALARM) {
            return new AlarmGeofenceStrategy(this);
        }
        return null;
    }

    @Override
    public TaskActivityStrategy getActivityStrategy() {
        if (this.getTaskType() == TASK_TYPE.ALARM) {
            return new NoActivityStrategy();
        }
        return null;
    }

    @Override
    public TaskAutomationStrategy getAutomationStrategy() {
        if (this.getTaskType() == TASK_TYPE.ALARM) {
            return new FinishOnDepartureAutomationStrategy(this);
        }
        return null;
    }


    @Override
    public TaskController getController() {
        if (this.getTaskType() == TASK_TYPE.ALARM) {
            return new AlarmController();
        }
        return null;
    }


    @Override
    public int getEventCode() {
        if (this.getTaskType() == TASK_TYPE.ALARM) {
            return EventLog.EventCodes.ALARM_OTHER;
        }
        return 0;
    }


    @Override
    public TASK_TYPE getTaskType() {
//        String taskType = getString(ParseTask.taskType);
//        if (taskType.equalsIgnoreCase(TASK_TYPE.ALARM.toString())) {
            return TASK_TYPE.ALARM;
//        }
//        return null;
    }

    private void setStatus(String status) {
        put(ParseTask.status, status);
    }

    public String getStatus() {
        return getStringSafe(ParseTask.status, STATUS.PENDING);
    }

    @Override
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

    @Override
    public String getType() {
        return has(ParseTask.type) ? getString(ParseTask.type) : "";
    }

    @Override
    public String getReportId() {
        String groupId = "";
        if (has(ParseTask.taskGroup)) {
            groupId = getParseObject(ParseTask.taskGroup).getObjectId();
        }
        return groupId + getObjectId();
    }


    @Override
    public ExtendedParseObject getParseObject() {
        return this;
    }


    @Override
    public String getParseClassName() {
        return ParseTask.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<ParseTask> getAllNetworkQuery() {
        return new AlarmQueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    @Override
    public AlarmQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new AlarmQueryBuilder(fromLocalDatastore);
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }


    public Client getClient() {
        return (Client) getLDSFallbackParseObject(ParseTask.client);
    }

    public String getClientName() {
        return (getClient() != null) ? getClient().getName() : "";
    }

    private void setGuardCurrent() {
        put(ParseTask.guard, GuardSwiftApplication.getLoggedIn());
    }

    @Override
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
        setTimeStartedNow();
        setStatus(STATUS.ACCEPTED);
//        setGuardCurrent();
    }

    public void setArrived() {
        setStatus(STATUS.ARRIVED);
        setGuardCurrent();
    }

    public void setAborted() {
        setTimeEndedNow();
        setStatus(STATUS.ABORTED);
        setGuardCurrent();
    }

    public void setFinished() {
        setTimeEndedNow();
        setStatus(STATUS.FINISHED);
        setGuardCurrent();
    }

    @Override
    public boolean isPending() {
        return getStatus().equals(STATUS.PENDING);
    }

    @Override
    public boolean isAccepted() {
        return getStatus().equals(STATUS.ACCEPTED);
    }

    @Override
    public boolean isArrived() {
        return getStatus().equals(STATUS.ARRIVED);
    }

    @Override
    public boolean isAborted() {
        return getStatus().equals(STATUS.ABORTED);
    }

    @Override
    public boolean isFinished() {
        return getStatus().equals(STATUS.FINISHED);
    }

    @Override
    public boolean isWithinScheduledTime() {
        return true;
    }

    @Override
    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(ParseTask.position);
    }

    public Date getTimeStarted() {
        return getDate(timeStarted);
    }

    public Date getTimeEnded() {
        return getDate(timeEnded);
    }

    private void setTimeStartedNow() {
        put(CircuitUnit.timeStarted, new Date());
    }

    private void setTimeStarted(Date timeStarted) {
        put(CircuitUnit.timeStarted, timeStarted);
    }

    private void setTimeEndedNow() {
        put(CircuitUnit.timeEnded, new Date());
    }

    private void setTimeEnded(Date timeEnded) {
        put(CircuitUnit.timeEnded, timeEnded);
    }

    public String getTimeStartString() {
        if (getTimeStarted() == null) {
            return "";
        }

        DateTime dt = new DateTime(getTimeStarted());
        return dt.toString(DateTimeFormat.shortTime());
    }

    public String getTimeEndString() {
        if (getTimeEnded() == null) {
            return "";
        }

        DateTime dt = new DateTime(getTimeEnded());
        return dt.toString(DateTimeFormat.shortTime());
    }
}

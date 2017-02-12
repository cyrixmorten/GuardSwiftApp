package com.guardswift.persistence.parse.execution.task.regular;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.activity.ArriveWhenNotInVehicleStrategy;
import com.guardswift.core.tasks.activity.NoActivityStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
import com.guardswift.core.tasks.automation.ResetOnDepartureAutomationStrategy;
import com.guardswift.core.tasks.automation.StandardTaskAutomationStrategy;
import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
import com.guardswift.core.tasks.controller.CircuitUnitController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.geofence.DistrictWatchGeofenceStrategy;
import com.guardswift.core.tasks.geofence.RegularGeofenceStrategy;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.TaskQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.data.client.ClientLocation;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.ParseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


@ParseClassName("CircuitUnit")
public class CircuitUnit extends BaseTask implements Comparable<CircuitUnit> {


    private static final String TAG = CircuitUnit.class.getSimpleName();


    public CircuitUnit() {
    }


    @Override
    public TaskGeofenceStrategy getGeofenceStrategy() {
        if (isRaid()) {
            return new DistrictWatchGeofenceStrategy(this);
        }

        return new RegularGeofenceStrategy(this);
    }

    @Override
    public TaskActivityStrategy getActivityStrategy() {
        if (isRaid()) {
            return new NoActivityStrategy();
        }
        return new ArriveWhenNotInVehicleStrategy(this);
    }

    @Override
    public BaseTaskCache<CircuitUnit> getCache() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache();
    }

    @Override
    public TaskAutomationStrategy getAutomationStrategy() {
        if (isRaid()) {
            return new ResetOnDepartureAutomationStrategy(this);
        }
        return new StandardTaskAutomationStrategy(this);
    }


    @Override
    public TaskController getController() {
        return new CircuitUnitController();
    }

    @Override
    public int getEventCode() {
        return EventLog.EventCodes.CIRCUITUNIT_OTHER;
    }

    public boolean isRaid() {
        return has(isRaid) && getBoolean(isRaid);
    }

    public void reset() {
//        resetTimeStarted();
//        resetTimeEnded();
        setGuard(null);
        setPending();
        clearCheckpoints();
    }



    @Override
    public TASK_TYPE getTaskType() {
        return TASK_TYPE.REGULAR;
    }

    /**
     * Notice - Ordering matters!
     * <p>
     * A task can both be arrived and finished, in which case finished has precedence
     *
     * @return
     */
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

        return TASK_STATE.PENDING;
    }

    @Override
    public String getType() {
        return getName();
    }

    @Override
    public String getReportId() {
        return (getCircuitStarted() != null) ? getCircuitStarted().getObjectId() + getObjectId() : "";
    }



    @Override
    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(CircuitUnit.clientPosition);
    }


    public CircuitStarted getCircuitStarted() {
        Circuit circuit = getCircuit();
        CircuitStarted cachedCircuitStarted = GuardSwiftApplication.getInstance().getCacheFactory().getCircuitStartedCache().matching(circuit);
        if (cachedCircuitStarted != null) {
            return cachedCircuitStarted;
        }
        try {
            CircuitStarted ldsCircuitStarted = CircuitStarted.Query.findFrom(circuit);
            // add to cache to prevent LDS on next get
            GuardSwiftApplication.getInstance().getCacheFactory().getCircuitStartedCache().addActive(ldsCircuitStarted);
            return ldsCircuitStarted;
        } catch (ParseException e) {
            new HandleException(TAG, "getCircuitStarted() LDS fallback", e);
        }
        return null;
    }



    public static final int SORTBY_NEAREST = 1;
    public static final int SORTBY_ID = 2;

    public static final String status = "status";

    // predefined
    public static final String objectName = "CircuitUnit";
    public static final String name = "name";
    public static final String client = "client";
    public static final String clientId = "clientId";
    public static final String clientName = "clientName";
    public static final String circuit = "circuit";
    public static final String supervisions = "supervisions";

    public static final String description = "description";
    public static final String messages = "messages";
    public static final String days = "days";
    public static final String timeStartDate = "timeStartDate";
    public static final String timeEndDate = "timeEndDate";
    public static final String isRaid = "isRaid";
    public static final String clientPosition = "clientPosition";
    // cleared locally at startup
    public static final String guard = "guard";
    public static final String guardId = "guardId";
    public static final String guardName = "guardName";
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";

    // calculated at startup
    public static final String timeStartSortable = "timeStartSortable";
    public static final String timeEndSortable = "timeEndSortable";

    public static final String timesArrived = "timesArrived";



    @Override
    public String getParseClassName() {
        return CircuitUnit.class.getSimpleName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<CircuitUnit> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {

    }


    private void setTimeStartDate(Date updatedTimeStart) {
        put(timeStartDate, updatedTimeStart);
    }

    private void setTimeEndDate(Date updatedTimeEnd) {
        put(timeEndDate, updatedTimeEnd);
    }


    public TaskQueryBuilder<CircuitUnit> getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    @Override
    public int compareTo(@NonNull CircuitUnit another) {
        // another has clientId and this does not
        if (this.getClientId().isEmpty() && !another.getClientId().isEmpty()) {
            return -1;
        }
        // this has clientId and another does not
        if (!this.getClientId().isEmpty() && another.getClientId().isEmpty()) {
            return 1;
        }
        // both has clientId
        if (!this.getClientId().isEmpty() && !another.getClientId().isEmpty()) {
            return this.getClientId().compareTo(another.getClientId());
        }
        // neither contains clientId
        return this.getClientName().compareTo(another.getClientName());
    }

    public boolean completeButNotFinished() {
        return isPending() && getTimesArrived() >= getPlannedSuperVisions();
    }


    public static class QueryBuilder extends TaskQueryBuilder<CircuitUnit> {

        public QueryBuilder() {
            super(ParseObject.DEFAULT_PIN, false, ParseQuery
                    .getQuery(CircuitUnit.class));
        }

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                    .getQuery(CircuitUnit.class));
        }

//        public QueryBuilder(ParseQueryBuilder<CircuitUnit> fromQueryBuilder) {
//            super(fromQueryBuilder);
//        }

        @Override
        public ParseQuery<CircuitUnit> build() {
//            query.include(messages);
            query.include(circuit);
            query.include(client);
            query.include(client + "." + Client.contacts);
            query.include(client + "." + Client.roomLocations);
            query.include(client + "." + Client.people);
            query.setLimit(1000);
            return super.build();
        }


        public ParseQuery<CircuitUnit> buildNoIncludes() {
            query.setLimit(1000);
            return super.build();
        }

        public QueryBuilder isRunToday() {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int day_of_week = c.get(Calendar.DAY_OF_WEEK);

            int javascript_day_of_week = day_of_week - 1;

            query.whereEqualTo(CircuitUnit.days, javascript_day_of_week);
            return this;
        }

        public QueryBuilder matchingNotEnded(
                CircuitStarted circuitStarted) {
            if (circuitStarted == null) {
                return this;
            }

            matching(circuitStarted.getCircuit());

            query.whereNotEqualTo(CircuitUnit.status, ParseTask.STATUS.FINISHED);

            return this;
        }

        public QueryBuilder matchingEnded(
                CircuitStarted circuitStarted) {

            Log.d(TAG, "matchingEnded " + circuitStarted.getName());

            matching(circuitStarted.getCircuit());

            query.whereEqualTo(CircuitUnit.status, ParseTask.STATUS.FINISHED);

            query.orderByDescending(CircuitUnit.timeEnded);

            return this;
        }

        public QueryBuilder within(int kilometers, Location fromLocation) {
            ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
            query.whereWithinKilometers(clientPosition, parseGeoPoint, kilometers);
            return this;
        }

        public QueryBuilder sortBy(int sortBy) {
            switch (sortBy) {
                case CircuitUnit.SORTBY_NEAREST:
                    ParseModule.sortNearest(query,
                            CircuitUnit.clientPosition);
                    break;
                case CircuitUnit.SORTBY_ID:
                    query.orderByAscending(CircuitUnit.clientId);
                    break;
            }


            return this;
        }

        public QueryBuilder matching(Guard guard) {

            query.whereEqualTo(CircuitUnit.guardId, guard.getGuardId());

            return this;
        }

        public QueryBuilder matching(Circuit circuit) {

            if (circuit == null) {
                Log.e(TAG, "CIRCUIT IS NULL");
                return this;
            }

            query.whereEqualTo(CircuitUnit.circuit, circuit);

            return this;
        }

        public QueryBuilder isTouched() {

            query.whereNotEqualTo(CircuitUnit.guardName, "");
            query.whereNotEqualTo(CircuitUnit.guardId, 0);

            return this;
        }

    }

//    public static CroutonText createCroutonText(CircuitUnit circuitUnit) {
//        Style style = Style.INFO;
//        String eventText = "Tilsyn hos " + circuitUnit.getClient().getName()
//                + " ";
//
//        if (circuitUnit.isFinished()) {
//            eventText += "afsluttet";
//            style = Style.CONFIRM;
//        } else if (circuitUnit.isArrived()) {
//            eventText += "ankommet";
//            style = Style.INFO;
//        } else if (circuitUnit.getGuardId() == 0) {
//            // TODO kan ikke skelne mellem afbrudt og stjernemarkeret
//            // eventText += "afbrudt";
//            // style = Style.ALERT;
//            eventText = "";
//        } else {
//            eventText = "";
//        }
//
//        if (!eventText.isEmpty() && !circuitUnit.getGuardName().isEmpty())
//            eventText += " af " + circuitUnit.getGuardName();
//
//        return new CroutonText(eventText, style);
//    }

    public String getDescription() {

        return (has(description)) ? getString(description) : "";
    }

    public void clearCheckpoints() {
        if (getClient() != null) {
            getClient().clearCheckpoints();
        }
    }

//    public boolean hasCheckPoints() {
//        return getClient().hasCheckPoints();
//    }

    public List<ClientLocation> getCheckpoints() {
        return getClient().getCheckpoints();
    }

    public List<String> getCheckpointNamesAsList() {
        return getClient().getCheckpointNamesAsList();
    }

    public String[] getCheckpointsNamesAsArray() {
        return getClient().getCheckpointsNamesAsArray();
    }


    public boolean[] getCheckpointsCheckedArray() {
        return getClient().getCheckpointsCheckedArray();
    }


//    public int minutesSinceLastArrival() {
//        DateTime now = new DateTime();
//        DateTime lastArrived = new DateTime(getTimeStarted());
//
//        return Minutes.minutesBetween(lastArrived, now).getMinutes();
//    }

    private void setStatus(String status) {
        put(ParseTask.status, status);
    }

    private boolean isStatus(String status) {
        return getStringSafe(ParseTask.status).equals(status);
    }

    public void setPending() {
        setStatus(ParseTask.STATUS.PENDING);

        if (isRaid() && getTimesArrived() >= getPlannedSuperVisions()) {
            getController().performAutomaticAction(TaskController.ACTION.FINISH, this);
        }
    }

    public void setAccepted() {
        setStatus(ParseTask.STATUS.ACCEPTED);
    }

    public void setArrived() {

        Guard guard = GuardSwiftApplication.getLoggedIn();

        setGuard(guard);
        setStatus(ParseTask.STATUS.ARRIVED);
        increment(CircuitUnit.timesArrived);

    }

    public void setAborted() {
        setStatus(ParseTask.STATUS.ABORTED);
    }


    public void setFinished() {
        Guard guard = GuardSwiftApplication.getLoggedIn();

        setGuard(guard);
        setStatus(ParseTask.STATUS.FINISHED);
    }


    @Override
    public boolean isPending() {
        return isStatus(ParseTask.STATUS.PENDING);
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    public boolean isAborted() {
        return isStatus(ParseTask.STATUS.ABORTED);
    }

    public boolean isArrived() {
        return isStatus(ParseTask.STATUS.ARRIVED);
    }

    public boolean isFinished() {
        return isStatus(ParseTask.STATUS.FINISHED);
    }

    @Override
    public Guard getGuard() {
        return (Guard) getLDSFallbackParseObject(guard);
    }

//    public boolean takenByAnyGuard() {
//        return getGuardId() != 0;
//    }
//
//    public boolean takenByThisGuard(Guard guard) {
//        return takenByAnyGuard() && getGuardId() == guard.getGuardId();
//    }
//
//    public boolean takenByAnotherGuard(Guard guard) {
//        return guard != null && takenByAnyGuard() && getGuardId() != guard.getGuardId();
//    }
//    public boolean arrivedByAnotherGuard(
//            Guard guard) {
//
//        return takenByAnotherGuard(guard) && isArrived();
//    }
//
//    public boolean finishedByAnotherGuard(
//            Guard guard) {
//        return takenByAnotherGuard(guard) && isFinished();®
//    }
//
//    public void setSortableTimes(int timeStartSortable, int timeEndSortable) {
//        setTimeStartSortable(timeStartSortable);
//        setTimeEndSortable(timeEndSortable);
//    }

    public String getName() {
        return getString(name);
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    public Circuit getCircuit() {
        return (Circuit) getParseObject(circuit);
    }

    public Date getTimeStart() {
        return getDate(timeStartDate);
    }

    public String getTimeStartString() {
        Date date = getDate(timeStartDate);
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }

    public Date getTimeEnd() {
        return getDate(timeEndDate);
    }

    public String getTimeEndString() {
        Date date = getDate(timeEndDate);
        DateTime dt = new DateTime(date);
        return dt.toString(DateTimeFormat.shortTime());
    }


    public void setGuard(Guard guard) {
        // put(CircuitUnit.guard, guard);
        if (guard == null) {
            remove(CircuitUnit.guard);
            put(CircuitUnit.guardId, 0);
            put(CircuitUnit.guardName, "");
        } else {
            put(CircuitUnit.guard, guard);
            put(CircuitUnit.guardId, guard.getGuardId());
            put(CircuitUnit.guardName, guard.getName());
        }

    }


    public int getGuardId() {
        return getInt(guardId);
    }

    public String getGuardName() {
        return getString(guardName);
    }

    public void removeGuard() {
        remove(guard);
        remove(guardId);
        remove(guardName);
    }

    public String getClientId() {
        if (getClient() != null) {
            return getClient().getId();
        }
        return (has(clientId)) ? getString(clientId) : "";
    }

    public String getClientName() {
        if (getClient() != null) {
            return getClient().getName();
        }
        return (has(clientName)) ? getString(clientName) : "";
    }

    public Client getClient() {
        return (Client)getLDSFallbackParseObject(CircuitUnit.client);
    }



//    public void setTimeStartSortable(int timeStartSortable) {
//        put(CircuitUnit.timeStartSortable, timeStartSortable);
//    }
//
//    public int getTimeStartSortable() {
//        return getInt(timeStartSortable);
//    }
//
//    public void setTimeEndSortable(int timeEndSortable) {
//        put(CircuitUnit.timeEndSortable, timeEndSortable);
//    }
//
//    public int getTimeEndSortable() {
//        return getInt(timeEndSortable);
//    }
//
//    private void resetTimeStarted() {
//
//        CircuitStarted circuitStarted = getCircuitStarted();
//
//        Date justBeforeCircuitStarted = new Date(circuitStarted
//                .getTimeStarted().getTime() - 3600);
//        put(CircuitUnit.timeStarted, justBeforeCircuitStarted);
//    }
//
//    private void resetTimeEnded() {
//
//        CircuitStarted circuitStarted = getCircuitStarted();
//
//        Date justBeforeCircuitStarted = new Date(circuitStarted
//                .getTimeStarted().getTime() - 3600);
//        put(CircuitUnit.timeEnded, justBeforeCircuitStarted);
//    }
//
//    private void setTimeStartedNow() {
//        put(CircuitUnit.timeStarted, new Date());
//    }
//
//    private void setTimeStarted(Date timeStarted) {
//        put(CircuitUnit.timeStarted, timeStarted);
//    }
//
//    private void setTimeEndedNow() {
//        put(CircuitUnit.timeEnded, new Date());
//    }
//
//    private void setTimeEnded(Date timeEnded) {
//        put(CircuitUnit.timeEnded, timeEnded);
//    }
//
//    public Date getTimeStarted() {
//        return getDate(timeStarted);
//    }
//
//    public Date getTimeEnded() {
//        return getDate(timeEnded);
//    }

    public int getPlannedSuperVisions() {
        return getInt(supervisions);
    }

    public int getTimesArrived() {
        return getInt(timesArrived);
    }


    public boolean isWithinScheduledTime() {

//        if (BuildConfig.DEBUG)
//            return true;

        Log.d(TAG, "isWithinScheduledTime ");

        DateTime timeStartOrg = new DateTime(getTimeStart());
        DateTime timeEndOrg = new DateTime(getTimeEnd());

        CircuitStarted selectedCircuitStarted = getCircuitStarted();

        if (selectedCircuitStarted == null)
            return true; // lets be optimistic

        MutableDateTime timeStart = new MutableDateTime(selectedCircuitStarted.getCreatedAt());
        int startHour = timeStartOrg.getHourOfDay();
        timeStart.setHourOfDay(startHour);
        timeStart.setMinuteOfHour(timeStartOrg.getMinuteOfHour());

        MutableDateTime timeEnd = new MutableDateTime(selectedCircuitStarted.getCreatedAt());
        int endHour = timeEndOrg.getHourOfDay();
        if (endHour < startHour)
            timeEnd.addDays(1);

        timeEnd.setHourOfDay(endHour);
        timeEnd.setMinuteOfHour(timeEndOrg.getMinuteOfHour());

        DateTime now = DateTime.now(DateTimeZone.getDefault());

//        Log.d(TAG, "-- timeStart: " + timeStart.getHourOfDay() + ":" + timeStart.getMinuteOfHour());
//        Log.d(TAG, "-- timeEnd: " + timeEnd.getHourOfDay() + ":" + timeEnd.getMinuteOfHour());
//        Log.d(TAG, "-- now: " + now.getHourOfDay() + ":" + now.getMinuteOfHour());

        boolean afterTimeStart = now.isAfter(timeStart);
        boolean beforeTimeEnd = now.isBefore(timeEnd);

//        Log.d(TAG, "  -- afterTimeStart: " + afterTimeStart);
//        Log.d(TAG, "  -- beforeTimeEnd: " + beforeTimeEnd);

        return afterTimeStart && beforeTimeEnd;
    }

    @Override
    public ExtendedParseObject getParseObject() {
        return this;
    }


}

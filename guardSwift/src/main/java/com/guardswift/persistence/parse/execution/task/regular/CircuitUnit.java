package com.guardswift.persistence.parse.execution.task.regular;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.guardswift.core.documentation.report.NoTaskReportingStrategy;
import com.guardswift.core.documentation.report.StandardTaskReportingStrategy;
import com.guardswift.core.documentation.report.TaskReportingStrategy;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.activity.ArriveWhenNotInVehicleStrategy;
import com.guardswift.core.tasks.activity.NoActivityStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
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
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

//import de.keyboardsurfer.android.widget.crouton.Style;

@ParseClassName("CircuitUnit")
public class CircuitUnit extends BaseTask  {


    private static final String TAG = CircuitUnit.class.getSimpleName();

    private TaskController<CircuitUnit> controller;
    private TaskReportingStrategy<CircuitUnit> taskReportingStrategy;
    private TaskAutomationStrategy<CircuitUnit> automationStrategy;

    public CircuitUnit() {
        this.controller = new CircuitUnitController();
        taskReportingStrategy = new NoTaskReportingStrategy<>(this);
        automationStrategy = new StandardTaskAutomationStrategy<>(this);
    }


    @Override
    public TaskGeofenceStrategy getGeofenceStrategy() {
        TaskGeofenceStrategy<CircuitUnit> geofenceStrategy = new RegularGeofenceStrategy<>(this);
        if (isRaid()) {
            geofenceStrategy = new DistrictWatchGeofenceStrategy<>(this);
        }

        return geofenceStrategy;
    }

    @Override
    public TaskActivityStrategy getActivityStrategy() {
//        if (activityStrategy == null) {
        TaskActivityStrategy<CircuitUnit> activityStrategy = new ArriveWhenNotInVehicleStrategy<>(this);
        if (isRaid()) {
            activityStrategy = new NoActivityStrategy<>();
        }
//        }
        return activityStrategy;
    }

    @Override
    public BaseTaskCache<CircuitUnit> getCache() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getCircuitUnitCache();
    }

    @Override
    public TaskAutomationStrategy getAutomationStrategy() {
        return automationStrategy;
    }

    @Override
    public TaskReportingStrategy getTaskReportingStrategy() {
        return taskReportingStrategy;
    }

    @Override
    public TaskController getController() {
        return controller;
    }

    @Override
    public int getEventCode() {
        return EventLog.EventCodes.CIRCUITUNIT_OTHER;
    }

    public boolean isRaid() {
        return has(isRaid) && getBoolean(isRaid);
    }

    public void reset() {
        resetTimeStarted();
        resetTimeEnded();
        setGuard(null);
        put(isAborted, false);
        clearCheckpoints();
    }

    public String getClientName() {
        if (getClient() != null) {
            return getClient().getName();
        }
        return "";
    }

//    public static class Recent {
//        private static CircuitUnit selected;
//        private static CircuitUnit arrived;
//
//
//        public static CircuitUnit getSelected() {
//            return selected;
//        }
//
//        public static void setSelected(CircuitUnit selected) {
//            Recent.selected = selected;
//
//            Client.Recent.setSelected(null);
//            if (selected != null) {
//                Client.Recent.setSelected(selected.getClient());
//            }
//        }
//
//        public static CircuitUnit getArrived() {
//            return arrived;
//        }
//
//        public static void setArrived(CircuitUnit circuitUnit) {
//            Recent.arrived = circuitUnit;
//
//            Client.Recent.setArrived(null);
//            if (circuitUnit != null) {
//                Client.Recent.setArrived(circuitUnit.getClient());
//            }
//        }
//
//    }


    @Override
    public TASK_TYPE getTaskType() {
        return TASK_TYPE.REGULAR;
    }

    /**
     * Notice - Ordering matters!
     *
     * A task can both be arrived and finished, in which case finished has precedence
     *
     * @return
     */
    @Override
    public TASK_STATE getTaskState() {
        if (isFinished()) {
            return TASK_STATE.FINSIHED;
        }
        if (isAborted()) {
            return TASK_STATE.ABORTED;
        }
        if (isStarted()) {
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

//    @Override
//    public String getTaskTitle(Context context) {
//        return context.getString(R.string.task_circuit) + " \n" + getClient().getName() + " " + getClient().getFullAddress();
//    }


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

    @Override
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

        Log.d(TAG, "-- timeStart: " + timeStart.getHourOfDay() + ":" + timeStart.getMinuteOfHour());
        Log.d(TAG, "-- timeEnd: " + timeEnd.getHourOfDay() + ":" + timeEnd.getMinuteOfHour());
        Log.d(TAG, "-- now: " + now.getHourOfDay() + ":" + now.getMinuteOfHour());

        boolean afterTimeStart = now.isAfter(timeStart);
        boolean beforeTimeEnd = now.isBefore(timeEnd);

        Log.d(TAG, "  -- afterTimeStart: " + afterTimeStart);
        Log.d(TAG, "  -- beforeTimeEnd: " + beforeTimeEnd);

        return afterTimeStart && beforeTimeEnd;
    }



    public static final int SORTBY_NEAREST = 1;
    public static final int SORTBY_TIME_END = 2;

    // predefined
    public static final String objectName = "CircuitUnit";
    public static final String name = "name";
    public static final String client = "client";
    public static final String circuit = "circuit";
//    public static final String circuitStarted = "circuitStarted";

    // public static final String circuits = "circuits";
    public static final String description = "description";
    public static final String messages = "messages";
    public static final String days = "days";
    public static final String timeStartDate = "timeStartDate";
    public static final String timeEndDate = "timeEndDate";
    public static final String isRaid = "isRaid";
    public static final String isExtra = "isExtra";
    public static final String isHidden = "isHidden";
    public static final String isAborted = "isAborted";
    public static final String clientPosition = "clientPosition";
    // cleared locally at startup
    // public static final String circuitStarted = "circuitStarted";
    // public static final String guard = "guard";
    public static final String guard = "guard";
    public static final String guardId = "guardId";
    public static final String guardName = "guardName";
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";

    // calculated at startup
    // public static final String timeStartHour = "timeStartHour";
    // public static final String timeStartMinute = "timeStartMinute";
    // public static final String timeEndHour = "timeEndHour";
    // public static final String timeEndMinute = "timeEndMinute";
    public static final String timeStartSortable = "timeStartSortable";
    public static final String timeEndSortable = "timeEndSortable";


    public static CircuitUnit createExtra(String name,
                                          CircuitUnit fromCircuitUnit, CircuitStarted circuitStarted,
                                          int timeStartHour, int timeStartMinute, int timeEndHour, int timeEndMinute) {


        circuitStarted.incrementExtras();

        CircuitUnit circuitUnit = new CircuitUnit();
        for (String key : fromCircuitUnit.keySet()) {
            if (fromCircuitUnit.get(key) != null) {
                circuitUnit.put(key, fromCircuitUnit.get(key));
            }
        }


//        circuitUnit.put(CircuitUnit.circuitStarted, circuitStarted);
        circuitUnit.put(CircuitUnit.isExtra, true);
        circuitUnit.put(CircuitUnit.isRaid, fromCircuitUnit.isRaid());
        circuitUnit.put(CircuitUnit.name, name);
//        circuitUnit.put(CircuitUnit.client, fromCircuitUnit.getClient());
//        circuitUnit.put(CircuitUnit.circuit, fromCircuitUnit.getCircuit());

        MutableDateTime startDate = new MutableDateTime();
        startDate.setHourOfDay(timeStartHour);
        startDate.setMinuteOfHour(timeStartMinute);
        circuitUnit.put(CircuitUnit.timeStartDate, startDate.toDate());

        MutableDateTime endDate = new MutableDateTime();
        endDate.setHourOfDay(timeEndHour);
        endDate.setMinuteOfHour(timeEndMinute);
        circuitUnit.put(CircuitUnit.timeEndDate, endDate.toDate());

//        circuitUnit.put(owner, ParseUser.getCurrentUser());
//        int[] daysValues = new int[]{0, 1, 2, 3, 4, 5, 6};
//        JSONArray days = new JSONArray();
//        for (int day : daysValues) {
//            days.put(day);
//        }
//        circuitUnit.put(CircuitUnit.days, days);

        circuitUnit.saveEventually();

        return circuitUnit;

    }

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
//        try {
//            final String objectId = jsonObject
//                    .getString(ExtendedParseObject.objectId);
//            if (objectId != null) {
//                getFromObjectId(CircuitUnit.class, objectId, true,
//                        new GetCallback<CircuitUnit>() {
//
//                            @Override
//                            public void done(CircuitUnit object,
//                                             ParseException e) {
//                                if (e != null) {
//                                    Log.e(TAG, "updateFromJSON circuitUnit not found: " + objectId);
//                                    return;
//                                }
//                                try {
//                                    int updatedGuardId = (jsonObject
//                                            .has(guardId)) ? jsonObject
//                                            .getInt(guardId) : 0;
//
//									/*
//                                     * Push checkpoint should weed out updates
//									 * made by this device
//									 *
//									 * Nevertheless as an precaution an extra
//									 * check is made here
//									 */
//                                    Guard currentGuard = GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().getLoggedIn();
//                                    if (currentGuard != null) {
//                                        if (currentGuard.getGuardId() == updatedGuardId) {
//                                            Log.e(TAG,
//                                                    "I should not receive this init!");
//                                            return;
//                                        }
//                                    }
//
//                                    final String updatedGuardName = (jsonObject
//                                            .has(guardName)) ? jsonObject
//                                            .getString(guardName) : "";
//
//                                    String updatedTimeStartDateString = (jsonObject
//                                            .has(timeStartDate)) ? jsonObject
//                                            .getJSONObject(timeStartDate)
//                                            .getString("iso") : null;
//
//                                    String updatedTimeEndDateString = (jsonObject
//                                            .has(timeEndDate)) ? jsonObject
//                                            .getJSONObject(timeEndDate)
//                                            .getString("iso") : null;
//
//                                    String updatedTimeStartedString = (jsonObject
//                                            .has(timeStarted)) ? jsonObject
//                                            .getJSONObject(timeStarted)
//                                            .getString("iso") : null;
//                                    String updatedTimeEndedString = (jsonObject
//                                            .has(timeEnded)) ? jsonObject
//                                            .getJSONObject(timeEnded)
//                                            .getString("iso") : null;
//
//                                    Log.e(TAG, "updatedTimeStartDateString "
//                                            + updatedTimeStartDateString);
//                                    Log.e(TAG, "updatedTimeEndDateString "
//                                            + updatedTimeEndDateString);
//
//                                    DateTimeFormatter fmt = ISODateTimeFormat
//                                            .dateTime();
//
//                                    Date updatedTimeStart = null;
//                                    if (updatedTimeStartDateString != null) {
//                                        updatedTimeStart = fmt.parseDateTime(
//                                                updatedTimeStartDateString)
//                                                .toDate();
//                                    }
//
//                                    Date updatedTimeEnd = null;
//                                    if (updatedTimeEndDateString != null) {
//                                        updatedTimeEnd = fmt.parseDateTime(
//                                                updatedTimeEndDateString)
//                                                .toDate();
//                                    }
//
//                                    Date updatedTimeStarted = null;
//                                    if (updatedTimeStartedString != null) {
//                                        updatedTimeStarted = fmt.parseDateTime(
//                                                updatedTimeStartedString)
//                                                .toDate();
//                                    }
//
//                                    Date updatedTimeEnded = null;
//                                    if (updatedTimeEndedString != null) {
//                                        updatedTimeEnded = fmt.parseDateTime(
//                                                updatedTimeEndedString)
//                                                .toDate();
//                                    }
//
//                                    // init values
//
//                                    object.setGuardId(updatedGuardId);
//                                    object.setGuardName(updatedGuardName);
//
//                                    object.setTimeStartDate(updatedTimeStart);
//                                    object.setTimeEndDate(updatedTimeEnd);
//
//                                    if (updatedTimeStarted != null) {
//                                        object.setTimeStarted(updatedTimeStarted);
//                                    } else {
//                                        object.remove(timeStarted);
//                                    }
//                                    if (updatedTimeEnded != null) {
//                                        object.setTimeEnded(updatedTimeEnded);
//                                    } else {
//                                        object.remove(timeEnded);
//                                    }
//
//                                    pinUpdate(
//                                            object,
//                                            new DataStoreCallback<CircuitUnit>() {
//
//                                                @Override
//                                                public void success(
//                                                        List<CircuitUnit> objects) {
//                                                    EventBusController.postParseObjectUpdated(objects.get(0));
//                                                }
//
//                                                @Override
//                                                public void failed(
//                                                        ParseException e) {
//                                                    Log.e(TAG,
//                                                            "updateFromJSON", e);
//                                                }
//
//                                            });
//                                } catch (JSONException e1) {
//                                    Log.e(TAG, "updateFromJSON data", e1);
//                                }
//                            }
//                        });
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "updateFromJSON no objectId", e);
//        }
    }


//    public void setCircuitStarted(CircuitStarted selected) {
//        put(CircuitUnit.circuitStarted, selected);
////        pinInBackground(PIN);
//    }
//
//    public CircuitStarted getCircuitStarted(boolean withData) {
//        CircuitStarted circuitStarted = (CircuitStarted) getParseObject(CircuitUnit.circuitStarted);
//
//        if (circuitStarted == null) {
//            circuitStarted = CircuitStarted.Recent.getSelected();
//        }
//
//        if (withData && circuitStarted != null && !circuitStarted.isDataAvailable()) {
//            try {
//                circuitStarted.fetchFromLocalDatastore();
//            } catch (ParseException e) {
//                Log.e(TAG, "getCircuitStarted1", e);
//                circuitStarted = CircuitStarted.Recent.getSelected();
//                try {
//                    circuitStarted.fetchFromLocalDatastore();
//                } catch (ParseException e1) {
//                    Log.e(TAG, "getCircuitStarted2", e);
//                }
//            }
//        }
//        return circuitStarted;
//    }

    private void setTimeStartDate(Date updatedTimeStart) {
        put(timeStartDate, updatedTimeStart);
    }

    private void setTimeEndDate(Date updatedTimeEnd) {
        put(timeEndDate, updatedTimeEnd);
    }

    // @Override
    // public ParseQuery<CircuitUnit> getAllDatastoreQuery() {
    // return new QueryBuilder(true).build();
    // };

    public TaskQueryBuilder<CircuitUnit> getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
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
            query.whereNotEqualTo(isHidden, true);
            return this;
        }

        public QueryBuilder matchingNotEnded(
                CircuitStarted circuitStarted) {
            if (circuitStarted == null) {
                return this;
            }

            matching(circuitStarted.getCircuit());

            query.whereLessThan(CircuitUnit.timeEnded,
                    circuitStarted.getTimeStarted());

            return this;
        }

        public QueryBuilder matchingEnded(
                CircuitStarted circuitStarted) {

            Log.d(TAG, "matchingEnded " + circuitStarted.getName());

            matching(circuitStarted.getCircuit());
            query.orderByDescending(CircuitUnit.timeEnded);
            query.whereGreaterThan(CircuitUnit.timeEnded,
                    circuitStarted.getTimeStarted());

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
                case CircuitUnit.SORTBY_TIME_END:
                    query.orderByDescending(CircuitUnit.timeEnded);
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
//        } else if (circuitUnit.isStarted()) {
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

    public boolean hasCheckPoints() {
        return getClient().hasCheckPoints();
    }

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


    public boolean isExtra() {
        return getBoolean(isExtra);
    }

    public void setExtra(boolean extra) {
        put(CircuitUnit.isExtra, extra);
    }

    public int minutesSinceLastArrival() {
        DateTime now = new DateTime();
        DateTime lastArrived = new DateTime(getTimeStarted());

        return Minutes.minutesBetween(lastArrived, now).getMinutes();
    }

    public void setArrived(Guard guard) {
//        setArrivedReported(true);

        setTimeStartedNow();
        setGuard(guard);
//        resetTimeEnded();
//        setCircuitStartOed(CircuitStarted.Recent.getSelected());
//        clearAutomaticDepartureReports();
        put(isAborted, false);
        put(CircuitUnit.guard, guard);
    }

    public void setFinished(Guard guard) {
        setTimeEndedNow();
        setGuard(guard);
//        clearAutomaticArrivalReports();
        put(isAborted, false);
    }

    public void setAborted() {
        removeGuard();
        resetTimeStarted();
        resetTimeEnded();
        put(isAborted, true);
    }

    public boolean isAborted() {
        return getBoolean(isAborted);
    }


    public boolean isStarted() {
        CircuitStarted circuitStarted = getCircuitStarted();
        if (getTimeStarted() == null || circuitStarted == null) {
            return false;
        } else {
            boolean arrived = getTimeStarted().after(
                    circuitStarted.getCreatedAt());
            return arrived;
        }
    }

    public boolean isFinished() {
        CircuitStarted circuitStarted = getCircuitStarted();
        if (getTimeStarted() == null || getTimeEnded() == null
                || circuitStarted == null)
            return false;
        return getTimeEnded().after(circuitStarted.getCreatedAt())
                && getTimeEnded().after(getTimeStarted());

    }

    @Override
    public Guard getGuard() {
        return (Guard)getLDSFallbackParseObject(guard);
    }

    public boolean takenByAnyGuard() {
        return getGuardId() != 0;
    }

    public boolean takenByThisGuard(Guard guard) {
        return takenByAnyGuard() && getGuardId() == guard.getGuardId();
    }

    public boolean takenByAnotherGuard(Guard guard) {
        return guard != null && takenByAnyGuard() && getGuardId() != guard.getGuardId();
    }

    public boolean arrivedByAnotherGuard(
            Guard guard) {

        return takenByAnotherGuard(guard) && isStarted();
    }

    public boolean finishedByAnotherGuard(
            Guard guard) {
        return takenByAnotherGuard(guard) && isFinished();
    }

    public void setSortableTimes(int timeStartSortable, int timeEndSortable) {
        setTimeStartSortable(timeStartSortable);
        setTimeEndSortable(timeEndSortable);
    }

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
            put(CircuitUnit.guardId, 0);
            put(CircuitUnit.guardName, "");
        } else {
            put(CircuitUnit.guardId, guard.getGuardId());
            put(CircuitUnit.guardName, guard.getName());
        }

    }

    private void setGuardId(int guardId) {
        put(CircuitUnit.guardId, guardId);
    }

    private void setGuardName(String guardName) {
        put(CircuitUnit.guardName, guardName);
    }

    // public Guard getGuard() {
    // return (Guard) getParseObject(guard);
    // }

    public int getGuardId() {
        return getInt(guardId);
    }

    public String getGuardName() {
        return getString(guardName);
    }

    public void removeGuard() {
        // remove(guard);
        remove(guardId);
        remove(guardName);
    }


    public Client getClient() {
        Client client = (Client) getParseObject(CircuitUnit.client);
        if (client != null && !client.isDataAvailable()) {
            try {
                client.fetchFromLocalDatastore();
            } catch (ParseException e) {
                Crashlytics.logException(e);
            }
        }

        return client;
    }


    // public void setCircuitStarted(CircuitStarted circuitStarted) {
    // put(CircuitUnit.circuitStarted, circuitStarted);
    // }
    //
    // public CircuitStarted getCircuitStarted() {
    // return (CircuitStarted) getParseObject(circuitStarted);
    // }

    // public List<Circuit> getSharedCircuits() {
    // return getSet(circuits);
    // }

    // public void setTimeStart(int hour, int minute) {
    // put(CircuitUnit.timeStartHour, hour);
    // put(CircuitUnit.timeStartMinute, minute);
    // }
    //
    // public void setTimeEnd(int hour, int minute) {
    // put(CircuitUnit.timeEndHour, hour);
    // put(CircuitUnit.timeEndMinute, minute);
    // }
    //
    // public int getTimeStartHour() {
    // return getInt(CircuitUnit.timeEndHour);
    // }

    public void setTimeStartSortable(int timeStartSortable) {
        put(CircuitUnit.timeStartSortable, timeStartSortable);
    }

    public int getTimeStartSortable() {
        return getInt(timeStartSortable);
    }

    public void setTimeEndSortable(int timeEndSortable) {
        put(CircuitUnit.timeEndSortable, timeEndSortable);
    }

    public int getTimeEndSortable() {
        return getInt(timeEndSortable);
    }

    private void resetTimeStarted() {

        CircuitStarted circuitStarted = getCircuitStarted();

        Date justBeforeCircuitStarted = new Date(circuitStarted
                .getTimeStarted().getTime() - 3600);
        put(CircuitUnit.timeStarted, justBeforeCircuitStarted);
    }

    private void resetTimeEnded() {

        CircuitStarted circuitStarted = getCircuitStarted();

        Date justBeforeCircuitStarted = new Date(circuitStarted
                .getTimeStarted().getTime() - 3600);
        put(CircuitUnit.timeEnded, justBeforeCircuitStarted);
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

    public Date getTimeStarted() {
        return getDate(timeStarted);
    }

    public Date getTimeEnded() {
        return getDate(timeEnded);
    }

//    public boolean hasUnreadMessagesFor(Guard guard) {
//        for (Message info : getMessages()) {
//            if (!info.isReadBy(guard)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public List<Message> getMessages() {
//        if (has(messages)) return getList(messages);
//        return new ArrayList<Message>();
//    }
//
//    public void addMessage(Message message) {
//        add(CircuitUnit.messages, message);
//    }

    @Override
    public ExtendedParseObject getParseObject() {
        return this;
    }


}

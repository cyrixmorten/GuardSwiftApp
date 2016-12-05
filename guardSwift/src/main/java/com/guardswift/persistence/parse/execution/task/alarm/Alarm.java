//package com.guardswift.persistence.parse.execution.task.alarm;
//
//import android.content.Context;
//import android.location.Location;
//import android.util.Log;
//
//import com.guardswift.core.documentation.report.StandardTaskReportingStrategy;
//import com.guardswift.core.documentation.report.TaskReportingStrategy;
//import com.guardswift.core.exceptions.HandleException;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.core.tasks.activity.ArriveWhenNotInVehicleStrategy;
//import com.guardswift.core.tasks.activity.TaskActivityStrategy;
//import com.guardswift.core.tasks.automation.StandardTaskAutomationStrategy;
//import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
//import com.guardswift.core.tasks.controller.AlarmController;
//import com.guardswift.core.tasks.controller.TaskController;
//import com.guardswift.core.tasks.geofence.AlarmGeofenceStrategy;
//import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
//import com.guardswift.persistence.cache.task.BaseTaskCache;
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.TaskQueryBuilder;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.documentation.event.EventLog;
//import com.guardswift.persistence.parse.execution.BaseTask;
//import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
//import com.guardswift.ui.GuardSwiftApplication;
//import com.parse.ParseClassName;
//import com.parse.ParseException;
//import com.parse.ParseGeoPoint;
//import com.parse.ParseQuery;
//
//import org.json.JSONObject;
//
//import java.util.Date;
//
//@ParseClassName("Alarm")
//public class Alarm extends BaseTask {
//
//
//    private static String TAG = Alarm.class.getSimpleName();
//
//    private final TaskController<Alarm> controller;
//    private final TaskReportingStrategy<Alarm> taskReportingStrategy;
//    private final TaskGeofenceStrategy<Alarm> geofenceStrategy;
//    private final TaskActivityStrategy<Alarm> activityStrategy;
//    private final TaskAutomationStrategy<Alarm> automationStrategy;
//
//    public Alarm() {
//        this.controller =  new AlarmController();
//        taskReportingStrategy = new StandardTaskReportingStrategy<>(this);
//        automationStrategy = new StandardTaskAutomationStrategy<>(this);
//        geofenceStrategy = new AlarmGeofenceStrategy(this);
//        activityStrategy = new ArriveWhenNotInVehicleStrategy<>(this);
//    }
//
//    @Override
//    public BaseTaskCache getCache() {
//        return GuardSwiftApplication.getInstance().getCacheFactory().getAlarmCache();
//    }
//
//    @Override
//    public TaskGeofenceStrategy getGeofenceStrategy() {
//        return geofenceStrategy;
//    }
//
//    @Override
//    public TaskActivityStrategy getActivityStrategy() {
//        return activityStrategy;
//    }
//
//    @Override
//    public TaskAutomationStrategy getAutomationStrategy() {
//        return automationStrategy;
//    }
//
//    @Override
//    public TaskReportingStrategy getTaskReportingStrategy() {
//        return taskReportingStrategy;
//    }
//
//    @Override
//    public TaskController getController() {
//        return controller;
//    }
//
//    private String forwardedTo;
//
//    public void setSecurityLevel(String securityLevel) {
//        put(Alarm.securityLevel, securityLevel);
//    }
//
//    public JSONObject getJSONReport() {
//        return getJSONObject("report");
//    }
//
//    @Override
//    public int getEventCode() {
//        return EventLog.EventCodes.ALARM_OTHER;
//    }
//
//
//    /*
//     * HOLDERS
//     */
//
////    public static class Recent {
////        private static Alarm selected;
////        private static boolean isDialogShowing;
////
////
////        public static Alarm getSelected() {
////            return selected;
////        }
////
////        public static void setSelected(Alarm selected) {
////            Recent.selected = selected;
////
////            Client.Recent.setSelected(selected.getClient());
////        }
////
////        public static boolean isDialogShowing() {
////            return isDialogShowing;
////        }
////
////        public static void setDialogShowing(boolean isDialogShowing) {
////            Recent.isDialogShowing = isDialogShowing;
////        }
////    }
//
//    /*
//     * TASK
//     */
//
//
//
////    @Override
////    public String getTaskTitle(Context context) {
////        return context.getString(R.string.task_alarm) + " \n" + getClient().getName() + " " + getClient().getFullAddress();
////    }
//
//
//
//    @Override
//    public TASK_TYPE getTaskType() {
//        return TASK_TYPE.ALARM;
//    }
//
//    @Override
//    public TASK_STATE getTaskState() {
//        if (isFinished()) {
//            return TASK_STATE.FINISHED;
//        }
//        if (isAborted()) {
//            return TASK_STATE.ABORTED;
//        }
//        if (isArrived()) {
//            return TASK_STATE.ARRIVED;
//        }
//        if (isAccepted()) {
//            return TASK_STATE.ACCEPTED;
//        }
//        return TASK_STATE.PENDING;
//    }
//
//    @Override
//    public ParseGeoPoint getPosition() {
//        return getParseGeoPoint(position);
//    }
//
//    @Override
//    public ExtendedParseObject getParseObject() {
//        return this;
//    }
//
//    @Override
//    public boolean isWithinScheduledTime() {
//        return true;
//    }
//
//
//
//    /*
//     * DATA
//     */
//
//    public static final String PIN = "Alarm";
//
//    // alarmInfo
//    public static final String position = "clientPosition";
//    public static final String type = "type";
//    public static final String zone = "zone";
//    public static final String alarmTime = "alarmTime";
//    public static final String securityLevel = "securityLevel";
//    public static final String hardwareId = "hardwareId";
//    public static final String serial = "serial";
//    public static final String installer = "installer";
//    public static final String drivingGuidance = "drivingGuidance";
//    public static final String accessRoute = "accessRoute";
//    public static final String keyboxLocation = "keyboxLocation";
//    public static final String bypassLocation = "bypassLocation";
//    public static final String controlpanelLocation = "controlpanelLocation";
//    public static final String smokecannonLocation = "smokecannonLocation";
//    public static final String guardCode = "guardCode";
//    public static final String bypassCode = "bypassCode";
//    public static final String remark = "remark";
//
//    public static final String guard = "guard";
//    public static final String client = "client";
//    public static final String central = "central";
//
//    // handling
//    public static final String alarmGroup = "alarmGroup"; // alarm group assigned to handle the alarm
//    public static final String ignoredBy = "ignoredBy";
//    public static final String aborted = "aborted";
//    public static final String accepted = "accepted";
//    public static final String timeAccepted = "timeAccepted";
//    public static final String timeStartedDriving = "timeStartedDriving";
//    public static final String timeStarted = "timeStarted";
//    public static final String timeArrived = "timeArrived";
//    public static final String timeEnded = "timeEnded";
//
//    @Override
//    public String getPin() {
//        return PIN;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public ParseQuery<Alarm> getAllNetworkQuery() {
//        return new QueryBuilder(false).build();
//    }
//
//    @Override
//    public void updateFromJSON(final Context context,
//                               final JSONObject jsonObject) {
//        new HandleException(context, TAG, "updateFromJSON", new IllegalStateException("Alarm handling disabled!"));
//
////        try {
////            String objectId = jsonObject
////                    .getString(ExtendedParseObject.objectId);
////            final String guardObjectId = (jsonObject.has(guard)) ? jsonObject
////                    .getJSONObject(guard).getString(
////                            ExtendedParseObject.objectId) : null;
////            if (objectId != null) {
////                // ensure that we know the client, otherwise pin it
////                String clientObjectId = (jsonObject.has(client)) ? jsonObject
////                        .getJSONObject(client).getString(
////                                ExtendedParseObject.objectId) : null;
////                getFromObjectId(Client.class, clientObjectId, true, new GetCallback<Client>() {
////                    @Override
////                    public void done(Client client, ParseException e) {
////                        if (e != null) {
////                            Log.e(TAG,
////                                    "updateFromJSON get client from objectId",
////                                    e);
////                            return;
////                        }
////                        // just in case we did not know the client
////                        client.pinInBackground();
////                    }
////                });
////                getFromObjectId(Alarm.class, objectId, true,
////                        new GetCallback<Alarm>() {
////
////                            @Override
////                            public void done(final Alarm alarm, ParseException e) {
////                                if (e != null) {
////                                    Log.e(TAG,
////                                            "updateFromJSON get alarm from objectId",
////                                            e);
////                                    return;
////                                }
////
////                                getFromObjectId(Guard.class, guardObjectId,
////                                        true, new GetCallback<Guard>() {
////
////                                            @Override
////                                            public void done(
////                                                    Guard updatedGuard,
////                                                    ParseException e) {
////                                                if (e != null) {
////                                                    Log.e(TAG,
////                                                            "updateFromJSON get guard from objectId",
////                                                            e);
////                                                    return;
////                                                }
////                                                /*
////                                                 * Push checkpoint should weed
////												 * out updates made by this
////												 * device
////												 *
////												 * Nevertheless as a precaution
////												 * an extra check is made here
////												 */
////                                                Guard currentGuard =  new GuardCache(context).getLoggedIn();
////                                                if (currentGuard != null) {
////                                                    if (updatedGuard
////                                                            .equals(currentGuard)) {
////                                                        Log.e(TAG,
////                                                                "I should not receive this init!");
////                                                        return;
////                                                    }
////                                                }
////
////                                                try {
////                                                    boolean updatedAccepted = (jsonObject
////                                                            .has(accepted)) ? jsonObject
////                                                            .getBoolean(accepted)
////                                                            : false;
////
////                                                    boolean updatedAborted = (jsonObject
////                                                            .has(aborted)) ? jsonObject
////                                                            .getBoolean(aborted)
////                                                            : false;
////
////                                                    String updatedTimeStartedString = (jsonObject
////                                                            .has(timeStarted)) ? jsonObject
////                                                            .getJSONObject(
////                                                                    timeStarted)
////                                                            .getString("iso")
////                                                            : null;
////                                                    String updatedTimeEndedString = (jsonObject
////                                                            .has(timeEnded)) ? jsonObject
////                                                            .getJSONObject(
////                                                                    timeEnded)
////                                                            .getString("iso")
////                                                            : null;
////
////                                                    DateTimeFormatter fmt = ISODateTimeFormat
////                                                            .dateTime();
////                                                    Date updatedTimeStarted = null;
////                                                    if (updatedTimeStartedString != null) {
////                                                        updatedTimeStarted = fmt
////                                                                .parseDateTime(
////                                                                        updatedTimeStartedString)
////                                                                .toDate();
////                                                    }
////
////                                                    Date updatedTimeEnded = null;
////                                                    if (updatedTimeEndedString != null) {
////                                                        updatedTimeEnded = fmt
////                                                                .parseDateTime(
////                                                                        updatedTimeEndedString)
////                                                                .toDate();
////                                                    }
////
////                                                    // init values
////
////                                                    final boolean notify = (!alarm
////                                                            .isAccepted() && updatedAccepted);
////
////                                                    Log.e(TAG,
////                                                            "Alarm is setAccepted: "
////                                                                    + alarm.isAccepted());
////                                                    Log.e(TAG,
////                                                            "Update is setAccepted: "
////                                                                    + updatedAccepted);
////                                                    Log.e(TAG, "Notify: "
////                                                            + notify);
////
////                                                    if (updatedGuard != null) {
////                                                        alarm.setGuard(updatedGuard);
////                                                    } else {
////                                                        alarm.remove(guard);
////                                                    }
////                                                    put(Alarm.accepted, updatedAccepted);
////                                                    put(Alarm.aborted, updatedAborted);
////                                                    if (updatedTimeStarted != null) {
////                                                        alarm.setTimeStarted(updatedTimeStarted);
////                                                    } else {
////                                                        alarm.remove(timeStarted);
////                                                    }
////                                                    if (updatedTimeEnded != null) {
////                                                        alarm.setTimeEnded(updatedTimeEnded);
////                                                    } else {
////                                                        alarm.remove(timeEnded);
////                                                    }
////
////                                                    pinUpdate(
////                                                            alarm,
////                                                            new DataStoreCallback<Alarm>() {
////
////                                                                @Override
////                                                                public void success(
////                                                                        List<Alarm> objects) {
////
////                                                                    Alarm alarm = objects
////                                                                            .get(0);
////
////                                                                    if (notify) {
////                                                                        EventBusController.postParseObjectUpdated(alarm);
////                                                                    }
////                                                                }
////
////                                                                @Override
////                                                                public void failed(
////                                                                        ParseException e) {
////                                                                    Log.e(TAG,
////                                                                            "updateFromJSON",
////                                                                            e);
////                                                                }
////
////                                                            });
////
////                                                } catch (JSONException e2) {
////                                                    Log.e(TAG, e2.getMessage(),
////                                                            e2);
////                                                }
////                                            }
////
////                                        });
////                            }
////                        });
////            }
////
////        } catch (JSONException e) {
////            Log.e(TAG, e.getMessage(), e);
////        }
//    }
//    @Override
//    public TaskQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
//        return new QueryBuilder(fromLocalDatastore);
//    }
//
//
//    public static class QueryBuilder extends TaskQueryBuilder<Alarm> {
//
//        public QueryBuilder(boolean fromLocalDatastore) {
//            super(PIN, fromLocalDatastore, ParseQuery.getQuery(Alarm.class));
//        }
//
//        @Override
//        public ParseQuery<Alarm> build() {
//            query.include(Alarm.client);
//            query.include(Alarm.client + "." + Client.contacts);
//            query.include(Alarm.guard);
//            query.include(Alarm.central);
//
//            query.whereDoesNotExist("missingCentral");
//            query.whereExists(Alarm.client);
//
//            query.setLimit(1000);
//            return super.build();
//        }
//
//        public ParseQuery<Alarm> buildNoIncludes() {
//            query.setLimit(1000);
//            return super.build();
//        }
//
//        public QueryBuilder whereNotAssigned() {
//
//            ParseQuery<Alarm> queryNotAssigned = ParseQuery
//                    .getQuery(Alarm.class);
//            queryNotAssigned.whereDoesNotExist(Alarm.guard);
//
//            ParseQuery<Alarm> queryAborted = ParseQuery.getQuery(Alarm.class);
//            queryAborted.whereEqualTo(Alarm.aborted, true);
//
//            appendQueries(queryNotAssigned, queryAborted);
//
//            return this;
//        }
//
//        public QueryBuilder whereNotIgnoredBy(Guard guard) {
//
//            if (guard != null) {
//                query.whereNotEqualTo(ignoredBy, guard);
//            }
//
//            return this;
//        }
//
//
//        public QueryBuilder matching(Guard guard) {
//
//            query.whereEqualTo(Alarm.guard, guard);
//
//            return this;
//        }
//
//        public QueryBuilder notMatching(Alarm alarm) {
//
//            if (alarm != null)
//                query.whereNotEqualTo(objectId, alarm.getObjectId());
//
//            return this;
//        }
//
//        public QueryBuilder whereNotEnded() {
//
//            query.whereDoesNotExist(Alarm.timeEnded);
//
//            return this;
//        }
//
//        public QueryBuilder whereEnded() {
//
//            query.whereExists(Alarm.timeEnded);
//            query.orderByDescending(CircuitUnit.timeEnded);
//
//            return this;
//        }
//
//        public QueryBuilder sortedByCreateDate() {
//            query.orderByDescending(Alarm.createdAt);
//
//            return this;
//        }
//
//        public QueryBuilder within(int kilometers, Location fromLocation) {
//            ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
//            query.whereWithinKilometers(position, parseGeoPoint, kilometers);
//            return this;
//        }
//    }
//
////    public static CroutonText createCroutonText(Alarm alarm) {
////        Style style = Style.INFO;
////        String eventText = "Alarm hos " + alarm.getClient().getName() + " ";
////
////        if (alarm.getGuard() != null) {
////            if (alarm.isClosed()) {
////                style = Style.CONFIRM;
////                eventText += "afsluttet";
////            } else if (alarm.isArrived()) {
////                style = Style.INFO;
////                eventText += "ankommet";
////            } else if (alarm.isAccepted()) {
////                style = Style.INFO;
////                eventText += "accepteret";
////            }
////
////            eventText += " af " + alarm.getGuardName();
////
////        } else {
////            style = Style.ALERT;
////            eventText += "afbrudt";
////        }
////
////        return new CroutonText(eventText, style);
////    }
//
//    public String getForwardedTo() {
//        return getString(alarmGroup);
//    }
//
//    public void setForwardedTo(CharSequence forwardedTo) {
//        put(alarmGroup, forwardedTo);
//    }
//
//    public String getCentralName() {
//        return getParseObject(central).getString("name");
//    }
//
//    public boolean isArrived() {
//        return has(Alarm.timeStarted);
//    }
//
//    public boolean isFinished() {
//        return has(Alarm.timeEnded);
//    }
//
//    // reaction
//
//    public void setIgnored(Guard guard) {
//        addUnique(ignoredBy, guard);
//    }
//
//    public void cancelIgnored(Guard guard) {
//        if (getList(ignoredBy) != null)
//            getList(ignoredBy).remove(guard);
//    }
//
//    public void setAccepted(Guard guard) {
//        setGuard(guard);
//        cancelIgnored(guard);
//        put(Alarm.accepted, true);
//        put(Alarm.aborted, false);
//        if (!has(timeAccepted)) {
//            put(timeAccepted, new Date());
//        }
//
//    }
//
//
//    public void setArrived(Context context, Guard guard) {
////        setArrivedReported(true);
//
//        setGuard(guard);
//
//        put(Alarm.timeArrived, new Date());
//        put(Alarm.timeStarted, new Date());
//
//
//
////        getTaskSummaryInstance(context).event(event, TaskSummary.EVENT_TYPE.ARRIVE);
//    }
//
//    public void setAborted(Guard guard) {
//        setIgnored(guard);
//        put(Alarm.aborted, true);
//    }
//
//    public void setFinished(Guard guard) {
//        setGuard(guard);
//        setTimeEndedNow();
//    }
//
//    public boolean isAborted() {
//        return getBoolean(Alarm.aborted);
//    }
//
//
//    public boolean isAccepted() {
//        return getBoolean(Alarm.accepted);
//    }
//
//    public boolean isClosed() {
//        return has(Alarm.timeEnded);
//    }
//
//    public void setGuard(Guard guard) {
//        put(Alarm.guard, guard);
//    }
//
//
//    private void setTimeEndedNow() {
//        put(Alarm.timeEnded, new Date());
//    }
//
//    public Date getTimeAccepted() {
//        return getDate(timeAccepted);
//    }
//
//    public Date getTimeArrived() {
//        return getDate(timeStarted);
//    }
//
//    public Date getTimeEnded() {
//        return getDate(timeEnded);
//    }
//
//    // shared status
//
//    public String getGuardName() {
//        return getGuard().getName();
//    }
//
//    public boolean takenByAnyGuard() {
//        return has(Alarm.guard);
//    }
//
//    public boolean takenByThisGuard(Guard guard) {
//        return takenByAnyGuard() && getGuard().equals(guard);
//    }
//
//    public boolean takenByAnotherGuard(Guard guard) {
//        return takenByAnyGuard() && !takenByThisGuard(guard);
//    }
//
//    public boolean arrivedByAnotherGuard(Guard guard) {
//
//        return takenByAnotherGuard(guard) && isArrived();
//    }
//
//    public boolean finishedByAnotherGuard(Guard guard) {
//
//        return takenByAnotherGuard(guard) && isClosed();
//    }
//
//    // public void setAborted(Guard guard) {
//    // setAccepted(false);
//    // remove(ignoredBy);
//    // setIgnored(guard);
//    // remove(Alarm.guard);
//    // remove(Alarm.timeStarted);
//    // remove(Alarm.timeEnded);
//    // }
//
//    // message
//
//    public String getDrivingGuidance() {
//        return getString(Alarm.drivingGuidance);
//    }
//
//    public String getAccessRoute() {
//        return getString(Alarm.accessRoute);
//    }
//
//    public String getType() {
//        return getString(Alarm.type);
//    }
//
//    @Override
//    public String getReportId() {
//        return getObjectId();
//    }
//
//    public String getZone() {
//        return getString(Alarm.zone);
//    }
//
//    public Date getAlarmTime() {
//        return getDate(Alarm.alarmTime);
//    }
//
//    public String getSecurityLevelString() {
//        if (!has(Alarm.securityLevel)) {
//            return "10";
//        }
//        if (getString(Alarm.securityLevel) == null
//                || getString(Alarm.securityLevel).isEmpty()) {
//            return "10";
//        }
//        return getString(Alarm.securityLevel);
//    }
//
//    public int getSecurityLevelInteger() {
//        return Integer.parseInt(getString(Alarm.securityLevel));
//    }
//
//    public String getHardwareId() {
//        return getString(Alarm.hardwareId);
//    }
//
//    public String getSerial() {
//        return getString(Alarm.serial);
//    }
//
//    public String getInstaller() {
//        return getString(Alarm.installer);
//    }
//
//    public String getKeyboxLocation() {
//        return getString(Alarm.keyboxLocation);
//    }
//
//    public String getBypassLocation() {
//        return getString(Alarm.bypassLocation);
//    }
//
//    public String getControlpanelLocation() {
//        return getString(Alarm.controlpanelLocation);
//    }
//
//    public String getSmokecannonLocation() {
//        return getString(Alarm.smokecannonLocation);
//    }
//
//    public String getGuardCode() {
//        return getString(Alarm.guardCode);
//    }
//
//    public String getBypassCode() {
//        return getString(Alarm.bypassCode);
//    }
//
//    public String getRemark() {
//        return getString(Alarm.remark);
//    }
//
//    public Guard getGuard() {
//        Guard guard = (Guard) getParseObject(Alarm.guard);
//        if (guard == null)
//            return null;
//
//        if (guard.isDataAvailable()) {
//            return guard;
//        } else {
//            try {
//                guard.fetchFromLocalDatastore();
//            } catch (ParseException e) {
//                Log.e(TAG, "getGuard", e);
//            }
//            return guard;
//        }
//    }
//
//    public Client getClient() {
//        Client client = (Client) getParseObject(Alarm.client);
//        if (client == null)
//            return null;
//
//        if (client.isDataAvailable()) {
//            return client;
//        } else {
//            try {
//                client.fetchFromLocalDatastore();
//            } catch (ParseException e) {
//                Log.e(TAG, "getClient", e);
//            }
//            return client;
//        }
//    }
//
//    @Override
//    public String getClientName() {
//        return getClient().getName();
//    }
//
//    public Date getTimeStartedDriving() {
//        if (has(timeStartedDriving)) {
//            return getDate(timeStartedDriving);
//        } else {
//            if (getTimeAccepted() != null) {
//                put(timeStartedDriving, getTimeAccepted());
//                pinThenSaveEventually();
//            }
//            return getTimeAccepted();
//        }
//    }
//
//    public void setTimeStartedDriving(Date date) {
//        put(timeStartedDriving, date);
//    }
//
//    protected void setTimeEnded(Date updatedTimeEnded) {
//        put(Alarm.timeEnded, updatedTimeEnded);
//
//    }
//
//    protected void setTimeStarted(Date updatedTimeStarted) {
//        put(Alarm.timeStarted, updatedTimeStarted);
//    }
//
////    public static void createAlarmDialogIfNeeded(final Context context) {
////
////        ParseQuery<Alarm> query = Alarm.getQueryBuilder(true)
////                .whereNotAssigned().notMatching(Alarm.Recent.getSelected())
////                .whereNotIgnoredBy(Guard.Recent.getSelected()).sortedByCreateDate().build();
////
////        query.findInBackground(new FindCallback<Alarm>() {
////
////            @Override
////            public void done(List<Alarm> alarms, ParseException e) {
////                if (e != null) {
////                    Log.e(TAG, e.getMessage(), e);
////                    return;
////                }
////                Log.d(TAG, "Found alarms: " + alarms.size());
////                if (alarms.size() > 0) {
////
////                    Alarm alarm = alarms.get(0);
////
////                    if (alarm.getClient() != null) {
////                        Log.e(TAG, "!! -- CREATING ALARM DIALOG -- !!");
////                        Intent i = new Intent(context, AlarmDialogActivity.class);
////                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                        context.startActivity(i);
////                    } else {
////                        Crashlytics.logException(new IllegalStateException("Missing client for Alarm" + alarm.getObjectId()));
////                    }
////
////                }
////
////
////            }
////        });
////
////    }
//
//}

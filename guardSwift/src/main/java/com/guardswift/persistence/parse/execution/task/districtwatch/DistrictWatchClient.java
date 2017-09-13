//package com.guardswift.persistence.parse.execution.task.districtwatch;
//
//import android.content.Context;
//import android.location.Location;
//
//import com.guardswift.core.exceptions.HandleException;
//import com.guardswift.core.parse.ParseModule;
//import com.guardswift.core.tasks.activity.NoActivityStrategy;
//import com.guardswift.core.tasks.activity.TaskActivityStrategy;
//import com.guardswift.core.tasks.automation.ResetOnDepartureAutomationStrategy;
//import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
//import com.guardswift.core.tasks.controller.RaidController;
//import com.guardswift.core.tasks.controller.TaskController;
//import com.guardswift.core.tasks.geofence.RaidGeofenceStrategy;
//import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
//import com.guardswift.persistence.cache.task.BaseTaskCache;
//import com.guardswift.persistence.parse.ExtendedParseObject;
//import com.guardswift.persistence.parse.TaskQueryBuilder;
//import com.guardswift.persistence.parse.data.Guard;
//import com.guardswift.persistence.parse.data.client.Client;
//import com.guardswift.persistence.parse.documentation.event.EventLog;
//import com.guardswift.persistence.parse.execution.BaseTask;
//import com.guardswift.ui.GuardSwiftApplication;
//import com.parse.ParseClassName;
//import com.parse.ParseException;
//import com.parse.ParseGeoPoint;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;
//
//import org.json.JSONObject;
//
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//
//@ParseClassName("DistrictWatchClient")
//public class DistrictWatchClient extends BaseTask {
//
//
//    @Override
//    public BaseTaskCache<DistrictWatchClient> getCache() {
//        return GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchClientCache();
//    }
//
//    @Override
//    public void setPending() {
//
//    }
//
//    @Override
//    public void setAccepted() {
//
//    }
//
//    @Override
//    public void setArrived() {
//
//    }
//
//    @Override
//    public void setAborted() {
//
//    }
//
//    @Override
//    public void setFinished() {
//
//    }
//
//    @Override
//    public TaskGeofenceStrategy getGeofenceStrategy() {
//        return RaidGeofenceStrategy.getInstance(this);
//    }
//
//    @Override
//    public TaskActivityStrategy getActivityStrategy() {
//        return NoActivityStrategy.getInstance();
//    }
//
//    @Override
//    public TaskAutomationStrategy getAutomationStrategy() {
//        return ResetOnDepartureAutomationStrategy.getInstance(this);
//    }
//
//
//    @Override
//    public TaskController getController() {
//        return RaidController.getInstance();
//    }
//
//    @Override
//    public int getEventCode() {
//        return EventLog.EventCodes.RAID_OTHER;
//    }
//
////    public static class Recent {
////        private static DistrictWatchClient selected;
////
////        public static DistrictWatchClient getSelected() {
////            return selected;
////        }
////
////        public static void setSelected(DistrictWatchClient selected) {
////            Recent.selected = selected;
////
////            if (selected != null) {
////                Client.Recent.setSelected(selected.getClient());
////            }
////        }
////
////    }
//
//
//
////    @Override
////    public String getTaskTitle(Context context) {
////        return context.getString(R.string.task_districtwatch) + "\n" + getClient().getName() + " " + getFullAddress();
////    }
//
//
//
//
//    @Override
//    public TASK_TYPE getTaskType() {
//        return TASK_TYPE.DISTRICTWATCH;
//    }
//
//    @Override
//    public List<TASK_TYPE> getPossibleTaskTypes() {
//        return Collections.singletonList(TASK_TYPE.DISTRICTWATCH);
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
//        return TASK_STATE.PENDING;
//    }
//
//    @Override
//    public String getType() {
//        return getString(districtWatchType);
//    }
//
//    @Override
//    public String getReportId() {
//        try {
//            return DistrictWatchStarted.Query.findFrom(getDistrictWatch()).getObjectId();
//        } catch (ParseException e) {
//            return "";
//        }
//    }
//
//
//
//    @Override
//    public ExtendedParseObject getParseObject() {
//        return this;
//    }
//
//    @Override
//    public boolean isPending() {
//        return false;
//    }
//
//    @Override
//    public boolean isAccepted() {
//        return false;
//    }
//
//
//    public static final int SORTBY_NEAREST = 1;
//    public static final int SORTBY_ADDRESS = 2;
//
//    public static final String days = "days";
//    public static final String guard = "guard";
//    public static final String client = "client";
//    //	public static final String clientName = "clientName";
//    public static final String addressName = "addressName";
//    public static final String addressNumber = "addressNumber";
//    public static final String fullAddress = "fullAddress";
//    public static final String cityName = "cityName";
//    public static final String zipcode = "zipcode";
//    public static final String position = "position";
//    public static final String districtWatch = "districtWatch";
//    public static final String districtWatchType = "districtWatchType";
//
//    public static final String completed = "completed"; // timesArrived == supervisions
//    public static final String supervisions = "supervisions";
//    public static final String timesArrived = "timesArrived";
//    public static final String arrived = "arrived";
//
//    @Override
//    public String getParseClassName() {
//        return DistrictWatchClient.class.getSimpleName();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public ParseQuery<DistrictWatchClient> getAllNetworkQuery() {
//        return new QueryBuilder(false).build();
//    }
//
//    @Override
//    public void updateFromJSON(final Context context,
//                               final JSONObject jsonObject) {
//        // TODO Auto-generated method stub
//    }
//
//    public TaskQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
//        return new QueryBuilder(fromLocalDatastore);
//    }
//
//    public DistrictWatchStarted getDistrictWatchStarted() {
//        DistrictWatch districtWatch = getDistrictWatch();
//        try {
//            return DistrictWatchStarted.Query.findFrom(districtWatch);
//        } catch (ParseException e) {
//            new HandleException(TAG, "getDistrictWatchStarted", e);
//        }
//        return null;
//    }
//
//    public static class QueryBuilder extends
//            TaskQueryBuilder<DistrictWatchClient> {
//
//        public QueryBuilder(boolean fromLocalDatastore) {
//            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
//                    .getQuery(DistrictWatchClient.class));
//        }
//
//        @Override
//        public ParseQuery<DistrictWatchClient> build() {
//            query.include(client);
//            query.include(districtWatch);
//            query.setLimit(1000);
//            return super.build();
//        }
//
//
//        public ParseQuery<DistrictWatchClient> buildNoIncludes() {
//            query.setLimit(1000);
//            return super.build();
//        }
//
//        public QueryBuilder matching(DistrictWatch districtWatch) {
//
//            query.whereEqualTo(DistrictWatchClient.districtWatch, districtWatch);
//
//            return this;
//        }
//
//        public QueryBuilder isRunToday() {
//            Calendar c = Calendar.getInstance();
//            c.setTime(new Date());
//            int day_of_week = c.get(Calendar.DAY_OF_WEEK);
//
//            int javascript_day_of_week = day_of_week - 1;
//
//            query.whereEqualTo(days, javascript_day_of_week);
//            return this;
//        }
//
//        public QueryBuilder within(int kilometers, Location fromLocation) {
//            ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
//            if (parseGeoPoint != null)
//                query.whereWithinKilometers(position, parseGeoPoint, kilometers);
//            return this;
//        }
//
//        public QueryBuilder whereArrived(boolean arrivedState) {
//            query.whereEqualTo(DistrictWatchClient.arrived, arrivedState);
//            return this;
//        }
//
//        public QueryBuilder whereVisited() {
//            query.whereGreaterThan(DistrictWatchClient.timesArrived, 0);
//            return this;
//        }
//
//
//        public QueryBuilder sortBy(int sortBy) {
//            switch (sortBy) {
//                case DistrictWatchClient.SORTBY_NEAREST:
//                    ParseModule.sortNearest(query, Client.position);
//                    break;
//                case DistrictWatchClient.SORTBY_ADDRESS:
//                    query.orderByAscending(DistrictWatchClient.fullAddress);
//                    break;
//                default:
//                    break;
//            }
//
//            query.orderByAscending(DistrictWatchClient.fullAddress);
//
//            return this;
//        }
//
//        public QueryBuilder whereTimesArrivedEqualsExpected() {
////            query.whereMatchesKeyInQuery(supervisions, timesArrived, new QueryBuilder(fromLocalDatastore).buildAsParseObject());
//            query.whereEqualTo(completed, true);
//            return this;
//        }
//
//        public QueryBuilder whereTimesArrivedNotEqualsExpected() {
//            query.whereEqualTo(completed, false);
////            query.whereDoesNotMatchKeyInQuery(supervisions, timesArrived, ParseQuery.getQuery(DistrictWatchClient.class).fromLocalDatastore().setLimit(1000));
//            return this;
//        }
//    }
//
//    public ParseObject getOwner() {
//        return getParseObject(owner);
//    }
//
//    public String getClientName() {
//        return (getClient() != null) ? getClient().getName() : "N/A";
//    }
//
//    public String getAddressName() {
//        return getString(addressName);
//    }
//
//    public String getAddressNumber() {
//        return getString(addressNumber);
//    }
//
//    public String getFullAddress() {
//        return getString(fullAddress);
//    }
//
//    public String getCityName() {
//        return getString(cityName);
//    }
//
//    public String getZipcode() {
//        return getString(zipcode);
//    }
//
//    public int getSupervisions() {
//        return getInt(supervisions);
//    }
//
//    public void setCompleted(boolean value) {
//        put(completed, value);
//    }
//
//    public ParseGeoPoint getPosition() {
//        return getParseGeoPoint(position);
//    }
//
//    // districtWatch
//
//    public Client getClient() {
//        return (Client)getLDSFallbackParseObject(client);
//    }
//
//    public String getDistrictWatchType() {
//        return getString(districtWatchType);
//    }
//
//    public DistrictWatch getDistrictWatch() {
//        return (DistrictWatch)getLDSFallbackParseObject(districtWatch);
//    }
//
//
//    public int getTimesArrived() {
//        if (has(timesArrived)) {
//            return getInt(timesArrived);
//        }
//        return 0;
//    }
//
//    private void incrementArrived() {
//        if (!has(timesArrived)) {
//            put(DistrictWatchClient.timesArrived, 1);
//            return;
//        }
//        increment(DistrictWatchClient.timesArrived);
//
//        if (getTimesArrived() >= getSupervisions()) {
//            setCompleted(true);
//        }
//    }
//
//    public void reset() {
//        setArrived(false);
//        setCompleted(false);
//    }
//
//    public void setArrived(boolean arrived) {
//        if (arrived) {
//            incrementArrived();
//        }
//        put(DistrictWatchClient.arrived, arrived);
//        put(DistrictWatchClient.guard, GuardSwiftApplication.getInstance().getCacheFactory().getGuardCache().getLoggedIn());
//    }
//
//    public boolean isFinished() {
//        return getTimesArrived() == getSupervisions();
//    }
//
//    @Override
//    public boolean isWithinScheduledTime() {
//        return true;
//    }
//
//    @Override
//    public int getRadius() {
//        return 50;
//    }
//
//    @Override
//    public Guard getGuard() {
//        return (Guard)getLDSFallbackParseObject(guard);
//    }
//
//    public boolean isArrived() {
//        return getBoolean(arrived);
//    }
//
//    @Override
//    public boolean isAborted() {
//        return false;
//    }
//
//}

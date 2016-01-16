package com.guardswift.persistence.parse.execution.districtwatch;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.guardswift.core.documentation.report.NoTaskReportingStrategy;
import com.guardswift.core.documentation.report.TaskReportingStrategy;
import com.guardswift.core.exceptions.HandleException;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.activity.DoNothingStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
import com.guardswift.core.tasks.automation.DistrictWatchAutomationStrategy;
import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
import com.guardswift.core.tasks.controller.DistrictWatchClientController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.geofence.DistrictWatchGeofenceStrategy;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.TaskQueryBuilder;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

@ParseClassName("DistrictWatchClient")
public class DistrictWatchClient extends BaseTask {

    DistrictWatchClientController controller;
    TaskReportingStrategy<DistrictWatchClient> taskReportingStrategy;
    TaskGeofenceStrategy<DistrictWatchClient> geofenceStrategy;
    TaskActivityStrategy<DistrictWatchClient> activityStrategy;
    TaskAutomationStrategy<DistrictWatchClient> automationStrategy;

    public DistrictWatchClient() {
        controller = new DistrictWatchClientController();
        taskReportingStrategy = new NoTaskReportingStrategy<>();
        automationStrategy = new DistrictWatchAutomationStrategy<>(this);
        geofenceStrategy = new DistrictWatchGeofenceStrategy<>(this);
        activityStrategy = new DoNothingStrategy<>();
    }

    @Override
    public BaseTaskCache getCache() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getDistrictWatchClientCache();
    }

    @Override
    public TaskGeofenceStrategy<DistrictWatchClient> getGeofenceStrategy() {
        return geofenceStrategy;
    }

    @Override
    public TaskActivityStrategy<DistrictWatchClient> getActivityStrategy() {
        return activityStrategy;
    }

    @Override
    public TaskAutomationStrategy<DistrictWatchClient> getAutomationStrategy() {
        return automationStrategy;
    }

    @Override
    public TaskReportingStrategy<DistrictWatchClient> getTaskReportingStrategy() {
        return taskReportingStrategy ;
    }

    @Override
    public TaskController<DistrictWatchClient> getController() {
        return new DistrictWatchClientController();
    }

    @Override
    public int getEventCode() {
        return EventLog.EventCodes.DISTRICTWATCH_OTHER;
    }

//    public static class Recent {
//        private static DistrictWatchClient selected;
//
//        public static DistrictWatchClient getSelected() {
//            return selected;
//        }
//
//        public static void setSelected(DistrictWatchClient selected) {
//            Recent.selected = selected;
//
//            if (selected != null) {
//                Client.Recent.setSelected(selected.getClient());
//            }
//        }
//
//    }



//    @Override
//    public String getTaskTitle(Context context) {
//        return context.getString(R.string.task_districtwatch) + "\n" + getClient().getName() + " " + getFullAddress();
//    }




    @Override
    public TASK_TYPE getTaskType() {
        return TASK_TYPE.DISTRICTWATCH;
    }

    @Override
    public TASK_STATE getTaskState() {
        if (isFinished()) {
            return TASK_STATE.FINSIHED;
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
        return getString(districtWatchType);
    }

    @Override
    public String getReportId() {
        try {
            return DistrictWatchStarted.Query.findFrom(getDistrictWatch()).getObjectId() + getObjectId();
        } catch (ParseException e) {
            return "";
        }
    }



    @Override
    public ExtendedParseObject getParseObject() {
        return this;
    }

    @Override
    public boolean isWithinScheduledTime() {

//        if (BuildConfig.DEBUG)
//            return true;

        Log.d(TAG, "isWithinScheduledTime ");

        return true;

//        DateTime timeStartOrg = new DateTime(getDistrictWatch().getTimeStart());
//        DateTime timeEndOrg = new DateTime(getDistrictWatch().getTimeEnd());
//
//        DistrictWatchStarted districtWatchStarted = DistrictWatchStarted.Recent.getSelected();
//
//        MutableDateTime timeStart = new MutableDateTime(districtWatchStarted.getCreatedAt());
//        int startHour = timeStartOrg.getHourOfDay();
//        timeStart.setHourOfDay(startHour);
//        timeStart.setMinuteOfHour(timeStartOrg.getMinuteOfHour());
//
//        MutableDateTime timeEnd = new MutableDateTime(districtWatchStarted.getCreatedAt());
//        int endHour = timeEndOrg.getHourOfDay();
//        if (endHour < startHour)
//            timeEnd.addDays(1);
//
//        timeEnd.setHourOfDay(endHour);
//        timeEnd.setMinuteOfHour(timeEndOrg.getMinuteOfHour());
//
//        DateTime now = DateTime.now(DateTimeZone.getDefault());
//
////        Log.d(TAG, "-- timeStart: " + timeStart.getHourOfDay() + ":" + timeStart.getMinuteOfHour());
////        Log.d(TAG, "-- timeEnd: " + timeEnd.getHourOfDay() + ":" + timeEnd.getMinuteOfHour());
////        Log.d(TAG, "-- now: " + now.getHourOfDay() + ":" + now.getMinuteOfHour());
//
//        boolean afterTimeStart = now.isAfter(timeStart);
//        boolean beforeTimeEnd = now.isBefore(timeEnd);
//
////        Log.d(TAG, "  -- afterTimeStart: " + afterTimeStart);
////        Log.d(TAG, "  -- beforeTimeEnd: " + beforeTimeEnd);
//
//        return afterTimeStart && beforeTimeEnd;
    }




    public static final String PIN = "DistrictWatchClient";

    public static final int SORTBY_NEAREST = 1;
    public static final int SORTBY_ADDRESS = 2;

    public static final String days = "days";
    public static final String client = "client";
    //	public static final String clientName = "clientName";
    public static final String addressName = "addressName";
    public static final String addressNumber = "addressNumber";
    public static final String fullAddress = "fullAddress";
    public static final String cityName = "cityName";
    public static final String zipcode = "zipcode";
    public static final String position = "position";
    public static final String districtWatch = "districtWatch";
    public static final String districtWatchUnit = "districtWatchUnit";
    public static final String districtWatchType = "districtWatchType";

    public static final String completed = "completed"; // timesArrived == supervisions
    public static final String supervisions = "supervisions";
    public static final String timesArrived = "timesArrived";
    public static final String arrived = "arrived";

    @Override
    public String getPin() {
        return PIN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<DistrictWatchClient> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
        // TODO Auto-generated method stub
    }

    public TaskQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public DistrictWatchStarted getDistrictWatchStarted() {
        DistrictWatch districtWatch = getDistrictWatch();
        try {
            return DistrictWatchStarted.Query.findFrom(districtWatch);
        } catch (ParseException e) {
            new HandleException(TAG, "getDistrictWatchStarted", e);
        }
        return null;
    }

    public static class QueryBuilder extends
            TaskQueryBuilder<DistrictWatchClient> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(PIN, fromLocalDatastore, ParseQuery
                    .getQuery(DistrictWatchClient.class));
        }

        @Override
        public ParseQuery<DistrictWatchClient> build() {
            query.include(client);
            query.include(districtWatch);
            query.include(districtWatchUnit);
            query.setLimit(1000);
            return super.build();
        }


        public ParseQuery<DistrictWatchClient> buildNoIncludes() {
            query.setLimit(1000);
            return super.build();
        }

        public QueryBuilder matching(DistrictWatch districtWatch) {

            query.whereEqualTo(DistrictWatchClient.districtWatch, districtWatch);

            return this;
        }

        public QueryBuilder isRunToday() {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int day_of_week = c.get(Calendar.DAY_OF_WEEK);

            int javascript_day_of_week = day_of_week - 1;

            query.whereEqualTo(days, javascript_day_of_week);
            return this;
        }

        public QueryBuilder within(int kilometers, Location fromLocation) {
            ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
            if (parseGeoPoint != null)
                query.whereWithinKilometers(position, parseGeoPoint, kilometers);
            return this;
        }

        public QueryBuilder whereArrived(boolean arrivedState) {
            query.whereEqualTo(DistrictWatchClient.arrived, arrivedState);
            return this;
        }

        public QueryBuilder whereVisited() {
            query.whereGreaterThan(DistrictWatchClient.timesArrived, 0);
            return this;
        }


        public QueryBuilder sortBy(int sortBy) {
            switch (sortBy) {
                case DistrictWatchClient.SORTBY_NEAREST:
                    ParseModule.sortNearest(query, Client.position);
                    break;
                case DistrictWatchClient.SORTBY_ADDRESS:
                    query.orderByAscending(DistrictWatchClient.fullAddress);
                    break;
                default:
                    break;
            }

            query.orderByAscending(DistrictWatchClient.fullAddress);

            return this;
        }

        public QueryBuilder whereTimesArrivedEqualsExpected() {
//            query.whereMatchesKeyInQuery(supervisions, timesArrived, new QueryBuilder(fromLocalDatastore).buildAsParseObject());
            query.whereEqualTo(completed, true);
            return this;
        }

        public QueryBuilder whereTimesArrivedNotEqualsExpected() {
            query.whereEqualTo(completed, false);
//            query.whereDoesNotMatchKeyInQuery(supervisions, timesArrived, ParseQuery.getQuery(DistrictWatchClient.class).fromLocalDatastore().setLimit(1000));
            return this;
        }
    }

    public ParseObject getOwner() {
        return getParseObject(owner);
    }

    public String getClientName() {
        return getClient().getName();
    }

    public String getAddressName() {
        return getString(addressName);
    }

    public String getAddressNumber() {
        return getString(addressNumber);
    }

    public String getFullAddress() {
        return getString(fullAddress);
    }

    public String getCityName() {
        return getString(cityName);
    }

    public String getZipcode() {
        return getString(zipcode);
    }

    public int getSupervisions() {
        return getInt(supervisions);
    }

    public void setCompleted(boolean value) {
        put(completed, value);
    }

    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(position);
    }

    // districtWatch

    public Client getClient() {
        return (Client) getParseObject(client);
    }

    public String getDistrictWatchType() {
        return getString(districtWatchType);
    }

    public DistrictWatch getDistrictWatch() {
        return (DistrictWatch) getParseObject(districtWatch);
    }

    public DistrictWatchUnit getDistrictWatchUnit() {
        return (DistrictWatchUnit) getParseObject(districtWatchUnit);
    }

    public int getTimesArrived() {
        if (has(timesArrived)) {
            return getInt(timesArrived);
        }
        return 0;
    }

    private void incrementArrived() {
        if (!has(timesArrived)) {
            put(DistrictWatchClient.timesArrived, 1);
            return;
        }
        increment(DistrictWatchClient.timesArrived);

        if (getTimesArrived() >= getSupervisions()) {
            setCompleted(true);
        }
    }

    public void reset() {
        setArrived(false);
        setCompleted(false);
    }

    public void setArrived(boolean arrived) {
        if (arrived) {
            incrementArrived();
        }
        put(DistrictWatchClient.arrived, arrived);
    }

    public boolean isFinished() {
        return getTimesArrived() == getSupervisions();
    }

    public boolean isArrived() {
        return getBoolean(arrived);
    }

    @Override
    public boolean isAborted() {
        return false;
    }

}

package com.guardswift.persistence.parse.execution.task.statictask;

import android.content.Context;
import android.location.Location;

import com.guardswift.core.documentation.report.NoTaskReportingStrategy;
import com.guardswift.core.documentation.report.TaskReportingStrategy;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.core.tasks.activity.NoActivityStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
import com.guardswift.core.tasks.automation.NoAutomationStrategy;
import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
import com.guardswift.core.tasks.controller.StaticTaskController;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.geofence.NoGeofenceStrategy;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.TaskQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.documentation.event.EventLog;
import com.guardswift.persistence.parse.execution.BaseTask;
import com.guardswift.persistence.parse.execution.GSTask;
import com.guardswift.persistence.parse.execution.task.regular.CircuitUnit;
import com.guardswift.ui.GuardSwiftApplication;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.util.Date;

@ParseClassName("StaticTask")
public class StaticTask extends BaseTask {

    private static String TAG = StaticTask.class.getSimpleName();

    /**
     * Create
     */

    public static void create(Client client, Guard guard, final GetCallback<StaticTask> getCallback) {
        final StaticTask task = new StaticTask();
        task.setDefaultOwner();
        task.setClient(client);
        task.pinThenSaveEventually(null, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                getCallback.done(task, e);
            }
        });
    }

    /**
     * Data
     */

    public static final String clientPosition = "clientPosition";
    public static final String guard = "guard";
    public static final String client = "client";
    // handling
    public static final String timeStarted = "timeStarted";
    public static final String timeEnded = "timeEnded";


    /**
     * Logic
     */
    private final TaskController controller;
    private final TaskReportingStrategy taskReportingStrategy;
    private final TaskGeofenceStrategy geofenceStrategy;
    private final TaskActivityStrategy activityStrategy;
    private final TaskAutomationStrategy automationStrategy;

    public StaticTask() {
        this.controller =  StaticTaskController.getInstance();
        this.taskReportingStrategy = new NoTaskReportingStrategy(this); // StandardTaskReportingStrategy<>(this);
        this.automationStrategy = NoAutomationStrategy.getInstance();
        this.geofenceStrategy = NoGeofenceStrategy.getInstance(this);
        this.activityStrategy = NoActivityStrategy.getInstance();
    }



    @Override
    public BaseTaskCache<StaticTask> getCache() {
        return GuardSwiftApplication.getInstance().getCacheFactory().getStaticTaskCache();
    }

    @Override
    public void setPending() {

    }

    @Override
    public void setAccepted() {

    }

    @Override
    public void setArrived() {

    }

    @Override
    public void setAborted() {

    }

    @Override
    public TaskGeofenceStrategy getGeofenceStrategy() {
        return geofenceStrategy;
    }

    @Override
    public TaskActivityStrategy getActivityStrategy() {
        return activityStrategy;
    }

    @Override
    public TaskAutomationStrategy getAutomationStrategy() {
        return automationStrategy;
    }

    public TaskReportingStrategy getTaskReportingStrategy() {
        return taskReportingStrategy;
    }

    @Override
    public TaskController getController() {
        return controller;
    }


    @Override
    public int getEventCode() {
        return EventLog.EventCodes.STATIC_OTHER;
    }


    @Override
    public TASK_TYPE getTaskType() {
        return TASK_TYPE.STATIC;
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public TASK_STATE getTaskState() {
        if (isFinished()) {
            return TASK_STATE.FINISHED;
        }
        if (isArrived()) {
            return TASK_STATE.ARRIVED;
        }
        return TASK_STATE.PENDING;
    }

    @Override
    public ParseGeoPoint getPosition() {
        return getParseGeoPoint(clientPosition);
    }


    @Override
    public ExtendedParseObject getParseObject() {
        return this;
    }


    @Override
    public String getParseClassName() {
        return StaticTask.class.getSimpleName();
    }

    @Override
    public void updateFromJSON(final Context context,
                               final JSONObject jsonObject) {
    }


    /**
     * Query
     */

    @SuppressWarnings("unchecked")
    @Override
    public ParseQuery<StaticTask> getAllNetworkQuery() {
        return new QueryBuilder(false).build();
    }

    @Override
    public TaskQueryBuilder getQueryBuilder(boolean fromLocalDatastore) {
        return new QueryBuilder(fromLocalDatastore);
    }

    public void addReportEntry(Context context, String remarks, GetCallback<EventLog> pinned) {
        addReportEntry(context, remarks, pinned, null);
    }
    public void addReportEntry(Context context, String remarks, GetCallback<EventLog> pinned, GetCallback<EventLog> saved) {
        new EventLog.Builder(context)
                .taskPointer(this, GSTask.EVENT_TYPE.OTHER)
                .remarks(remarks)
                .eventCode(this.getEventCode())
                .saveAsync(pinned, saved);
    }


    public static class QueryBuilder extends TaskQueryBuilder<StaticTask> {

        public QueryBuilder(boolean fromLocalDatastore) {
            super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery.getQuery(StaticTask.class));
        }

        @Override
        public ParseQuery<StaticTask> build() {
            query.include(StaticTask.client);
            query.include(client + "." + Client.contacts);
            query.include(client + "." + Client.roomLocations);
            query.include(client + "." + Client.people);
            query.include(StaticTask.guard);
            query.whereExists(StaticTask.client);

            query.setLimit(1000);
            return super.build();
        }

        public ParseQuery<StaticTask> buildNoIncludes() {
            query.setLimit(1000);
            return super.build();
        }


        public QueryBuilder matching(Guard guard) {

            query.whereEqualTo(StaticTask.guard, guard);

            return this;
        }

        public QueryBuilder notMatching(StaticTask staticTask) {

            if (staticTask != null)
                query.whereNotEqualTo(objectId, staticTask.getObjectId());

            return this;
        }

        public QueryBuilder daysBack(int days) {
            Date oneWeekAgo = new DateTime().minusDays(days).toDate();
            query.whereGreaterThan(StaticTask.createdAt, oneWeekAgo);

            return this;
        }

        public QueryBuilder active() {

            query.whereExists(StaticTask.timeStarted);
            query.whereDoesNotExist(StaticTask.timeEnded);


            return this;
        }

        public QueryBuilder finished() {

            query.whereExists(StaticTask.timeEnded);

            return this;
        }

        public QueryBuilder pending() {
            query.whereDoesNotExist(StaticTask.timeStarted);
            return this;
        }

        public QueryBuilder sortedByTimeEnded() {
            query.orderByDescending(CircuitUnit.timeEnded);
            return this;
        }

        public QueryBuilder sortedByCreated() {
            query.orderByDescending(CircuitUnit.createdAt);
            return this;
        }

        public QueryBuilder within(int kilometers, Location fromLocation) {
            ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
            query.whereWithinKilometers(clientPosition, parseGeoPoint, kilometers);
            return this;
        }


    }


    /**
     * Setter/Getters
     */

    public boolean isPending() {
        return !isArrived() && !isFinished();
    }

    @Override
    public boolean isAccepted() {
        return true;
    }

    @Override
    public boolean isArrived() {
        return has(StaticTask.timeStarted);
    }

    @Override
    public boolean isAborted() {
        return false;
    }

    @Override
    public boolean isFinished() {
        return has(StaticTask.timeEnded);
    }

    @Override
    public boolean isWithinScheduledTime() {
        return true;
    }

    @Override
    public int getRadius() {
        return 0;
    }


    public void setStartedBy(Guard guard) {

        setGuard(guard);

        put(StaticTask.timeStarted, new Date());
    }

    public void setFinished() {
        put(StaticTask.timeEnded, new Date());
    }


    public void setGuard(Guard guard) {
        put(StaticTask.guard, guard);
    }

    public Date getTimeArrived() {
        return getDate(timeStarted);
    }

    public Date getTimeEnded() {
        return getDate(timeEnded);
    }



    @Override
    public String getReportId() {
        return getClient().getObjectId()+getObjectId();
    }

    public Guard getGuard() {
        return (Guard)getLDSFallbackParseObject(StaticTask.guard);
    }

    public void setClient(Client client) {
        put(StaticTask.client, client);
        put(StaticTask.clientPosition, client.getPosition());
    }

    public Client getClient() {
        return (Client)getLDSFallbackParseObject(StaticTask.client);
    }

    @Override
    public String getClientName() {
        return getClient().getName();
    }


}

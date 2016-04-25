package com.guardswift.persistence.parse.execution;

import com.guardswift.core.documentation.report.TaskReportingStrategy;
import com.guardswift.core.tasks.activity.TaskActivityStrategy;
import com.guardswift.core.tasks.automation.TaskAutomationStrategy;
import com.guardswift.core.tasks.controller.TaskController;
import com.guardswift.core.tasks.geofence.TaskGeofenceStrategy;
import com.guardswift.persistence.cache.task.BaseTaskCache;
import com.guardswift.persistence.parse.ExtendedParseObject;
import com.guardswift.persistence.parse.Positioned;
import com.guardswift.persistence.parse.TaskQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;

/**
 * Created by cyrix on 4/10/15.
 */
public interface GSTask<T extends BaseTask> extends Positioned {




    enum TASK_TYPE {REGULAR, DISTRICTWATCH, ALARM, STATIC}
    enum TASK_STATE {PENDING, ACCEPTED, ARRIVED, ABORTED, FINSIHED}
    enum EVENT_TYPE {BEGIN, ARRIVE, ABORT, CHECKPOINT, FINISH, DEPARTURE, ACCEPT, GEOFENCE_ENTER, GEOFENCE_EXIT, GEOFENCE_ENTER_GPS, GEOFENCE_EXIT_GPS, OTHER}

    TASK_TYPE getTaskType();
    TASK_STATE getTaskState();

//    public boolean equals(Object o1);

    /**
     * @return unique id
     */
    String getObjectId();
    String getGeofenceId();

    /*
     * A small descriptive name specified by the guarding company during planning
     */
    String getType();

    TaskQueryBuilder<T> getQueryBuilder(boolean fromLocalDatastore);
    String getReportId();

    int getEventCode();
//    TaskSummary getTaskSummaryInstance(Context context);

//    String getTaskTitle(Context context);

    TaskReportingStrategy<T> getTaskReportingStrategy();
    TaskGeofenceStrategy<T> getGeofenceStrategy();
    TaskActivityStrategy<T> getActivityStrategy();
    TaskAutomationStrategy<T> getAutomationStrategy();
    TaskController<T> getController();
    BaseTaskCache<T> getCache();

    boolean isWithinScheduledTime();

    boolean isStarted();
    boolean isAborted();
    boolean isFinished();

//    boolean automaticArrival(Context context);
//    boolean automaticDeparture(Context context);

    Guard getGuard();
    Client getClient();
    String getClientName();

    ExtendedParseObject getParseObject();
}

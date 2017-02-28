package com.guardswift.persistence.parse.execution;

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

import java.util.List;

/**
 * Created by cyrix on 4/10/15.
 */
public interface GSTask extends Positioned {

    int DEFAULT_RADIUS_RAID = 50;
    int DEFAULT_RADIUS_REGULAR = 75;
    int DEFAULT_RADIUS_ALARM = 75;

    enum TASK_TYPE {REGULAR, RAID, DISTRICTWATCH, STATIC, ALARM}
    enum TASK_STATE {PENDING, ACCEPTED, ARRIVED, ABORTED, FINISHED}
    enum EVENT_TYPE {BEGIN, ARRIVE, ABORT, CHECKPOINT, FINISH, DEPARTURE, ACCEPT, GEOFENCE_ENTER, GEOFENCE_EXIT, GEOFENCE_ENTER_GPS, GEOFENCE_EXIT_GPS, OTHER, LEAVE}


    TASK_TYPE getTaskType();
    List<TASK_TYPE> getPossibleTaskTypes();
    TASK_STATE getTaskState();

//    public boolean equals(Object o1);

    /**
     * @return unique id
     */
    String getObjectId();
    String getGeofenceId();
    String getParseClassName();

    /*
     * A small descriptive name specified by the guarding company during planning
     */
    String getType();

    TaskQueryBuilder getQueryBuilder(boolean fromLocalDatastore);
    String getReportId();

    int getEventCode();

    TaskGeofenceStrategy getGeofenceStrategy();
    TaskActivityStrategy getActivityStrategy();
    TaskAutomationStrategy getAutomationStrategy();
    TaskController getController();
    BaseTaskCache getCache();


    void setPending();
    void setAccepted();
    void setArrived();
    void setAborted();
    void setFinished();

    boolean isPending();
    boolean isAccepted();
    boolean isArrived();
    boolean isAborted();
    boolean isFinished();

    boolean isWithinScheduledTime();

    // radius in meters for automatic arrival
    int getRadius();

    Guard getGuard();
    Client getClient();
    String getClientName();

    ExtendedParseObject getParseObject();

    void pinThenSaveEventually();
}

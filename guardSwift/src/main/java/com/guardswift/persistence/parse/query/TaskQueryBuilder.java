package com.guardswift.persistence.parse.query;


import android.location.Location;
import android.util.Log;

import com.google.common.collect.Lists;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;


public class TaskQueryBuilder extends
        ParseQueryBuilder<ParseTask> {

    private static final String TAG = TaskQueryBuilder.class.getSimpleName();

    public TaskQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));
    }


    public TaskQueryBuilder within(int kilometers, Location fromLocation) {
        ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
        query.whereWithinKilometers(ParseTask.position, parseGeoPoint, kilometers);
        return this;
    }

    public TaskQueryBuilder matching(TaskGroupStarted taskGroupStarted) {
        if (taskGroupStarted == null) {
            return this;
        }

        matching(taskGroupStarted.getTaskGroup());


        return this;
    }

    public TaskQueryBuilder matching(TaskGroup taskGroup) {
        if (taskGroup == null) {
            return this;
        }

        Log.d(TAG, "matching taskGroup: " + taskGroup.getObjectId());

        query.whereEqualTo(ParseTask.taskGroup, taskGroup);

        return this;
    }
    public TaskQueryBuilder matchingTaskTypes(ArrayList<String> taskTypeStrings) {
        query.whereContainedIn(ParseTask.taskType, taskTypeStrings);
        return this;
    }

    public TaskQueryBuilder isRunToday() {
        query.whereEqualTo(ParseTask.isRunToday, true);
        return this;
    }

    public TaskQueryBuilder pendingOrArrived() {
        query.whereContainedIn(ParseTask.status, Lists.newArrayList(ParseTask.STATUS.PENDING, ParseTask.STATUS.ARRIVED));
        return this;
    }

}

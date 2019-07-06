package com.guardswift.persistence.parse.query;

import android.location.Location;
import android.util.Log;

import com.google.common.collect.Lists;
import com.guardswift.core.parse.ParseModule;
import com.guardswift.persistence.parse.ParseQueryBuilder;
import com.guardswift.persistence.parse.data.Guard;
import com.guardswift.persistence.parse.data.client.Client;
import com.guardswift.persistence.parse.execution.task.ParseTask;
import com.guardswift.persistence.parse.execution.task.TaskGroup;
import com.guardswift.persistence.parse.execution.task.TaskGroupStarted;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class RegularRaidTaskQueryBuilder extends ParseQueryBuilder<ParseTask> {

    public static final int SORTBY_NEAREST = 1;
    public static final int SORTBY_ID = 2;

    private static final String TAG = RegularRaidTaskQueryBuilder.class.getSimpleName();


    public RegularRaidTaskQueryBuilder(boolean fromLocalDatastore) {
        super(ParseObject.DEFAULT_PIN, fromLocalDatastore, ParseQuery
                .getQuery(ParseTask.class));

        query.whereContainedIn(ParseTask.taskType, Lists.newArrayList(ParseTask.TASK_TYPE_STRING.REGULAR, ParseTask.TASK_TYPE_STRING.RAID));
    }
    


    @Override
    public ParseQuery<ParseTask> build() {
        query.include(ParseTask.taskGroup); // to get reset time
        query.include(ParseTask.taskGroupStarted); // may not be needed
        query.include(ParseTask.client);
        query.include(ParseTask.client + "." + Client.contacts);
        query.include(ParseTask.client + "." + Client.roomLocations);
        query.include(ParseTask.client + "." + Client.people);
        query.whereExists(ParseTask.client);
        query.setLimit(1000);



        return super.build();
    }

    public RegularRaidTaskQueryBuilder isRunToday() {
        query.whereEqualTo(ParseTask.isRunToday, true);
        return this;
    }

    public RegularRaidTaskQueryBuilder matchingNotEnded(
            TaskGroupStarted taskGroupStarted) {
        if (taskGroupStarted == null) {
            return this;
        }

        Log.d(TAG, "matchingNotEnded " + taskGroupStarted.getName());

        matching(taskGroupStarted.getTaskGroup());

        // TODO: rewrite query to OR pending, aborted and arrived
        query.whereNotEqualTo(ParseTask.status, ParseTask.STATUS.FINISHED);

        return this;
    }

    public RegularRaidTaskQueryBuilder matchingEnded(
            TaskGroupStarted taskGroupStarted) {

        Log.d(TAG, "matchingEnded " + taskGroupStarted.getName());

        matching(taskGroupStarted.getTaskGroup());

        query.whereEqualTo(ParseTask.status, ParseTask.STATUS.FINISHED);

        query.orderByDescending(ParseTask.updatedAt);

        return this;
    }

    public RegularRaidTaskQueryBuilder within(int kilometers, Location fromLocation) {
        ParseGeoPoint parseGeoPoint = ParseModule.geoPointFromLocation(fromLocation);
        query.whereWithinKilometers(ParseTask.position, parseGeoPoint, kilometers);
        return this;
    }

    public RegularRaidTaskQueryBuilder sortBy(int sortBy) {
        switch (sortBy) {
            case SORTBY_NEAREST:
                ParseModule.sortNearest(query,
                        ParseTask.position);
                break;
            case SORTBY_ID:
                query.orderByAscending(ParseTask.clientId);
                break;
        }


        return this;
    }

    public RegularRaidTaskQueryBuilder matching(Guard guard) {

        query.whereEqualTo(ParseTask.guard, guard);

        return this;
    }

    public RegularRaidTaskQueryBuilder matching(TaskGroup taskGroup) {

        if (taskGroup == null) {
            return this;
        }

        Log.d(TAG, "matching taskGroup: " + taskGroup.getObjectId());

        query.whereEqualTo(ParseTask.taskGroup, taskGroup);

        return this;
    }



    public RegularRaidTaskQueryBuilder matching(ParseTask.TASK_TYPE taskType) {
        query.whereEqualTo(ParseTask.taskType, taskType.toString());
        return this;
    }

    public RegularRaidTaskQueryBuilder isExtraTask() {
        query.whereExists(ParseTask.expireDate);
        return this;
    }
}
